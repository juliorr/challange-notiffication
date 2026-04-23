package com.messageschallenge.notifications.web.exception;

import org.springframework.http.HttpStatus;

public final class NotFoundException extends DomainException {
  public NotFoundException(String message) {
    super(message);
  }

  @Override
  public HttpStatus httpStatus() {
    return HttpStatus.NOT_FOUND;
  }

  @Override
  public String slug() {
    return "not-found";
  }

  @Override
  public String title() {
    return "Not found";
  }
}
