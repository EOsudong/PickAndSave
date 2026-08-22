package org.sy.pickandsave.domain.alert.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.sy.pickandsave.domain.alert.entity.Notification;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
}