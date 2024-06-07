package com.springboot.MyTodoList.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.springboot.MyTodoList.service.ApiKeys;

// Assuming OpenAiChatModel is part of the langchain4j-open-ai library
// Adjust the import path according to where OpenAiChatModel is actually located
import dev.langchain4j.model.openai.OpenAiChatModel;

@Configuration
public class OpenAiConfig {

    @Bean
    public OpenAiChatModel openAiChatModel() {
        // Dynamically configure the OpenAiChatModel instance based on environment variables or configuration properties
        // This example assumes ApiKeys.OPENAI_API_KEY is a constant holding your API key
        return OpenAiChatModel.builder()
               .apiKey(ApiKeys.OPENAI_API_KEY) // Replace ApiKeys.OPENAI_API_KEY with your actual API key retrieval logic
               .modelName("gpt-4") // Specify the model name
               .build(); // Build the configured instance
    }
}
