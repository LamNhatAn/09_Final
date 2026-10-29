package com.utetra.dto;

import java.util.List;

public record NotificationFeed(long unreadCount, List<NotificationItem> items) {
}
