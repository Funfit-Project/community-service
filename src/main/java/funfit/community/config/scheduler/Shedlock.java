package funfit.community.config.scheduler;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "shedlock")
public class Shedlock {

    @Id
    @Column(length = 64, nullable = false)
    private String name;

    @Column(name = "lock_until", nullable = false)
    private LocalDateTime lockUntil;

    @Column(name = "locked_at", nullable = false, columnDefinition = "TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP(3)")
    private LocalDateTime lockedAt;

    @Column(name = "locked_by", nullable = false, length = 255)
    private String lockedBy;
}
