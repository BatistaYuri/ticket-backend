package br.com.yuri.ticketbackend.ticket.service;

import br.com.yuri.ticketbackend.queue.entity.QueueState;
import br.com.yuri.ticketbackend.queue.repository.QueueStateRepository;
import br.com.yuri.ticketbackend.ticket.entity.Ticket;
import br.com.yuri.ticketbackend.ticket.entity.TicketStatus;
import br.com.yuri.ticketbackend.ticket.entity.TicketType;
import br.com.yuri.ticketbackend.ticket.repository.TicketRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Objects;

@Service
public class TicketService {
    private final TicketRepository ticketRepository;
    private final QueueStateRepository queueStateRepository;
    private static final Logger LOGGER = LoggerFactory.getLogger(TicketService.class);

    public TicketService(TicketRepository ticketRepository, QueueStateRepository queueStateRepository) {
        this.ticketRepository = ticketRepository;
        this.queueStateRepository = queueStateRepository;
    }

    @Transactional
    public Ticket create(TicketType type){
        Objects.requireNonNull(type, "Ticket type must not be null");
        QueueState queueState = queueStateRepository.getLockCurrentQueueState();
        Integer sequenceNumber = this.getNextSequence(queueState, type);
        Ticket ticket = ticketRepository.save(new Ticket(sequenceNumber, type, TicketStatus.WAITING, queueState.getCycle(), LocalDateTime.now()));
        LOGGER.info(
                "Ticket created: id={}, number={}, type={}, cycle={}",
                ticket.getId(),
                ticket.getNumber(),
                ticket.getType(),
                ticket.getQueueCycle()
        );
        return ticket;
    }

    private Integer getNextSequence(QueueState queueState, TicketType type){
        return switch (type) {
            case NORMAL -> queueState.incrementAndGetNormalSequenceNumber();
            case PREFERRED -> queueState.incrementAndGetPreferredSequenceNumber();
        };
    }
}
