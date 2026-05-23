package ru.sber.dealservice.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;
import org.springframework.kafka.support.serializer.JacksonJsonSerializer;
import ru.sber.messages.RiskRequestMessage;
import ru.sber.messages.RiskResponseMessage;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

    @Value("${app.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${app.kafka.reply-topic:risk-responses}")
    private String replyTopic;

    @Bean
    public ProducerFactory<String, RiskRequestMessage> riskRequestProducerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JacksonJsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    public ConsumerFactory<String, RiskResponseMessage> riskResponseConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "deal-risk-reply-group");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JacksonJsonDeserializer.class);
        props.put(JacksonJsonDeserializer.VALUE_DEFAULT_TYPE, RiskResponseMessage.class.getName());
        props.put(JacksonJsonDeserializer.TRUSTED_PACKAGES, "ru.sber.messages");
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConcurrentMessageListenerContainer<String, RiskResponseMessage> riskReplyContainer() {
        ContainerProperties containerProperties = new ContainerProperties(replyTopic);
        containerProperties.setGroupId("deal-risk-reply-group");
        ConcurrentMessageListenerContainer<String, RiskResponseMessage> container =
                new ConcurrentMessageListenerContainer<>(riskResponseConsumerFactory(), containerProperties);
        container.setAutoStartup(false);
        return container;
    }

    @Bean
    public ReplyingKafkaTemplate<String, RiskRequestMessage, RiskResponseMessage> replyingKafkaTemplate() {
        ReplyingKafkaTemplate<String, RiskRequestMessage, RiskResponseMessage> template =
                new ReplyingKafkaTemplate<>(riskRequestProducerFactory(), riskReplyContainer());
        template.setDefaultReplyTimeout(Duration.ofSeconds(10));
        return template;
    }
}