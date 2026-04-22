package com.narsiit.ai.web.api;

import com.narsiit.ai.beans.ChatBotRequest;
import com.narsiit.ai.beans.ChatBotResponse;
import com.narsiit.ai.web.service.ChatBotManager;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
public class ChatBotController {

  private final ChatClient chatClient;
  private final ChatBotManager chatBotManager;
  final String systemMessage = "You are mypersonal assistant";

  public ChatBotController(ChatClient.Builder chatBuilder, ChatBotManager chatBotManager) {

    this.chatClient = chatBuilder.build();

    this.chatBotManager = chatBotManager;
  }

  @PostMapping(value = {"/api/chat"})
  public ChatBotResponse askQuestion(@RequestBody ChatBotRequest chatBotRequest) {

    String sessionId = chatBotRequest.sessionId();


    // step-1: check new session or not
    boolean isExistedSession = chatBotManager.isExistedSession(sessionId);
    List<Message> previousChatHistory = new ArrayList<>();
    if (isExistedSession) {
      previousChatHistory = chatBotManager.getChatHistory(sessionId);
    }else{
      chatBotManager.addSystemMessage(sessionId, systemMessage);
    }
    var newChatMessage = new ArrayList<>(previousChatHistory);

    newChatMessage.add(new UserMessage(chatBotRequest.question()));

    Prompt prompt = new Prompt(newChatMessage);
    String assistantAnswer =
        this.chatClient
                .prompt(prompt)
                .call()
                .chatResponse()
                .getResult().
                getOutput().
                getText();

    chatBotManager.addChatHistory(sessionId, chatBotRequest.question(), assistantAnswer);

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
