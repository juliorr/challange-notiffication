package com.messageschallenge.notifications.service;

import com.messageschallenge.notifications.domain.Category;
import com.messageschallenge.notifications.domain.Message;
import com.messageschallenge.notifications.repository.CategoryRepository;
import com.messageschallenge.notifications.repository.MessageRepository;
import com.messageschallenge.notifications.web.dto.CreateMessageResponse;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MessageService {

  private static final Logger log = LoggerFactory.getLogger(MessageService.class);

  private final CategoryRepository categories;
  private final MessageRepository messages;
  private final NotificationFanoutService fanout;
  private final NotificationDispatcher dispatcher;
  private final AfterCommitExecutor afterCommit;

  public MessageService(
      CategoryRepository categories,
      MessageRepository messages,
      NotificationFanoutService fanout,
      NotificationDispatcher dispatcher,
      AfterCommitExecutor afterCommit) {
    this.categories = categories;
    this.messages = messages;
    this.fanout = fanout;
    this.dispatcher = dispatcher;
    this.afterCommit = afterCommit;
  }

  @Transactional
  public CreateMessageResponse create(String categoryCode, String body) {
    Category category = categories.requireByCode(categoryCode);
    Message message = messages.save(new Message(category, body));
    MDC.put("messageId", String.valueOf(message.getId()));
    try {
      List<Long> ids = fanout.expand(message);

      log.info(
          "Created message id={} category={} fanout={}", message.getId(), categoryCode, ids.size());
      afterCommit.run(() -> dispatcher.dispatchAll(ids));

      return new CreateMessageResponse(message.getId(), ids.size());
    } finally {
      MDC.remove("messageId");
    }
  }
}
