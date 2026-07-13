package br.com.yuri.ticketbackend.queue.dto;

import br.com.yuri.ticketbackend.ticket.entity.TicketType;

import java.time.LocalDateTime;

public record CurrentTicketResponse(String number, TicketType type, LocalDateTime calledAt) {
}
