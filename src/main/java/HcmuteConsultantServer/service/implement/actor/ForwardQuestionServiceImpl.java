package HcmuteConsultantServer.service.implement.actor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import HcmuteConsultantServer.constant.SecurityConstants;
import HcmuteConsultantServer.model.entity.DepartmentEntity;
import HcmuteConsultantServer.model.entity.ForwardQuestionEntity;
import HcmuteConsultantServer.model.entity.QuestionEntity;
import HcmuteConsultantServer.model.entity.UserInformationEntity;
import HcmuteConsultantServer.model.exception.Exceptions.ErrorException;
import HcmuteConsultantServer.model.payload.dto.actor.ForwardQuestionDTO;
import HcmuteConsultantServer.model.payload.mapper.actor.ForwardQuestionMapper;
import HcmuteConsultantServer.model.payload.request.ForwardQuestionRequest;
import HcmuteConsultantServer.model.payload.request.UpdateForwardQuestionRequest;
import HcmuteConsultantServer.model.payload.response.DataResponse;
import HcmuteConsultantServer.repository.actor.ForwardQuestionRepository;
import HcmuteConsultantServer.repository.actor.QuestionRepository;
import HcmuteConsultantServer.repository.admin.DepartmentRepository;
import HcmuteConsultantServer.repository.admin.UserRepository;
import HcmuteConsultantServer.service.interfaces.actor.IForwardQuestionService;
import HcmuteConsultantServer.specification.actor.ForwardQuestionSpecification;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.util.Optional;

@Service
public class ForwardQuestionServiceImpl implements IForwardQuestionService {

    @Autowired
    private ForwardQuestionRepository forwardQuestionRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private UserRepository consultantRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ForwardQuestionMapper forwardQuestionMapper;

    @Override
    @Transactional
    public DataResponse<ForwardQuestionDTO> forwardQuestion(ForwardQuestionRequest forwardQuestionRequest, String email) {
        Optional<UserInformationEntity> userOpt = userRepository.findUserInfoByEmail(email);
        UserInformationEntity user = userOpt.orElseThrow(() -> new ErrorException("Người dùng không tồn tại."));

        if (!SecurityConstants.Role.TUVANVIEN.equals(user.getAccount().getRole().getName())) {
            throw new ErrorException("Bạn không có quyền thực hiện chức năng này.");
        }

        DepartmentEntity fromDepartment = user.getAccount().getDepartment();
        if (fromDepartment == null) {
            throw new ErrorException("Phòng ban của tư vấn viên không tồn tại.");
        }

        DepartmentEntity toDepartment = departmentRepository.findById(forwardQuestionRequest.getToDepartmentId())
                .orElseThrow(() -> new ErrorException("Phòng ban chuyển đến không tồn tại"));

        if (fromDepartment.getId().equals(toDepartment.getId())) {
            throw new ErrorException("Không thể chuyển tiếp câu hỏi đến cùng một phòng ban.");
        }

        QuestionEntity question = questionRepository.findById(forwardQuestionRequest.getQuestionId())
                .orElseThrow(() -> new ErrorException("Câu hỏi không tồn tại"));

        UserInformationEntity consultant = userRepository.findById(forwardQuestionRequest.getConsultantId())
                .orElseThrow(() -> new ErrorException("Tư vấn viên không tồn tại"));

        if (!consultant.getAccount().getDepartment().equals(toDepartment)) {
            throw new ErrorException("Tư vấn viên không thuộc phòng ban chuyển đến.");
        }

        ForwardQuestionEntity forwardQuestion = ForwardQuestionEntity.builder()
                .fromDepartment(fromDepartment)
                .toDepartment(toDepartment)
                .question(question)
                .title("Đã chuyển tiếp câu hỏi từ " + fromDepartment.getName() + " cho " + toDepartment.getName())
                .statusForward(true)
                .createdAt(LocalDate.now())
                .consultant(consultant)
                .createdBy(user)
                .createdAt(LocalDate.now())
                .build();

        forwardQuestionRepository.save(forwardQuestion);

        question.setDepartment(toDepartment);
        questionRepository.save(question);

        ForwardQuestionDTO forwardQuestionDTO = forwardQuestionMapper.mapToDTO(forwardQuestion, forwardQuestionRequest.getConsultantId());

        return DataResponse.<ForwardQuestionDTO>builder()
                .status("success")
                .message("Câu hỏi đã được chuyển tiếp thành công.")
                .data(forwardQuestionDTO)
                .build();
    }


    @Override
    public Page<ForwardQuestionDTO> getForwardQuestionByRole(String title, LocalDate startDate, LocalDate endDate, Pageable pageable, Integer userId, Integer departmentId, boolean isAdmin, boolean isAdvisor) {

        Specification<ForwardQuestionEntity> spec = Specification.where(null);
        if (!isAdmin) {
            if (isAdvisor) {
                spec = spec.and(
                        ForwardQuestionSpecification.hasFromDepartment(departmentId)
                                .or(ForwardQuestionSpecification.hasFromDepartment(departmentId))
                                .or(ForwardQuestionSpecification.hasCreatedBy(userId))
                );
            } else {
                spec = spec.and(
                        ForwardQuestionSpecification.hasCreatedBy(userId)
                                .or(ForwardQuestionSpecification.hasFromDepartment(departmentId))
                );
            }
        }
        if (title != null) {
            spec = spec.and(ForwardQuestionSpecification.hasTitle(title));
        }
        if (startDate != null && endDate != null) {
            spec = spec.and(ForwardQuestionSpecification.hasExactDateRange(startDate, endDate));
        } else if (startDate != null) {
            spec = spec.and(ForwardQuestionSpecification.hasExactStartDate(startDate));
        } else if (endDate != null) {
            spec = spec.and(ForwardQuestionSpecification.hasDateBefore(endDate));
        }

        Page<ForwardQuestionEntity> forwardQuestions = forwardQuestionRepository.findAll(spec, pageable);

        return forwardQuestions.map(forwardQuestion -> forwardQuestionMapper.mapToDTO(forwardQuestion, forwardQuestion.getConsultant().getId()));
    }


    @Override
    public ForwardQuestionDTO updateForwardQuestionByRole(Integer forwardQuestionId, UpdateForwardQuestionRequest forwardQuestionRequest, Integer userId, Integer departmentId, boolean isAdmin, boolean isAdvisor) {

        ForwardQuestionEntity forwardQuestion = forwardQuestionRepository.findById(forwardQuestionId)
                .orElseThrow(() -> new ErrorException("Không tìm thấy câu hỏi chuyển tiếp"));

        if (!isAdmin) {
            if (isAdvisor) {
                if ((!forwardQuestion.getFromDepartment().getId().equals(departmentId)
                        && !forwardQuestion.getCreatedBy().getId().equals(userId))) {
                    throw new ErrorException("Bạn không có quyền cập nhật câu hỏi này.");
                }
            } else {
                if (!forwardQuestion.getCreatedBy().getId().equals(userId)) {
                    throw new ErrorException("Bạn chỉ có thể cập nhật câu hỏi do bạn tạo.");
                }
            }
        }

        forwardQuestion.setTitle(forwardQuestionRequest.getTitle());
        DepartmentEntity toDepartment = departmentRepository.findById(forwardQuestionRequest.getToDepartmentId())
                .orElseThrow(() -> new ErrorException("Phòng ban không tồn tại"));
        forwardQuestion.setToDepartment(toDepartment);

        QuestionEntity question = questionRepository.findById(forwardQuestionRequest.getQuestionId())
                .orElseThrow(() -> new ErrorException("Câu hỏi không tồn tại"));
        forwardQuestion.setQuestion(question);

        ForwardQuestionEntity updatedForwardQuestion = forwardQuestionRepository.save(forwardQuestion);
        return forwardQuestionMapper.mapToDTO(updatedForwardQuestion, forwardQuestionRequest.getConsultantId());
    }


    @Override
    @Transactional
    public void deleteForwardQuestionByRole(Integer forwardQuestionId, Integer userId, Integer departmentId, boolean isAdmin, boolean isAdvisor) {

        ForwardQuestionEntity forwardQuestion = forwardQuestionRepository.findById(forwardQuestionId)
                .orElseThrow(() -> new ErrorException("Không tìm thấy câu hỏi chuyển tiếp"));

        if (!isAdmin) {
            if (isAdvisor) {
                if ((!forwardQuestion.getFromDepartment().getId().equals(departmentId)
                        && !forwardQuestion.getCreatedBy().getId().equals(userId))) {
                    throw new ErrorException("Bạn không có quyền xóa câu hỏi này.");
                }
            } else {
                if (!forwardQuestion.getCreatedBy().getId().equals(userId)) {
                    throw new ErrorException("Bạn chỉ có thể xóa câu hỏi do bạn tạo.");
                }
            }
        }

        forwardQuestionRepository.delete(forwardQuestion);
    }


    @Override
    public ForwardQuestionDTO getForwardQuestionDetailByRole(Integer forwardQuestionId, Integer userId, Integer departmentId, boolean isAdmin, boolean isAdvisor) {
        Specification<ForwardQuestionEntity> spec;

        if (isAdmin) {
            spec = Specification.where(ForwardQuestionSpecification.hasId(forwardQuestionId));
        } else if (isAdvisor) {
            spec = Specification.where(ForwardQuestionSpecification.hasId(forwardQuestionId))
                    .and(ForwardQuestionSpecification.hasCreatedBy(userId))
                    .or(ForwardQuestionSpecification.hasToDepartment(departmentId));
        } else {
            spec = Specification.where(ForwardQuestionSpecification.hasId(forwardQuestionId))
                    .and(ForwardQuestionSpecification.hasCreatedBy(userId));
        }

        return forwardQuestionRepository.findOne(spec)
                .map(forwardQuestion -> forwardQuestionMapper.mapToDTO(forwardQuestion, forwardQuestion.getConsultant().getId())) // Sử dụng lambda thay vì method reference
                .orElse(null);
    }
}
