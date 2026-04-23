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
public class SmsMockSender extends AbstractChannelSender {

  private static final Logger log = LoggerFactory.getLogger(SmsMockSender.class);
  private static final Set<String> KEYS = Set.of("to", "body");

  private final AppProperties.Senders.Sms config;

  public SmsMockSender(AppProperties props) {
    this.config = props.senders().sms();
  }

  @Override
  public String channelCode() {
    return "SMS";
  }

  @Override
  public Set<String> requiredPayloadKeys() {
    return KEYS;
  }

  @Override
  protected NotificationPayload buildPayload(Notification n) {
    Map<String, Object> data = new HashMap<>();
    data.put("to", n.getUser() == null ? null : n.getUser().getPhoneNumber());
    data.put("body", n.getMessage() == null ? null : n.getMessage().getBody());
    return new NotificationPayload(data);
  }

  @Override
  protected SendResult doSend(Notification n, NotificationPayload payload) {
    log.info(
        "[SMS mock] from={} to={} body={}",
        config.from(),
        payload.getString("to"),
        payload.getString("body"));
    return SendResult.success(mockProviderId());
  }
}
