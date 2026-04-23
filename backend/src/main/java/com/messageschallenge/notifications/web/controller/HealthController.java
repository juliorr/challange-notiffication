package com.messageschallenge.notifications.web.controller;

import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/health")
public class HealthController {

  private static final String SUFFIX = "HealthIndicator";

  private final Map<String, HealthIndicator> indicators;

  public HealthController(Map<String, HealthIndicator> indicators) {
    this.indicators = indicators;
  }

  @GetMapping
  public Map<String, String> health() {
    return indicators.entrySet().stream()
        .collect(Collectors.toMap(e -> stripSuffix(e.getKey()), e -> statusCode(e.getValue())));
  }

  private static String stripSuffix(String beanName) {
    return beanName.endsWith(SUFFIX)
        ? beanName.substring(0, beanName.length() - SUFFIX.length())
        : beanName;
  }

  private static String statusCode(HealthIndicator indicator) {
    try {
      return Status.UP.equals(indicator.health().getStatus()) ? "UP" : "DOWN";
    } catch (Exception e) {
      return "DOWN";
    }
  }
}
