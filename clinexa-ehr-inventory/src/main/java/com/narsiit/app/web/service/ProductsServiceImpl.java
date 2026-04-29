package com.narsiit.app.web.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.narsiit.app.models.Product;
import com.narsiit.app.repos.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

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
