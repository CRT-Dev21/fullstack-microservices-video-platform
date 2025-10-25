package dev.crt.processor.service.kafka.consumer;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.kafka.receiver.KafkaReceiver;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class KafkaConsumerRunner {
    private final KafkaReceiver<String, String> receiver;
    private final Map<String, EventHandler> handlers;

    public KafkaConsumerRunner(KafkaReceiver<String, String> receiver,
                               List<EventHandler> handlerList){
        this.receiver = receiver;
        this.handlers = handlerList.stream().collect(Collectors.toMap(EventHandler::getTopic, h -> h));
    }

    @PostConstruct
    public void init(){
        subscribe();
    }

    public void subscribe(){
        receiver.receive()
                .flatMap(record -> {
                    String topic = record.topic();
                    String json = record.value();

                    System.out.println(json);

                    EventHandler handler = handlers.get(topic);
                    if(handler == null){
                        System.err.println("No handler for topic: "+topic);
                        record.receiverOffset().acknowledge();
                        return Mono.empty();
                    }

                    return handler.handle(json)
                            .doFinally(s -> record.receiverOffset().acknowledge());
                })
                .subscribe();
    }
}
