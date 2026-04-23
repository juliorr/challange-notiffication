package com.messageschallenge.notifications.web.controller;

import com.messageschallenge.notifications.service.NotificationQueryService;
import com.messageschallenge.notifications.web.dto.NotificationView;
import com.messageschallenge.notifications.web.dto.PageResponse;
import com.messageschallenge.notifications.web.sse.SseBroadcaster;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

  private final NotificationQueryService query;
  private final SseBroadcaster sse;

  public NotificationController(NotificationQueryService query, SseBroadcaster sse) {
    this.query = query;
    this.sse = sse;
  }

  @GetMapping
  public PageResponse<NotificationView> list(
      @RequestParam(required = false) Integer limit,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) String cursor) {
    return query.list(limit, status, cursor);
  }

  @GetMapping(path = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public SseEmitter stream() {
    return sse.subscribe();
  }
}
