package com.springboot.MyTodoList.controller;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.springboot.MyTodoList.model.OracleUser;
import com.springboot.MyTodoList.model.ToDoItem;
import com.springboot.MyTodoList.service.AuthenticationService;
import com.springboot.MyTodoList.service.ToDoItemService;
import com.springboot.MyTodoList.service.OracleUserService;
import com.springboot.MyTodoList.util.BotCommands;
import com.springboot.MyTodoList.util.BotHelper;
import com.springboot.MyTodoList.util.BotLabels;
import com.springboot.MyTodoList.util.BotMessages;

public class ToDoItemBotController extends TelegramLongPollingBot {

    private static final Logger logger = LoggerFactory.getLogger(ToDoItemBotController.class);
    private ToDoItemService toDoItemService;
    private AuthenticationService authenticationService;
    private OracleUserService oracleUserService;
    private Map<Long, OracleUser> authenticatedUsers = new HashMap<>();
    private String botName;

    public ToDoItemBotController(String botToken, String botName, ToDoItemService toDoItemService, AuthenticationService authenticationService, OracleUserService oracleUserService) {
        super(botToken);
        this.authenticationService = authenticationService;
        this.toDoItemService = toDoItemService;
        this.oracleUserService = oracleUserService;
        this.botName = botName;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageTextFromTelegram = update.getMessage().getText();
            Long chatId = update.getMessage().getChatId();

            // Manejo de comandos de autenticación
            if (messageTextFromTelegram.startsWith("/login")) {
                handleLogin(messageTextFromTelegram, chatId);
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
        if (messageText.equals("/start") || messageText.equals(BotLabels.SHOW_MAIN_SCREEN.getLabel())) {
            sendMainMenu(chatId);
        } else if (messageText.equals("/viewAllTasks")) {
            List<ToDoItem> allItems = getAllToDoItems();
            String tasks = allItems.stream().map(ToDoItem::getItemDescription).collect(Collectors.joining("\n"));
            sendMessage(chatId, tasks);
        } else if (messageText.startsWith("/viewTasksForDev ")) {
            String devName = messageText.substring(17);
            //List<ToDoItem> devTasks = getTasksForDeveloper(devName);
            //String tasks = devTasks.stream().map(ToDoItem::getItemDescription).collect(Collectors.joining("\n"));
            //sendMessage(chatId, tasks);
            sendMessage(chatId, messageText);
        } else {
            sendMessage(chatId, "Manager command received: " + messageText);
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
            //List<ToDoItem> userTasks = getTasksForUser(user);
            //String tasks = userTasks.stream().map(ToDoItem::getItemDescription).collect(Collectors.joining("\n"));
            //sendMessage(chatId, tasks);
            sendMessage(chatId, messageText);;
        } else {
            sendMessage(chatId, "Developer command received: " + messageText);
        }
    }

    private void sendMainMenu(Long chatId) {
        SendMessage messageToTelegram = new SendMessage();
        messageToTelegram.setChatId(chatId);
        messageToTelegram.setText(BotMessages.HELLO_MYTODO_BOT.getMessage());

        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();

        // First row
        KeyboardRow row = new KeyboardRow();
        row.add(BotLabels.LIST_ALL_ITEMS.getLabel());
        row.add(BotLabels.ADD_NEW_ITEM.getLabel());
        keyboard.add(row);

        // Second row
        row = new KeyboardRow();
        row.add(BotLabels.SHOW_MAIN_SCREEN.getLabel());
        row.add(BotLabels.HIDE_MAIN_SCREEN.getLabel());
        keyboard.add(row);

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
    //public List<ToDoItem> getTasksForUser(OracleUser user) {
    //    return toDoItemService.findTasksByUserId(user.getUserId());
    //}

    // GET BY ID /todolist/{id}
    public ResponseEntity<ToDoItem> getToDoItemById(@PathVariable int id) {
        try {
            ResponseEntity<ToDoItem> responseEntity = toDoItemService.getItemById(id);
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
            sendMessage(chatId, "Inicio de crear");
            ToDoItem newItem = new ToDoItem();
            sendMessage(chatId, "Setting description");
            newItem.setItemDescription(taskDescription);
            sendMessage(chatId, "Setting TS");
            newItem.setItemCreationTs(OffsetDateTime.now());
            sendMessage(chatId, "Setting status");
            newItem.setItemStatus("Not Started");
            sendMessage(chatId, "Setting user");
            newItem.setUser(user);
            sendMessage(chatId, "Adding item");
            toDoItemService.addToDoItem(newItem);
            sendMessage(chatId, "Success");
            sendMessage(chatId, "Task created successfully.");
            logger.info("Task created successfully for user: " + user.getUserName());
        } catch (Exception e) {
            logger.error("Failed to create task for user: " + user.getUserName(), e);
            sendMessage(chatId, "Failed to create task." + user.getUserName() + e);
        }
    }

    private void updateTask(Long chatId, int taskId, String taskDescription) {
        try {
            ToDoItem item = getToDoItemById(taskId).getBody();
            item.setItemDescription(taskDescription);
            updateToDoItem(item, taskId);
            sendMessage(chatId, "Task updated successfully.");
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
            sendMessage(chatId, "Failed to update task.");
        }
    }

    private void markTaskDone(Long chatId, int taskId) {
        try {
            ToDoItem item = getToDoItemById(taskId).getBody();
            item.setItemStatus("Done");
            updateToDoItem(item, taskId);
            sendMessage(chatId, "Task marked as done.");
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
            sendMessage(chatId, "Failed to mark task as done.");
        }
    }

        private void markTaskInProgress(Long chatId, int taskId) {
        try {
            ToDoItem item = getToDoItemById(taskId).getBody();
            item.setItemStatus("In Progress");
            updateToDoItem(item, taskId);
            sendMessage(chatId, "Task marked as in progress.");
        } catch (Exception e) {
            logger.error("Failed to mark task as in progress for taskId: " + taskId, e);
            sendMessage(chatId, "Failed to mark task as in progress.");
        }
    }

}
