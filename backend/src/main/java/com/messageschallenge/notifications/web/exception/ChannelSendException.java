package com.messageschallenge.notifications.web.exception;

import org.springframework.http.HttpStatus;

public final class ChannelSendException extends DomainException {
  public ChannelSendException(String message) {
    super(message);
  }

  public ChannelSendException(String message, Throwable cause) {
    super(message, cause);
  }

  @Override
  public HttpStatus httpStatus() {
    return HttpStatus.BAD_GATEWAY;
  }

  @Override
  public String slug() {
    return "channel-send";
  }

  @Override
  public String title() {
    return "Channel send failed";
  }
}
