package com.messageschallenge.notifications.web.sse;

import com.messageschallenge.notifications.web.dto.NotificationView;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface SseBroadcaster {

  SseEmitter subscribe();

  void publish(NotificationView view);
}
