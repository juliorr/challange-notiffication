package com.messageschallenge.notifications.repository;

import com.messageschallenge.notifications.domain.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UserRepository extends JpaRepository<User, Integer> {

  @Query(
      """
          SELECT DISTINCT u FROM User u
          JOIN u.subscribedCategories c
          LEFT JOIN FETCH u.preferredChannels
          WHERE c.code = :categoryCode
      """)
  List<User> findSubscribersWithChannelsByCategoryCode(String categoryCode);
}
