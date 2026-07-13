package br.com.yuri.ticketbackend.queue.controller;

import br.com.yuri.ticketbackend.queue.dto.QueueStatusResponse;
import br.com.yuri.ticketbackend.queue.service.QueueService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/queue")
public class QueueController {
    private final QueueService queueService;

    public QueueController(QueueService queueService) {
        this.queueService = queueService;
    }

    @GetMapping("/status")
    public ResponseEntity<QueueStatusResponse> getStatus(){
        return ResponseEntity.ok(queueService.getStatus());
    }
}
