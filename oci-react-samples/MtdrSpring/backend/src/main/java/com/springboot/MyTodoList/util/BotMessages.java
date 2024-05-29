package com.springboot.MyTodoList.util;

public enum BotMessages {
    
    HELLO_MYTODO_BOT(
    "Hello! I'm MyTodoList Bot!\nType a new todo item below and press the send button (blue arrow), or select an option below:"),
    BOT_REGISTERED_STARTED("Bot registered and started successfully!"),
    ITEM_DONE("Item done! Select /todolist to return to the list of todo items, or /start to go to the main screen."), 
    ITEM_UNDONE("Item undone! Select /todolist to return to the list of todo items, or /start to go to the main screen."), 
    ITEM_DELETED("Item deleted! Select /todolist to return to the list of todo items, or /start to go to the main screen."),
    TYPE_NEW_TODO_ITEM("Type a new todo item below and press the send button (blue arrow) on the right-hand side."),
    NEW_ITEM_ADDED("New item added! Select /todolist to return to the list of todo items, or /start to go to the main screen."),
    BYE("Bye! Select /start to resume!"),
    LOGIN_SUCCESSFUL("Login successful. Welcome!"),
    LOGIN_FAILED("Login failed. User ID not found."),
    INVALID_LOGIN("Invalid login command. Use: /login <USER_ID>"),
    INVALID_USER_ID_FORMAT("Invalid USER_ID format. Use: /login <USER_ID>"),
    UNAUTHORIZED_ROLE("Unauthorized role."),
    DEVELOPER_COMMAND_RECEIVED("Developer command received: "),
    MANAGER_COMMAND_RECEIVED("Manager command received: "),
    TASK_CREATED("Task created successfully."),
    TASK_CREATION_FAILED("Failed to create task."),
    TASK_UPDATED("Task updated successfully."),
    TASK_UPDATE_FAILED("Failed to update task."),
    TASK_MARKED_DONE("Task marked as done."),
    TASK_MARKING_FAILED("Failed to mark task as done."),
    VIEW_TASKS("View tasks:");

    private String message;

    BotMessages(String enumMessage) {
        this.message = enumMessage;
    }

    public String getMessage() {
        return message;
    }
}
