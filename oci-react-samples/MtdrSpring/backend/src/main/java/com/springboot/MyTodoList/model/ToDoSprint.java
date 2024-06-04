package com.springboot.MyTodoList.model;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "TODOSPRINT")
public class ToDoSprint {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int sprint_id;

    @Column(name = "SPRINT_NAME")
    private String sprint_name;

    @Column(name = "SPRINT_START_DATE") // Date
    private Date sprint_start_date;

    @Column(name = "SPRINT_END_DATE") // Date
    private Date sprint_end_date;

    @Column(name = "SPRINT_STATUS")
    private String sprint_status;

    @ManyToOne
    @JoinColumn(name = "TEAM_ID", nullable = false)
    private ToDoTeam team;

    public ToDoSprint() {
    }

    public ToDoSprint (int sprint_id, String sprint_name, Date sprint_start_date, Date sprint_end_date, String sprint_status, ToDoTeam team) {
        this.sprint_id = sprint_id;
        this.sprint_name = sprint_name;
        this.sprint_start_date = sprint_start_date;
        this.sprint_end_date = sprint_end_date;
        this.sprint_status = sprint_status;
        this.team = team;
    }

    public int getSprintId() {
        return sprint_id;
    }

    public String getSprintName() {
        return sprint_name;
    }

    public Date getSprintStartDate() {
        return sprint_start_date;
    }

    public Date getSprintEndDate() {
        return sprint_end_date;
    }

    public String getSprintStatus() {
        return sprint_status;
    }

    public ToDoTeam getTeam() {
        return team;
    }

    public void setSprintId (int sprint_id) {
        this.sprint_id = sprint_id;
    }

    public void setSprintName(String sprint_name) {
        this.sprint_name = sprint_name;
    }

    public void setSprintStartDate(Date sprint_start_date) {
        this.sprint_start_date = sprint_start_date;
    }

    public void setSprintEndDate(Date sprint_end_date) {
        this.sprint_end_date = sprint_end_date;
    }

    public void setSprintStatus(String sprint_status) {
        this.sprint_status = sprint_status;
    }

    public void setTeam(ToDoTeam team) {
        this.team = team;
    }

    @Override
    public String toString() {
        return "ToDoSprint{" +
                "sprint_id=" + sprint_id +
                ", sprint_name='" + sprint_name + '\'' +
                ", sprint_start_date=" + sprint_start_date +
                ", sprint_end_date=" + sprint_end_date +
                ", sprint_status='" + sprint_status + '\'' +
                ", team=" + team +
                '}';
    }

}
