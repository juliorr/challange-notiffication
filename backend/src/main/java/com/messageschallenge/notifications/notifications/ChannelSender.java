package com.messageschallenge.notifications.notifications;

import com.messageschallenge.notifications.domain.Notification;
import java.util.Set;

public interface ChannelSender {

  SendResult send(Notification notification);

  String channelCode();

  Set<String> requiredPayloadKeys();

  record SendResult(boolean ok, String providerId, String error) {
    public static SendResult success(String providerId) {
      return new SendResult(true, providerId, null);
    }

    public static SendResult failure(String error) {
      return new SendResult(false, null, error);
    }
  }
}
