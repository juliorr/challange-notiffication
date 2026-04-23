package com.messageschallenge.notifications.notifications;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class ChannelSenderRegistry {

  private final Map<String, ChannelSender> byCode;

  public ChannelSenderRegistry(List<ChannelSender> senders) {
    this.byCode =
        senders.stream()
            .collect(
                Collectors.toUnmodifiableMap(
                    ChannelSender::channelCode,
                    Function.identity(),
                    (a, b) -> {
                      throw new IllegalStateException(
                          "Duplicate ChannelSender for code '"
                              + a.channelCode()
                              + "': "
                              + a.getClass().getSimpleName()
                              + " and "
                              + b.getClass().getSimpleName());
                    }));
  }

  public Optional<ChannelSender> find(String channelCode) {
    return Optional.ofNullable(byCode.get(channelCode));
  }

  public ChannelSender require(String channelCode) {
    return find(channelCode)
        .orElseThrow(
            () ->
                new IllegalArgumentException(
                    "No ChannelSender registered for channel code: " + channelCode));
  }

  public int size() {
    return byCode.size();
  }
}
