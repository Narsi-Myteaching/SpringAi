package com.narsiit.app.web.service;

import com.narsiit.app.beans.ChatBotRequest;
import com.narsiit.app.beans.ChatBotResponse;
import com.narsiit.app.models.Product;
import lombok.SneakyThrows;

import java.util.List;

public interface ProductsService {

    @SneakyThrows
    List<String> getProductsFromMultipleDbQuesries(List<String> sqlQuery);

    Product createProduct(Product product);
    List<Product> getAllPorducts();
    Product getProductById(String productId);

    String generateSqlQuery(String userQuestion);

    String getProductsFromDb(String sqlQuery);

    String generateFinalAnswer(String question, String sqlQuery, List<String> sqlJson);
    String generateFinalAnswer(String question, String sqlQuery, String singleQuery);

    @SneakyThrows
    List<String> convertJsonToArray(String multipleQueriesJson);

    ChatBotResponse processTextFiles(ChatBotRequest chatBotRequest);

    ChatBotResponse processPdfFiles(ChatBotRequest chatBotRequest);

    ChatBotResponse processImageFiles(ChatBotRequest chatBotRequest);

    ChatBotResponse processAudioFiles(ChatBotRequest chatBotRequest);
}
