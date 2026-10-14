package dev.vality.fraudbusters.management.config;

import dev.vality.testcontainers.annotations.KafkaSpringBootTest;
import dev.vality.testcontainers.annotations.kafka.KafkaTestcontainer;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.flyway.autoconfigure.FlywayAutoConfiguration;
import org.springframework.boot.jooq.autoconfigure.JooqAutoConfiguration;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@KafkaTestcontainer(topicsKeys = {
        "kafka.topic.fraudbusters.unknown-initiating-entity",
        "kafka.topic.wblist.command",
        "kafka.topic.wblist.event.sink",
        "kafka.topic.fraudbusters.payment.template",
        "kafka.topic.fraudbusters.payment.reference",
        "kafka.topic.fraudbusters.payment.group.list",
        "kafka.topic.fraudbusters.payment.group.reference",
        "kafka.topic.wblist.candidate"
})
@KafkaSpringBootTest
@EnableAutoConfiguration(exclude = {FlywayAutoConfiguration.class, JooqAutoConfiguration.class})
public @interface KafkaITest {
}
