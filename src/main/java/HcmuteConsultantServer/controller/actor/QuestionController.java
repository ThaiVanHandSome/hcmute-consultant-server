package HcmuteConsultantServer.controller.actor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import HcmuteConsultantServer.constant.SecurityConstants;
import HcmuteConsultantServer.constant.enums.NotificationContent;
import HcmuteConsultantServer.constant.enums.NotificationType;
import HcmuteConsultantServer.model.entity.QuestionEntity;
import HcmuteConsultantServer.model.entity.UserInformationEntity;
import HcmuteConsultantServer.model.exception.Exceptions;
import HcmuteConsultantServer.model.exception.Exceptions.ErrorException;
import HcmuteConsultantServer.model.payload.dto.actor.DeletionLogDTO;
import HcmuteConsultantServer.model.payload.dto.actor.MyQuestionDTO;
import HcmuteConsultantServer.model.payload.dto.actor.QuestionDTO;
import HcmuteConsultantServer.model.payload.request.CreateQuestionRequest;
import HcmuteConsultantServer.model.payload.request.UpdateQuestionRequest;
import HcmuteConsultantServer.model.payload.response.DataResponse;
import HcmuteConsultantServer.repository.actor.QuestionRepository;
import HcmuteConsultantServer.repository.admin.RoleAskRepository;
import HcmuteConsultantServer.repository.admin.UserRepository;
import HcmuteConsultantServer.service.interfaces.actor.IQuestionService;
import HcmuteConsultantServer.service.interfaces.common.INotificationService;
import HcmuteConsultantServer.service.interfaces.common.IUserService;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("${base.url}")
public class QuestionController {

    @Autowired
    private IQuestionService questionService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleAskRepository roleAskRepository;

    @Autowired
    private IUserService userService;

    @Autowired
    private INotificationService notificationService;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    @PreAuthorize(SecurityConstants.PreAuthorize.USER)
    @PostMapping(value = "/user/question/create", consumes = {"multipart/form-data"})
    public DataResponse<QuestionDTO> createQuestion(Principal principal,
                                                    @RequestParam("departmentId") Integer departmentId, @RequestParam("fieldId") Integer fieldId,
                                                    @RequestParam("roleAskId") Integer roleAskId, @RequestParam("title") String title,
                                                    @RequestParam("content") String content, @RequestParam("firstName") String firstName,
                                                    @RequestParam("lastName") String lastName, @RequestParam("studentCode") String studentCode,
                                                    @RequestParam("statusPublic") Boolean statusPublic, @RequestPart(name = "file", required = false) MultipartFile file) {

        String email = principal.getName();
        
        Optional<UserInformationEntity> userOpt = userRepository.findUserInfoByEmail(email);
        if (!userOpt.isPresent()) {
            throw new ErrorException("Không tìm thấy người dùng");
        }

        UserInformationEntity user = userOpt.get();
        List<UserInformationEntity> consultants = userService.findConsultantsByDepartmentId(departmentId);
        if (consultants.isEmpty()) {
            throw new ErrorException("Không tìm thấy tư vấn viên nào thuộc phòng ban này.");
        }
        CreateQuestionRequest questionRequest = CreateQuestionRequest.builder().departmentId(departmentId)
                .fieldId(fieldId).roleAskId(roleAskId).title(title).content(content).firstName(firstName)
                .lastName(lastName).statusPublic(statusPublic).file(file).build();

        QuestionDTO questionDTO = questionService.createQuestion(questionRequest, user.getId()).getData();

        for (UserInformationEntity consultant : consultants) {
            notificationService.sendUserNotification(
                    user.getId(),
                    consultant.getId(),
                    NotificationContent.NEW_QUESTION.formatMessage(user.getLastName() + " " + user.getFirstName()),
                    NotificationType.TUVANVIEN
            );

        }

        return DataResponse.<QuestionDTO>builder().status("success").message("Đặt câu hỏi thành công.")
                .data(questionDTO).build();
    }

    @PreAuthorize(SecurityConstants.PreAuthorize.USER)
    @PutMapping(value = "/user/question/update", consumes = {"multipart/form-data"})
    public DataResponse<QuestionDTO> updateQuestion(@RequestParam("questionId") Integer questionId,
                                                    @RequestParam("departmentId") Integer departmentId, @RequestParam("fieldId") Integer fieldId,
                                                    @RequestParam("roleAskId") Integer roleAskId, @RequestParam("title") String title,
                                                    @RequestParam("content") String content, @RequestParam("firstName") String firstName,
                                                    @RequestParam("lastName") String lastName, @RequestParam("studentCode") String studentCode,
                                                    @RequestParam("statusPublic") Boolean statusPublic,
                                                    @RequestPart(value = "file", required = false) MultipartFile file, Principal principal) {

        String email = principal.getName();
        
        Optional<UserInformationEntity> userOpt = userRepository.findUserInfoByEmail(email);
        if (!userOpt.isPresent()) {
            throw new ErrorException("Không tìm thấy người dùng");
        }

        UpdateQuestionRequest questionRequest = UpdateQuestionRequest.builder().departmentId(departmentId)
                .fieldId(fieldId).roleAskId(roleAskId).title(title).content(content).firstName(firstName)
                .lastName(lastName).studentCode(studentCode).statusPublic(statusPublic).file(file).build();

        return questionService.updateQuestion(questionId, questionRequest);
    }

    @PreAuthorize(SecurityConstants.PreAuthorize.USER)
    @DeleteMapping("/user/question/delete")
    public DataResponse<Void> deleteQuestion(@RequestParam("id") Integer questionId, Principal principal) {
        String username = principal.getName();

        return questionService.deleteQuestion(questionId, username);
    }

    @PreAuthorize(SecurityConstants.PreAuthorize.USER)
    @PostMapping(value = "/user/question/create-follow-up", consumes = {"multipart/form-data"})
    public DataResponse<QuestionDTO> askFollowUpQuestion(Principal principal,
                                                         @RequestParam("parentQuestionId") Integer parentQuestionId, @RequestParam("title") String title,
                                                         @RequestParam("content") String content,
                                                         @RequestPart(value = "file", required = false) MultipartFile file) {

        String email = principal.getName();
        
        Optional<UserInformationEntity> userOpt = userRepository.findUserInfoByEmail(email);
        if (!userOpt.isPresent()) {
            throw new ErrorException("Không tìm thấy người dùng");
        }

        UserInformationEntity user = userOpt.get();
        return questionService.askFollowUpQuestion(parentQuestionId, title, content, file, user.getId());
    }

    @PreAuthorize(SecurityConstants.PreAuthorize.USER + " or " + SecurityConstants.PreAuthorize.TUVANVIEN + " or " + SecurityConstants.PreAuthorize.TRUONGBANTUVAN + " or " + SecurityConstants.PreAuthorize.ADMIN)
    @GetMapping("/question-answer/list")
    public DataResponse<Page<MyQuestionDTO>> getQuestions(Principal principal,
                                                          @RequestParam Boolean statusApproval,
                                                          @RequestParam(required = false) String title,
                                                          @RequestParam(required = false) Integer departmentId,
                                                          @RequestParam(required = false) String status,
                                                          @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                                          @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                                                          @RequestParam(defaultValue = "0") int page,
                                                          @RequestParam(defaultValue = "10") int size,
                                                          @RequestParam(defaultValue = "createdAt") String sortBy,
                                                          @RequestParam(defaultValue = "desc") String sortDir) {

        String email = principal.getName();
        UserInformationEntity user = userRepository.findUserInfoByEmail(email)
                .orElseThrow(() -> new ErrorException("Không tìm thấy người dùng"));

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDir), sortBy));

        Page<MyQuestionDTO> questions = questionService.getQuestionAnswerByRole(statusApproval, user, title, status, departmentId, startDate, endDate, pageable);

        return DataResponse.<Page<MyQuestionDTO>>builder()
                .status("success")
                .message("Lấy câu hỏi thành công.")
                .data(questions)
                .build();
    }


    @PreAuthorize(SecurityConstants.PreAuthorize.TUVANVIEN + " or " + SecurityConstants.PreAuthorize.TRUONGBANTUVAN + " or " + SecurityConstants.PreAuthorize.ADMIN)
    @DeleteMapping("/question/delete")
    public DataResponse<String> deleteQuestion(@RequestParam("questionId") Integer questionId,
                                               @RequestParam(required = false) String reason,
                                               Principal principal) {

        if ((reason == null || reason.trim().isEmpty()) &&
                (!SecurityConstants.Role.ADMIN.equals(principal.getName()))) {
            throw new ErrorException("Lý do xóa là bắt buộc đối với tư vấn viên và trưởng ban tư vấn.");
        }

        String email = principal.getName();
        UserInformationEntity user = userRepository.findUserInfoByEmail(email)
                .orElseThrow(() -> new ErrorException("Không tìm thấy người dùng"));

        Optional<QuestionEntity> questionOpt = questionRepository.findById(questionId);
        if (questionOpt.isEmpty()) {
            throw new ErrorException("Câu hỏi không tồn tại.");
        }

        QuestionEntity question = questionOpt.get();
        UserInformationEntity questionOwner = question.getUser();
        Integer userDepartmentId = user.getAccount().getDepartment() != null ? user.getAccount().getDepartment().getId() : null;
        Integer questionDepartmentId = question.getDepartment() != null ? question.getDepartment().getId() : null;

        String userRole = user.getAccount().getRole().getName();

        switch (userRole) {
            case SecurityConstants.Role.TUVANVIEN:
            case SecurityConstants.Role.TRUONGBANTUVAN:
                if (userDepartmentId == null || !userDepartmentId.equals(questionDepartmentId)) {
                    throw new ErrorException("Bạn chỉ có thể xóa câu hỏi trong phòng ban của mình.");
                }
                if (reason == null || reason.trim().isEmpty()) {
                    throw new ErrorException("Lý do xóa là bắt buộc cho vai trò của bạn.");
                }
                break;
            case SecurityConstants.Role.ADMIN:
                if (reason == null || reason.trim().isEmpty()) {
                    throw new ErrorException("Lý do xóa là bắt buộc cho vai trò của bạn.");
                }
                break;
            default:
                throw new ErrorException("Bạn không có quyền thực hiện hành động này.");
        }

        questionService.deleteQuestion(questionId, reason, email);

        if (userRole.equals(SecurityConstants.Role.TUVANVIEN) || userRole.equals(SecurityConstants.Role.TRUONGBANTUVAN)) {
            notificationService.sendUserNotification(
                    user.getId(),
                    questionOwner.getId(),
                    NotificationContent.DELETE_QUESTION.formatMessage(user.getLastName() + " " + user.getFirstName()),
                    NotificationType.USER
            );
        }

        return DataResponse.<String>builder()
                .status("success")
                .message("Câu hỏi đã được xóa thành công.")
                .build();
    }

    @PreAuthorize(SecurityConstants.PreAuthorize.USER + " or " + SecurityConstants.PreAuthorize.TUVANVIEN + " or " + SecurityConstants.PreAuthorize.TRUONGBANTUVAN + " or " + SecurityConstants.PreAuthorize.ADMIN)
    @GetMapping("/question/detail")
    public DataResponse<MyQuestionDTO> getQuestionDetail(
            @RequestParam("questionId") Integer questionId,
            Principal principal) {
        String email = principal.getName();
        Optional<UserInformationEntity> userOpt = userRepository.findUserInfoByEmail(email);
        if (!userOpt.isPresent()) {
            throw new ErrorException("Không tìm thấy người dùng");
        }
        UserInformationEntity user = userOpt.get();
        MyQuestionDTO questionDetail = questionService.getQuestionDetail(user.getId(), questionId, user);
        return DataResponse.<MyQuestionDTO>builder()
                .status("success")
                .data(questionDetail)
                .build();
    }

    @PreAuthorize(SecurityConstants.PreAuthorize.TUVANVIEN + " or " + SecurityConstants.PreAuthorize.TRUONGBANTUVAN + " or " + SecurityConstants.PreAuthorize.ADMIN)
    @GetMapping("/deletion-log/list")
    public ResponseEntity<DataResponse<Page<DeletionLogDTO>>> getDeletionLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "deletedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            Principal principal) {

        String email = principal.getName();
        UserInformationEntity user = userRepository.findUserInfoByEmail(email)
                .orElseThrow(() -> new ErrorException("Không tìm thấy người dùng"));

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDir), sortBy));
        Page<DeletionLogDTO> logs = questionService.getDeletionLogs(user, pageable);

        return ResponseEntity.ok(
                DataResponse.<Page<DeletionLogDTO>>builder()
                        .status("success")
                        .data(logs).build()
        );
    }


    @PreAuthorize(SecurityConstants.PreAuthorize.TUVANVIEN + " or " + SecurityConstants.PreAuthorize.TRUONGBANTUVAN + " or " + SecurityConstants.PreAuthorize.ADMIN)
    @GetMapping("/deletion-log/detail")
    public ResponseEntity<DataResponse<DeletionLogDTO>> getDeletionLogDetail(
            @RequestParam Integer questionId,
            Principal principal) {

        String email = principal.getName();

        UserInformationEntity user = userRepository.findUserInfoByEmail(email)
                .orElseThrow(() -> new ErrorException("Không tìm thấy người dùng"));

        DeletionLogDTO log = questionService.getDeletionLogDetail(user, questionId);

        return ResponseEntity.ok(
                DataResponse.<DeletionLogDTO>builder()
                        .status("success")
                        .data(log)
                        .build()
        );
    }



}
