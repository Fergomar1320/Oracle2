package com.springboot.MyTodoList.controller;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import org.springframework.web.bind.annotation.GetMapping;


import com.springboot.MyTodoList.model.OracleUser;
import com.springboot.MyTodoList.model.ToDoItem;
import com.springboot.MyTodoList.model.ToDoSprint;
import com.springboot.MyTodoList.service.AuthenticationService;
import com.springboot.MyTodoList.service.ToDoItemService;
import com.springboot.MyTodoList.service.OracleUserService;
import com.springboot.MyTodoList.util.BotCommands;
import com.springboot.MyTodoList.util.BotHelper;
import com.springboot.MyTodoList.util.BotLabels;
import com.springboot.MyTodoList.util.BotMessages;
import com.springboot.MyTodoList.service.LangChainService;

public class ToDoItemBotController extends TelegramLongPollingBot {

    private static final Logger logger = LoggerFactory.getLogger(ToDoItemBotController.class);
    @Autowired
    private ToDoItemService toDoItemService;
    private AuthenticationService authenticationService;
    private OracleUserService oracleUserService;
    private Map<Long, OracleUser> authenticatedUsers = new HashMap<>();
    private String botName;
    private LangChainService langChainService;

    public ToDoItemBotController(String botToken, String botName, ToDoItemService toDoItemService, AuthenticationService authenticationService, OracleUserService oracleUserService, LangChainService langChainService) {
        super(botToken);
        this.authenticationService = authenticationService;
        this.toDoItemService = toDoItemService;
        this.oracleUserService = oracleUserService;
        this.botName = botName;
        this.langChainService = langChainService;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageTextFromTelegram = update.getMessage().getText();
            Long chatId = update.getMessage().getChatId();

            // Manejo de comandos de autenticación
            if (messageTextFromTelegram.startsWith("/login")) {
                handleLogin(messageTextFromTelegram, chatId);
            } else if (messageTextFromTelegram.equals("/logout")){
                handleLogout(chatId);
            } else if (authenticatedUsers.containsKey(chatId)) {
                // Procesar otros comandos solo si el usuario está autenticado
                handleCommands(messageTextFromTelegram, chatId);
            } else {
                sendMessage(chatId, "Please login using /login <USER_ID>");
            }
        }
    }

    private void handleLogin(String messageText, Long chatId) {
        String[] parts = messageText.split(" ");
        if (parts.length == 2) {
            try {
                int userId = Integer.valueOf(parts[1]);
                OracleUser user = authenticationService.authenticate(userId);
                if (user != null) {
                    user.setUserChatId(String.valueOf(chatId));
                    oracleUserService.updateUser(user);
                    authenticatedUsers.put(chatId, user);
                    sendMessage(chatId, "Login successful. Welcome " + user.getUserName() + "!");
                } else {
                    sendMessage(chatId, "Login failed. User ID not found.");
                }
            } catch (NumberFormatException e) {
                sendMessage(chatId, "Invalid USER_ID format. Use: /login <USER_ID>");
            }
        } else {
            sendMessage(chatId, "Invalid login command. Use: /login <USER_ID>");
        }
    }

    private void handleLogout(Long chatId) {
        if (authenticatedUsers.containsKey(chatId)) {
            OracleUser user = authenticatedUsers.remove(chatId);
            sendMessage(chatId, "Logout successful. Goodbye " + user.getUserName() + "!");
        } else {
            sendMessage(chatId, "You are not logged in.");
        }
    }


    private void handleCommands(String messageText, Long chatId) {
        OracleUser user = authenticatedUsers.get(chatId);
        if (authenticationService.isManager(user)) {
            // Manejar comandos para Manager
            handleManagerCommands(messageText, chatId);
        } else if (authenticationService.isDeveloper(user)) {
            // Manejar comandos para Developer
            handleDeveloperCommands(messageText, chatId, user);
        } else {
            sendMessage(chatId, "Unauthorized role.");
        }
    }

    private void handleManagerCommands(String messageText, Long chatId) {
        try {
            if (messageText.equals("/start") || messageText.equals(BotLabels.SHOW_MAIN_SCREEN.getLabel())) {
                sendMainMenu(chatId);
            } else if (messageText.equals("/viewAllTasks")) {
                sendMessage(chatId, "Fetching all tasks for the Team...");
                List<ToDoItem> allItems = getAllToDoItems();
                sendMessage(chatId, "All tasks for the Team: " + allItems.size());
                StringBuilder allItemsMessage = new StringBuilder("All tasks for the Team:\n");
                for (ToDoItem item : allItems) {
                    allItemsMessage.append("• <b>Task ID:</b>").append(item.getItemId())
                                   .append("\n    <b>Description:</b> ").append(item.getItemDescription())
                                   .append("\n    <b>Status:</b> ").append(item.getItemStatus())
                                   .append("\n");
                    if (item.getItemDeadline() != null) {
                        allItemsMessage.append("    <b>Deadline:</b> ").append(item.getItemDeadline()).append("\n");
                    } else {
                        allItemsMessage.append("    <b>Deadline:</b> Not set\n");
                    }
                    if (item.getSprint() != null) {
                        allItemsMessage.append("    <b>Sprint:</b> ").append(item.getSprint().getSprintId()).append("\n");
                    }
                    allItemsMessage.append("\n");
                }
                sendSplitMessage(chatId, allItemsMessage.toString());
                sendMessage(chatId, "All tasks for the Team sent.");
            } else if (messageText.startsWith("/viewTasksForDev ")) {
                sendMessage(chatId, "Fetching tasks for the Developer...");
                String userIdString = messageText.substring(17).trim();
                //sendMessage(chatId, "User ID: " + userIdString);
                try {
                    //sendMessage(chatId, "Parsing User ID...");
                    int userId = Integer.parseInt(userIdString);
                    //sendMessage(chatId, "User ID parsed: " + userId);
                    OracleUser user = oracleUserService.getUserById(userId);
                    //sendMessage(chatId, "User fetched");
                    if (user != null) {
                        //sendMessage(chatId, "User found");
                        List<ToDoItem> userTasks = getTasksForUser(user);
                        //sendMessage(chatId, "User tasks fetched: " + userTasks.size());
                        StringBuilder userTasksMessage = new StringBuilder(user.getUserName() + "'s tasks:\n");
                        for (ToDoItem task : userTasks) {
                            userTasksMessage.append("• <b>Task ID:</b>").append(task.getItemId())
                                            .append("\n    <b>Description:</b> ").append(task.getItemDescription())
                                            .append("\n    <b>Status:</b> ").append(task.getItemStatus())
                                            .append("\n");
                            if (task.getItemDeadline() != null) {
                                userTasksMessage.append("    <b>Deadline:</b> ").append(task.getItemDeadline()).append("\n");
                            } else {
                                userTasksMessage.append("    <b>Deadline:</b> Not set\n");
                            }
                            if (task.getSprint() != null) {
                                userTasksMessage.append("    <b>Sprint:</b> ").append(task.getSprint().getSprintId()).append("\n");
                            }
                            userTasksMessage.append("\n");
                        }
                        sendSplitMessage(chatId, userTasksMessage.toString());
                        sendMessage(chatId, "User tasks sent.");
                    } else {
                        sendMessage(chatId, "User not found. Please provide a valid user id.");
                    }
                } catch (NumberFormatException e) {
                    sendMessage(chatId, "Invalid USER_ID format. Use: /viewTasksForDev <USER_ID>");
                }
            } else {
                sendMessage(chatId, "Command or instruction '" + messageText
                        + "' not recognized. Please use one of the following commands: "
                        + "\n/viewAllTasks\n/viewTasksForDev USER_ID");
            }
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
            sendMessage(chatId, "Failed to process manager command. " + e.getMessage());
        }
    }
            
    private void handleDeveloperCommands(String messageText, Long chatId, OracleUser user) {
        if (messageText.equals("/start") || messageText.equals(BotLabels.SHOW_MAIN_SCREEN.getLabel())) {
            sendMainMenu(chatId);
        } else if (messageText.startsWith("/createTask ")) {
            String taskDescription = messageText.substring(12);
            createTask(chatId, user, taskDescription);
        } else if (messageText.startsWith("/updateTask ")) {
            String[] parts = messageText.substring(12).split(" ", 2);
            int taskId = Integer.parseInt(parts[0]);
            String taskDescription = parts[1];
            updateTask(chatId, taskId, taskDescription);
        } else if (messageText.startsWith("/markDone ")) {
            int taskId = Integer.parseInt(messageText.substring(10));
            markTaskDone(chatId, taskId);
        } else if (messageText.startsWith("/markInProgress ")) {
            int taskId = Integer.parseInt(messageText.substring(16));
            markTaskInProgress(chatId, taskId);
        } else if (messageText.equals("/viewMyTasks")) {
            List<ToDoItem> userTasks = getTasksForUser(user);
            StringBuilder tasksMessage = new StringBuilder();
            for (ToDoItem task : userTasks) {
                tasksMessage.append("• <b>Task ID:</b>").append(task.getItemId()).append("\n")
                            .append("    <b>Description:</b> ").append(task.getItemDescription()).append("\n")
                            .append("    <b>Status:</b> ").append(task.getItemStatus()).append("\n");
                if (task.getItemDeadline() != null) {
                    tasksMessage.append("    <b>Deadline:</b> ").append(task.getItemDeadline()).append("\n");
                } else {
                    tasksMessage.append("    <b>Deadline:</b> Not set\n");
                }
                if (task.getSprint() != null) {
                    tasksMessage.append("    <b>Sprint:</b> ").append(task.getSprint().getSprintId()).append("\n");
                }
                tasksMessage.append("\n");
            }
            sendSplitMessage(chatId, tasksMessage.toString());
        } else {
            sendMessage(chatId, "Command or instruction '" + messageText 
            + "' not recognized. Please use one of the following commands: "
            + "\n/createTask taskDescription\n/updateTask taskId "
            + "taskDescription\n/markDone taskId\n/markInProgress "
            + "taskId\n/viewMyTasks");
        }
    }

    // Utility method to split and send message in chunks
    private void sendSplitMessage(Long chatId, String message) {
        int maxLength = 4096; // Telegram's message size limit
        int start = 0;
        while (start < message.length()) {
            int end = Math.min(start + maxLength, message.length());
            sendMessage(chatId, message.substring(start, end));
            start = end;
        }
    }

    private void sendMainMenu(Long chatId) {
        OracleUser user = authenticatedUsers.get(chatId);
        if (user == null) {
            sendMessage(chatId, "Please login using /login <USER_ID>");
            return;
        }

        SendMessage messageToTelegram = new SendMessage();
        messageToTelegram.setChatId(chatId);
        messageToTelegram.setText(BotMessages.HELLO_MYTODO_BOT.getMessage());

        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();

        if (authenticationService.isManager(user)) {
            // Manager buttons
            KeyboardRow row = new KeyboardRow();
            row.add(BotCommands.VIEW_TASKS_FOR_DEV.getCommand());
            row.add(BotCommands.VIEW_ALL_TASKS.getCommand());
            keyboard.add(row);
        } else if (authenticationService.isDeveloper(user)) {
            // Developer buttons
            KeyboardRow row = new KeyboardRow();
            row.add(BotCommands.CREATE_TASK.getCommand());
            row.add(BotCommands.UPDATE_TASK.getCommand());
            keyboard.add(row);

            row = new KeyboardRow();
            row.add(BotCommands.MARK_DONE.getCommand());
            row.add(BotCommands.MARK_IN_PROGRESS.getCommand());
            keyboard.add(row);
        }

        // Common buttons
        KeyboardRow commonRow = new KeyboardRow();
        commonRow.add(BotCommands.START_COMMAND.getCommand());
        commonRow.add(BotCommands.HIDE_COMMAND.getCommand());
        commonRow.add(BotCommands.LOGOUT.getCommand());
        keyboard.add(commonRow);

        // Set the keyboard
        keyboardMarkup.setKeyboard(keyboard);

        // Add the keyboard markup
        messageToTelegram.setReplyMarkup(keyboardMarkup);

        try {
            execute(messageToTelegram);
        } catch (TelegramApiException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }


    private void sendMessage(Long chatId, String text) {
        SendMessage message = new SendMessage();
        message.enableHtml(true);
        message.setChatId(chatId);
        message.setText(text);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getBotUsername() {        
        return botName;
    }

    // GET /todolist
    public List<ToDoItem> getAllToDoItems() { 
        return toDoItemService.findAll();
    }

    // GET tasks for a specific developer
    //public List<ToDoItem> getTasksForDeveloper(String developerName) {
    //    return toDoItemService.findTasksByDeveloperName(developerName);
    //}

    // GET tasks for a specific user
    public List<ToDoItem> getTasksForUser(OracleUser user) {
        return toDoItemService.findByUserId(user.getUserId());
    }

    // GET BY ID /todolist/{id}
    @GetMapping("/todolist/user")
    public ResponseEntity<ToDoItem> getToDoItemById(@RequestParam int userId) {
        try {
            ResponseEntity<ToDoItem> responseEntity = toDoItemService.getItemById(userId);
            return new ResponseEntity<ToDoItem>(responseEntity.getBody(), HttpStatus.OK);
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // PUT /todolist
    public ResponseEntity addToDoItem(@RequestBody ToDoItem todoItem) throws Exception {
        ToDoItem td = toDoItemService.addToDoItem(todoItem);
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("location", "" + td.getItemId());
        responseHeaders.set("Access-Control-Expose-Headers", "location");
        return ResponseEntity.ok().headers(responseHeaders).build();
    }

    // UPDATE /todolist/{id}
    public ResponseEntity updateToDoItem(@RequestBody ToDoItem toDoItem, @PathVariable int id) {
        try {
            ToDoItem toDoItem1 = toDoItemService.updateToDoItem(id, toDoItem);
            return new ResponseEntity<>(toDoItem1, HttpStatus.OK);
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    // DELETE todolist/{id}
    public ResponseEntity<Boolean> deleteToDoItem(@PathVariable("id") int id) {
        Boolean flag = false;
        try {
            flag = toDoItemService.deleteToDoItem(id);
            return new ResponseEntity<>(flag, HttpStatus.OK);
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
            return new ResponseEntity<>(flag, HttpStatus.NOT_FOUND);
        }
    }

    // Métodos para los comandos específicos de Developer
    private void createTask(Long chatId, OracleUser user, String taskDescription) {
        logger.info("Creating task for user: " + user.getUserName() + " with description: " + taskDescription);
        try {
            ToDoItem newItem = new ToDoItem();
            Map<String, Object> taskDetails = langChainService.categorizeMessage(taskDescription);
            //sendMessage(chatId, "Task details: " + taskDetails); // TODO: Remove this debug message
            newItem.setItemDescription((String) taskDetails.get("taskName"));
            newItem.setItemCreationTs(OffsetDateTime.now());
            newItem.setItemStatus("Not Started");
            //sendMessage(chatId, "Desc, creationTs, status set " + newItem.getItemDescription() + newItem.getItemCreationTs() + newItem.getItemStatus()); // TODO: Remove this debug message
            if (taskDetails.get("taskDeadline") != null) {
                newItem.setItemDeadline((OffsetDateTime) taskDetails.get("taskDeadline"));
            }
            //sendMessage(chatId, "Deadline set " + newItem.getItemDeadline()); // TODO: Remove this debug message
            if (taskDetails.get("sprintNumber") != null) {
                ToDoSprint sprint = (ToDoSprint) taskDetails.get("sprintNumber");
                newItem.setSprint(sprint);
            }
            //sendMessage(chatId, "Sprint set " + newItem.getSprint()); // TODO: Remove this debug message
            newItem.setUser(user);
            //sendMessage(chatId, "User set " + newItem.getUser()); // TODO: Remove this debug message

            toDoItemService.addToDoItem(newItem);
            sendMessage(chatId, "Task created successfully for user: " 
            + user.getUserName() + " with description: " + taskDetails.get("taskName"));
            logger.info("Task created successfully for user: " + user.getUserName());
        } catch (Exception e) {
            logger.error("Failed to create task for user: " + user.getUserName(), e);
            sendMessage(chatId, "Failed to create task." + user.getUserName() + e);
        }
    }

    private void updateTask(Long chatId, int taskId, String taskDescription) {
        try {
            ToDoItem item = getToDoItemById(taskId).getBody();
            if (item == null) {
                sendMessage(chatId, "Task not found, please provide a valid task id.");
                return;
            }
            Map<String, Object> taskDetails = LangChainService.categorizeMessageUpdate(taskDescription);
            //sendMessage(chatId, "Task details: " + taskDetails); // TODO: Remove this debug message
            if (taskDetails.get("taskName") != null) {
                item.setItemDescription((String) taskDetails.get("taskName"));
                //sendMessage(chatId, "Description set" + taskDetails.get("taskName")); // TODO: Remove this debug message
            }
            if (taskDetails.get("taskDeadline") != null) {
                //item.setItemDeadline((OffsetDateTime) taskDetails.get("taskDeadline"));
                //sendMessage(chatId, "Deadline set" + taskDetails.get("taskDeadline")); // TODO: Remove this debug message
            }
            if (taskDetails.get("sprintNumber") != null) {
                ToDoSprint sprint = (ToDoSprint) taskDetails.get("sprintNumber");
                item.setSprint(sprint);
                //sendMessage(chatId, "Sprint set" + taskDetails.get("sprintNumber")); // TODO: Remove this debug message
            }
            updateToDoItem(item, taskId);
            sendMessage(chatId, "Task updated successfully." + item.getItemDescription() + item.getItemDeadline() + item.getSprint());
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
            sendMessage(chatId, "Failed to update task, please review the task details.");
        }
    }

    private void markTaskDone(Long chatId, int taskId) {
        try {
            ToDoItem item = getToDoItemById(taskId).getBody();
            item.setItemStatus("Done");
            updateToDoItem(item, taskId);
            sendMessage(chatId, "Task " + taskId + " marked as done.");
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
            sendMessage(chatId, "Failed to mark task as done, please review the task id.");
        }
    }

        private void markTaskInProgress(Long chatId, int taskId) {
        try {
            ToDoItem item = getToDoItemById(taskId).getBody();
            item.setItemStatus("In Progress");
            updateToDoItem(item, taskId);
            sendMessage(chatId, "Task " + taskId + " marked as in progress.");
        } catch (Exception e) {
            logger.error("Failed to mark task as in progress for taskId: " + taskId, e);
            sendMessage(chatId, "Failed to mark task as in progress, please review the task id.");
        }
    }

}
