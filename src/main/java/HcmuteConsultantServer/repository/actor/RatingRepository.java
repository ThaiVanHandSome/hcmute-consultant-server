package HcmuteConsultantServer.repository.actor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import HcmuteConsultantServer.model.entity.RatingEntity;
import HcmuteConsultantServer.model.entity.UserInformationEntity;

import java.util.List;
import java.util.Optional;
import java.time.LocalDate;
@Repository
public interface RatingRepository extends PagingAndSortingRepository<RatingEntity, Integer>, JpaSpecificationExecutor<RatingEntity> {

    @Query("SELECT r FROM RatingEntity r WHERE r.user = :user AND r.department.id = :departmentId")
    Page<RatingEntity> findByUserAndDepartment(UserInformationEntity user, Integer departmentId, Pageable pageable);

    @Query("SELECT r FROM RatingEntity r WHERE r.user = :user AND CONCAT(r.consultant.firstName, ' ', r.consultant.lastName) LIKE %:consultantName%")
    Page<RatingEntity> findByUserAndConsultantName(UserInformationEntity user, String consultantName, Pageable pageable);

    @Query("SELECT r FROM RatingEntity r WHERE r.user = :user AND r.department.id = :departmentId AND CONCAT(r.consultant.firstName, ' ', r.consultant.lastName) LIKE %:consultantName%")
    Page<RatingEntity> findByUserAndDepartmentAndConsultantName(UserInformationEntity user, Integer departmentId, String consultantName, Pageable pageable);

    @Query("SELECT r FROM RatingEntity r WHERE r.user = :user")
    Page<RatingEntity> findByUser(UserInformationEntity user, Pageable pageable);

    @Query("SELECT r FROM RatingEntity r WHERE r.id = :ratingId AND r.user.id = :userId")
    Optional<RatingEntity> findByIdAndUserId(@Param("ratingId") Integer ratingId, @Param("userId") Integer userId);

    @Query("SELECT r FROM RatingEntity r WHERE r.id = :ratingId AND r.consultant.id = :consultantId")
    Optional<RatingEntity> findByIdAndConsultantId(@Param("ratingId") Integer ratingId, @Param("consultantId") Integer consultantId);

    @Query("SELECT r FROM RatingEntity r WHERE r.id = :ratingId AND r.department.id = :departmentId")
    Optional<RatingEntity> findByIdAndDepartmentId(@Param("ratingId") Integer ratingId, @Param("departmentId") Integer departmentId);

    Optional<RatingEntity> findByUserIdAndConsultantId(Integer userId, Integer consultantId);

    @Query("SELECT r FROM RatingEntity r WHERE r.department.id = :departmentId")
    List<RatingEntity> findAllByDepartmentId(@Param("departmentId") Integer departmentId);

    void deleteByConsultantId(Integer consultantId);

    @Query("SELECT r FROM RatingEntity r WHERE r.submittedAt BETWEEN :fromDate AND :toDate")
    List<RatingEntity> findBySubmittedAtBetween(@Param("fromDate") LocalDate fromDate, @Param("toDate") LocalDate toDate);
}
