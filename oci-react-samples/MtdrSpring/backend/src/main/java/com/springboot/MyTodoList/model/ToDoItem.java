package com.springboot.MyTodoList.model;


import javax.persistence.*;
import java.time.OffsetDateTime;

/*
    representation of the TODOITEM table that exists already
    in the autonomous database
 */
@Entity
@Table(name = "TODOITEM")
public class ToDoItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int item_id;

    @Column(name = "ITEM_DESCRIPTION")
    private String item_description;

    @Column(name = "ITEM_CREATION_TS")
    private OffsetDateTime item_creation_ts;

    @Column(name = "ITEM_DEADLINE")
    private OffsetDateTime item_deadline;

    @Column(name = "ITEM_STATUS")
    private String item_status;

    @ManyToOne
    @JoinColumn(name = "USER_ID", nullable = false)
    private OracleUser user;

    @ManyToOne
    @JoinColumn(name = "SPRINT_ID", nullable = false)
    private ToDoSprint sprint;

    public ToDoItem(){

    }
    public ToDoItem(int item_id, String item_description, OffsetDateTime item_creation_ts, String item_status, OracleUser user, ToDoSprint sprint) {
        this.item_id = item_id;
        this.item_description = item_description;
        this.item_creation_ts = item_creation_ts;
        this.item_status = item_status;
        this.user = user;
        this.sprint = sprint;
    }

    public int getItemId() {
        return item_id;
    }

    public String getItemDescription() {
        return item_description;
    }

    public OffsetDateTime getItemCreationTs() {
        return item_creation_ts;
    }

    public OffsetDateTime getItemDeadline() {
        return item_deadline;
    }

    public String getItemStatus() {
        return item_status;
    }

    public OracleUser getUser() {
        return user;
    }

    public ToDoSprint getSprint() {
        return sprint;
    }

    public void setItemId(int item_id) {
        this.item_id = item_id;
    }

    public void setItemDescription(String item_description) {
        this.item_description = item_description;
    }

    public void setItemCreationTs(OffsetDateTime item_creation_ts) {
        this.item_creation_ts = item_creation_ts;
    }

    public void setItemDeadline(OffsetDateTime item_deadline) {
        this.item_deadline = item_deadline;
    }

    public void setItemStatus(String item_status) {
        this.item_status = item_status;
    }

    public void setUser(OracleUser user) {
        this.user = user;
    }

    public void setSprint(ToDoSprint sprint) {
        this.sprint = sprint;
    }

    @Override
    public String toString() {
        return "ToDoItem{" +
                "item_id=" + item_id +
                ", item_description='" + item_description + '\'' +
                ", item_creation_ts=" + item_creation_ts +
                ", item_deadline=" + item_deadline +
                ", item_status='" + item_status + '\'' +
                ", user=" + user +
                ", sprint=" + sprint +
                '}';
    }
}
