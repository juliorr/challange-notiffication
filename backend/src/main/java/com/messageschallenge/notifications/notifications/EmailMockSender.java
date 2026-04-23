package com.messageschallenge.notifications.notifications;

import com.messageschallenge.notifications.config.AppProperties;
import com.messageschallenge.notifications.domain.Notification;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class EmailMockSender extends AbstractChannelSender {

  private static final Logger log = LoggerFactory.getLogger(EmailMockSender.class);
  private static final Set<String> KEYS = Set.of("to", "subject", "body");

  // Sub-addressing (RFC 5233) marker used to deterministically force DLQ in dev/demo.
  // Any recipient email containing this token makes the mock return a permanent failure,
  // which — after max-attempts retries — naturally drives the notification to DEAD_LETTER.
  public static final String FAILURE_MARKER = "+fail@";

  private final AppProperties.Senders.Email config;

  public EmailMockSender(AppProperties props) {
    this.config = props.senders().email();
  }

  @Override
  public String channelCode() {
    return "EMAIL";
  }

  @Override
  public Set<String> requiredPayloadKeys() {
    return KEYS;
  }

  @Override
  protected NotificationPayload buildPayload(Notification n) {
    Map<String, Object> data = new HashMap<>();
    data.put("to", n.getUser() == null ? null : n.getUser().getEmail());
    data.put("subject", "Notification");
    data.put("body", n.getMessage() == null ? null : n.getMessage().getBody());
    return new NotificationPayload(data);
  }

  @Override
  protected SendResult doSend(Notification n, NotificationPayload payload) {
    String to = payload.getString("to");
    if (to != null && to.contains(FAILURE_MARKER)) {
      log.info("[EMAIL mock] simulated permanent failure for to={}", to);
      return SendResult.failure("SIMULATED_FAILURE: email contains '" + FAILURE_MARKER + "'");
    }
    log.info(
        "[EMAIL mock] from={} to={} subject={} body={}",
        config.from(),
        to,
        payload.getString("subject"),
        payload.getString("body"));
    return SendResult.success(mockProviderId());
  }
}
