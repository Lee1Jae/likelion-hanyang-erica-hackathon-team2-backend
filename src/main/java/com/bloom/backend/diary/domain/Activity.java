package com.bloom.backend.diary.domain;

import com.bloom.backend.global.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "activities")
public class Activity extends BaseTimeEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "diary_id", nullable = false)
    private Diary diary;

    @Column(name = "activity_amount", nullable = false)
    private int activityAmount;

    @Column(length = 200)
    private String memo;

    protected Activity() {}

    public Activity(Diary diary, int activityAmount, String memo) {
        this.diary = diary;
        update(activityAmount, memo);
    }

    public void update(int activityAmount, String memo) {
        this.activityAmount = activityAmount;
        this.memo = memo;
    }

    public Long getId() { return id; }
    public Diary getDiary() { return diary; }
    public int getActivityAmount() { return activityAmount; }
    public String getMemo() { return memo; }
}
