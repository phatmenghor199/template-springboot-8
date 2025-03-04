package com.mailsender.api.repository;

import com.mailsender.api.models.EmailSendHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface EmailSendHistoryRepository extends JpaRepository<EmailSendHistory, UUID> , JpaSpecificationExecutor<EmailSendHistory> {

}