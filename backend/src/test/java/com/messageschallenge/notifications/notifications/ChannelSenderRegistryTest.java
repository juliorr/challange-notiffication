package com.messageschallenge.notifications.notifications;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.messageschallenge.notifications.domain.Notification;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class ChannelSenderRegistryTest {

  private static ChannelSender stub(String code) {
    return new ChannelSender() {
      @Override
      public SendResult send(Notification notification) {
        return SendResult.success("ok");
      }

      @Override
      public String channelCode() {
        return code;
      }

      @Override
      public Set<String> requiredPayloadKeys() {
        return Set.of("body");
      }
    };
  }

  @Test
  void resolvesSenderByChannelCode() {
    var registry = new ChannelSenderRegistry(List.of(stub("SMS"), stub("EMAIL"), stub("PUSH")));

    assertThat(registry.size()).isEqualTo(3);
    assertThat(registry.find("SMS")).isPresent();
    assertThat(registry.require("EMAIL").channelCode()).isEqualTo("EMAIL");
  }

  @Test
  void requireThrowsForUnknownChannel() {
    var registry = new ChannelSenderRegistry(List.of(stub("SMS")));

    assertThatThrownBy(() -> registry.require("WHATSAPP"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("WHATSAPP");
  }

  @Test
  void findReturnsEmptyForUnknownChannel() {
    var registry = new ChannelSenderRegistry(List.of(stub("SMS")));

    assertThat(registry.find("PUSH")).isEmpty();
  }

  @Test
  void duplicateChannelCodeIsRejectedAtStartup() {
    assertThatThrownBy(() -> new ChannelSenderRegistry(List.of(stub("SMS"), stub("SMS"))))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("Duplicate ChannelSender");
  }
}
