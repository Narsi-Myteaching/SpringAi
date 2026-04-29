package com.narsiit.app.web.api;

import java.util.*;

import com.narsiit.app.beans.ChatBotRequest;
import com.narsiit.app.beans.ChatBotResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

@RestController
public class ChatBotController {

  private final ChatClient jdbcMemoryChatClient;
  private final ChatMemory chatMemory;

  final String systemMessage = "You are mypersonal assistant. you should reply either yes nor no or not sure.";

  public ChatBotController(OpenAiChatModel openAiChatModel, ChatMemory chatMemory) {
      this.chatMemory = chatMemory;


      //step-1: prepare the chat memory advisor
      var messageChatMemoryAdvisor = MessageChatMemoryAdvisor
              .builder(chatMemory)
              .build();

     //step-2: Create Chat Client with the Advisors
      this.jdbcMemoryChatClient = ChatClient
              .builder(openAiChatModel)
              .defaultAdvisors(messageChatMemoryAdvisor)
              .build();
  }

  @PostMapping(value = {"/api/chat"})
  public ChatBotResponse askQuestion(@RequestBody ChatBotRequest chatBotRequest) {

    String sessionId = chatBotRequest.sessionId();
    var chatRequest = this.jdbcMemoryChatClient
            .prompt()
            .system(systemMessage)
            .user(chatBotRequest.question())
            .advisors(advisor->advisor.param(ChatMemory.CONVERSATION_ID,sessionId));

    String assistantAnswer =
            chatRequest
                .call()
                .chatResponse()
                .getResult().
                getOutput().
                getText();

    return new ChatBotResponse(chatBotRequest.question(), assistantAnswer);
  }

  @DeleteMapping(value = {"/api/chat/{sessionId}"})
  public Map<String, Boolean> deleteConversation(@PathVariable String sessionId){
    Map<String, Boolean> responseMap = new HashMap<>();
    chatMemory.clear(sessionId);
    List<Message> conversationList = chatMemory.get(sessionId);
    if(CollectionUtils.isEmpty(conversationList)){
      responseMap.put("isConversationDeleted", true);
    }else{
      responseMap.put("isConversationDeleted", false);
    }
    return  responseMap;
  }


}
