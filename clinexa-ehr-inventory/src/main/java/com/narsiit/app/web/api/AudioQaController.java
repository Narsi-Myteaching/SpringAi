package com.narsiit.app.web.api;

import com.narsiit.app.beans.AudioQaResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.audio.transcription.AudioTranscriptionPrompt;
import org.springframework.ai.audio.transcription.AudioTranscriptionResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiAudioTranscriptionModel;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;

@RestController
@RequestMapping("/api/v1/audio")
@Slf4j
public class AudioQaController {

  private OpenAiAudioTranscriptionModel transcriptionModel;
  private ChatClient chatClient;

  public AudioQaController(
      OpenAiAudioTranscriptionModel transcriptionModel, ChatClient.Builder chatClientBuilder) {
    this.transcriptionModel = transcriptionModel;
    this.chatClient = chatClientBuilder.build();
  }

  @PostMapping("/ask")
  public ResponseEntity<AudioQaResponse> processAudioQA(
      @RequestParam(name = "file") MultipartFile file,
      @RequestParam(name = "question") String question) {
    try {
      Resource resource =
          new ByteArrayResource(file.getBytes()) {
            @Override
            public String getFilename() {
              return file.getOriginalFilename();
            }
          };

      AudioTranscriptionPrompt audioPropmpt = new AudioTranscriptionPrompt(resource);
      AudioTranscriptionResponse transcriptionResponse = transcriptionModel.call(audioPropmpt);
      String transcription = transcriptionResponse.getResult().getOutput();
      log.info("transcription is {}", transcription);

     String answer = chatClient
          .prompt()
          .system(
                """
                        you are an assistant that answers questions only from the provided audio transcript.
                        if the transcript doesn't contains the enough information, say that clearly.
                """)
          .user(
                  """
                           Audio Transcript:
                           %s
                           User Question:
                           %s
                   """
                  .formatted(transcription, question))
          .call()
          .content();

     return ResponseEntity.ok(new AudioQaResponse(transcription, answer));
    } catch (Exception e) {
      log.error(e.getLocalizedMessage());
    }

    return null;
  }
}
