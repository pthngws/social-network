package com.phithang.mysocialnetwork.controller;

import com.phithang.mysocialnetwork.service.Impl.OnlineStatusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/online-status")
public class OnlineStatusController {

    @Autowired
    private OnlineStatusService onlineStatusService;

    @GetMapping("/{userId}")
    public ResponseEntity<Boolean> isUserOnline(@PathVariable Long userId) {
        boolean isOnline = onlineStatusService.isUserOnline(userId);
        return ResponseEntity.ok(isOnline);
    }

    @PostMapping("/ping/{userId}")
    public ResponseEntity<Void> ping(@PathVariable Long userId) {
        onlineStatusService.ping(userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/last-seen/{userId}")
    public ResponseEntity<Long> getLastSeen(@PathVariable Long userId) {
        Long lastSeen = onlineStatusService.getLastSeen(userId);
        return ResponseEntity.ok(lastSeen);
    }

    @GetMapping("/minutes-ago/{userId}")
    public ResponseEntity<Long> getLastSeenMinutesAgo(@PathVariable Long userId) {
        Long minutesAgo = onlineStatusService.getLastSeenMinutesAgo(userId);
        return ResponseEntity.ok(minutesAgo);
    }
}