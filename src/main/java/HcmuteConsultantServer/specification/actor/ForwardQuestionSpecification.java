package HcmuteConsultantServer.specification.actor;

import org.springframework.data.jpa.domain.Specification;
import HcmuteConsultantServer.model.entity.ForwardQuestionEntity;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.time.LocalDate;

public class ForwardQuestionSpecification {
    public static Specification<ForwardQuestionEntity> hasCreatedBy(Integer consultantId) {
        return (root, query, criteriaBuilder) -> {
            if (consultantId == null) {
                return criteriaBuilder.conjunction(); // Không áp dụng tiêu chí nếu consultantId là null
            }
            return criteriaBuilder.equal(root.get("createdBy").get("id"), consultantId);
        };
    }

    public static Specification<ForwardQuestionEntity> hasId(Integer id) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("id"), id);
    }

    public static Specification<ForwardQuestionEntity> isFromOrToDepartment(Integer departmentId) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.or(
                criteriaBuilder.equal(root.get("fromDepartment").get("id"), departmentId),
                criteriaBuilder.equal(root.get("toDepartment").get("id"), departmentId)
        );
    }

    public static Specification<ForwardQuestionEntity> hasConsultantAnswer(Integer consultantId) {
        return (root, query, criteriaBuilder) -> {
            if (consultantId == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("consultant").get("id"), consultantId);
        };
    }

    public static Specification<ForwardQuestionEntity> hasConsultantAnswer() {
        return (root, query, criteriaBuilder) -> {
            return criteriaBuilder.isNotNull(root.get("consultant").get("id"));
        };
    }

    public static Specification<ForwardQuestionEntity> hasFromDepartment(Integer fromDepartmentId) {
        return (root, query, builder) -> {
            if (fromDepartmentId == null) {
                return null;
            }
            return builder.equal(root.get("fromDepartment").get("id"), fromDepartmentId);
        };
    }

    public static Specification<ForwardQuestionEntity> hasToDepartment(Integer toDepartmentId) {
        return (root, query, builder) -> {
            if (toDepartmentId == null) {
                return null;
            }
            return builder.equal(root.get("toDepartment").get("id"), toDepartmentId);
        };
    }

    public static Specification<ForwardQuestionEntity> hasDepartment(Integer departmentId) {
        return (root, query, builder) -> {
            if (departmentId == null) {
                return null;
            }
            return builder.or(
                    builder.equal(root.get("fromDepartment").get("id"), departmentId),
                    builder.equal(root.get("toDepartment").get("id"), departmentId)
            );
        };
    }

    public static Specification<ForwardQuestionEntity> hasTitle(String title) {
        return (Root<ForwardQuestionEntity> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) -> {
            if (title == null || title.isEmpty()) {
                return null;
            }
            return criteriaBuilder.like(root.get("title"), "%" + title + "%");
        };
    }

    public static Specification<ForwardQuestionEntity> hasToDepartmentId(Integer toDepartmentId) {
        return (Root<ForwardQuestionEntity> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) -> {
            if (toDepartmentId == null) {
                return null;
            }
            return criteriaBuilder.equal(root.get("toDepartment").get("id"), toDepartmentId);
        };
    }

    public static Specification<ForwardQuestionEntity> hasExactStartDate(LocalDate startDate) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("createdAt").as(LocalDate.class), startDate);
    }

    public static Specification<ForwardQuestionEntity> hasDateBefore(LocalDate endDate) {
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("createdAt").as(LocalDate.class), endDate);
    }

    public static Specification<ForwardQuestionEntity> hasExactDateRange(LocalDate startDate, LocalDate endDate) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.between(root.get("createdAt").as(LocalDate.class), startDate, endDate);
    }

    public static Specification<ForwardQuestionEntity> hasExactYear(Integer year) {
        return (root, query, criteriaBuilder) -> {
            if (year == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(criteriaBuilder.function("YEAR", Integer.class, root.get("createdAt")), year);
        };
    }
}

