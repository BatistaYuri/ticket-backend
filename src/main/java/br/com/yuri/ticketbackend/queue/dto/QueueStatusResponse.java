package br.com.yuri.ticketbackend.queue.dto;

public record QueueStatusResponse(CurrentTicketResponse currentTicket, long waitingPreferred, long waitingNormal, Integer cycle) {
}
