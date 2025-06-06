package HcmuteConsultantServer.model.payload.mapper.actor;

import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import HcmuteConsultantServer.constant.enums.QuestionFilterStatus;
import HcmuteConsultantServer.model.entity.*;
import HcmuteConsultantServer.model.payload.dto.actor.DeletionLogDTO;
import HcmuteConsultantServer.model.payload.dto.actor.ForwardQuestionDTO;
import HcmuteConsultantServer.model.payload.dto.actor.MyQuestionDTO;
import HcmuteConsultantServer.model.payload.dto.actor.QuestionDTO;
import HcmuteConsultantServer.model.payload.request.CreateFollowUpQuestionRequest;
import HcmuteConsultantServer.model.payload.request.CreateQuestionRequest;
import HcmuteConsultantServer.repository.actor.AnswerRepository;
import HcmuteConsultantServer.repository.actor.QuestionRepository;
import HcmuteConsultantServer.repository.admin.DepartmentRepository;
import HcmuteConsultantServer.repository.admin.FieldRepository;
import HcmuteConsultantServer.repository.admin.RoleAskRepository;
import HcmuteConsultantServer.repository.admin.UserRepository;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface QuestionMapper {

    @Mapping(source = "id", target = "id")
    @Mapping(source = "title", target = "title")
    @Mapping(source = "content", target = "content")
    @Mapping(source = "createdAt", target = "createdAt")
    @Mapping(source = "views", target = "views")
    @Mapping(source = "fileName", target = "fileName")
    @Mapping(source = "user.firstName", target = "askerFirstname")
    @Mapping(source = "user.lastName", target = "askerLastname")
    @Mapping(source = "user.avatarUrl", target = "askerAvatarUrl")
    @Mapping(source = "user.id", target = "askerId")
    @Mapping(source = "department.id", target = "department.id")
    @Mapping(source = "department.name", target = "department.name")
    @Mapping(source = "field.id", target = "field.id")
    @Mapping(source = "field.name", target = "field.name")
    @Mapping(source = "roleAsk.id", target = "roleAsk.id")
    @Mapping(source = "roleAsk.name", target = "roleAsk.name")
    @Mapping(target = "filterStatus", source = "question", qualifiedByName = "determineFilterStatus")
    @Mapping(target = "answerId", source = "question", qualifiedByName = "getAnswerId")
    @Mapping(target = "answerTitle", source = "question", qualifiedByName = "getAnswerTitle")
    @Mapping(target = "answerContent", source = "question", qualifiedByName = "getAnswerContent")
    @Mapping(target = "answerUserEmail", source = "question", qualifiedByName = "getAnswerUserEmail")
    @Mapping(target = "answerUserFirstname", source = "question", qualifiedByName = "getAnswerUserFirstname")
    @Mapping(target = "answerUserLastname", source = "question", qualifiedByName = "getAnswerUserLastname")
    @Mapping(target = "answerCreatedAt", source = "question", qualifiedByName = "getAnswerCreatedAt")
    @Mapping(target = "answerAvatarUrl", source = "question", qualifiedByName = "getAnswerAvatarUrl")
    @Mapping(target = "answerFileName", source = "question", qualifiedByName = "getAnswerFileName")
    MyQuestionDTO mapToMyQuestionDTOs(QuestionEntity question, @Context AnswerRepository answerRepository);

    @Mapping(source = "question.id", target = "id")
    @Mapping(source = "question.title", target = "title")
    @Mapping(source = "question.content", target = "content")
    @Mapping(source = "question.createdAt", target = "createdAt")
    @Mapping(source = "question.views", target = "views")
    @Mapping(source = "question.fileName", target = "fileName")
    @Mapping(source = "question.user.firstName", target = "askerFirstname")
    @Mapping(source = "question.user.lastName", target = "askerLastname")
    @Mapping(source = "question.user.avatarUrl", target = "askerAvatarUrl")
    @Mapping(source = "question.user.id", target = "askerId")
    @Mapping(source = "question.department.id", target = "department.id")
    @Mapping(source = "question.department.name", target = "department.name")
    @Mapping(source = "question.field.id", target = "field.id")
    @Mapping(source = "question.field.name", target = "field.name")
    @Mapping(source = "question.roleAsk.id", target = "roleAsk.id")
    @Mapping(source = "question.roleAsk.name", target = "roleAsk.name")
    @Mapping(target = "filterStatus", source = "question", qualifiedByName = "determineFilterStatus")
    @Mapping(target = "answerId", source = "question", qualifiedByName = "getAnswerId")
    @Mapping(target = "answerTitle", source = "question", qualifiedByName = "getAnswerTitle")
    @Mapping(target = "answerContent", source = "question", qualifiedByName = "getAnswerContent")
    @Mapping(target = "answerUserEmail", source = "question", qualifiedByName = "getAnswerUserEmail")
    @Mapping(target = "answerUserFirstname", source = "question", qualifiedByName = "getAnswerUserFirstname")
    @Mapping(target = "answerUserLastname", source = "question", qualifiedByName = "getAnswerUserLastname")
    @Mapping(target = "answerCreatedAt", source = "question", qualifiedByName = "getAnswerCreatedAt")
    @Mapping(target = "answerAvatarUrl", source = "question", qualifiedByName = "getAnswerAvatarUrl")
    @Mapping(target = "answerFileName", source = "question", qualifiedByName = "getAnswerFileName")
    @Mapping(source = "forwardQuestion", target = "forwardQuestionDTO", qualifiedByName = "mapForwardQuestion")
    MyQuestionDTO mapToMyQuestionDTO(QuestionEntity question, Optional<ForwardQuestionEntity> forwardQuestion, @Context AnswerRepository answerRepository);   @Named("mapForwardQuestion")

    @Mapping(source = "question", target = "followUpQuestions", qualifiedByName = "mapFollowUpQuestions")
    MyQuestionDTO mapToMyQuestionDTO(QuestionEntity question, @Context QuestionRepository questionRepository, @Context Set<Integer> processedQuestionIds);

    @Mapping(source = "questionDTO.title", target = "title")
    @Mapping(source = "questionDTO.content", target = "content")
    @Mapping(source = "questionDTO.statusPublic", target = "statusPublic")
    @Mapping(source = "questionDTO.views", target = "views")
    @Mapping(source = "userId", target = "user", qualifiedByName = "mapUserWithDetails")
    @Mapping(source = "questionDTO.departmentId", target = "department", qualifiedByName = "mapDepartment")
    @Mapping(source = "questionDTO.fieldId", target = "field", qualifiedByName = "mapField")
    @Mapping(source = "questionDTO.roleAskId", target = "roleAsk", qualifiedByName = "mapRoleAsk")
    @Mapping(source = "questionDTO.fileName", target = "fileName")
    @Mapping(target = "createdAt", expression = "java(java.time.LocalDate.now())")
        // Sử dụng expression để gán giá trị hiện tại
    QuestionEntity mapDTOToEntity(QuestionDTO questionDTO, Integer userId, @Context UserRepository userRepository, @Context DepartmentRepository departmentRepository, @Context FieldRepository fieldRepository, @Context RoleAskRepository roleAskRepository);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "department.id", target = "departmentId")
    @Mapping(source = "field.id", target = "fieldId")
    @Mapping(source = "roleAsk.id", target = "roleAskId")
    @Mapping(source = "title", target = "title")
    @Mapping(source = "content", target = "content")
    @Mapping(source = "user.firstName", target = "firstName")
    @Mapping(source = "user.lastName", target = "lastName")
    @Mapping(source = "statusPublic", target = "statusPublic")
    @Mapping(source = "fileName", target = "fileName")
    @Mapping(source = "statusApproval", target = "statusApproval")
    QuestionDTO mapEntityToDTO(QuestionEntity question);

    @Mapping(source = "request.departmentId", target = "departmentId")
    @Mapping(source = "request.fieldId", target = "fieldId")
    @Mapping(source = "request.roleAskId", target = "roleAskId")
    @Mapping(source = "request.title", target = "title")
    @Mapping(source = "request.content", target = "content")
    @Mapping(source = "request.firstName", target = "firstName")
    @Mapping(source = "request.lastName", target = "lastName")
    @Mapping(source = "request.statusPublic", target = "statusPublic")
    @Mapping(source = "fileName", target = "fileName")
    @Mapping(target = "statusApproval", constant = "false")
    QuestionDTO mapRequestToDTO(CreateQuestionRequest request, String fileName);

    @Mapping(source = "request.departmentId", target = "departmentId")
    @Mapping(source = "request.fieldId", target = "fieldId")
    @Mapping(source = "request.roleAskId", target = "roleAskId")
    @Mapping(source = "request.title", target = "title")
    @Mapping(source = "request.content", target = "content")
    @Mapping(source = "request.firstName", target = "firstName")
    @Mapping(source = "request.lastName", target = "lastName")
    @Mapping(source = "request.studentCode", target = "studentCode")
    @Mapping(source = "request.statusPublic", target = "statusPublic")
    @Mapping(source = "fileName", target = "fileName")
    QuestionDTO mapRequestToDTO(CreateFollowUpQuestionRequest request, String fileName);

    @Mapping(source = "question.id", target = "questionId")
    @Mapping(source = "question.title", target = "questionTitle")
    @Mapping(source = "reason", target = "reason")
    @Mapping(source = "deletedBy", target = "deletedBy")
    @Mapping(source = "deletedAt", target = "deletedAt")
    DeletionLogDTO mapToDeletionLogDTO(DeletionLogEntity deletionLog);


    @Named("determineFilterStatus")
    default List<String> determineFilterStatus(QuestionEntity question) {
        List<String> statuses = new ArrayList<>();

        if (Boolean.TRUE.equals(question.getStatusDelete())) {
            if (Boolean.TRUE.equals(question.getStatusPublic())) {
                statuses.add(QuestionFilterStatus.DELETED.getDisplayName());
                statuses.add(QuestionFilterStatus.PUBLIC.getDisplayName());
            } else {
                statuses.add(QuestionFilterStatus.DELETED.getDisplayName());
                statuses.add(QuestionFilterStatus.PRIVATE.getDisplayName());
            }
        } else {
            if (Boolean.TRUE.equals(question.getStatusApproval())) {
                statuses.add(QuestionFilterStatus.ANSWERED.getDisplayName());
                if (Boolean.TRUE.equals(question.getStatusPublic())) {
                    statuses.add(QuestionFilterStatus.PUBLIC.getDisplayName());
                } else {
                    statuses.add(QuestionFilterStatus.PRIVATE.getDisplayName());
                }
            } else {
                statuses.add(QuestionFilterStatus.NOT_ANSWERED.getDisplayName());
                if (Boolean.TRUE.equals(question.getStatusPublic())) {
                    statuses.add(QuestionFilterStatus.PUBLIC.getDisplayName());
                } else {
                    statuses.add(QuestionFilterStatus.PRIVATE.getDisplayName());
                }
            }
        }

        if (question.getForwardQuestions() != null && !question.getForwardQuestions().isEmpty()) {
            for (ForwardQuestionEntity forward : question.getForwardQuestions()) {
                if (Boolean.TRUE.equals(forward.getStatusForward())) {
                    statuses.add("Câu hỏi chuyển tiếp");
                    break;
                }
            }
        }

        return statuses;
    }


    @Named("getAnswerId")
    default Integer getAnswerId(QuestionEntity question, @Context AnswerRepository answerRepository) {
        return answerRepository.findFirstAnswerByQuestionId(question.getId())
                .map(AnswerEntity::getId)
                .orElse(null);
    }

    @Named("getAnswerTitle")
    default String getAnswerTitle(QuestionEntity question, @Context AnswerRepository answerRepository) {
        return answerRepository.findFirstAnswerByQuestionId(question.getId())
                .map(AnswerEntity::getTitle)
                .orElse(null);
    }

    @Named("getAnswerContent")
    default String getAnswerContent(QuestionEntity question, @Context AnswerRepository answerRepository) {
        return answerRepository.findFirstAnswerByQuestionId(question.getId())
                .map(AnswerEntity::getContent)
                .orElse(null);
    }

    @Named("getAnswerUserEmail")
    default String getAnswerUserEmail(QuestionEntity question, @Context AnswerRepository answerRepository) {
        return answerRepository.findFirstAnswerByQuestionId(question.getId())
                .flatMap(answer -> Optional.ofNullable(answer.getUser()).map(user -> user.getAccount().getEmail()))
                .orElse(null);
    }

    @Named("getAnswerUserFirstname")
    default String getAnswerUserFirstname(QuestionEntity question, @Context AnswerRepository answerRepository) {
        return answerRepository.findFirstAnswerByQuestionId(question.getId())
                .flatMap(answer -> Optional.ofNullable(answer.getUser()).map(user -> user.getFirstName()))
                .orElse(null);
    }

    @Named("getAnswerUserLastname")
    default String getAnswerUserLastname(QuestionEntity question, @Context AnswerRepository answerRepository) {
        return answerRepository.findFirstAnswerByQuestionId(question.getId())
                .flatMap(answer -> Optional.ofNullable(answer.getUser()).map(user -> user.getLastName()))
                .orElse(null);
    }

    @Named("getAnswerCreatedAt")
    default LocalDate getAnswerCreatedAt(QuestionEntity question, @Context AnswerRepository answerRepository) {
        return answerRepository.findFirstAnswerByQuestionId(question.getId())
                .map(AnswerEntity::getCreatedAt)
                .orElse(null);
    }

    @Named("getAnswerAvatarUrl")
    default String getAnswerAvatarUrl(QuestionEntity question, @Context AnswerRepository answerRepository) {
        return answerRepository.findFirstAnswerByQuestionId(question.getId())
                .flatMap(answer -> Optional.ofNullable(answer.getUser()).map(user -> user.getAvatarUrl()))
                .orElse(null);
    }

    @Named("getAnswerFileName")
    default String getAnswerFileName(QuestionEntity question, @Context AnswerRepository answerRepository) {
        return answerRepository.findFirstAnswerByQuestionId(question.getId())
                .map(AnswerEntity::getFile)
                .orElse(null);
    }

    @Named("mapFollowUpQuestions")
    default List<MyQuestionDTO> mapFollowUpQuestions(QuestionEntity question, @Context QuestionRepository questionRepository, @Context Set<Integer> processedQuestionIds) {
        if (processedQuestionIds.contains(question.getId())) {
            return null; // Tránh lặp vô hạn trong đệ quy
        }

        processedQuestionIds.add(question.getId());

        return questionRepository.findFollowUpQuestionsByParentId(question.getId())
                .stream()
                .map(followUpQuestion -> mapToMyQuestionDTO(followUpQuestion, questionRepository, processedQuestionIds))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Named("mapUserWithDetails")
    default UserInformationEntity mapUserWithDetails(Integer userId, @Context UserRepository userRepository) {
        UserInformationEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user;
    }

    @Named("mapDepartment")
    default DepartmentEntity mapDepartment(Integer departmentId, @Context DepartmentRepository departmentRepository) {
        return departmentRepository.findById(departmentId)
                .orElseThrow(() -> new RuntimeException("Department not found with id: " + departmentId));
    }

    @Named("mapField")
    default FieldEntity mapField(Integer fieldId, @Context FieldRepository fieldRepository) {
        return fieldRepository.findById(fieldId)
                .orElseThrow(() -> new RuntimeException("Field not found with id: " + fieldId));
    }

    @Named("mapRoleAsk")
    default RoleAskEntity mapRoleAsk(Integer roleAskId, @Context RoleAskRepository roleAskRepository) {
        return roleAskRepository.findById(roleAskId)
                .orElseThrow(() -> new RuntimeException("RoleAsk not found with id: " + roleAskId));
    }

    @Named("mapForwardQuestion")
    public static ForwardQuestionDTO mapForwardQuestion(Optional<ForwardQuestionEntity> forwardQuestion) {
        if (forwardQuestion.isPresent()) {
            ForwardQuestionEntity fq = forwardQuestion.get();

            return ForwardQuestionDTO.builder()
                    .id(fq.getId())
                    .title(fq.getTitle())
                    .fromDepartment(new ForwardQuestionDTO.DepartmentDTO(
                            fq.getFromDepartment().getId(),
                            fq.getFromDepartment().getName()
                    ))
                    .toDepartment(new ForwardQuestionDTO.DepartmentDTO(
                            fq.getToDepartment().getId(),
                            fq.getToDepartment().getName()
                    ))
                    .consultant(new ForwardQuestionDTO.ConsultantDTO(
                            fq.getConsultant().getId(),
                            fq.getConsultant().getLastName() +" "+ fq.getConsultant().getFirstName()
                    ))
                    .statusForward(fq.getStatusForward())
                    .createdBy(fq.getCreatedBy().getId())
                    .questionId(fq.getQuestion().getId())
                    .createdAt(fq.getCreatedAt())
                    .build();
        } else {
            return null;
        }
    }


}
