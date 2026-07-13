package br.com.yuri.ticketbackend.queue.service;

import br.com.yuri.ticketbackend.queue.entity.QueueState;
import br.com.yuri.ticketbackend.queue.repository.QueueStateRepository;
import br.com.yuri.ticketbackend.ticket.entity.Ticket;
import br.com.yuri.ticketbackend.ticket.entity.TicketStatus;
import br.com.yuri.ticketbackend.ticket.entity.TicketType;
import br.com.yuri.ticketbackend.ticket.repository.TicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QueueServiceTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private QueueStateRepository queueStateRepository;

    @Mock
    private QueueState queueState;

    private QueueService queueService;

    @BeforeEach
    void setUp() {
        queueService = new QueueService(
                ticketRepository,
                queueStateRepository
        );

        when(queueStateRepository.getLockCurrentQueueState()).thenReturn(queueState);
    }

    @Test
    void shouldCallPreferredTicketBeforeNormalTicket() {
        Ticket preferredTicket = createWaitingTicket(
                1,
                TicketType.PREFERRED
        );

        when(ticketRepository
                .findFirstByTypeAndStatusOrderByCreatedAtAscIdAsc(
                        TicketType.PREFERRED,
                        TicketStatus.WAITING
                ))
                .thenReturn(Optional.of(preferredTicket));

        Optional<Ticket> result = queueService.callNextTicket();

        assertAll(
                () -> assertTrue(result.isPresent()),
                () -> assertSame(
                        preferredTicket,
                        result.orElseThrow()
                ),
                () -> assertEquals(
                        TicketStatus.CALLED,
                        preferredTicket.getStatus()
                ),
                () -> assertNotNull(
                        preferredTicket.getCalledAt()
                )
        );

        verify(ticketRepository, never())
                .findFirstByTypeAndStatusOrderByCreatedAtAscIdAsc(
                        TicketType.NORMAL,
                        TicketStatus.WAITING
                );

        InOrder executionOrder = inOrder(
                queueStateRepository,
                ticketRepository
        );

        executionOrder.verify(queueStateRepository).getLockCurrentQueueState();

        executionOrder.verify(ticketRepository)
                .findFirstByTypeAndStatusOrderByCreatedAtAscIdAsc(
                        TicketType.PREFERRED,
                        TicketStatus.WAITING
                );
    }

    @Test
    void shouldCallNormalTicketWhenNoPreferredTicketIsWaiting() {
        Ticket normalTicket = createWaitingTicket(
                1,
                TicketType.NORMAL
        );

        when(ticketRepository
                .findFirstByTypeAndStatusOrderByCreatedAtAscIdAsc(
                        TicketType.PREFERRED,
                        TicketStatus.WAITING
                ))
                .thenReturn(Optional.empty());

        when(ticketRepository
                .findFirstByTypeAndStatusOrderByCreatedAtAscIdAsc(
                        TicketType.NORMAL,
                        TicketStatus.WAITING
                ))
                .thenReturn(Optional.of(normalTicket));

        Optional<Ticket> result = queueService.callNextTicket();

        assertAll(
                () -> assertTrue(result.isPresent()),
                () -> assertSame(
                        normalTicket,
                        result.orElseThrow()
                ),
                () -> assertEquals(
                        TicketStatus.CALLED,
                        normalTicket.getStatus()
                ),
                () -> assertNotNull(
                        normalTicket.getCalledAt()
                )
        );

        InOrder executionOrder = inOrder(
                queueStateRepository,
                ticketRepository
        );

        executionOrder.verify(queueStateRepository).getLockCurrentQueueState();

        executionOrder.verify(ticketRepository)
                .findFirstByTypeAndStatusOrderByCreatedAtAscIdAsc(
                        TicketType.PREFERRED,
                        TicketStatus.WAITING
                );

        executionOrder.verify(ticketRepository)
                .findFirstByTypeAndStatusOrderByCreatedAtAscIdAsc(
                        TicketType.NORMAL,
                        TicketStatus.WAITING
                );
    }

    @Test
    void shouldReturnEmptyWhenNoTicketIsWaiting() {
        when(ticketRepository
                .findFirstByTypeAndStatusOrderByCreatedAtAscIdAsc(
                        TicketType.PREFERRED,
                        TicketStatus.WAITING
                ))
                .thenReturn(Optional.empty());

        when(ticketRepository
                .findFirstByTypeAndStatusOrderByCreatedAtAscIdAsc(
                        TicketType.NORMAL,
                        TicketStatus.WAITING
                ))
                .thenReturn(Optional.empty());

        Optional<Ticket> result = queueService.callNextTicket();

        assertFalse(result.isPresent());

        verify(queueStateRepository).getLockCurrentQueueState();

        verify(ticketRepository)
                .findFirstByTypeAndStatusOrderByCreatedAtAscIdAsc(
                        TicketType.PREFERRED,
                        TicketStatus.WAITING
                );

        verify(ticketRepository)
                .findFirstByTypeAndStatusOrderByCreatedAtAscIdAsc(
                        TicketType.NORMAL,
                        TicketStatus.WAITING
                );
    }

    private Ticket createWaitingTicket(
            Integer sequenceNumber,
            TicketType type
    ) {
        return new Ticket(
                sequenceNumber,
                type,
                TicketStatus.WAITING,
                1,
                LocalDateTime.of(
                        2026,
                        7,
                        13,
                        10,
                        30
                )
        );
    }
}