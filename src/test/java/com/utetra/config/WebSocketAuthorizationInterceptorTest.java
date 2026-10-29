package com.utetra.config;

import org.junit.jupiter.api.Test;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class WebSocketAuthorizationInterceptorTest {

    private final WebSocketAuthorizationInterceptor interceptor = new WebSocketAuthorizationInterceptor();

    @Test
    void rejectsAnonymousConnect() {
        assertThrows(AccessDeniedException.class,
                () -> interceptor.preSend(message(StompCommand.CONNECT, null, false), mock(MessageChannel.class)));
    }

    @Test
    void rejectsSubscriptionToAnotherDestination() {
        assertThrows(AccessDeniedException.class,
                () -> interceptor.preSend(message(StompCommand.SUBSCRIBE, "/queue/anything", true), mock(MessageChannel.class)));
    }

    @Test
    void allowsAuthenticatedUsersPrivateNotificationQueue() {
        assertDoesNotThrow(() -> interceptor.preSend(message(StompCommand.SUBSCRIBE,
                WebSocketAuthorizationInterceptor.USER_NOTIFICATIONS, true), mock(MessageChannel.class)));
    }

    private static Message<byte[]> message(StompCommand command, String destination, boolean authenticated) {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(command);
        if (destination != null) accessor.setDestination(destination);
        if (authenticated) {
            accessor.setUser(new UsernamePasswordAuthenticationToken("user@utetra.vn", null));
        }
        accessor.setLeaveMutable(true);
        return MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
    }
}
