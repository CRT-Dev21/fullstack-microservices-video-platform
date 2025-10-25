package dev.crt.processor.service.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverOptions;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderOptions;

import java.util.List;
import java.util.Map;

@Configuration
public class KafkaReactiveConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    private final ObjectMapper objectMapper = new ObjectMapper();

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
    public KafkaReceiver<String, String> kafkaReceiver() {
        Map<String, Object> consumerProps = Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers,
                ConsumerConfig.GROUP_ID_CONFIG, "processor-service-group",
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer",
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer",
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest"
        );

        ReceiverOptions<String, String> receiverOptions =
                ReceiverOptions.<String, String>create(consumerProps)
                        .subscription(List.of("video.cataloged.event"));

        return KafkaReceiver.create(receiverOptions);
    }
}
