package com.messageschallenge.notifications.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.messageschallenge.notifications.AbstractIntegrationTest;
import com.messageschallenge.notifications.domain.User;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class CatalogAndUserRepositoryIT extends AbstractIntegrationTest {

  @Autowired CategoryRepository categories;
  @Autowired ChannelRepository channels;
  @Autowired UserRepository users;

  @Test
  void seedsLoadedFourCategories() {
    assertThat(categories.findAll())
        .extracting("code")
        .containsExactlyInAnyOrder("SPORTS", "FINANCE", "MOVIES", "TRAVEL");
  }

  @Test
  void seedsLoadedThreeChannels() {
    assertThat(channels.findAll())
        .extracting("code")
        .containsExactlyInAnyOrder("SMS", "EMAIL", "PUSH");
  }

  @Test
  void findByCode_returnsExpected() {
    assertThat(categories.findByCode("SPORTS")).isPresent();
    assertThat(channels.findByCode("EMAIL")).isPresent();
    assertThat(categories.findByCode("NOPE")).isEmpty();
  }

  @Test
  void sportsSubscribersAreAnaAndCarla_withTheirPreferredChannels() {
    List<User> subs = users.findSubscribersWithChannelsByCategoryCode("SPORTS");

    assertThat(subs)
        .extracting(User::getEmail)
        .containsExactlyInAnyOrder("ana@example.com", "carla@example.com");

    User ana =
        subs.stream().filter(u -> u.getEmail().equals("ana@example.com")).findFirst().orElseThrow();
    assertThat(ana.getPreferredChannels())
        .extracting("code")
        .containsExactlyInAnyOrder("EMAIL", "PUSH");

    User carla =
        subs.stream()
            .filter(u -> u.getEmail().equals("carla@example.com"))
            .findFirst()
            .orElseThrow();
    assertThat(carla.getPreferredChannels())
        .extracting("code")
        .containsExactlyInAnyOrder("EMAIL", "SMS", "PUSH");
  }

  @Test
  void findSubscribers_unknownCategory_returnsEmpty() {
    assertThat(users.findSubscribersWithChannelsByCategoryCode("NOPE")).isEmpty();
  }

  @Test
  void findSubscribers_travelCategoryHasNoSubscribers() {
    assertThat(categories.findByCode("TRAVEL")).isPresent();
    assertThat(users.findSubscribersWithChannelsByCategoryCode("TRAVEL")).isEmpty();
  }

  @Test
  void elenaHasNoSubscriptions_soNoCategoryReturnsHer() {
    assertThat(users.findSubscribersWithChannelsByCategoryCode("SPORTS"))
        .extracting(User::getEmail)
        .doesNotContain("elena@example.com");
    assertThat(users.findSubscribersWithChannelsByCategoryCode("FINANCE"))
        .extracting(User::getEmail)
        .doesNotContain("elena@example.com");
    assertThat(users.findSubscribersWithChannelsByCategoryCode("MOVIES"))
        .extracting(User::getEmail)
        .doesNotContain("elena@example.com");
  }
}
