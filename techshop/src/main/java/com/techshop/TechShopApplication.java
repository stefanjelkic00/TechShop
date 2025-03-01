package com.techshop;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

@SpringBootApplication
@EnableElasticsearchRepositories(basePackages = "com.techshop.elasticsearch.repository")
public class TechShopApplication {
    public static void main(String[] args) {
        SpringApplication.run(TechShopApplication.class, args);
    }
}
