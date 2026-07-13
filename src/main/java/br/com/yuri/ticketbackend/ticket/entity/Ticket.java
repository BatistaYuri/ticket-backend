package br.com.yuri.ticketbackend.ticket.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "tickets")
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sequence_number", nullable = false)
    private Integer sequenceNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 10)
    private TicketType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 10)
    private TicketStatus status;

    @Column(name = "queue_cycle", nullable = false)
    private Integer queueCycle;

    @Column(name = "createdAt", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "calledAt")
    private LocalDateTime calledAt;

    public Ticket(Integer sequenceNumber, TicketType type, TicketStatus status, Integer queueCycle, LocalDateTime createdAt) {
        this.sequenceNumber = sequenceNumber;
        this.type = type;
        this.status = status;
        this.queueCycle = queueCycle;
        this.createdAt = createdAt;
    }

    protected Ticket() {

    }

    public Long getId() {
        return id;
    }

    public Integer getSequenceNumber() {
        return sequenceNumber;
    }

    public TicketStatus getStatus() {
        return status;
    }

    public TicketType getType() {
        return type;
    }

    public Integer getQueueCycle() {
        return queueCycle;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getCalledAt() {
        return calledAt;
    }

    @Transient
    public String getNumber(){
        return switch (type) {
            case NORMAL -> "N%04d".formatted(sequenceNumber);
            case PREFERRED -> "P%04d".formatted(sequenceNumber);
        };
    }

}
