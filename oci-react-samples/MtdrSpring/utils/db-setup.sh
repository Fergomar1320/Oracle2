#!/bin/bash
# Copyright (c) 2022 Oracle and/or its affiliates.
# Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl.

# Fail on error
set -e


# Create Object Store Bucket (Should be replaced by terraform one day)
while ! state_done OBJECT_STORE_BUCKET; do
  echo "Checking object storage bucket"
#  oci os bucket create --compartment-id "$(state_get COMPARTMENT_OCID)" --name "$(state_get RUN_NAME)"
  if oci os bucket get --name "$(state_get RUN_NAME)-$(state_get MTDR_KEY)"; then
    state_set_done OBJECT_STORE_BUCKET
    echo "finished checking object storage bucket"
  fi
done


# Wait for Order DB OCID
while ! state_done MTDR_DB_OCID; do
  echo "`date`: Waiting for MTDR_DB_OCID"
  sleep 2
done


# Get Wallet
while ! state_done WALLET_GET; do
  echo "creating wallet"
  cd $MTDRWORKSHOP_LOCATION
  mkdir wallet
  cd wallet
  oci db autonomous-database generate-wallet --autonomous-database-id "$(state_get MTDR_DB_OCID)" --file 'wallet.zip' --password 'Welcome1' --generate-type 'ALL'
  unzip wallet.zip
  cd $MTDRWORKSHOP_LOCATION
  state_set_done WALLET_GET
  echo "finished creating wallet"
done


# Get DB Connection Wallet and to Object Store
while ! state_done CWALLET_SSO_OBJECT; do
  echo "grabbing wallet"
  cd $MTDRWORKSHOP_LOCATION/wallet
  oci os object put --bucket-name "$(state_get RUN_NAME)-$(state_get MTDR_KEY)" --name "cwallet.sso" --file 'cwallet.sso'
  cd $MTDRWORKSHOP_LOCATION
  state_set_done CWALLET_SSO_OBJECT
  echo "done grabbing wallet"
done


# Create Authenticated Link to Wallet
while ! state_done CWALLET_SSO_AUTH_URL; do
  echo "creating authenticated link to wallet"
  ACCESS_URI=`oci os preauth-request create --object-name 'cwallet.sso' --access-type 'ObjectRead' --bucket-name "$(state_get RUN_NAME)-$(state_get MTDR_KEY)" --name 'mtdrworkshop' --time-expires $(date '+%Y-%m-%d' --date '+7 days') --query 'data."access-uri"' --raw-output`
  state_set CWALLET_SSO_AUTH_URL "https://objectstorage.$(state_get REGION).oraclecloud.com${ACCESS_URI}"
  echo "done creating authenticated link to wallet"
done


# Give DB_PASSWORD priority
while ! state_done DB_PASSWORD; do
  echo "Waiting for DB_PASSWORD"
  sleep 5
done


# Create Inventory ATP Bindings
while ! state_done DB_WALLET_SECRET; do
  echo "creating Inventory ATP Bindings"
  cd $MTDRWORKSHOP_LOCATION/wallet
  cat - >sqlnet.ora <<!
WALLET_LOCATION = (SOURCE = (METHOD = file) (METHOD_DATA = (DIRECTORY="/mtdrworkshop/creds")))
SSL_SERVER_DN_MATCH=yes
!
  if kubectl create -f - -n mtdrworkshop; then
    state_set_done DB_WALLET_SECRET
  else
    echo "Error: Failure to create db-wallet-secret.  Retrying..."
    sleep 5
  fi <<!
apiVersion: v1
data:
  README: $(base64 -w0 README)
  cwallet.sso: $(base64 -w0 cwallet.sso)
  ewallet.p12: $(base64 -w0 ewallet.p12)
  keystore.jks: $(base64 -w0 keystore.jks)
  ojdbc.properties: $(base64 -w0 ojdbc.properties)
  sqlnet.ora: $(base64 -w0 sqlnet.ora)
  tnsnames.ora: $(base64 -w0 tnsnames.ora)
  truststore.jks: $(base64 -w0 truststore.jks)
kind: Secret
metadata:
  name: db-wallet-secret
!
  cd $MTDRWORKSHOP_LOCATION
done


# DB Connection Setup
export TNS_ADMIN=$MTDRWORKSHOP_LOCATION/wallet
cat - >$TNS_ADMIN/sqlnet.ora <<!
WALLET_LOCATION = (SOURCE = (METHOD = file) (METHOD_DATA = (DIRECTORY="$TNS_ADMIN")))
SSL_SERVER_DN_MATCH=yes
!
MTDR_DB_SVC="$(state_get MTDR_DB_NAME)_tp"
TODO_USER=TODOUSER
ORDER_LINK=ORDERTOINVENTORYLINK
ORDER_QUEUE=ORDERQUEUE


# Get DB Password
while true; do
  if DB_PASSWORD=`kubectl get secret dbuser -n mtdrworkshop --template={{.data.dbpassword}} | base64 --decode`; then
    if ! test -z "$DB_PASSWORD"; then
      break
    fi
  fi
  echo "Error: Failed to get DB password.  Retrying..."
  sleep 5
done


# Wait for DB Password to be set in Order DB
while ! state_done MTDR_DB_PASSWORD_SET; do
  echo "`date`: Waiting for MTDR_DB_PASSWORD_SET"
  sleep 2
done


# Order DB User, Objects
while ! state_done TODO_USER; do
  echo "connecting to mtdr database"
  U=$TODO_USER
  SVC=$MTDR_DB_SVC
  sqlplus /nolog <<!
WHENEVER SQLERROR EXIT 1
connect admin/"$DB_PASSWORD"@$SVC
CREATE USER $U IDENTIFIED BY "$DB_PASSWORD" DEFAULT TABLESPACE data QUOTA UNLIMITED ON data;
GRANT CREATE SESSION, CREATE VIEW, CREATE SEQUENCE, CREATE PROCEDURE TO $U;
GRANT CREATE TABLE, CREATE TRIGGER, CREATE TYPE, CREATE MATERIALIZED VIEW TO $U;
GRANT CONNECT, RESOURCE, pdb_dba, SODA_APP to $U;

create table TODOUSER.TODOTEAM(
    team_id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    team_name VARCHAR2 (4000)
);

create table TODOUSER.TODOSPRINT(
    sprint_id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    sprint_name VARCHAR2 (4000),
    sprint_start_date DATE,
    sprint_end_date DATE,
    sprint_status VARCHAR(100) CHECK (sprint_status IN ('Not Started', 'Open', 'Closed')),
    team_id NUMBER REFERENCES TODOUSER.TODOTEAM(team_id)
);

create table TODOUSER.ORACLEUSER(
    user_id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_name VARCHAR2(4000),
    user_role VARCHAR(10) CHECK (user_role IN ('Developer', 'Manager')),
    user_chat_id VARCHAR2(4000),
    team_id NUMBER REFERENCES TODOUSER.TODOTEAM(team_id)
);

CREATE TABLE TODOUSER.TODOITEM (
    item_id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY, 
    item_description VARCHAR2(4000), 
    item_creation_ts TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    item_deadline DATE, 
    item_status VARCHAR(20) CHECK (item_status IN ('Not Started', 'In Progress', 'Done')), 
    user_id NUMBER REFERENCES TODOUSER.ORACLEUSER(user_id),
    sprint_id NUMBER REFERENCES TODOUSER.TODOSPRINT(sprint_id)
);

insert into TODOUSER.TODOTEAM  (team_name) values ('Team 1');
insert into TODOUSER.TODOTEAM  (team_name) values ('Team 2');
insert into TODOUSER.TODOTEAM  (team_name) values ('Team 3');

INSERT INTO TODOUSER.ORACLEUSER(USER_NAME, USER_ROLE, TEAM_ID) VALUES  ('Fernando Gomez','Developer',1);
INSERT INTO TODOUSER.ORACLEUSER(USER_NAME, USER_ROLE, TEAM_ID) VALUES  ('Emma Alfaro', 'Developer', 1);
INSERT INTO TODOUSER.ORACLEUSER(USER_NAME, USER_ROLE, TEAM_ID) VALUES  ('Omar Macias', 'Manager',2);
INSERT INTO TODOUSER.ORACLEUSER(USER_NAME, USER_ROLE, TEAM_ID) VALUES  ('Sebastian Garcia','Developer', 2);
INSERT INTO TODOUSER.ORACLEUSER(USER_NAME, USER_ROLE, TEAM_ID) VALUES  ('Jose Fong', 'Developer', 3);


INSERT INTO TODOUSER.TODOSPRINT(SPRINT_NAME, SPRINT_START_DATE, SPRINT_END_DATE, TEAM_ID, SPRINT_STATUS) 
    VALUES ('Sprint 1', TO_DATE('2024-05-14', 'YYYY-MM-DD'), TO_DATE('2024-05-19', 'YYYY-MM-DD'),1, 'Not Started');

INSERT INTO TODOUSER.TODOSPRINT(SPRINT_NAME, SPRINT_START_DATE, SPRINT_END_DATE, TEAM_ID, SPRINT_STATUS) 
    VALUES ('Sprint 2', TO_DATE('2024-05-20', 'YYYY-MM-DD'), TO_DATE('2024-05-27', 'YYYY-MM-DD'),1, 'Not Started');

INSERT INTO TODOUSER.TODOSPRINT(SPRINT_NAME, SPRINT_START_DATE, SPRINT_END_DATE, TEAM_ID, SPRINT_STATUS) 
    VALUES ('Sprint 1', TO_DATE('2024-05-17', 'YYYY-MM-DD'), TO_DATE('2024-05-27', 'YYYY-MM-DD'),2, 'Not Started');   


INSERT INTO TODOUSER.TODOITEM (ITEM_DESCRIPTION, ITEM_STATUS, USER_ID, SPRINT_ID)
    VALUES ('Arreglar Base de datos', 'Not Started', 1, 2);

INSERT INTO TODOUSER.TODOITEM (ITEM_DESCRIPTION, ITEM_STATUS, USER_ID, SPRINT_ID)
    VALUES ('Arreglar Front', 'Not Started', 2, 2);

INSERT INTO TODOUSER.TODOITEM (ITEM_DESCRIPTION, ITEM_STATUS, USER_ID, SPRINT_ID)
    VALUES ('Corregir Telegram', 'Not Started', 4, 2);


commit;
!
  state_set_done TODO_USER
  echo "finished connecting to database and creating attributes"
done
# DB Setup Done
state_set_done DB_SETUP