package br.com.yuri.ticketbackend.ticket.repository;

import br.com.yuri.ticketbackend.ticket.entity.Ticket;
import br.com.yuri.ticketbackend.ticket.entity.TicketStatus;
import br.com.yuri.ticketbackend.ticket.entity.TicketType;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.Optional;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Ticket> findFirstByTypeAndStatusOrderByCreatedAtAscIdAsc(
            TicketType type,
            TicketStatus status
    );
    Optional<Ticket> findFirstByStatusAndQueueCycleOrderByCalledAtDescIdDesc(
            TicketStatus status,
            Integer cycle
    );

    long countByStatusAndTypeAndQueueCycle(
            TicketStatus status,
            TicketType type,
            Integer queueCycle
    );
}
