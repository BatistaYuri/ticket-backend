package br.com.yuri.ticketbackend.ticket.repository;

import br.com.yuri.ticketbackend.ticket.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
}
