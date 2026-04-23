package com.messageschallenge.notifications.web.dto;

import com.messageschallenge.notifications.web.exception.ValidationException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;

public record Cursor(Instant createdAt, long id) {

  public static Cursor decode(String encoded) {
    if (encoded == null || encoded.isBlank()) {
      throw new ValidationException("Cursor must not be blank");
    }
    try {
      String raw = new String(Base64.getUrlDecoder().decode(encoded), StandardCharsets.UTF_8);
      int sep = raw.indexOf(':');
      if (sep <= 0 || sep == raw.length() - 1) {
        throw new ValidationException("Invalid cursor format");
      }
      long ts = Long.parseLong(raw.substring(0, sep));
      long id = Long.parseLong(raw.substring(sep + 1));
      return new Cursor(Instant.ofEpochMilli(ts), id);
    } catch (IllegalArgumentException e) {
      throw new ValidationException("Invalid cursor");
    }
  }

  public String encode() {
    String raw = createdAt.toEpochMilli() + ":" + id;
    return Base64.getUrlEncoder()
        .withoutPadding()
        .encodeToString(raw.getBytes(StandardCharsets.UTF_8));
  }
}
