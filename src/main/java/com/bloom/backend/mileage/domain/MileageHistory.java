package com.bloom.backend.mileage.domain;

import com.bloom.backend.global.entity.BaseTimeEntity;
import com.bloom.backend.user.domain.User;
import jakarta.persistence.*;

@Entity
@Table(name = "mileage_histories", uniqueConstraints =
        @UniqueConstraint(name = "uk_mileage_user_reference", columnNames = {"user_id", "reference_id"}))
public class MileageHistory extends BaseTimeEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
    private MileageType type;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 40)
    private MileageReason reason;
    @Column(nullable = false)
    private int amount;
    @Column(name = "balance_after", nullable = false)
    private int balanceAfter;
    @Column(name = "reference_id", nullable = false, length = 100)
    private String referenceId;
    protected MileageHistory() {}
    public MileageHistory(User user, MileageType type, MileageReason reason, int amount,
                           int balanceAfter, String referenceId) {
        this.user = user; this.type = type; this.reason = reason; this.amount = amount;
        this.balanceAfter = balanceAfter; this.referenceId = referenceId;
    }
    public Long getId() { return id; }
    public MileageType getType() { return type; }
    public MileageReason getReason() { return reason; }
    public int getAmount() { return amount; }
    public int getBalanceAfter() { return balanceAfter; }
}
