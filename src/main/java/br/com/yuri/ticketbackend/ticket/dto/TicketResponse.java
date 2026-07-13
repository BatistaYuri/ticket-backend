package br.com.yuri.ticketbackend.ticket.dto;

import br.com.yuri.ticketbackend.ticket.entity.Ticket;
import br.com.yuri.ticketbackend.ticket.entity.TicketStatus;
import br.com.yuri.ticketbackend.ticket.entity.TicketType;

import java.time.LocalDateTime;

public record TicketResponse(
        Long id,
        String ticketNumber,
        Integer sequenceNumber,
        TicketType type,
        TicketStatus status,
        Integer queueCycle,
        LocalDateTime createdAt,
        LocalDateTime calledAt
) {
    public static TicketResponse from(Ticket ticket) {
        return new TicketResponse(
                ticket.getId(),
                ticket.getNumber(),
                ticket.getSequenceNumber(),
                ticket.getType(),
                ticket.getStatus(),
                ticket.getQueueCycle(),
                ticket.getCreatedAt(),
                ticket.getCalledAt()
        );
    }
}
