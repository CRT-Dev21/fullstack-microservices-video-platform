package dev.crt.processor.service.kafka.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderRecord;

@Service
public class KafkaPublisher {

    private final KafkaSender<String, String> sender;

    private final ObjectMapper objectMapper;

    public KafkaPublisher(KafkaSender<String, String> sender, ObjectMapper objectMapper){
        this.sender = sender;
        this.objectMapper = objectMapper;
    }

    public <T> Mono<Void> sendEvent(String topic, String key, T event){
        try{
            String json = objectMapper.writeValueAsString(event);
            return sender.send(Mono.just(
                    SenderRecord.create(new ProducerRecord<>(topic, key, json), null)
            )).then();
        } catch(JsonProcessingException e){
            return Mono.error(e);
        }
    }
}
