package com.narsiit.app.web.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.narsiit.app.beans.ChatBotRequest;
import com.narsiit.app.beans.ChatBotResponse;
import com.narsiit.app.beans.ClinexaUserMessageModel;
import com.narsiit.app.models.Product;
import com.narsiit.app.repos.ProductRepository;
import jdk.jfr.ContentType;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.*;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.content.Media;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductsServiceImpl implements ProductsService {

  private final ProductRepository productRepository;
  private final ChatClient chatClient;
  private final JdbcTemplate jdbcTemplate;

  @Override
  @SneakyThrows
  public String getProductsFromDb(String sqlQuery) {

    var dbProducts = jdbcTemplate.queryForList(sqlQuery);
    ObjectMapper mapper = new ObjectMapper();
    String productsJson = mapper.writeValueAsString(dbProducts);
    return productsJson;
  }

  @SneakyThrows
  @Override
  public List<String> getProductsFromMultipleDbQuesries(List<String> sqlQuery) {

    List<String> quesryResponse = new ArrayList<>();

    sqlQuery.forEach(
        query -> {
          var dbProducts = jdbcTemplate.queryForList(query);
          ObjectMapper mapper = new ObjectMapper();
          try {
            String productsJson = mapper.writeValueAsString(dbProducts);
            quesryResponse.add(productsJson);
          } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
          }
        });

    return quesryResponse;
  }

  @Override
  public Product createProduct(Product product) {
    return productRepository.save(product);
  }

  @Override
  public List<Product> getAllPorducts() {
    return productRepository.findAll();
  }

  @Override
  public Product getProductById(String productId) {
    return productRepository.findById(productId).get();
  }

  @Override
  public String generateSqlQuery(String userQuestion) {
    String promptTextFileName = "generate-sql-query-prompt.txt";
    String promptContent = readFromClasspath(promptTextFileName);

    var messagesList = new ArrayList<Message>();
    messagesList.add(new SystemMessage(promptContent));
    messagesList.add(new UserMessage(userQuestion));

    Prompt prompt = new Prompt(messagesList);

    String llmQuery =
        this.chatClient.prompt(prompt).call().chatResponse().getResult().getOutput().getText();
    return llmQuery;
  }

  @Override
  public String generateFinalAnswer(String question, String sqlQuery, List<String> sqlJson) {
    String context = readFromClasspath("sql-to-natural-prompt.txt");
    String userMessage =
        "Question: \n" + question + "sqlQuery: \n" + sqlQuery + "results: " + sqlJson;
    var messages = new ArrayList<Message>();
    messages.add(new SystemMessage(context));
    messages.add(new UserMessage(userMessage));
    Prompt prompt = new Prompt(messages);
    // call the chat client
    ChatResponse chatResponse = chatClient.prompt(prompt).call().chatResponse();
    // get the answer
    String result = chatResponse.getResult().getOutput().getText();
    return result;
  }

  @Override
  public String generateFinalAnswer(String question, String sqlQuery, String sqlJson) {
    String context = readFromClasspath("sql-to-natural-prompt.txt");
    String userMessage =
        "Question: \n" + question + "sqlQuery: \n" + sqlQuery + "results: " + sqlJson;
    var messages = new ArrayList<Message>();
    messages.add(new SystemMessage(context));
    messages.add(new UserMessage(userMessage));
    Prompt prompt = new Prompt(messages);
    // call the chat client
    ChatResponse chatResponse = chatClient.prompt(prompt).call().chatResponse();
    // get the answer
    String result = chatResponse.getResult().getOutput().getText();
    return result;
  }

  @SneakyThrows
  @Override
  public List<String> convertJsonToArray(String multipleQueriesJson) {
    ObjectMapper mapper = new ObjectMapper();
    return mapper.readValue(multipleQueriesJson, new TypeReference<List<String>>() {});
  }

  @Override
  public ChatBotResponse processTextFiles(ChatBotRequest chatBotRequest) {
    String question = chatBotRequest.question();

    ClassPathResource classPathResource = new ClassPathResource("vacuum-cleaner-products.txt");
    Media media = new Media(MimeTypeUtils.TEXT_PLAIN, classPathResource);

    ClinexaUserMessageModel userMessageModel =
        ClinexaUserMessageModel.builder().question(question).media(media).build();

    String assistantContext = readFromClasspath("systemPrompt.txt");

    SystemMessage systemMessageForContext = new SystemMessage(assistantContext);
    UserMessage userMessage = new UserMessage(userMessageModel.toString());

    var messagesList = new ArrayList<Message>();
    messagesList.add(systemMessageForContext);
    messagesList.add(userMessage);

    Prompt prompt = new Prompt(messagesList);

    String llmResponse =
        chatClient.prompt(prompt).call().chatResponse().getResult().getOutput().getText();

    return new ChatBotResponse(question, llmResponse);
  }

  @Override
  public ChatBotResponse processPdfFiles(ChatBotRequest chatBotRequest) {
    String question = chatBotRequest.question();

    ClassPathResource classPathResource = new ClassPathResource("laptop_user_manual.pdf");
    Media media = new Media(MimeTypeUtils.parseMimeType("application/pdf"), classPathResource);

    ClinexaUserMessageModel userMessageModel =
        ClinexaUserMessageModel.builder().question(question).media(media).build();

    String assistantContext =
        "You are an assistant, who can provide assistance  with product manual information mentioned in the attachment. "
            + "You should answer only based on below data, You don’t know any other stuff.";

    SystemMessage systemMessageForContext = new SystemMessage(assistantContext);
    UserMessage userMessage = new UserMessage(userMessageModel.toString());
    var messagesList = new ArrayList<Message>();
    messagesList.add(systemMessageForContext);
    messagesList.add(userMessage);
    Prompt prompt = new Prompt(messagesList);
    String llmResponse =
        chatClient.prompt(prompt).call().chatResponse().getResult().getOutput().getText();

    return new ChatBotResponse(question, llmResponse);
  }

  @Override
  public ChatBotResponse processImageFiles(ChatBotRequest chatBotRequest) {
    String question = chatBotRequest.question();

    ClassPathResource classPathResource = new ClassPathResource("coupons_info.jpg");
    log.info("is file existed {}", classPathResource.exists());
    log.info("File loaded is {}", classPathResource.getFilename());
    Media media = new Media(MimeTypeUtils.APPLICATION_OCTET_STREAM, classPathResource);

    ClinexaUserMessageModel userMessageModel =
        ClinexaUserMessageModel.builder().question(question).media(media).build();

    String assistantContext =
        "You are an assistant, who can provide assistance  with information mentioned in the image file. "
            + "You should answer only based on the below content."
            + "You don’t know any other stuff. do not include any special characters and new lines";

    // String assistantContext = readFromClasspath("image-data-loading-system-prompt.txt");

    SystemMessage systemMessageForContext = new SystemMessage(assistantContext);
    UserMessage userMessage = new UserMessage(userMessageModel.toString());
    var messagesList = new ArrayList<Message>();
    messagesList.add(systemMessageForContext);
    messagesList.add(userMessage);
    Prompt prompt = new Prompt(messagesList);
    String llmResponse =
        chatClient.prompt(prompt).call().chatResponse().getResult().getOutput().getText();

    return new ChatBotResponse(question, llmResponse);
  }

  @SneakyThrows
    @Override
  public ChatBotResponse processAudioFiles(ChatBotRequest chatBotRequest) {

    String assistantContext =
        "You are an assistant, who can provide assistance with information based on audio. " +
                "You should answer the question only based on audio, " +
                "You dont know any other stuff. " +
                "Be precise of whats has been asked and answer accordingly.";

    SystemMessage systemMessage = new SystemMessage(assistantContext);

    String question = chatBotRequest.question();

    ClassPathResource audioFile = new ClassPathResource("/sarah-customercare.mp3");
    log.info("is audio file existed {}", audioFile.exists());
    log.info("audio File loaded is {}", audioFile.getFilename());
    Media media = new Media(MimeTypeUtils.parseMimeType("audio/transcript"), audioFile);
    //Media media = new Media(MimeTypeUtils.APPLICATION_OCTET_STREAM, audioFile.getURI());

    ClinexaUserMessageModel clinexaUserMessageModel =
        ClinexaUserMessageModel.builder().question(question).media(media).build();
    UserMessage userMessage = new UserMessage(clinexaUserMessageModel.toString());

    var promptMessages = new ArrayList<Message>();
    promptMessages.add(systemMessage);
    promptMessages.add(userMessage);

    Prompt prompt = new Prompt(promptMessages);
    ChatResponse chatResponse = chatClient.prompt(prompt).call().chatResponse();
    log.info("Chat Response {}", chatResponse);
    String llmAnswer =  chatClient.prompt(prompt).call().chatResponse().getResult().getOutput().getText();
    return new ChatBotResponse(question,llmAnswer);
  }

  public String readFromClasspath(String filename) {
    InputStream inputStream = getClass().getClassLoader().getResourceAsStream(filename);
    if (inputStream == null) {
      throw new RuntimeException("No file named " + filename + " found in classpath");
    }
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
      return reader.lines().collect(Collectors.joining(System.lineSeparator()));
    } catch (Exception e) {
      throw new RuntimeException("Failed to read file from classpath", e);
    }
  }
}
