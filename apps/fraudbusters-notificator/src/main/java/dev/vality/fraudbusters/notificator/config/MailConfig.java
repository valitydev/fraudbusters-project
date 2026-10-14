package dev.vality.fraudbusters.notificator.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
public class MailConfig {

    @Bean
    public Properties mailProperties(@Value("${mail.protocol}") String protocol,
                                     @Value("${mail.smtp.auth}") boolean smtpsAuth,
                                     @Value("${mail.smtp.starttls.enable}") boolean starttls,
                                     @Value("${mail.smtp.ssl.enable}") boolean sslEnable,
                                     @Value("${mail.smtp.timeout}") int timeout,
                                     @Value("${mail.host}") String host,
                                     @Value("${mail.port}") int port,
                                     @Value("${mail.username}") String username,
                                     @Value("${mail.factory}") String factory) {
        Properties properties = new Properties();
        properties.put("mail.smtp.auth", smtpsAuth);
        properties.put("mail.smtp.starttls.enable", starttls);
        properties.put("mail.smtp.connectiontimeout", timeout);
        properties.put("mail.smtp.timeout", timeout);
        properties.put("mail.smtp.ssl.enable", sslEnable);
        properties.put("mail.transport.protocol", protocol);
        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", port);
        properties.put("mail.username", username);
        properties.put("mail.smtp.socketFactory.port", port);
        properties.put("mail.smtp.socketFactory.class", factory);
        return properties;
    }

    @Bean
    public JavaMailSender javaMailSender(Properties mailProperties,
                                         @Value("${mail.host}") String host,
                                         @Value("${mail.port}") int port,
                                         @Value("${mail.username}") String username,
                                         @Value("${mail.password}") String password) {
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(host);
        sender.setPort(port);
        sender.setUsername(username);
        sender.setPassword(password);
        sender.setJavaMailProperties(mailProperties);
        return sender;
    }
}
