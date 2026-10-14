package dev.vality.fraudbusters.notificator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.server.servlet.context.ServletComponentScan;

@ServletComponentScan
@SpringBootApplication
public class FraudbustersNotificatorApplication extends SpringApplication {

    public static void main(String[] args) {
        SpringApplication.run(FraudbustersNotificatorApplication.class, args);
    }

}
