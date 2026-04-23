package com.messageschallenge.notifications.notifications;

import com.messageschallenge.notifications.domain.Notification;
import com.messageschallenge.notifications.web.exception.ChannelSendException;
import java.util.UUID;

public abstract class AbstractChannelSender implements ChannelSender {

  @Override
  public final SendResult send(Notification notification) {
    NotificationPayload payload = buildPayload(notification);
    validate(payload);
    return doSend(notification, payload);
  }

  protected abstract NotificationPayload buildPayload(Notification notification);

  protected abstract SendResult doSend(Notification notification, NotificationPayload payload);

  protected final String mockProviderId() {
    return "mock-" + channelCode().toLowerCase() + "-" + UUID.randomUUID();
  }

  private void validate(NotificationPayload payload) {
    for (String key : requiredPayloadKeys()) {
      if (payload.getString(key) == null) {
        throw new ChannelSendException(
            "channel=" + channelCode() + " missing required payload key: " + key);
      }
    }
  }
}
