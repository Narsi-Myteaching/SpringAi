package com.narsiit.ai.web.api;

import com.narsiit.ai.beans.QuestionAnswerBean;
import com.narsiit.ai.beans.QuestionsBean;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ChatBotAPI {

  private final ChatClient chatClient;
  private final ChatMemory chatMemory;

  ChatBotAPI(ChatClient.Builder chatBuilder, ChatMemory chatMemory) {
    this.chatClient = chatBuilder.build();
      this.chatMemory = chatMemory;
  }

  @PostMapping(value = {"/qas"})
  public ResponseEntity<QuestionAnswerBean> getAnswer(@RequestBody QuestionsBean questionsBean) {

    ChatOptions chatOptions = ChatOptions.builder().model("gpt-5.4-nano").temperature(0.5).build();
    Prompt prompt = new Prompt(questionsBean.getQuestion(), chatOptions);
    String answer = this.chatClient.prompt(prompt).call().content();
    QuestionAnswerBean qasBean =
        QuestionAnswerBean.builder().question(questionsBean.getQuestion()).answer(answer).build();

    return ResponseEntity.ok(qasBean);
  }
}
