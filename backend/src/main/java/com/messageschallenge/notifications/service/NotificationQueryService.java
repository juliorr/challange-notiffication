package com.messageschallenge.notifications.service;

import com.messageschallenge.notifications.config.AppProperties;
import com.messageschallenge.notifications.domain.Notification;
import com.messageschallenge.notifications.domain.NotificationStatus;
import com.messageschallenge.notifications.repository.NotificationRepository;
import com.messageschallenge.notifications.web.dto.Cursor;
import com.messageschallenge.notifications.web.dto.NotificationView;
import com.messageschallenge.notifications.web.dto.PageResponse;
import com.messageschallenge.notifications.web.exception.ValidationException;
import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationQueryService {

  private final NotificationRepository repo;
  private final int defaultPageSize;
  private final int maxPageSize;

  public NotificationQueryService(NotificationRepository repo, AppProperties props) {
    this.repo = repo;
    this.defaultPageSize = props.pagination().defaultSize();
    this.maxPageSize = props.pagination().maxSize();
  }

  @Transactional(readOnly = true)
  public PageResponse<NotificationView> list(Integer limit, String statusFilter, String cursor) {
    int size = (limit == null || limit <= 0) ? defaultPageSize : Math.min(limit, maxPageSize);

    Cursor decoded = (cursor == null || cursor.isBlank()) ? null : Cursor.decode(cursor);
    Instant cursorTs = decoded == null ? null : decoded.createdAt();
    Long cursorId = decoded == null ? null : decoded.id();

    var pageable = PageRequest.of(0, size + 1);

    boolean hasStatus = statusFilter != null && !statusFilter.isBlank();
    NotificationStatus status = hasStatus ? parseStatus(statusFilter) : null;

    List<Notification> rows;
    if (decoded == null) {
      rows =
          hasStatus ? repo.findFirstPageByStatus(status, pageable) : repo.findFirstPage(pageable);
    } else {
      rows =
          hasStatus
              ? repo.findPageAfterByStatus(status, cursorTs, cursorId, pageable)
              : repo.findPageAfter(cursorTs, cursorId, pageable);
    }

    boolean hasMore = rows.size() > size;
    List<Notification> page = hasMore ? rows.subList(0, size) : rows;

    String nextCursor = null;
    if (hasMore) {
      Notification last = page.get(page.size() - 1);
      nextCursor = new Cursor(last.getCreatedAt(), last.getId()).encode();
    }

    return new PageResponse<>(page.stream().map(NotificationView::from).toList(), nextCursor);
  }

  private static NotificationStatus parseStatus(String raw) {
    try {
      return NotificationStatus.valueOf(raw);
    } catch (IllegalArgumentException e) {
      throw new ValidationException("Unknown status: " + raw);
    }
  }
}
