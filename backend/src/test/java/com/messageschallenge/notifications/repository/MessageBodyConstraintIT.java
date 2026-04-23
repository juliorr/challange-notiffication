package com.messageschallenge.notifications.repository;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.messageschallenge.notifications.AbstractIntegrationTest;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

class MessageBodyConstraintIT extends AbstractIntegrationTest {

  @Autowired EntityManager em;

  @Test
  @Transactional
  void bodyLongerThan4000_violatesCheckConstraint() {
    String tooLong = "x".repeat(4001);
    assertThatThrownBy(
            () -> {
              em.createNativeQuery("INSERT INTO messages (category_id, body) VALUES (1, :body)")
                  .setParameter("body", tooLong)
                  .executeUpdate();
              em.flush();
            })
        .isInstanceOfAny(
            DataIntegrityViolationException.class, jakarta.persistence.PersistenceException.class);
  }

  @Test
  @Transactional
  void emptyBody_violatesCheckConstraint() {
    assertThatThrownBy(
            () -> {
              em.createNativeQuery("INSERT INTO messages (category_id, body) VALUES (1, '')")
                  .executeUpdate();
              em.flush();
            })
        .isInstanceOfAny(
            DataIntegrityViolationException.class, jakarta.persistence.PersistenceException.class);
  }
}
