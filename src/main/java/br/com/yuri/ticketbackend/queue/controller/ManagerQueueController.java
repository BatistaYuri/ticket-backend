package br.com.yuri.ticketbackend.queue.controller;

import br.com.yuri.ticketbackend.queue.service.QueueService;
import br.com.yuri.ticketbackend.ticket.dto.TicketResponse;
import br.com.yuri.ticketbackend.ticket.entity.Ticket;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/api/manager/queue")
public class ManagerQueueController {
    private final QueueService queueService;

    public ManagerQueueController(QueueService queueService) {
        this.queueService = queueService;
    }

    @PostMapping("/next")
    public ResponseEntity<TicketResponse> callNextTicket() {
        Optional<Ticket> nextTicket = queueService.callNextTicket();
        if(nextTicket.isEmpty()){
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(TicketResponse.from(nextTicket.get()));
    }

    @PostMapping("/reset")
    public ResponseEntity<Void> resetQueue() {
        queueService.resetQueue();
        return ResponseEntity.noContent().build();
    }
}
