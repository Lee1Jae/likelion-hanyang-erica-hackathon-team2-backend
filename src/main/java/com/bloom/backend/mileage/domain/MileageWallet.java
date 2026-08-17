package com.bloom.backend.mileage.domain;

import com.bloom.backend.global.entity.BaseTimeEntity;
import com.bloom.backend.user.domain.User;
import jakarta.persistence.*;

@Entity
@Table(name = "mileage_wallets")
public class MileageWallet extends BaseTimeEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;
    @Column(nullable = false)
    private int balance;
    protected MileageWallet() {}
    public MileageWallet(User user) { this.user = user; }
    public void credit(int amount) { this.balance = Math.addExact(balance, amount); }
    public int getBalance() { return balance; }
}
