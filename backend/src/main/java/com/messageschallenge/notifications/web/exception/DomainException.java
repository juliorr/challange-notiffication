package com.messageschallenge.notifications.web.exception;

import org.springframework.http.HttpStatus;

public abstract sealed class DomainException extends RuntimeException
    permits ValidationException, NotFoundException, ChannelSendException {

  protected DomainException(String message) {
    super(message);
  }

  protected DomainException(String message, Throwable cause) {
    super(message, cause);
  }

  public abstract HttpStatus httpStatus();

  public abstract String slug();

  public abstract String title();
}
