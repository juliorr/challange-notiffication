package com.messageschallenge.notifications.service;

import com.messageschallenge.notifications.domain.Channel;
import com.messageschallenge.notifications.domain.Message;
import com.messageschallenge.notifications.domain.Notification;
import com.messageschallenge.notifications.domain.User;
import com.messageschallenge.notifications.repository.NotificationRepository;
import com.messageschallenge.notifications.repository.UserRepository;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class NotificationFanoutService {

  private final UserRepository users;
  private final NotificationRepository notifications;

  public NotificationFanoutService(UserRepository users, NotificationRepository notifications) {
    this.users = users;
    this.notifications = notifications;
  }

  public List<Long> expand(Message message) {
    List<User> subscribers =
        users.findSubscribersWithChannelsByCategoryCode(message.getCategory().getCode());

    List<Long> ids = new ArrayList<>();
    for (User user : subscribers) {
      for (Channel channel : user.getPreferredChannels()) {
        Notification saved = notifications.save(new Notification(message, user, channel));
        ids.add(saved.getId());
      }
    }
    return ids;
  }
}
