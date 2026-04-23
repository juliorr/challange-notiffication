package com.messageschallenge.notifications.web.controller;

import com.messageschallenge.notifications.service.MessageService;
import com.messageschallenge.notifications.web.dto.CreateMessageRequest;
import com.messageschallenge.notifications.web.dto.CreateMessageResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

  private final MessageService service;

  public MessageController(MessageService service) {
    this.service = service;
  }

  @PostMapping
  public ResponseEntity<CreateMessageResponse> create(
      @Valid @RequestBody CreateMessageRequest req) {
    CreateMessageResponse res = service.create(req.category(), req.body());
    return ResponseEntity.status(201).body(res);
  }
}
