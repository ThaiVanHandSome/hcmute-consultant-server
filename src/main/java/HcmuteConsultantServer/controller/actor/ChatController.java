package HcmuteConsultantServer.controller.actor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import HcmuteConsultantServer.constant.SecurityConstants;
import HcmuteConsultantServer.constant.enums.MessageStatus;
import HcmuteConsultantServer.constant.enums.NotificationContent;
import HcmuteConsultantServer.constant.enums.NotificationType;
import HcmuteConsultantServer.model.entity.*;
import HcmuteConsultantServer.model.exception.Exceptions.ErrorException;
import HcmuteConsultantServer.model.payload.dto.actor.MessageDTO;
import HcmuteConsultantServer.model.payload.dto.actor.MessageDTO.UserInformationDTO;
import HcmuteConsultantServer.model.payload.response.DataResponse;
import HcmuteConsultantServer.repository.actor.*;
import HcmuteConsultantServer.repository.admin.UserRepository;
import HcmuteConsultantServer.service.implement.common.FileStorageServiceImpl;
import HcmuteConsultantServer.service.interfaces.actor.IConversationService;
import HcmuteConsultantServer.service.interfaces.common.INotificationService;
import HcmuteConsultantServer.service.interfaces.common.IUserService;
import HcmuteConsultantServer.specification.actor.MessageSpecification;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("${base.url}")
public class ChatController {

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private MessageRecallRepository messageRecallRepository;

    @Autowired
    private ConversationUserRepository conversationUserRepository;

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private INotificationService notificationService;

    @Autowired
    private IUserService userService;

    @Autowired
    private ConversationDeleteRepository conversationDeleteRepository;

    @Autowired
    private IConversationService conversationService;

    private void processFileUrl(MessageDTO messageDTO) {
        if (messageDTO.getImageUrl() != null && !messageDTO.getImageUrl().isEmpty()) {
            String imageUrl = messageDTO.getImageUrl();
            int lastDotIndex = imageUrl.lastIndexOf('.');
            if (lastDotIndex != -1) {
                String fileExtension = imageUrl.substring(lastDotIndex);
                messageDTO.setTypeUrl(fileExtension);
            }
        } else if (messageDTO.getFileUrl() != null && !messageDTO.getFileUrl().isEmpty()) {
            String fileUrl = messageDTO.getFileUrl();
            int lastDotIndex = fileUrl.lastIndexOf('.');
            if (lastDotIndex != -1) {
                String fileExtension = fileUrl.substring(lastDotIndex);
                messageDTO.setTypeUrl(fileExtension);
            }
        }
    }

    @MessageMapping("/private-message")
    public MessageDTO recMessage(@Payload MessageDTO messageDTO) {
        if (messageDTO == null || messageDTO.getSender() == null || messageDTO.getSender().getId() == null) {
            throw new ErrorException("Thông tin người gửi bị thiếu.");
        }

        UserInformationEntity senderEntity = userService.findById(messageDTO.getSender().getId())
                .orElseThrow(() -> {
                    return new ErrorException("Không tìm thấy người gửi với ID: " + messageDTO.getSender().getId());
                });

        if (messageDTO.getReceiver() == null || messageDTO.getReceiver().isEmpty()) {
            throw new ErrorException("Thông tin người nhận bị thiếu.");
        }

        List<UserInformationEntity> receiverEntities = new ArrayList<>();
        for (UserInformationDTO receiverDTO : messageDTO.getReceiver()) {
            UserInformationEntity receiverEntity = userService.findById(receiverDTO.getId())
                    .orElseThrow(() -> {
                        return new ErrorException("Không tìm thấy người nhận với ID: " + receiverDTO.getId());
                    });
            receiverEntities.add(receiverEntity);
        }

        processFileUrl(messageDTO);

        messageDTO.setSender(UserInformationDTO.builder()
                .id(senderEntity.getId())
                .name(senderEntity.getLastName() + " " + senderEntity.getFirstName())
                .avatarUrl(senderEntity.getAvatarUrl())
                .build());
        messageDTO.setReceiver(messageDTO.getReceiver());
        messageDTO.setDate(LocalDateTime.now());
        messageDTO.setMessageStatus(MessageStatus.PRIVATE);

        ConversationEntity conversation = conversationRepository.findById(messageDTO.getConversationId())
                .orElseThrow(() -> {
                    return new ErrorException("Không tìm thấy cuộc trò chuyện.");
                });

        for (UserInformationEntity receiverEntity : receiverEntities) {
            MessageEntity messageEntity = toEntity(messageDTO, senderEntity, receiverEntity);
            messageRepository.save(messageEntity);

            simpMessagingTemplate.convertAndSendToUser(String.valueOf(receiverEntity.getId()), "/private", messageDTO);

            NotificationType notificationType = receiverEntity.getAccount().getRole().getName().contains(SecurityConstants.Role.TUVANVIEN)
                    ? NotificationType.TUVANVIEN
                    : NotificationType.USER;

            notificationService.sendUserNotification(
                    senderEntity.getId(),
                    receiverEntity.getId(),
                    NotificationContent.NEW_CHAT_PRIVATE.formatMessage(senderEntity.getLastName() + " " + senderEntity.getFirstName()),
                    notificationType
            );
        }
        List<ConversationUserEntity> members = conversationUserRepository.findByConversationIdAndExcludeSender(conversation.getId(), senderEntity.getId());

        for (ConversationUserEntity member : members) {
            boolean isDeleted = conversationDeleteRepository.existsByConversationIdAndUserId(conversation.getId(), member.getUser().getId());

            if (isDeleted) {
                conversationService.deleteMembersFromConversation(conversation, member.getUser().getId());
            }
        }

        return messageDTO;
    }


    @MessageMapping("/group-message")
    public MessageDTO receiveGroupMessage(@Payload MessageDTO messageDTO) {

        if (messageDTO == null || messageDTO.getConversationId() == null) {
            throw new ErrorException("Conversation ID không hợp lệ.");
        }

        UserInformationEntity senderEntity = userService.findById(messageDTO.getSender().getId())
                .orElseThrow(() -> {
                    return new ErrorException("Không tìm thấy người gửi với ID: " + messageDTO.getSender().getId());
                });

        if (messageDTO.getReceiver() == null || messageDTO.getReceiver().isEmpty()) {
            throw new ErrorException("Thông tin người nhận bị thiếu.");
        }

        ConversationEntity conversation = conversationRepository.findById(messageDTO.getConversationId())
                .orElseThrow(() -> {
                    return new ErrorException("Không tìm thấy cuộc trò chuyện.");
                });

        conversationUserRepository.findByConversation_IdAndUser_Id(conversation.getId(), senderEntity.getId())
                .orElseThrow(() -> {
                    return new ErrorException("Người dùng không có quyền gửi tin nhắn trong cuộc trò chuyện này.");
                });

        List<ConversationUserEntity> members = conversationUserRepository.findByConversationIdAndExcludeSender(conversation.getId(), senderEntity.getId());

        UserInformationDTO senderDTO = UserInformationDTO.builder()
                .id(senderEntity.getId())
                .name(senderEntity.getLastName() + " " + senderEntity.getFirstName())
                .avatarUrl(senderEntity.getAvatarUrl())
                .build();

        List<UserInformationDTO> receivers = new ArrayList<>();

        members.forEach(member -> {
            if (!member.getUser().getId().equals(senderEntity.getId())) {
                receivers.add(UserInformationDTO.builder()
                        .id(member.getUser().getId())
                        .name(member.getUser().getLastName() + " " + member.getUser().getFirstName())
                        .avatarUrl(member.getUser().getAvatarUrl())
                        .build());
            }
        });


        processFileUrl(messageDTO);

        messageDTO.setSender(senderDTO);
        messageDTO.setDate(LocalDateTime.now());
        messageDTO.setMessageStatus(MessageStatus.PUBLIC);
        messageDTO.setReceivers(receivers);

        MessageEntity messageEntity = toEntity(messageDTO, senderEntity, null);
        messageRepository.save(messageEntity);

        for (UserInformationDTO receiver : receivers) {
            String destination = "/user/" + receiver.getId() + "/group";
            simpMessagingTemplate.convertAndSend(destination, messageDTO);
        }

        members.forEach(member -> {
            if (!member.getUser().getId().equals(senderEntity.getId())) {
                notificationService.sendUserNotification(
                        senderEntity.getId(),
                        member.getUser().getId(),
                        NotificationContent.NEW_CHAT_GROUP + conversation.getName(),
                        NotificationType.GROUP
                );
            }
        });

        for (ConversationUserEntity member : members) {
            boolean isDeleted = conversationDeleteRepository.existsByConversationIdAndUserId(conversation.getId(), member.getUser().getId());

            if (isDeleted) {
                conversationService.deleteMembersFromConversation(conversation, member.getUser().getId());
            }
        }

        return messageDTO;
    }

    @PostMapping("/recall-message-self")
    public MessageDTO recallMessageForSelf(@Payload Integer messageId, Principal principal) {
        String email = principal.getName();
        UserInformationEntity sender = userRepository.findUserInfoByEmail(email)
                .orElse(null);

        if (sender == null) {
            throw new ErrorException("Không tìm thấy người dùng");
        }

        MessageEntity message = messageRepository.findById(messageId)
                .orElse(null);

        if (message == null) {
            throw new ErrorException("Không tìm thấy tin nhắn");
        }

        if (Boolean.TRUE.equals(message.getRecalledForEveryone())) {
            throw new ErrorException("Tin nhắn này đã được thu hồi cho tất cả mọi người, không thể thu hồi chỉ từ phía bạn.");
        }

        MessageRecallEntity messageRecall = new MessageRecallEntity();
        messageRecall.setMessage(message);
        messageRecall.setUser(sender);
        messageRecallRepository.save(messageRecall);

        MessageDTO messageDTO = toDTO(message, sender.getId());

        return messageDTO;
    }


    @PostMapping("/recall-message-all")
    public MessageDTO recallMessageForAll(@Payload Integer messageId, Principal principal) {
        String email = principal.getName();
        UserInformationEntity sender = userRepository.findUserInfoByEmail(email)
                .orElse(null);

        if (sender == null) {
            throw new ErrorException("Không tìm thấy người dùng");
        }

        MessageEntity message = messageRepository.findById(messageId)
                .orElse(null);

        if (message == null) {
            throw new ErrorException("Không tìm thấy tin nhắn");
        }

        if (!message.getSender().getId().equals(sender.getId())) {
            throw new ErrorException("Bạn không có quyền thu hồi tin nhắn này.");
        }

        if (Boolean.TRUE.equals(message.getRecalledForEveryone())) {
            throw new ErrorException("Tin nhắn này đã được thu hồi cho tất cả mọi người và không thể thu hồi lại.");
        }

        message.setRecalledForEveryone(true);
        messageRepository.save(message);

        MessageDTO messageDTO = toDTO(message, sender.getId());

        return messageDTO;
    }

    @MessageMapping("/update-message")
    @PostMapping("/update-message")
    public MessageDTO updateMessage(@RequestParam Integer messageId,
                                                         @RequestParam String newContent,
                                                         Principal principal) {
        String email = principal.getName();
        UserInformationEntity sender = userRepository.findUserInfoByEmail(email)
                .orElseThrow(() -> new ErrorException("Không tìm thấy người dùng"));

        MessageEntity message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ErrorException("Không tìm thấy tin nhắn"));

        boolean isRecalledBySender = messageRecallRepository.existsByMessageIdAndUserId(messageId, sender.getId());

        if (isRecalledBySender || Boolean.TRUE.equals(message.getRecalledForEveryone())) {
            throw new ErrorException("Tin nhắn này đã được thu hồi và không thể chỉnh sửa.");
        }

        if (!message.getSender().getId().equals(sender.getId())) {
            throw new ErrorException("Bạn không có quyền sửa tin nhắn này.");
        }

        message.setMessage(newContent);
        message.setEdited(true);
        message.setEditedDate(LocalDateTime.now());
        messageRepository.save(message);

        MessageDTO messageDTO = toDTO(message, sender.getId());
        simpMessagingTemplate.convertAndSendToUser(String.valueOf(message.getReceiver().getId()), "/update-message", messageDTO);

        return messageDTO;
    }


    @RequestMapping("/chat/history")
    @PreAuthorize(SecurityConstants.PreAuthorize.USER + " or " + SecurityConstants.PreAuthorize.TUVANVIEN)
    public ResponseEntity<DataResponse<Page<MessageDTO>>> getConversationHistory(
            @RequestParam Integer conversationId,
            Principal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "date") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        String email = principal.getName();
        Optional<UserInformationEntity> userOpt = userRepository.findUserInfoByEmail(email);
        if (!userOpt.isPresent()) {
            throw new ErrorException("Không tìm thấy người dùng");
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDir), sortBy));
        Integer userId = userOpt.get().getId();
        Specification<MessageEntity> spec = Specification.where(MessageSpecification.hasConversationId(conversationId));

        Page<MessageEntity> messages = messageRepository.findAll(spec, pageable);

        Page<MessageDTO> messageDTOs = messages.map(message -> {
            return toDTO(message, userId);
        });

        DataResponse<Page<MessageDTO>> response = DataResponse.<Page<MessageDTO>>builder()
                .status("success")
                .message("Lịch sử tin nhắn của cuộc trò chuyện")
                .data(messageDTOs)
                .build();

        return ResponseEntity.ok(response);
    }

    public MessageDTO toDTO(MessageEntity entity, Integer userId) {
        boolean isRecalledBySender = messageRecallRepository.existsByMessageIdAndUserId(entity.getId(), userId);
        boolean isSender = entity.getSender().getId().equals(userId);

        String messageContent;
        String imageUrl;
        String fileUrl;

        if (Boolean.TRUE.equals(entity.getRecalledForEveryone())) {
            messageContent = "Đã thu hồi tin nhắn";
            imageUrl = "Đã thu hồi hình ảnh";
            fileUrl = "Đã thu hồi file";
        } else if (Boolean.TRUE.equals(isSender) && Boolean.TRUE.equals(isRecalledBySender)) {
            messageContent = "Đã thu hồi tin nhắn của bạn";
            imageUrl = "Đã thu hồi hình ảnh";
            fileUrl = "Đã thu hồi file";
        } else if (!Boolean.TRUE.equals(isSender) && Boolean.TRUE.equals(isRecalledBySender)) {
            messageContent = "";
            imageUrl = "";
            fileUrl = "";
        } else {
            messageContent = entity.getMessage();
            imageUrl = entity.getImageUrl();
            fileUrl = entity.getFileUrl();
        }


        return MessageDTO.builder()
                .id(entity.getId())
                .conversationId(entity.getConversationId())
                .sender(UserInformationDTO.builder()
                        .id(entity.getSender().getId())
                        .name(entity.getSender().getName())
                        .avatarUrl(entity.getSender().getAvatarUrl())
                        .build())
                .receiver(entity.getReceiver() != null ?
                        Collections.singletonList(
                                UserInformationDTO.builder()
                                        .id(entity.getReceiver().getId())
                                        .name(entity.getReceiver().getName())
                                        .avatarUrl(entity.getReceiver().getAvatarUrl())
                                        .build()
                        ) :
                        Collections.emptyList()
                )
                .message(messageContent)
                .imageUrl(imageUrl)
                .fileUrl(fileUrl)
                .date(entity.getDate())
                .messageStatus(entity.getMessageStatus())
                .recalledForEveryone(entity.getRecalledForEveryone())
                .recalledBySender(isRecalledBySender)
                .edited(entity.getEdited())
                .editedDate(entity.getEditedDate())
                .build();
    }


    public MessageEntity toEntity(MessageDTO dto, UserInformationEntity sender, UserInformationEntity receiver) {
        return MessageEntity.builder()
                .id(dto.getId())
                .conversationId(dto.getConversationId())
                .sender(sender)
                .receiver(receiver)
                .message(dto.getMessage())
                .imageUrl(dto.getImageUrl())
                .fileUrl(dto.getFileUrl())
                .typeUrl(dto.getTypeUrl())
                .date(dto.getDate())
                .messageStatus(dto.getMessageStatus())
                .recalledForEveryone(dto.getRecalledForEveryone())
                .edited(dto.getEdited())
                .editedDate(dto.getEditedDate())
                .build();
    }
}
