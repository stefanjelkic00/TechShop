package com.techshop.elasticsearch.controller;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.techshop.elasticsearch.service.ProductSyncService;

@RestController
@RequestMapping("/api/sync")
public class ProductSyncController {
    private final ProductSyncService productSyncService;

    public ProductSyncController(ProductSyncService productSyncService) {
        this.productSyncService = productSyncService;
    }

    @PostMapping("/mysql-to-elasticsearch")
    public String syncMySQLToElasticsearch() {
        Logger logger = LoggerFactory.getLogger(ProductSyncController.class);
        try {
            logger.info("Starting synchronization from MySQL to Elasticsearch...");
            productSyncService.syncProducts();
            logger.info("Synchronization completed successfully.");
            return "Podaci su sinhronizovani iz MySQL-a u Elasticsearch!";
        } catch (Exception e) {
            logger.error("Error during synchronization: ", e);
            return "Greška prilikom sinhronizacije: " + e.getMessage();
        }
    }
}