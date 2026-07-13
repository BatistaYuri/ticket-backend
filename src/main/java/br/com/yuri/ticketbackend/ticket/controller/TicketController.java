package br.com.yuri.ticketbackend.ticket.controller;

import br.com.yuri.ticketbackend.ticket.dto.CreateTicketRequest;
import br.com.yuri.ticketbackend.ticket.dto.TicketResponse;
import br.com.yuri.ticketbackend.ticket.entity.Ticket;
import br.com.yuri.ticketbackend.ticket.service.TicketService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {
    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @PostMapping
    public ResponseEntity<TicketResponse> create(@Valid @RequestBody CreateTicketRequest request){
        Ticket ticket =  ticketService.create(request.type());
        return ResponseEntity.status(HttpStatus.CREATED).body(TicketResponse.from(ticket));
    }
}
