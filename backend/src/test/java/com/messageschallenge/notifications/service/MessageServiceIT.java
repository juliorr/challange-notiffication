package com.messageschallenge.notifications.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.messageschallenge.notifications.AbstractIntegrationTest;
import com.messageschallenge.notifications.domain.NotificationStatus;
import com.messageschallenge.notifications.repository.NotificationRepository;
import com.messageschallenge.notifications.web.dto.CreateMessageResponse;
import com.messageschallenge.notifications.web.exception.NotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class MessageServiceIT extends AbstractIntegrationTest {

  @Autowired MessageService service;
  @Autowired NotificationRepository repo;

  @Test
  void sportsFanout_createsOneRowPerSubscriberPerChannel() {
    CreateMessageResponse r = service.create("SPORTS", "hello SPORTS");

    assertThat(r.fanoutCount()).isEqualTo(5);
    assertThat(repo.count()).isGreaterThanOrEqualTo(5);
    assertThat(repo.findAll())
        .filteredOn(n -> n.getMessage().getId().equals(r.id()))
        .allSatisfy(n -> assertThat(n.getStatus()).isEqualTo(NotificationStatus.PENDING));
  }

  @Test
  void unknownCategory_throwsNotFound() {
    assertThatThrownBy(() -> service.create("NOPE", "x")).isInstanceOf(NotFoundException.class);
  }

  @Test
  void moviesFanoutMatchesCarlaAndBeto() {
    CreateMessageResponse r = service.create("MOVIES", "hello MOVIES");
    assertThat(r.fanoutCount()).isEqualTo(4);
  }
}
