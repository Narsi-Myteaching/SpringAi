package com.narsiit.app.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatMemoryConfig {

//    @Bean
//    ChatMemory chatMemory(){
//        return MessageWindowChatMemory.builder().build();
//    }

    @Bean
    public ChatMemory chatMemory(JdbcChatMemoryRepository repository) {
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(repository)
                .maxMessages(10) // Keeps the last 10 messages
                .build();
    }

    @Bean
    public ChatClient chatClient(ChatClient.Builder builder){
        return builder.defaultSystem("you are a helpful assistant").build();
    }
}
