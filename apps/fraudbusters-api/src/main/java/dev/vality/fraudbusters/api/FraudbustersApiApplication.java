package dev.vality.fraudbusters.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.server.servlet.context.ServletComponentScan;

@ServletComponentScan
@SpringBootApplication
public class FraudbustersApiApplication extends SpringApplication {

    public static void main(String[] args) {
        SpringApplication.run(FraudbustersApiApplication.class, args);
    }

}
