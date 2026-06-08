package com.narsiit.app.web.api;

import com.narsiit.app.beans.TTsRequestBean;
import org.springframework.ai.audio.tts.TextToSpeechPrompt;
import org.springframework.ai.audio.tts.TextToSpeechResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiAudioSpeechModel;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/tts")
public class TTsController {

  private OpenAiAudioSpeechModel speechModel;
  private ChatClient chatClient;

  public TTsController(OpenAiAudioSpeechModel speechModel, ChatClient.Builder builder) {
    this.speechModel = speechModel;
    this.chatClient = builder.build();
  }

  @PostMapping(produces = "audio/mpeg")
  public ResponseEntity<byte[]> convertSpeechToText(@RequestBody TTsRequestBean tTsRequestBean) {
    TextToSpeechPrompt speechPrompt = new TextToSpeechPrompt(tTsRequestBean.text());

    TextToSpeechResponse textToSpeechResponse = speechModel.call(speechPrompt);
    byte[] audioBytes = textToSpeechResponse.getResult().getOutput();
    return ResponseEntity.ok()
        .contentType(MediaType.valueOf("audio/mpeg"))
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=speech.mp3")
            .body(audioBytes);
  }
}
