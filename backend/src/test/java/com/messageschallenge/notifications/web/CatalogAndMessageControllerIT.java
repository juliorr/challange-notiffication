package com.messageschallenge.notifications.web;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.messageschallenge.notifications.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class CatalogAndMessageControllerIT extends AbstractIntegrationTest {

  @Autowired MockMvc mvc;

  @Test
  void listsCategoriesAndChannels() throws Exception {
    mvc.perform(get("/api/categories"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(4)));

    mvc.perform(get("/api/channels"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(3)));
  }

  @Test
  void createMessageReturns201WithFanoutCount() throws Exception {
    mvc.perform(
            post("/api/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"category":"SPORTS","body":"hello via test"}
                    """))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").exists())
        .andExpect(jsonPath("$.fanoutCount", greaterThanOrEqualTo(5)));
  }

  @Test
  void invalidBodyReturns400WithProblemDetail() throws Exception {
    mvc.perform(
            post("/api/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"category":"SPORTS","body":""}
                    """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.title").value("Validation failed"));
  }

  @Test
  void unknownCategoryReturns404() throws Exception {
    mvc.perform(
            post("/api/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"category":"NOPE","body":"x"}
                    """))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.title").value("Not found"));
  }

  @Test
  void blankCategoryReturns400() throws Exception {
    mvc.perform(
            post("/api/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"category":"","body":"x"}
                    """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.title").value("Validation failed"));
  }

  @Test
  void missingCategoryFieldReturns400() throws Exception {
    mvc.perform(
            post("/api/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"body":"x"}
                    """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.title").value("Validation failed"));
  }

  @Test
  void bodyOverMaxSizeReturns400() throws Exception {
    String tooLong = "a".repeat(4001);
    String json = "{\"category\":\"SPORTS\",\"body\":\"" + tooLong + "\"}";

    mvc.perform(post("/api/messages").contentType(MediaType.APPLICATION_JSON).content(json))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.title").value("Validation failed"));
  }

  @Test
  void wrongContentTypeReturns415() throws Exception {
    mvc.perform(
            post("/api/messages")
                .contentType(MediaType.TEXT_PLAIN)
                .content("category=SPORTS&body=x"))
        .andExpect(status().isUnsupportedMediaType());
  }

  @Test
  void malformedJsonReturns400() throws Exception {
    mvc.perform(
            post("/api/messages").contentType(MediaType.APPLICATION_JSON).content("{\"category\":"))
        .andExpect(status().isBadRequest());
  }
}
