package com.mailsender.api.models;

import lombok.Data;
import lombok.ToString;

import javax.persistence.*;
import java.util.List;
import java.util.UUID;

@Entity
@Data
@Table(name = "customer_schedule")
public class CustomerSchedule {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ElementCollection
    @CollectionTable(name = "schedule_days", joinColumns = @JoinColumn(name = "schedule_id"))
    @Column(name = "day")
    private List<String> sendDays;

    private String sendTime;  // Time in "hh:mm a" format (AM/PM)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Override
    public String toString() {
        return "CustomerSchedule{" +
                "id=" + id +
                ", sendDays=" + sendDays +
                ", sendTime='" + sendTime + '\'' +
                ", customer=" + (customer != null ? customer.getId() : "null") +
                '}';
    }
}