package br.com.yuri.ticketbackend.queue.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "tickets")
public class QueueState {
    public static final Long SINGLETON_ID = 1L;

    @Id
    private Long id;

    @Column(name = "normal_sequence_number", nullable = false)
    private Integer normalSequenceNumber;
    @Column(name = "preferred_sequence_number", nullable = false)
    private Integer preferredSequenceNumber;
    @Column(name = "cycle", nullable = false)
    private Integer cycle;

    public QueueState(Integer normalSequenceNumber, Integer preferredSequenceNumber, Integer cycle) {
        this.normalSequenceNumber = normalSequenceNumber;
        this.preferredSequenceNumber = preferredSequenceNumber;
        this.cycle = cycle;
    }

    protected QueueState(){

    }

    public Long getId() {
        return id;
    }

    public Integer getNormalSequenceNumber() {
        return normalSequenceNumber;
    }

    public Integer getPreferredSequenceNumber() {
        return preferredSequenceNumber;
    }

    public Integer getCycle() {
        return cycle;
    }

    public Integer incrementAndGetNormalSequenceNumber() {
        normalSequenceNumber++;
        return normalSequenceNumber;
    }

    public Integer incrementAndGetPreferredSequenceNumber() {
        preferredSequenceNumber++;
        return preferredSequenceNumber;
    }
}
