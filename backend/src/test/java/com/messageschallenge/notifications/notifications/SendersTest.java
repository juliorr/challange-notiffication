package com.messageschallenge.notifications.notifications;

import static com.messageschallenge.notifications.notifications.SenderTestFixtures.appPropertiesWithDefaults;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.messageschallenge.notifications.config.AppProperties;
import com.messageschallenge.notifications.domain.Category;
import com.messageschallenge.notifications.domain.Channel;
import com.messageschallenge.notifications.domain.Message;
import com.messageschallenge.notifications.domain.Notification;
import com.messageschallenge.notifications.domain.User;
import com.messageschallenge.notifications.web.exception.ChannelSendException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class SendersTest {

  private final AppProperties props = appPropertiesWithDefaults();

  private static Notification fixture(
      String email, String phone, String body, Integer userId, Long messageId) {
    Category cat = new Category("SPORTS", "Sports");
    Channel ch = new Channel("SMS", "SMS");
    User u = new User("Ana", email, phone);
    Message m = new Message(cat, body);
    setField(u, "id", userId);
    setField(m, "id", messageId);
    return new Notification(m, u, ch);
  }

  private static Notification fixture(String email, String phone, String body) {
    return fixture(email, phone, body, 1, 1L);
  }

  private static void setField(Object target, String name, Object value) {
    try {
      Field f = target.getClass().getDeclaredField(name);
      f.setAccessible(true);
      f.set(target, value);
    } catch (ReflectiveOperationException e) {
      throw new IllegalStateException(e);
    }
  }

  @Test
  void smsSender_returnsSuccessWithProviderId() {
    SmsMockSender sender = new SmsMockSender(props);

    ChannelSender.SendResult r = sender.send(fixture("a@b.com", "+525511111111", "hi"));

    assertThat(r.ok()).isTrue();
    assertThat(r.providerId()).startsWith("mock-sms-");
    assertThat(sender.channelCode()).isEqualTo("SMS");
    assertThat(sender.requiredPayloadKeys()).containsExactlyInAnyOrder("to", "body");
  }

  @Test
  void emailSender_returnsSuccessWithProviderId() {
    EmailMockSender sender = new EmailMockSender(props);

    ChannelSender.SendResult r = sender.send(fixture("a@b.com", "+525511111111", "body"));

    assertThat(r.ok()).isTrue();
    assertThat(r.providerId()).startsWith("mock-email-");
    assertThat(sender.channelCode()).isEqualTo("EMAIL");
    assertThat(sender.requiredPayloadKeys()).containsExactlyInAnyOrder("to", "subject", "body");
  }

  @Test
  void pushSender_returnsSuccessWithProviderId() {
    PushMockSender sender = new PushMockSender(props);

    ChannelSender.SendResult r = sender.send(fixture("a@b.com", "+525511111111", "body"));

    assertThat(r.ok()).isTrue();
    assertThat(r.providerId()).startsWith("mock-push-");
    assertThat(sender.channelCode()).isEqualTo("PUSH");
    assertThat(sender.requiredPayloadKeys())
        .containsExactlyInAnyOrder("deviceToken", "title", "body");
  }

  @Test
  void payload_missingKey_returnsNull() {
    var payload = new NotificationPayload(Map.of("to", "x"));
    assertThat(payload.getString("missing")).isNull();
    assertThat(payload.getString("to")).isEqualTo("x");
  }

  @Test
  void payload_getString_returnsToStringOfNonStringValues() {
    Map<String, Object> raw = new HashMap<>();
    raw.put("count", 42L);
    raw.put("flag", true);
    var payload = new NotificationPayload(raw);

    assertThat(payload.getString("count")).isEqualTo("42");
    assertThat(payload.getString("flag")).isEqualTo("true");
    assertThat(payload.getString("absent")).isNull();
  }

  @Test
  void smsSender_missingBody_throwsChannelSendException() {
    SmsMockSender sender = new SmsMockSender(props);

    assertThatThrownBy(() -> sender.send(fixture("a@b.com", "+525511111111", null)))
        .isInstanceOf(ChannelSendException.class)
        .hasMessageContaining("SMS")
        .hasMessageContaining("body");
  }

  @Test
  void smsSender_missingPhoneNumber_throwsChannelSendException() {
    SmsMockSender sender = new SmsMockSender(props);

    assertThatThrownBy(() -> sender.send(fixture("a@b.com", null, "hi")))
        .isInstanceOf(ChannelSendException.class)
        .hasMessageContaining("SMS")
        .hasMessageContaining("to");
  }

  @Test
  void emailSender_missingBody_throwsChannelSendException() {
    EmailMockSender sender = new EmailMockSender(props);

    assertThatThrownBy(() -> sender.send(fixture("a@b.com", "+525511111111", null)))
        .isInstanceOf(ChannelSendException.class)
        .hasMessageContaining("EMAIL")
        .hasMessageContaining("body");
  }

  @Test
  void emailSender_missingEmail_throwsChannelSendException() {
    EmailMockSender sender = new EmailMockSender(props);

    assertThatThrownBy(() -> sender.send(fixture(null, "+525511111111", "hi")))
        .isInstanceOf(ChannelSendException.class)
        .hasMessageContaining("EMAIL")
        .hasMessageContaining("to");
  }

  @Test
  void pushSender_missingUserId_throwsChannelSendException() {
    PushMockSender sender = new PushMockSender(props);

    assertThatThrownBy(() -> sender.send(fixture("a@b.com", "+525511111111", "hi", null, 1L)))
        .isInstanceOf(ChannelSendException.class)
        .hasMessageContaining("PUSH")
        .hasMessageContaining("deviceToken");
  }

  @Test
  void pushSender_missingBody_throwsChannelSendException() {
    PushMockSender sender = new PushMockSender(props);

    assertThatThrownBy(() -> sender.send(fixture("a@b.com", "+525511111111", null)))
        .isInstanceOf(ChannelSendException.class)
        .hasMessageContaining("PUSH")
        .hasMessageContaining("body");
  }

  @Test
  void emailSender_recipientWithFailureMarker_returnsFailure() {
    EmailMockSender sender = new EmailMockSender(props);

    ChannelSender.SendResult r =
        sender.send(fixture("dlq+fail@test.local", "+525511111111", "body"));

    assertThat(r.ok()).isFalse();
    assertThat(r.providerId()).isNull();
    assertThat(r.error()).contains("SIMULATED_FAILURE").contains(EmailMockSender.FAILURE_MARKER);
  }

  @Test
  void emailSender_recipientWithoutFailureMarker_returnsSuccess() {
    EmailMockSender sender = new EmailMockSender(props);

    ChannelSender.SendResult r = sender.send(fixture("plain@test.local", "+525511111111", "body"));

    assertThat(r.ok()).isTrue();
  }

  @Test
  void emailSender_failureMarker_isEmailOnly_smsAndPushStillSucceed() {
    SmsMockSender sms = new SmsMockSender(props);
    PushMockSender push = new PushMockSender(props);

    assertThat(sms.send(fixture("dlq+fail@test.local", "+525511111111", "body")).ok()).isTrue();
    assertThat(push.send(fixture("dlq+fail@test.local", "+525511111111", "body")).ok()).isTrue();
  }
}
