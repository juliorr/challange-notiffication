package com.messageschallenge.notifications.repository;

import com.messageschallenge.notifications.domain.Notification;
import com.messageschallenge.notifications.domain.NotificationStatus;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

  @Query(
      """
          SELECT n FROM Notification n
          JOIN FETCH n.user
          JOIN FETCH n.channel
          JOIN FETCH n.message m
          JOIN FETCH m.category
          ORDER BY n.createdAt DESC, n.id DESC
      """)
  List<Notification> findFirstPage(Pageable pageable);

  @Query(
      """
          SELECT n FROM Notification n
          JOIN FETCH n.user
          JOIN FETCH n.channel
          JOIN FETCH n.message m
          JOIN FETCH m.category
          WHERE (n.createdAt < :cursorTs)
             OR (n.createdAt = :cursorTs AND n.id < :cursorId)
          ORDER BY n.createdAt DESC, n.id DESC
      """)
  List<Notification> findPageAfter(
      @Param("cursorTs") Instant cursorTs, @Param("cursorId") Long cursorId, Pageable pageable);

  @Query(
      """
          SELECT n FROM Notification n
          JOIN FETCH n.user
          JOIN FETCH n.channel
          JOIN FETCH n.message m
          JOIN FETCH m.category
          WHERE n.status = :status
          ORDER BY n.createdAt DESC, n.id DESC
      """)
  List<Notification> findFirstPageByStatus(
      @Param("status") NotificationStatus status, Pageable pageable);

  @Query(
      """
          SELECT n FROM Notification n
          JOIN FETCH n.user
          JOIN FETCH n.channel
          JOIN FETCH n.message m
          JOIN FETCH m.category
          WHERE n.status = :status
            AND ((n.createdAt < :cursorTs)
                 OR (n.createdAt = :cursorTs AND n.id < :cursorId))
          ORDER BY n.createdAt DESC, n.id DESC
      """)
  List<Notification> findPageAfterByStatus(
      @Param("status") NotificationStatus status,
      @Param("cursorTs") Instant cursorTs,
      @Param("cursorId") Long cursorId,
      Pageable pageable);

  @Query(
      """
          SELECT n FROM Notification n
          JOIN FETCH n.user
          JOIN FETCH n.channel
          JOIN FETCH n.message m
          JOIN FETCH m.category
          WHERE n.id = :id
      """)
  Optional<Notification> findByIdWithAssociations(Long id);

  @Query(
      """
          SELECT n.id FROM Notification n
          WHERE n.status = :status
            AND n.createdAt < :threshold
          ORDER BY n.createdAt ASC
      """)
  List<Long> findStaleIdsByStatus(
      @Param("status") NotificationStatus status,
      @Param("threshold") Instant threshold,
      Pageable pageable);
}
