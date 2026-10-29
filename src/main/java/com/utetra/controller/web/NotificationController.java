package com.utetra.controller.web;

import com.utetra.dto.NotificationFeed;
import com.utetra.security.AppUserDetails;
import com.utetra.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public NotificationFeed feed(@AuthenticationPrincipal AppUserDetails user) {
        return notificationService.feed(user.getId());
    }

    @PostMapping("/{id}/read")
    public ResponseEntity<Void> markRead(@AuthenticationPrincipal AppUserDetails user,
                                         @PathVariable Long id) {
        return notificationService.markRead(user.getId(), id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    @PostMapping("/read-all")
    public ResponseEntity<Void> markAllRead(@AuthenticationPrincipal AppUserDetails user) {
        notificationService.markAllRead(user.getId());
        return ResponseEntity.noContent().build();
    }
}
