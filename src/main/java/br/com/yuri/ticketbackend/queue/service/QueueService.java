package br.com.yuri.ticketbackend.queue.service;

import br.com.yuri.ticketbackend.queue.dto.CurrentTicketResponse;
import br.com.yuri.ticketbackend.queue.dto.QueueStatusResponse;
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
import java.util.Optional;

@Service
public class QueueService {
    private final TicketRepository ticketRepository;
    private final QueueStateRepository queueStateRepository;
    private static final Logger LOGGER = LoggerFactory.getLogger(QueueService.class);

    public QueueService(TicketRepository ticketRepository, QueueStateRepository queueStateRepository) {
        this.ticketRepository = ticketRepository;
        this.queueStateRepository = queueStateRepository;
    }

    @Transactional
    public Optional<Ticket> callNextTicket() {
        QueueState queueState = queueStateRepository.getLockCurrentQueueState();
        Optional<Ticket> nextTicket = findNextWaitingTicket(TicketType.PREFERRED).or(() -> findNextWaitingTicket(TicketType.NORMAL));
        if (nextTicket.isEmpty()) {
            LOGGER.info(
                    "No waiting ticket found: cycle={}",
                    queueState.getCycle()
            );

            return Optional.empty();
        }

        Ticket ticket = nextTicket.get();

        ticket.markAsCalled(LocalDateTime.now());

        LOGGER.info(
                "Ticket called: id={}, number={}, type={}, cycle={}",
                ticket.getId(),
                ticket.getNumber(),
                ticket.getType(),
                ticket.getQueueCycle()
        );

        return Optional.of(ticket);
    }

    public QueueStatusResponse getStatus() {
        QueueState queueState = queueStateRepository.getCurrentQueueState();
        Integer currentCycle = queueState.getCycle();
        CurrentTicketResponse currentTicket = ticketRepository.findFirstByStatusAndQueueCycleOrderByCalledAtDescIdDesc(
                TicketStatus.CALLED, currentCycle).map(this::toCurrentTicketResponse).orElse(null);

        long waitingPreferred = ticketRepository.countByStatusAndTypeAndQueueCycle(TicketStatus.WAITING, TicketType.PREFERRED, currentCycle);
        long waitingNormal = ticketRepository.countByStatusAndTypeAndQueueCycle(TicketStatus.WAITING, TicketType.NORMAL, currentCycle);
        return new QueueStatusResponse(currentTicket, waitingPreferred, waitingNormal, currentCycle);
    }

    @Transactional
    public void resetQueue() {
        QueueState queueState =  queueStateRepository.getLockCurrentQueueState();
        queueState.reset();
        LOGGER.info("Queue reset: newCycle={}", queueState.getCycle());
    }

    private Optional<Ticket> findNextWaitingTicket(TicketType type){
        return ticketRepository.findFirstByTypeAndStatusOrderByCreatedAtAscIdAsc(type, TicketStatus.WAITING);
    }

    private CurrentTicketResponse toCurrentTicketResponse(Ticket ticket) {
        return new CurrentTicketResponse(
                ticket.getNumber(),
                ticket.getType(),
                ticket.getCalledAt()
        );
    }
}
