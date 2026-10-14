package dev.vality.fraudbusters.management;

import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.server.servlet.context.ServletComponentScan;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;

@ServletComponentScan
@SpringBootApplication
public class FraudbustersManagementApplication extends SpringApplication {

    @Autowired
    private KafkaListenerEndpointRegistry registry;

    public static void main(String[] args) {
        SpringApplication.run(FraudbustersManagementApplication.class, args);
    }

    @PreDestroy
    public void preDestroy() {
        registry.stop();
    }
}