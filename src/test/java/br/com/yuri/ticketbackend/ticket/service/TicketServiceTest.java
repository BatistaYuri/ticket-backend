package br.com.yuri.ticketbackend.ticket.service;

import br.com.yuri.ticketbackend.queue.entity.QueueState;
import br.com.yuri.ticketbackend.queue.repository.QueueStateRepository;
import br.com.yuri.ticketbackend.ticket.entity.Ticket;
import br.com.yuri.ticketbackend.ticket.entity.TicketStatus;
import br.com.yuri.ticketbackend.ticket.entity.TicketType;
import br.com.yuri.ticketbackend.ticket.repository.TicketRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private QueueStateRepository queueStateRepository;

    @InjectMocks
    private TicketService ticketService;

    @Test
    void shouldCreateNormalTicket() {
        QueueState queueState = new QueueState(
                QueueState.SINGLETON_ID,
                4,
                7,
                2
        );

        when(queueStateRepository.getLockCurrentQueueState())
                .thenReturn(queueState);

        when(ticketRepository.save(any(Ticket.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Ticket result = ticketService.create(TicketType.NORMAL);

        ArgumentCaptor<Ticket> ticketCaptor =
                ArgumentCaptor.forClass(Ticket.class);

        verify(queueStateRepository)
                .getLockCurrentQueueState();

        verify(ticketRepository)
                .save(ticketCaptor.capture());

        Ticket savedTicket = ticketCaptor.getValue();

        assertSame(savedTicket, result);

        assertAll(
                () -> assertEquals(
                        5,
                        savedTicket.getSequenceNumber()
                ),
                () -> assertEquals(
                        "N0005",
                        savedTicket.getNumber()
                ),
                () -> assertEquals(
                        TicketType.NORMAL,
                        savedTicket.getType()
                ),
                () -> assertEquals(
                        TicketStatus.WAITING,
                        savedTicket.getStatus()
                ),
                () -> assertEquals(
                        2,
                        savedTicket.getQueueCycle()
                ),
                () -> assertNotNull(
                        savedTicket.getCreatedAt()
                ),
                () -> assertNull(
                        savedTicket.getCalledAt()
                )
        );
    }

    @Test
    void shouldCreatePreferredTicket() {
        QueueState queueState = new QueueState(
                QueueState.SINGLETON_ID,
                4,
                7,
                2
        );

        when(queueStateRepository.getLockCurrentQueueState())
                .thenReturn(queueState);

        when(ticketRepository.save(any(Ticket.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Ticket result = ticketService.create(TicketType.PREFERRED);

        ArgumentCaptor<Ticket> ticketCaptor =
                ArgumentCaptor.forClass(Ticket.class);

        verify(queueStateRepository)
                .getLockCurrentQueueState();

        verify(ticketRepository)
                .save(ticketCaptor.capture());

        Ticket savedTicket = ticketCaptor.getValue();

        assertSame(savedTicket, result);

        assertAll(
                () -> assertEquals(
                        8,
                        savedTicket.getSequenceNumber()
                ),
                () -> assertEquals(
                        "P0008",
                        savedTicket.getNumber()
                ),
                () -> assertEquals(
                        TicketType.PREFERRED,
                        savedTicket.getType()
                ),
                () -> assertEquals(
                        TicketStatus.WAITING,
                        savedTicket.getStatus()
                ),
                () -> assertEquals(
                        2,
                        savedTicket.getQueueCycle()
                ),
                () -> assertNotNull(
                        savedTicket.getCreatedAt()
                ),
                () -> assertNull(
                        savedTicket.getCalledAt()
                )
        );
    }

    @Test
    void shouldThrowExceptionWhenQueueStateDoesNotExist() {
        when(queueStateRepository.getLockCurrentQueueState())
                .thenThrow(new IllegalStateException(
                        "Queue state was not initialized"
                ));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> ticketService.create(TicketType.NORMAL)
        );

        assertEquals(
                "Queue state was not initialized",
                exception.getMessage()
        );

        verify(queueStateRepository)
                .getLockCurrentQueueState();

        verify(ticketRepository, never())
                .save(any(Ticket.class));
    }

    @Test
    void shouldRejectNullTicketType() {
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> ticketService.create(null)
        );

        assertEquals(
                "Ticket type must not be null",
                exception.getMessage()
        );

        verifyNoInteractions(
                queueStateRepository,
                ticketRepository
        );
    }
}