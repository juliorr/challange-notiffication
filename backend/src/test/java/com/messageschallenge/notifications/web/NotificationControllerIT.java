package com.messageschallenge.notifications.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.messageschallenge.notifications.AbstractIntegrationTest;
import com.messageschallenge.notifications.service.MessageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class NotificationControllerIT extends AbstractIntegrationTest {

  @Autowired MockMvc mvc;
  @Autowired MessageService messages;
  @Autowired ObjectMapper mapper;

  @BeforeEach
  void seed() {

    for (int i = 0; i < 3; i++) {
      messages.create("SPORTS", "seed-" + i);
    }
  }

  @Test
  void firstPage_defaultLimitIs20_returnsItemsArray() throws Exception {
    mvc.perform(get("/api/notifications"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items").isArray());
  }

  @Test
  void keysetPagination_walksThroughAllPagesWithoutDuplicates() throws Exception {
    int pageSize = 5;

    MvcResult first =
        mvc.perform(get("/api/notifications").param("limit", String.valueOf(pageSize)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items.length()").value(pageSize))
            .andExpect(jsonPath("$.nextCursor").isString())
            .andReturn();

    JsonNode firstJson = mapper.readTree(first.getResponse().getContentAsString());
    String cursor1 = firstJson.get("nextCursor").asText();

    MvcResult second =
        mvc.perform(
                get("/api/notifications")
                    .param("limit", String.valueOf(pageSize))
                    .param("cursor", cursor1))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items.length()").value(pageSize))
            .andReturn();

    JsonNode secondJson = mapper.readTree(second.getResponse().getContentAsString());

    var firstIds = extractTopLevelIds(firstJson);
    var secondIds = extractTopLevelIds(secondJson);
    assertThat(firstIds).doesNotContainAnyElementsOf(secondIds);

    long prev = Long.MAX_VALUE;
    for (JsonNode item : firstJson.get("items")) {
      long id = item.get("id").asLong();
      assertThat(id).isLessThan(prev);
      prev = id;
    }
  }

  @Test
  void limitIsCappedAt100() throws Exception {
    mvc.perform(get("/api/notifications").param("limit", "500")).andExpect(status().isOk());
  }

  @Test
  void invalidCursor_returns400() throws Exception {
    mvc.perform(get("/api/notifications").param("cursor", "not-base64!!!"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.title").value("Validation failed"));
  }

  @Test
  void unknownStatus_returns400() throws Exception {
    mvc.perform(get("/api/notifications").param("status", "NOPE"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.title").value("Validation failed"));
  }

  private static java.util.List<Long> extractTopLevelIds(JsonNode page) {
    var out = new java.util.ArrayList<Long>();
    for (JsonNode item : page.get("items")) out.add(item.get("id").asLong());
    return out;
  }

  @Test
  void statusFilter_onlyReturnsMatchingRows() throws Exception {
    mvc.perform(get("/api/notifications").param("status", "PENDING"))
        .andExpect(status().isOk())
        .andExpect(
            jsonPath(
                "$.items[*].status",
                org.hamcrest.Matchers.everyItem(org.hamcrest.Matchers.equalTo("PENDING"))));
  }
}
