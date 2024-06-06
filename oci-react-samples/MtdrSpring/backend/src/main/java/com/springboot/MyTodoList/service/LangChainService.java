package com.springboot.MyTodoList.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.time.format.DateTimeParseException;

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

    private final OpenAiChatModel openAiChatModel;

    @Autowired
    public LangChainService(OpenAiChatModel openAiChatModel) {
        this.openAiChatModel = openAiChatModel;
    }

    public Map<String, Object> categorizeMessage(String message) {
        ChatLanguageModel model = this.openAiChatModel;
        
        // Current date and sprint number
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
            try {
                deadline = LocalDate.parse(taskDeadline).atStartOfDay().atOffset(OffsetDateTime.now().getOffset());
            } catch (DateTimeParseException e) {
                // Handle invalid date format here, perhaps by logging an error or setting a default value
                deadline = null; // Set to null or another default value if parsing fails
            }
        }

        if (deadline!= null &&!taskDeadline.equals("None")) {
            taskDeadline = taskDeadline.substring(0, Math.min(taskDeadline.length(), 10));
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
}