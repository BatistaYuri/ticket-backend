package br.com.yuri.ticketbackend.queue.service;

import br.com.yuri.ticketbackend.queue.dto.CurrentTicketResponse;
import br.com.yuri.ticketbackend.queue.dto.QueueStatusResponse;
import br.com.yuri.ticketbackend.queue.entity.QueueState;
import br.com.yuri.ticketbackend.queue.repository.QueueStateRepository;
import br.com.yuri.ticketbackend.ticket.entity.Ticket;
import br.com.yuri.ticketbackend.ticket.entity.TicketStatus;
import br.com.yuri.ticketbackend.ticket.entity.TicketType;
import br.com.yuri.ticketbackend.ticket.repository.TicketRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class QueueService {
    private final TicketRepository ticketRepository;
    private final QueueStateRepository queueStateRepository;

    public QueueService(TicketRepository ticketRepository, QueueStateRepository queueStateRepository) {
        this.ticketRepository = ticketRepository;
        this.queueStateRepository = queueStateRepository;
    }

    public Optional<Ticket> callNextTicket() {
        queueStateRepository.getLockCurrentQueueState();
        Optional<Ticket> nextTicket = findNextWaitingTicket(TicketType.PREFERRED).or(() -> findNextWaitingTicket(TicketType.NORMAL));
        nextTicket.ifPresent(ticket -> ticket.markAsCalled(LocalDateTime.now()));
        return nextTicket;
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

    public void resetQueue() {
        QueueState queueState =  queueStateRepository.getLockCurrentQueueState();
        queueState.reset();
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
