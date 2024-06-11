package com.springboot.MyTodoList.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;



@Service
public class LangChainService {

    @Autowired
    private OpenAiChatModel openAiChatModel;

    public static Map<String, Object> categorizeMessage(String message) {
        ChatLanguageModel model = OpenAiChatModel.builder()
            .apiKey(ApiKeys.OPENAI_API_KEY)
            .modelName("gpt-4")
            .build();
        
        // Current date 
        LocalDate today = LocalDate.now();

        List<ChatMessage> messages = new ArrayList<>();
        messages.add(new SystemMessage("Categorize this text in the following categories "
        + "and calculate date or sprint if needed (sprint MUST BE None if not said): "
        + "Task name, Task deadline, Sprint number"));
        messages.add(new SystemMessage("Today's date: " 
        + today.format(DateTimeFormatter.ofPattern("EEEE MMMM dd, yyyy"))));
        messages.add(new SystemMessage("Task name: (100 chars max)\nTask "
        + "deadline: (YYYY-MM-DD or None if not said by user)\n"
        + "Sprint number: (integer or None if not said by user)"));
        messages.add(new UserMessage(message));

        Response<AiMessage> response = model.generate(messages);

        String parsedResult = response.content().text();

        String taskName = parsedResult.split("\n")[0].split(": ")[1].trim();
        String taskDeadline = parsedResult.split("\n")[1].split(": ")[1].trim();
        String sprintNumberStr = parsedResult.split("\n")[2].split(": ")[1].trim();
        int sprintNumber = Integer.parseInt(sprintNumberStr.equals("None") ? "0" : sprintNumberStr);

        OffsetDateTime deadline;
        if (taskDeadline.equals("None")) {
            deadline = null;
        } else {
            deadline = LocalDate.parse(taskDeadline).atStartOfDay().atOffset(OffsetDateTime.now().getOffset());
        }

        String deliverSprint = "None";
        if (sprintNumber <= 0) {
            deliverSprint = null;
        }


        Map<String, Object> taskDetails = new HashMap<>();
        taskDetails.put("taskName", taskName);
        taskDetails.put("taskDeadline", deadline);
        if (sprintNumber <= 0) {
            taskDetails.put("sprintNumber", deliverSprint);
        } else {
            taskDetails.put("sprintNumber", sprintNumber);
        }

        return taskDetails;
    }

        public static Map<String, Object> categorizeMessageUpdate(String message) {
        ChatLanguageModel model = OpenAiChatModel.builder()
                .apiKey(ApiKeys.OPENAI_API_KEY)
                .modelName("gpt-4")
                .build();

        // Current date
        LocalDate today = LocalDate.now();

        List<ChatMessage> messages = new ArrayList<>();
        messages.add(new SystemMessage("Categorize this text in the following categories "
        + "if said by user: Task name, Task deadline, Sprint number. If not said, "
        + "it should be None. If needed, calculate date or sprint."));
        messages.add(new SystemMessage("Today's date: " 
        + today.format(DateTimeFormatter.ofPattern("EEEE MMMM dd, yyyy"))));
        messages.add(new SystemMessage("Task name: (100 chars max or None if "
        + "not said by user)\nTask deadline: (YYYY-MM-DD or None if not said "
        + "by user)\nSprint number: (integer or None if not said by user)"));
        messages.add(new UserMessage(message));

        Response<AiMessage> response = model.generate(messages);

        String parsedResult = response.content().text();

        // Extract the task details from the parsed result
        String taskName = parsedResult.split("\n")[0].split(": ")[1].trim();
        String taskDeadline = parsedResult.split("\n")[1].split(": ")[1].trim();
        String sprintNumberStr = parsedResult.split("\n")[2].split(": ")[1].trim();
        int sprintNumber = Integer.parseInt(sprintNumberStr.equals("None") ? "0" : sprintNumberStr);

        String taskNameValidated = taskName.equals("None") ? null : taskName;

        OffsetDateTime deadline;
        if (taskDeadline.equals("None")) {
            deadline = null;
        } else {
            deadline = LocalDate.parse(taskDeadline).atStartOfDay().atOffset(OffsetDateTime.now().getOffset());
        }

        String deliverSprint = "None";
        if (sprintNumber <= 0) {
            deliverSprint = null;
        }

        System.out.println("Extracted Task Details:");
        System.out.println("Task Name: " + taskNameValidated);
        System.out.println("Task Deadline: " + deadline);
        if (sprintNumber <= 0) {
            System.out.println("Sprint Number: None" + deliverSprint);
        } else {
            System.out.println("Sprint Number: " + sprintNumber);
        }

        Map<String, Object> taskDetails = new HashMap<>();
        taskDetails.put("taskName", taskNameValidated);
        taskDetails.put("taskDeadline", deadline);
        if (sprintNumber <= 0) {
            taskDetails.put("sprintNumber", deliverSprint);
        } else {
            taskDetails.put("sprintNumber", sprintNumber);
        }

        return taskDetails;
    }
}
