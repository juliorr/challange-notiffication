package com.messageschallenge.notifications.repository;

import com.messageschallenge.notifications.domain.Channel;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChannelRepository extends JpaRepository<Channel, Integer> {
  Optional<Channel> findByCode(String code);
}
