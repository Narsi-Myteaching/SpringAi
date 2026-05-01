package com.narsiit.app.web.api;

import com.narsiit.app.beans.ChatBotRequest;
import com.narsiit.app.beans.ChatBotResponse;
import com.narsiit.app.models.Product;
import com.narsiit.app.repos.ProductRepository;
import com.narsiit.app.web.service.ProductsService;
import lombok.RequiredArgsConstructor;

import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
public class ProductsController {
    private final ProductsService productsService;

    @PostMapping
    public ResponseEntity<Product> createProduct(@RequestBody Product product){
        return ResponseEntity.ok(productsService.createProduct(product));
    }
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable("id") String productId){
        return  ResponseEntity.ok(productsService.getProductById(productId));
    }
    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts(){
        return  ResponseEntity.ok(productsService.getAllPorducts());
    }
    @PostMapping(value = {"/query"})
    public ChatBotResponse askQuestion(@RequestBody ChatBotRequest chatBotRequest) {
        String assistantAnswer =  productsService.generateSqlQuery(chatBotRequest.question());
        return new ChatBotResponse(chatBotRequest.question(), assistantAnswer);
    }

    @PostMapping(value = {"/query-products"})
    public ChatBotResponse queryProducts(@RequestBody ChatBotRequest chatBotRequest) {
        String question = chatBotRequest.question();

        //LLM Generated Queries
        String sqlQuery =  productsService.generateSqlQuery(chatBotRequest.question());


        String sqlJson = productsService.getProductsFromDb(sqlQuery);
        //if multiple queries returned by the LLM (few LLMS does) then convert them to json
        List<String> multipleQuesries =  productsService.convertJsonToArray(sqlJson);
        String finalAnswer =  productsService.generateFinalAnswer(question,sqlQuery,multipleQuesries);
        return  new ChatBotResponse(question, finalAnswer);
    }
}
