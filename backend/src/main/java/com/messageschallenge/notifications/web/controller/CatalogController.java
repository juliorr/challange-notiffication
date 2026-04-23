package com.messageschallenge.notifications.web.controller;

import com.messageschallenge.notifications.repository.CategoryRepository;
import com.messageschallenge.notifications.repository.ChannelRepository;
import com.messageschallenge.notifications.web.dto.CatalogItem;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class CatalogController {

  private final CategoryRepository categories;
  private final ChannelRepository channels;

  public CatalogController(CategoryRepository categories, ChannelRepository channels) {
    this.categories = categories;
    this.channels = channels;
  }

  @GetMapping("/categories")
  public List<CatalogItem> listCategories() {
    return categories.findAll().stream()
        .map(c -> new CatalogItem(c.getCode(), c.getName()))
        .toList();
  }

  @GetMapping("/channels")
  public List<CatalogItem> listChannels() {
    return channels.findAll().stream().map(c -> new CatalogItem(c.getCode(), c.getName())).toList();
  }
}
