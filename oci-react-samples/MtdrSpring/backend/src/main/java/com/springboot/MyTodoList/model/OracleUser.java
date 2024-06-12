package com.springboot.MyTodoList.model;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "ORACLEUSER")
public class OracleUser implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "USER_NAME")
    private String user_name;

    @Column(name = "USER_ROLE")
    private String user_role;

    @Column(name = "USER_CHAT_ID")
    private String user_chat_id;

    @Column(name = "USER_PWD")
    private String user_pwd;

    @ManyToOne
    @JoinColumn(name = "TEAM_ID", nullable = false)
    private ToDoTeam team;

    public OracleUser() {
    }

    public OracleUser(int id, String user_name, String user_role, String user_chat_id, ToDoTeam team) {
        this.id = id;
        this.user_name = user_name;
        this.user_role = user_role;
        this.user_chat_id = user_chat_id;
        this.team = team;
    }

    public int getUserId() {
        return id;
    }

    public String getUserName() {
        return user_name;
    }

    public String getUserRole() {
        return user_role;
    }

    public String getUserChatId() {
        return user_chat_id;
    }

    public ToDoTeam getTeam() {
        return team;
    }

    public void setUserId(int id) {
        this.id = id;
    }

    public void setUserName(String user_name) {
        this.user_name = user_name;
    }

    public void setUserRole(String user_role) {
        this.user_role = user_role;
    }

    public void setUserChatId(String user_chat_id) {
        this.user_chat_id = user_chat_id;
    }

    public void setTeam(ToDoTeam team) {
        this.team = team;
    }

    @Override
    public String toString() {
        return "OracleUser{" +
                "id=" + id +
                ", user_name='" + user_name + '\'' +
                ", user_role='" + user_role + '\'' +
                ", user_chat_id='" + user_chat_id + '\'' +
                ", team=" + team +
                '}';
    }

}
