package dev.crt.notification.service.handler;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.CloseStatus;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class NotificationWebSocketHandler implements WebSocketHandler {
    private final ConcurrentHashMap<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    private final static String VALIDATED_ID_HEADER = "X-Creator-ID";

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        String creatorId = session.getHandshakeInfo()
                .getHeaders()
                .getFirst("X-Creator-ID");

        if (creatorId == null || creatorId.isEmpty()) {
            return session.close(CloseStatus.POLICY_VIOLATION.withReason("Missing validated Creator ID header."));
        }

        sessions.put(creatorId, session);

        return session.receive()
                .doFinally(signalType -> {
                    sessions.remove(creatorId);
                })
                .then();
    }


    public Mono<Void> sendNotification(String creatorId, String jsonMessage) {
        WebSocketSession session = sessions.get(creatorId);

        if (session != null && session.isOpen()) {
            return session.send(Mono.just(session.textMessage(jsonMessage)))
                    .then();
        } else {
            return Mono.empty();
        }
    }
}
