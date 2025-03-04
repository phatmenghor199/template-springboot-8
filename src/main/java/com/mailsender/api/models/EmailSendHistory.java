package com.mailsender.api.models;

import com.mailsender.api.enumation.Status;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@Table(name = "email_send_history")
public class EmailSendHistory extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exchange_rate_id", nullable = false)
    private ExchangeRate exchangeRate;  // Store the exchange rate used for sending email


    @Enumerated(EnumType.STRING) // Storing the status as a string
    private Status status; // "SUCCESS" or "FAILURE"

    private String errorMessage;  // Additional info, like error message if failed
    private LocalDateTime sentAt;  // Time when the email was sent
}
