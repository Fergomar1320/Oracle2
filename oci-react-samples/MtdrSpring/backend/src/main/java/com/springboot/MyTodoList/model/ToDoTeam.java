package com.springboot.MyTodoList.model;

import javax.persistence.*;

@Entity
@Table(name = "TODOTEAM")
public class ToDoTeam {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int team_id;

    @Column(name = "TEAM_NAME")
    private String team_name;

    public ToDoTeam() {
    }

    public ToDoTeam(int team_id, String team_name) {
        this.team_id = team_id;
        this.team_name = team_name;
    }

    public int getTeamId() {
        return team_id;
    }

    public String getTeamName() {
        return team_name;
    }

    public void setTeamId(int team_id) {
        this.team_id = team_id;
    }

    public void setTeamName(String team_name) {
        this.team_name = team_name;
    }

    @Override
    public String toString() {
        return "ToDoTeam{" +
                "team_id=" + team_id +
                ", team_name='" + team_name + '\'' +
                '}';
    }
}
