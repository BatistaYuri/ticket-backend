package br.com.yuri.ticketbackend.queue.repository;

import br.com.yuri.ticketbackend.queue.entity.QueueState;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface QueueStateRepository extends JpaRepository<QueueState, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT queueState
        FROM QueueState queueState
        WHERE queueState.id = :id
        """)
    Optional<QueueState> findByIdForUpdate(@Param("id") Long id);

    default QueueState getLockCurrentQueueState() {
        return findByIdForUpdate(QueueState.SINGLETON_ID)
                .orElseThrow(() -> new IllegalStateException(
                        "Queue state was not initialized"
                ));
    }

    default QueueState getCurrentQueueState() {
        return findById(QueueState.SINGLETON_ID)
                .orElseThrow(() -> new IllegalStateException(
                        "Queue state was not initialized")
                );
    }
}
