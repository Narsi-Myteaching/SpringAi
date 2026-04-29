package com.narsiit.ai.web.api;

import com.narsiit.ai.beans.ChatBotRequest;
import com.narsiit.ai.beans.ChatBotResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
public class ChatBotController {

  private final ChatClient inMemoryChatClient;
  private final ChatMemory chatMemory;

  final String systemMessage = "You are mypersonal assistant";

  public ChatBotController(OpenAiChatModel openAiChatModel, ChatMemory chatMemory) {
      this.chatMemory = chatMemory;


      //step-1: prepare the chat memory advisor
      var messageChatMemoryAdvisor = MessageChatMemoryAdvisor
              .builder(chatMemory)
              .build();

     //step-2: Create Chat Client with the Advisors
      this.inMemoryChatClient = ChatClient
              .builder(openAiChatModel)
              .defaultAdvisors(messageChatMemoryAdvisor)
              .build();
  }

  @PostMapping(value = {"/api/chat"})
  public ChatBotResponse askQuestion(@RequestBody ChatBotRequest chatBotRequest) {

    String sessionId = chatBotRequest.sessionId();
    var chatRequest = this.inMemoryChatClient
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

  @PostMapping(value = {"/api/fact-check"})
  public ChatBotResponse factCheck(@RequestBody ChatBotRequest chatBotRequest) {

    String sessionId = chatBotRequest.sessionId();
    var chatRequest = this.inMemoryChatClient
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

//  @PostMapping("/api/chat")
//  public ChatBotResponse askQuestion(@RequestBody ChatBotRequest chatBotRequest) {
//    String sessionId = chatBotRequest.sessionId();
//    String question = chatBotRequest.question();
//    // Setup System Message for new Session
//    if (chatBotManager.isNewSession(sessionId))
//      chatBotManager.addSystemMessage(sessionId, "You are mypersonal assistant");
//    // Combine chat history with the new question
//    var chatHistory = chatBotManager.getChatHistory(sessionId);
//    var messages = new ArrayList<>(chatHistory);
//    messages.add(new UserMessage(question));
//    // create a prompt
//    Prompt prompt = new Prompt(messages);
//    // call the chat client
//    ChatResponse chatResponse = this.chatClient.prompt(prompt).call().chatResponse();
//    // get the answer
//    String answer = chatResponse.getResult().getOutput().getText();
//    // add the chat history to our local cache
//    chatBotManager.addChatHistory(sessionId, question, answer);
//    return new ChatBotResponse(question, answer);
//  }
}
