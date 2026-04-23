package com.messageschallenge.notifications.repository;

import com.messageschallenge.notifications.domain.Category;
import com.messageschallenge.notifications.web.exception.NotFoundException;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Integer> {

  Optional<Category> findByCode(String code);

  default Category requireByCode(String code) {
    return findByCode(code).orElseThrow(() -> new NotFoundException("Unknown category: " + code));
  }
}
