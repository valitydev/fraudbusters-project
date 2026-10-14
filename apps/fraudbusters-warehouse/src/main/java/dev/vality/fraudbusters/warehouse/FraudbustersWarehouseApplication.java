package dev.vality.fraudbusters.warehouse;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.web.server.servlet.context.ServletComponentScan;

@ServletComponentScan
@SpringBootApplication
@ConfigurationPropertiesScan
public class FraudbustersWarehouseApplication extends SpringApplication {

    public static void main(String[] args) {
        SpringApplication.run(FraudbustersWarehouseApplication.class, args);
    }

}
