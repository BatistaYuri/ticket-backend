package br.com.yuri.ticketbackend.queue.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Id;

public class QueueState {
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
}
