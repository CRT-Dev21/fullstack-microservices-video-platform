package dev.crt.notification.service.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverOptions;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderOptions;

import java.util.*;

@Component
public class KafkaReactiveConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    // Reactive producer configuration
    @Bean
    public KafkaSender<String, String> kafkaSender(){
        Map<String, Object> props = Map.of(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers,
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer",
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer"
        );

        SenderOptions<String, String> senderOptions = SenderOptions.create(props);

        return KafkaSender.create(senderOptions);
    }

    // Reactive consumer configuration
    @Bean
    public KafkaReceiver<String, String> receiverOptions(){
        Map<String, Object> props = Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers,
                ConsumerConfig.GROUP_ID_CONFIG, "notification-service-group",
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer",
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer",
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest"
        );

        ReceiverOptions<String, String> receiverOptions =
                ReceiverOptions.<String, String>create(props)
                        .subscription(List.of("video.upload.failed.event", "video.notification.event"));

        return KafkaReceiver.create(receiverOptions);
    }

}
