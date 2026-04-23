package com.messageschallenge.notifications.repository;

import com.messageschallenge.notifications.domain.Message;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<Message, Long> {}
