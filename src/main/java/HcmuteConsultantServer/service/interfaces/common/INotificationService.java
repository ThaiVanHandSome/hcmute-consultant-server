package HcmuteConsultantServer.service.interfaces.common;

import HcmuteConsultantServer.constant.enums.NotificationType;
import HcmuteConsultantServer.model.entity.NotificationEntity;

import java.util.List;

public interface INotificationService {
    public void sendUserNotification(Integer senderId, Integer receiverId, String content, NotificationType type);

    List<NotificationEntity> getNotificationsByReceiverId(Integer receiverId);

//    Page<HcmuteConsultantServer.model.payload.dto.common.NotificationDTO> findNotificationsByUserWithFilters(Integer userId, String content, LocalDate startDate, LocalDate endDate, Pageable pageable);

}
