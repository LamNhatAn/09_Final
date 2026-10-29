package com.utetra.config;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.access.AccessDeniedException;

/** Chỉ user đã đăng nhập được kết nối và chỉ được subscribe hàng đợi riêng của chính mình. */
public class WebSocketAuthorizationInterceptor implements ChannelInterceptor {

    public static final String USER_NOTIFICATIONS = "/user/queue/notifications";

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        StompCommand command = accessor.getCommand();
        if (command == null) {
            return message;
        }
        if ((command == StompCommand.CONNECT || command == StompCommand.SUBSCRIBE)
                && accessor.getUser() == null) {
            throw new AccessDeniedException("WebSocket requires authentication");
        }
        if (command == StompCommand.SUBSCRIBE
                && !USER_NOTIFICATIONS.equals(accessor.getDestination())) {
            throw new AccessDeniedException("Subscription is not allowed");
        }
        return message;
    }
}
