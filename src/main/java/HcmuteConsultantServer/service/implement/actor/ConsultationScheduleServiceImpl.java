package HcmuteConsultantServer.service.implement.actor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import HcmuteConsultantServer.constant.SecurityConstants;
import HcmuteConsultantServer.model.entity.ConsultationScheduleEntity;
import HcmuteConsultantServer.model.entity.ConsultationScheduleRegistrationEntity;
import HcmuteConsultantServer.model.entity.DepartmentEntity;
import HcmuteConsultantServer.model.entity.UserInformationEntity;
import HcmuteConsultantServer.model.exception.CustomFieldErrorException;
import HcmuteConsultantServer.model.exception.Exceptions.ErrorException;
import HcmuteConsultantServer.model.exception.FieldErrorDetail;
import HcmuteConsultantServer.model.payload.dto.actor.CommonQuestionDTO;
import HcmuteConsultantServer.model.payload.dto.actor.ConsultationScheduleDTO;
import HcmuteConsultantServer.model.payload.dto.actor.ConsultationScheduleRegistrationDTO;
import HcmuteConsultantServer.model.payload.dto.actor.ConsultationScheduleRegistrationMemberDTO;
import HcmuteConsultantServer.model.payload.dto.manage.ManageConsultantScheduleDTO;
import HcmuteConsultantServer.model.payload.mapper.actor.ConsultationScheduleMapper;
import HcmuteConsultantServer.model.payload.request.ConsultationScheduleRegistrationRequest;
import HcmuteConsultantServer.model.payload.request.CreateScheduleConsultationRequest;
import HcmuteConsultantServer.model.payload.request.ManageCreateConsultantScheduleRequest;
import HcmuteConsultantServer.model.payload.request.UpdateConsultationScheduleRequest;
import HcmuteConsultantServer.model.payload.response.DataResponse;
import HcmuteConsultantServer.repository.actor.ConsultationScheduleRegistrationRepository;
import HcmuteConsultantServer.repository.actor.ConsultationScheduleRepository;
import HcmuteConsultantServer.repository.admin.DepartmentRepository;
import HcmuteConsultantServer.repository.admin.UserRepository;
import HcmuteConsultantServer.service.interfaces.actor.IConsultationScheduleService;
import HcmuteConsultantServer.specification.actor.CommonQuestionSpecification;
import HcmuteConsultantServer.specification.actor.ConsultationScheduleRegistrationSpecification;
import HcmuteConsultantServer.specification.actor.ConsultationScheduleSpecification;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ConsultationScheduleServiceImpl implements IConsultationScheduleService {

    @Autowired
    private ConsultationScheduleRepository consultationScheduleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private ConsultationScheduleRegistrationRepository consultationScheduleRegistrationRepository;

    @Autowired
    private ConsultationScheduleMapper consultationScheduleMapper;

    @Override
    public ConsultationScheduleDTO createConsultation(CreateScheduleConsultationRequest request, UserInformationEntity user) {
        List<FieldErrorDetail> errors = new ArrayList<>();

        Optional<UserInformationEntity> consultantOpt = userRepository.findById(request.getConsultantId());
        if (!consultantOpt.isPresent()) {
            errors.add(new FieldErrorDetail("consultant", "Tư vấn viên không tồn tại"));
            throw new CustomFieldErrorException(errors);
        }

        Optional<DepartmentEntity> departmentOpt = departmentRepository.findById(request.getDepartmentId());
        if (!departmentOpt.isPresent()) {
            errors.add(new FieldErrorDetail("department", "Phòng ban không tồn tại"));
            throw new CustomFieldErrorException(errors);
        }

        UserInformationEntity consultant = consultantOpt.get();
        DepartmentEntity department = departmentOpt.get();

        if (!consultant.getAccount().getDepartment().getId().equals(department.getId())) {
            errors.add(new FieldErrorDetail("consultant", "Tư vấn viên không thuộc phòng ban đã chọn"));
            throw new CustomFieldErrorException(errors);
        }

        Optional<ConsultationScheduleEntity> existingSchedule = consultationScheduleRepository
                .findByDepartmentAndStatusConfirmedFalseAndCreatedBy(department, user.getId());

        if (existingSchedule.isPresent()) {
            throw new ErrorException("Bạn không thể đặt lịch tư vấn cho phòng ban này nữa vì trước đó đã có lịch tư vấn tồn tại và lịch tư vấn chưa được xác nhận");
        }

        Optional<ConsultationScheduleEntity> scheduleExists = consultationScheduleRepository
                .findByDepartmentAndStatusConfirmedTrueAndConsultationDateAfterAndCreatedBy(department, LocalDate.now(), user.getId());

        if (scheduleExists.isPresent()) {
            throw new ErrorException("Bạn không thể đặt lịch tư vấn cho phòng ban này nữa vì trước đó đã có lịch tư vấn tồn tại và thời gian tư vấn chưa được diễn ra");
        }

        ConsultationScheduleEntity schedule = new ConsultationScheduleEntity();
        schedule.setUser(user);
        schedule.setConsultant(consultant);
        schedule.setDepartment(department);
        schedule.setTitle(request.getTitle());
        schedule.setContent(request.getContent());
        schedule.setMode(request.getMode());
        schedule.setStatusPublic(request.getStatusPublic());
        schedule.setCreatedBy(user.getId());
        schedule.setCreatedAt(LocalDate.now());
        schedule.setType(true);
        schedule.setStatusConfirmed(false);
        ConsultationScheduleEntity savedSchedule = consultationScheduleRepository.save(schedule);

        return consultationScheduleMapper.mapToDTO(savedSchedule);
    }

    @Override
    public ManageConsultantScheduleDTO confirmConsultationSchedule(Integer scheduleId, Integer departmentId, UpdateConsultationScheduleRequest request) {

        ConsultationScheduleEntity existingSchedule = consultationScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ErrorException("Lịch tư vấn không tồn tại"));

        if (request.getConsultationDate() != null && request.getConsultationDate().isBefore(LocalDate.now())) {
            throw new ErrorException("Ngày tư vấn không được tồn tại trong quá khứ, phải từ hiện tại hoặc trong tương lai");
        }
        existingSchedule.setMode(request.getMode() != null ? request.getMode() : existingSchedule.getMode()); // Kiểm tra mode có null không
        existingSchedule.setTitle(request.getTitle() != null && !request.getTitle().isBlank() ? request.getTitle() : existingSchedule.getTitle());
        existingSchedule.setContent(request.getContent() != null && !request.getContent().isBlank() ? request.getContent() : existingSchedule.getContent());
        existingSchedule.setStatusPublic(request.getStatusPublic() != null ? request.getStatusPublic() : existingSchedule.getStatusPublic()); // Kiểm tra statusPublic có null không
        existingSchedule.setStatusConfirmed(request.getStatusConfirmed() != null ? request.getStatusConfirmed() : existingSchedule.getStatusConfirmed());
        existingSchedule.setCreatedAt(LocalDate.now());

            if (Boolean.TRUE.equals(existingSchedule.getMode())) { // Tư vấn online
                if (request.getLocation() != null && !request.getLocation().isBlank()) {
                    throw new ErrorException("Không được phép nhập địa điểm cho tư vấn online.");
                }
                if (request.getLink() == null || request.getLink().isBlank()
                        || request.getConsultationDate() == null || request.getConsultationTime() == null) {
                    throw new ErrorException("Phải cung cấp đầy đủ thông tin link, ngày và giờ cho tư vấn online.");
                }
                existingSchedule.setLink(request.getLink());
                existingSchedule.setLocation(null);
            } else { // Tư vấn offline
                if (request.getLink() != null && !request.getLink().isBlank()) {
                    throw new ErrorException("Không được phép nhập link cho tư vấn offline.");
                }
                if (request.getLocation() == null || request.getLocation().isBlank()
                        || request.getConsultationDate() == null || request.getConsultationTime() == null) {
                    throw new ErrorException("Phải cung cấp đầy đủ thông tin địa điểm, ngày và giờ cho tư vấn offline.");
                }
                existingSchedule.setLocation(request.getLocation());
                existingSchedule.setLink(null);
            }

            existingSchedule.setConsultationDate(request.getConsultationDate());
            existingSchedule.setConsultationTime(request.getConsultationTime());
        ConsultationScheduleEntity updatedSchedule = consultationScheduleRepository.save(existingSchedule);

        return consultationScheduleMapper.mapToDTOs(updatedSchedule);
    }

    @Override
    public ConsultationScheduleDTO createConsultationSchedule(ManageCreateConsultantScheduleRequest request, Integer departmentId, Integer userId) {
        DepartmentEntity department = null;
        if (request.getConsultationDate() != null && request.getConsultationDate().isBefore(LocalDate.now())) {
            throw new ErrorException("Ngày tư vấn không được tồn tại trong quá khứ, phải từ hiện tại hoặc trong tương lai");
        }
        if (departmentId != null) {
            department = departmentRepository.findById(departmentId)
                    .orElseThrow(() -> new ErrorException("Phòng ban không tồn tại"));
        }

        List<FieldErrorDetail> errors = new ArrayList<>();

        if (Boolean.TRUE.equals(request.getMode())) { // Online
            if (request.getLocation() != null && !request.getLocation().isBlank()) {
                errors.add(new FieldErrorDetail("location", "Không được phép nhập địa điểm cho tư vấn online."));
            }
            if (request.getLink() == null || request.getLink().isBlank()
                    || request.getConsultationDate() == null || request.getConsultationTime() == null) {
                errors.add(new FieldErrorDetail("online", "Phải cung cấp đầy đủ thông tin link, ngày và giờ cho tư vấn online."));
            }
        } else { // Offline
            if (request.getLink() != null && !request.getLink().isBlank()) {
                errors.add(new FieldErrorDetail("link", "Không được phép nhập link cho tư vấn offline."));
            }
            if (request.getLocation() == null || request.getLocation().isBlank()
                    || request.getConsultationDate() == null || request.getConsultationTime() == null) {
                errors.add(new FieldErrorDetail("offline", "Phải cung cấp đầy đủ thông tin địa điểm, ngày và giờ cho tư vấn offline."));
            }
        }


        if (!errors.isEmpty()) {
            throw new CustomFieldErrorException(errors);
        }


        ConsultationScheduleEntity newSchedule = new ConsultationScheduleEntity();
        newSchedule.setTitle(request.getTitle());
        newSchedule.setContent(request.getContent());
        newSchedule.setConsultationDate(request.getConsultationDate());
        newSchedule.setConsultationTime(request.getConsultationTime());
        newSchedule.setMode(request.getMode());
        newSchedule.setStatusPublic(true);
        newSchedule.setStatusConfirmed(true);
        newSchedule.setCreatedBy(userId);
        newSchedule.setType(false);
        newSchedule.setCreatedAt(LocalDate.now());
        newSchedule.setDepartment(department);

        if (Boolean.TRUE.equals(request.getMode())) {
            newSchedule.setLink(request.getLink());
        } else {
            newSchedule.setLocation(request.getLocation());
        }

        ConsultationScheduleEntity savedSchedule = consultationScheduleRepository.save(newSchedule);

        UserInformationEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ErrorException("Người dùng không tồn tại"));

        ConsultationScheduleDTO dto = consultationScheduleMapper.mapToDTO(savedSchedule);
        dto.setName(user.getLastName() + " " + user.getFirstName());
        dto.setCreatedBy(userId);
        return dto;
    }


    @Override
    public Page<ConsultationScheduleDTO> getConsultationScheduleByRole(UserInformationEntity user, Integer departmentId, String title, Boolean type, Boolean statusPublic, Boolean statusConfirmed, Boolean mode, LocalDate startDate, LocalDate endDate, Pageable pageable) {
        String role = user.getAccount().getRole().getName();
        Integer departId = user.getAccount().getDepartment() != null ? user.getAccount().getDepartment().getId() : null;

        Specification<ConsultationScheduleEntity> spec = Specification.where(null);

        if (SecurityConstants.Role.USER.equals(role)) {
            spec = spec.and(ConsultationScheduleSpecification.hasUser(user));
            spec = spec.or(ConsultationScheduleSpecification.hasType(false));
        }

        if (SecurityConstants.Role.TUVANVIEN.equals(role)) {
            if (departId != null) {
                spec = spec.and(ConsultationScheduleSpecification.hasDepartment(departId));
            }
        }

        if (SecurityConstants.Role.TRUONGBANTUVAN.equals(role)) {
            if (departId != null) {
                spec = spec.and(ConsultationScheduleSpecification.hasDepartment(departId));
                spec = spec.or(ConsultationScheduleSpecification.isCreatedByAdvisor());
            }
        }

        if (departmentId != null) {
            spec = spec.and(ConsultationScheduleSpecification.hasDepartment(departmentId));
        }

        if (title != null && !title.trim().isEmpty()) {
            spec = spec.and(ConsultationScheduleSpecification.hasTitle(title));
        }
        if (statusPublic != null) {
            spec = spec.and(ConsultationScheduleSpecification.hasStatusPublic(statusPublic));
        }
        if (type != null) {
            spec = spec.and(ConsultationScheduleSpecification.hasType(type));
        }
        if (statusConfirmed != null) {
            spec = spec.and(ConsultationScheduleSpecification.hasStatusConfirmed(statusConfirmed));
        }
        if (mode != null) {
            spec = spec.and(ConsultationScheduleSpecification.hasMode(mode));
        }
        if (startDate != null && endDate != null) {
            spec = spec.and(ConsultationScheduleSpecification.hasExactDateRange(startDate, endDate));
        } else if (startDate != null) {
            spec = spec.and(ConsultationScheduleSpecification.hasExactStartDate(startDate));
        } else if (endDate != null) {
            spec = spec.and(ConsultationScheduleSpecification.hasDateBefore(endDate));
        }

        Page<ConsultationScheduleEntity> schedulePage = consultationScheduleRepository.findAll(spec, pageable);

        // Ánh xạ sang DTO và thêm logic tìm name từ createdBy
        return schedulePage.map(schedule -> {
            ConsultationScheduleDTO dto = consultationScheduleMapper.mapToDTO(schedule);

            if (schedule.getCreatedBy() != null) {
                UserInformationEntity createdByUser = userRepository.findById(schedule.getCreatedBy())
                        .orElseThrow(() -> new ErrorException("Người tạo không tồn tại"));
                dto.setCreatedBy(createdByUser.getId());
                dto.setName(createdByUser.getLastName() + " " + createdByUser.getFirstName());
                dto.setCreatedAt(schedule.getCreatedAt());
            }

            return dto;
        });    }

    @Override
    public Page<ConsultationScheduleDTO> getConsultationScheduleForGuest(Pageable pageable) {
        Specification<ConsultationScheduleEntity> spec = Specification.where(null);

        Page<ConsultationScheduleEntity> schedulePage = consultationScheduleRepository.findAll(spec, pageable);
        return schedulePage.map(schedule -> {
            ConsultationScheduleDTO dto = consultationScheduleMapper.mapToDTO(schedule);

            if (schedule.getCreatedBy() != null) {
                UserInformationEntity createdByUser = userRepository.findById(schedule.getCreatedBy())
                        .orElseThrow(() -> new ErrorException("Người tạo không tồn tại"));
                dto.setCreatedBy(createdByUser.getId());
                dto.setName(createdByUser.getLastName() + " " + createdByUser.getFirstName());
                dto.setCreatedAt(schedule.getCreatedAt());
            }

            return dto;
        });
    }

    @Override
    public ConsultationScheduleDTO updateConsultationSchedule(
            Integer scheduleId, Integer departmentId, boolean isAdmin,
            UpdateConsultationScheduleRequest request, String role, Integer userId) {

        Optional<ConsultationScheduleEntity> scheduleOpt;

        if (SecurityConstants.Role.ADMIN.equals(role)) {
            scheduleOpt = consultationScheduleRepository.findById(scheduleId);
        } else if (SecurityConstants.Role.TRUONGBANTUVAN.equals(role)) {
            scheduleOpt = consultationScheduleRepository.findByIdAndDepartmentOrCreatedBy(scheduleId, departmentId, userId);
        } else if (SecurityConstants.Role.TUVANVIEN.equals(role)) {
            scheduleOpt = consultationScheduleRepository.findByIdAndDepartmentId(scheduleId, departmentId);
        } else {
            scheduleOpt = consultationScheduleRepository.findByIdAndCreatedBy(scheduleId, userId);
        }

        ConsultationScheduleEntity existingSchedule = scheduleOpt
                .orElseThrow(() -> new ErrorException("Lịch tư vấn không tồn tại hoặc bạn không có quyền cập nhật lịch này"));

        if (request.getStatusConfirmed() != null && request.getStatusConfirmed()) {
            if (Boolean.TRUE.equals(request.getMode())) {
                if (request.getLocation() != null) {
                    throw new ErrorException("Không được phép nhập địa điểm cho tư vấn online.");
                }
                if (request.getLink() == null || request.getConsultationDate() == null || request.getConsultationTime() == null) {
                    throw new ErrorException("Phải cung cấp đầy đủ thông tin link, ngày và giờ cho tư vấn online.");
                }
                existingSchedule.setLink(request.getLink());
            } else {
                if (request.getLink() != null) {
                    throw new ErrorException("Không được phép nhập link cho tư vấn offline.");
                }
                if (request.getLocation() == null || request.getConsultationDate() == null || request.getConsultationTime() == null) {
                    throw new ErrorException("Phải cung cấp đầy đủ thông tin địa điểm, ngày và giờ cho tư vấn offline.");
                }
                existingSchedule.setLocation(request.getLocation());
            }
            existingSchedule.setStatusConfirmed(true);
        } else {
            existingSchedule.setStatusConfirmed(false);
        }

        existingSchedule.setTitle(request.getTitle() != null ? request.getTitle() : existingSchedule.getTitle());
        existingSchedule.setContent(request.getContent() != null ? request.getContent() : existingSchedule.getContent());
        existingSchedule.setConsultationDate(request.getConsultationDate() != null ? request.getConsultationDate() : existingSchedule.getConsultationDate());
        existingSchedule.setConsultationTime(request.getConsultationTime() != null ? request.getConsultationTime() : existingSchedule.getConsultationTime());
        existingSchedule.setLocation(request.getLocation() != null ? request.getLocation() : existingSchedule.getLocation());
        existingSchedule.setLink(request.getLink() != null ? request.getLink() : existingSchedule.getLink());
        existingSchedule.setMode(request.getMode() != null ? request.getMode() : existingSchedule.getMode());
        existingSchedule.setStatusPublic(request.getStatusPublic() != null ? request.getStatusPublic() : existingSchedule.getStatusPublic());
        existingSchedule.setStatusConfirmed(request.getStatusConfirmed() != null ? request.getStatusConfirmed() : existingSchedule.getStatusConfirmed());

        ConsultationScheduleEntity updatedSchedule = consultationScheduleRepository.save(existingSchedule);
        return consultationScheduleMapper.mapToDTO(updatedSchedule);
    }


    @Transactional
    @Override
    public void deleteConsultationSchedule(Integer scheduleId, Integer departmentId, Integer userId, String role) {
        Optional<ConsultationScheduleEntity> scheduleOpt;

        if (SecurityConstants.Role.ADMIN.equals(role)) {
            scheduleOpt = consultationScheduleRepository.findById(scheduleId);
        } else if (SecurityConstants.Role.TRUONGBANTUVAN.equals(role)) {
            scheduleOpt = consultationScheduleRepository.findByIdAndDepartmentOrCreatedBy(scheduleId, departmentId, userId);
        } else if (SecurityConstants.Role.TUVANVIEN.equals(role)) {
            scheduleOpt = consultationScheduleRepository.findByIdAndDepartmentId(scheduleId, departmentId);
        } else {
            scheduleOpt = consultationScheduleRepository.findByIdAndCreatedBy(scheduleId, userId);
        }

        if (!scheduleOpt.isPresent()) {
            throw new ErrorException("Lịch tư vấn không tồn tại hoặc bạn không có quyền xóa lịch này");
        }

        consultationScheduleRegistrationRepository.deleteByScheduleId(scheduleId);

        consultationScheduleRepository.delete(scheduleOpt.get());
    }

    @Override
    public ConsultationScheduleDTO getDetailConsultationScheduleByRole(Integer scheduleId, String role, Integer departmentId, Integer userId) {
        Optional<ConsultationScheduleEntity> scheduleOpt;

        if (SecurityConstants.Role.ADMIN.equals(role)) {
            scheduleOpt = consultationScheduleRepository.findById(scheduleId);
        } else if (SecurityConstants.Role.TRUONGBANTUVAN.equals(role)) {
            scheduleOpt = consultationScheduleRepository.findByIdAndDepartmentOrCreatedBy(scheduleId, departmentId, userId);
        } else if (SecurityConstants.Role.TUVANVIEN.equals(role)) {
            scheduleOpt = consultationScheduleRepository.findByIdAndDepartmentIds(scheduleId, departmentId);
        } else if (SecurityConstants.Role.USER.equals(role)){
            scheduleOpt = consultationScheduleRepository.findByScheduleIdAndConditions(scheduleId, userId);
        }
        else{
            scheduleOpt = null;
        }

        ConsultationScheduleEntity schedule = scheduleOpt.orElseThrow(() -> new ErrorException("Lịch tư vấn không tồn tại"));

        return consultationScheduleMapper.mapToDTO(schedule);
    }

    @Override
    public Page<ConsultationScheduleRegistrationDTO> getSchedulesJoinByUser(UserInformationEntity user, LocalDate startDate, LocalDate endDate, Pageable pageable) {

        Specification<ConsultationScheduleRegistrationEntity> spec = Specification
                .where(ConsultationScheduleRegistrationSpecification.hasUser(user))
                .and(ConsultationScheduleRegistrationSpecification.hasStatus(true));

        if (startDate != null && endDate != null) {
            spec = spec.and(ConsultationScheduleRegistrationSpecification.hasExactDateRange(startDate, endDate));
        } else if (startDate != null) {
            spec = spec.and(ConsultationScheduleRegistrationSpecification.hasStartDateAfterOrEqual(startDate));
        } else if (endDate != null) {
            spec = spec.and(ConsultationScheduleRegistrationSpecification.hasEndDateBeforeOrEqual(endDate));
        }

        Page<ConsultationScheduleRegistrationEntity> registrationEntities = consultationScheduleRegistrationRepository.findAll(spec, pageable);

        return registrationEntities.map(registration -> {
            ConsultationScheduleRegistrationDTO.UserDTO userDTO = ConsultationScheduleRegistrationDTO.UserDTO.builder()
                    .id(registration.getUser().getId())
                    .name(registration.getUser().getLastName() + " " + registration.getUser().getFirstName())
                    .build();

            ConsultationScheduleRegistrationDTO.ConsultationScheduleDTO consultationScheduleDTO = ConsultationScheduleRegistrationDTO.ConsultationScheduleDTO.builder()
                    .id(registration.getConsultationSchedule().getId())
                    .title(registration.getConsultationSchedule().getTitle())
                    .build();

            return ConsultationScheduleRegistrationDTO.builder()
                    .id(registration.getId())
                    .user(userDTO)
                    .consultationSchedule(consultationScheduleDTO)
                    .registeredAt(registration.getRegisteredAt())
                    .status(registration.getStatus())
                    .build();
        });
    }

    @Override
    public ConsultationScheduleRegistrationDTO registerForConsultation(Integer scheduleId, UserInformationEntity user) {
        ConsultationScheduleEntity schedule = consultationScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ErrorException("Lịch tư vấn không tồn tại"));

        LocalDateTime consultationDateTime = LocalDateTime.of(schedule.getConsultationDate(),
                LocalTime.parse(schedule.getConsultationTime()));

        if (LocalDateTime.now().isAfter(consultationDateTime)) {
            throw new ErrorException("Không thể đăng ký. Buổi tư vấn đã quá thời gian diễn ra.");
        }

        boolean isAlreadyRegistered = consultationScheduleRegistrationRepository.existsByUserAndConsultationSchedule(user, schedule);

        if (isAlreadyRegistered) {
            throw new ErrorException("Bạn đã đăng ký tham gia lịch tư vấn này trước đó.");
        }

        ConsultationScheduleRegistrationEntity registration = ConsultationScheduleRegistrationEntity.builder()
                .user(user)
                .consultationSchedule(schedule)
                .registeredAt(LocalDate.now())
                .status(true)
                .build();

        registration = consultationScheduleRegistrationRepository.save(registration);

        ConsultationScheduleRegistrationDTO.UserDTO userDTO = ConsultationScheduleRegistrationDTO.UserDTO.builder()
                .id(user.getId())
                .name(user.getLastName() + " " + user.getFirstName())
                .build();

        ConsultationScheduleRegistrationDTO.ConsultationScheduleDTO consultationScheduleDTO = ConsultationScheduleRegistrationDTO.ConsultationScheduleDTO.builder()
                .id(schedule.getId())
                .title(schedule.getTitle())
                .build();

        return ConsultationScheduleRegistrationDTO.builder()
                .id(registration.getId())
                .user(userDTO)
                .consultationSchedule(consultationScheduleDTO)
                .registeredAt(registration.getRegisteredAt())
                .status(registration.getStatus())
                .build();
    }

    @Override
    public void cancelRegistrationForConsultation(Integer id, UserInformationEntity user) {
        ConsultationScheduleEntity schedule = consultationScheduleRepository.findById(id)
                .orElseThrow(() -> new ErrorException("Lịch tư vấn không tồn tại"));

        LocalDateTime consultationDateTime = LocalDateTime.of(schedule.getConsultationDate(),
                LocalTime.parse(schedule.getConsultationTime()));

        if (LocalDateTime.now().isAfter(consultationDateTime)) {
            throw new ErrorException("Không thể hủy. Buổi tư vấn đã quá thời gian diễn ra.");
        }

        ConsultationScheduleRegistrationEntity registration = consultationScheduleRegistrationRepository
                .findByUserAndConsultationSchedule(user, schedule)
                .orElseThrow(() -> new ErrorException("Bạn không có đăng ký cho buổi tư vấn này."));

        consultationScheduleRegistrationRepository.delete(registration);
    }

    @Override
    public Page<ConsultationScheduleRegistrationMemberDTO> getMembersByConsultationSchedule(
            Integer consultationScheduleId, LocalDate startDate, LocalDate endDate, Pageable pageable, Integer userId) {

        consultationScheduleRepository.findById(consultationScheduleId)
                .orElseThrow(() -> new ErrorException("Buổi tư vấn không tồn tại."));

        Specification<ConsultationScheduleRegistrationEntity> spec = Specification
                .where(ConsultationScheduleRegistrationSpecification.hasConsultationSchedule(consultationScheduleId))
                .and(ConsultationScheduleRegistrationSpecification.hasStatus(true));

        if (startDate != null && endDate != null) {
            spec = spec.and(ConsultationScheduleRegistrationSpecification.hasExactDateRange(startDate, endDate));
        } else if (startDate != null) {
            spec = spec.and(ConsultationScheduleRegistrationSpecification.hasStartDateAfterOrEqual(startDate));
        } else if (endDate != null) {
            spec = spec.and(ConsultationScheduleRegistrationSpecification.hasEndDateBeforeOrEqual(endDate));
        }

        Page<ConsultationScheduleRegistrationEntity> registrations = consultationScheduleRegistrationRepository.findAll(spec, pageable);

        return registrations.map(registration -> {
            String userName = registration.getUser().getLastName() + " " + registration.getUser().getFirstName();
            String avatarUrl = registration.getUser().getAvatarUrl();
            return ConsultationScheduleRegistrationMemberDTO.builder()
                    .userName(userName)
                    .avatarUrl(avatarUrl)
                    .registeredAt(registration.getRegisteredAt())
                    .status(registration.getStatus())
                    .build();
        });
    }
}