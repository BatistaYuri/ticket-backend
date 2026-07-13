package br.com.yuri.ticketbackend.ticket.dto;

import br.com.yuri.ticketbackend.ticket.entity.TicketType;
import jakarta.validation.constraints.NotNull;

public record CreateTicketRequest(@NotNull(message = "Ticket type is required") TicketType type) {
}
