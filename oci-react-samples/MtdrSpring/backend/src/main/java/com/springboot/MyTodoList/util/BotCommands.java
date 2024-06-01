package com.springboot.MyTodoList.util;

public enum BotCommands {

    START_COMMAND("/start"), 
    HIDE_COMMAND("/hide"), 
    TODO_LIST("/todolist"),
    ADD_ITEM("/additem"),
    LOGIN("/login"),
    CREATE_TASK("/createTask"),
    UPDATE_TASK("/updateTask"),
    MARK_DONE("/markDone"),
    VIEW_MY_TASKS("/viewMyTasks"),
    VIEW_ALL_TASKS("/viewAllTasks"),
    VIEW_TASKS_FOR_DEV("/viewTasksForDev");

    private String command;

    BotCommands(String enumCommand) {
        this.command = enumCommand;
    }

    public String getCommand() {
        return command;
    }
}
