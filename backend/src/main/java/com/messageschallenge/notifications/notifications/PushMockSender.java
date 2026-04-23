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
public class PushMockSender extends AbstractChannelSender {

  private static final Logger log = LoggerFactory.getLogger(PushMockSender.class);
  private static final Set<String> KEYS = Set.of("deviceToken", "title", "body");

  private final AppProperties.Senders.Push config;

  public PushMockSender(AppProperties props) {
    this.config = props.senders().push();
  }

  @Override
  public String channelCode() {
    return "PUSH";
  }

  @Override
  public Set<String> requiredPayloadKeys() {
    return KEYS;
  }

  @Override
  protected NotificationPayload buildPayload(Notification n) {
    Map<String, Object> data = new HashMap<>();
    data.put(
        "deviceToken",
        n.getUser() == null || n.getUser().getId() == null
            ? null
            : "device-" + n.getUser().getId());
    data.put("title", "Notification");
    data.put("body", n.getMessage() == null ? null : n.getMessage().getBody());
    return new NotificationPayload(data);
  }

  @Override
  protected SendResult doSend(Notification n, NotificationPayload payload) {
    log.info(
        "[PUSH mock] app={} deviceToken={} title={} body={}",
        config.appId(),
        payload.getString("deviceToken"),
        payload.getString("title"),
        payload.getString("body"));
    return SendResult.success(mockProviderId());
  }
}
