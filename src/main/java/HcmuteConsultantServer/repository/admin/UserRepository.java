package HcmuteConsultantServer.repository.admin;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import HcmuteConsultantServer.model.entity.AccountEntity;
import HcmuteConsultantServer.model.entity.DepartmentEntity;
import HcmuteConsultantServer.model.entity.UserInformationEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserInformationEntity, Integer>, JpaSpecificationExecutor<UserInformationEntity> {
    @Query("SELECT u FROM UserInformationEntity u WHERE u.account.email = :email")
    Optional<UserInformationEntity> findUserInfoByEmail(@Param("email") String email);

    @Query("SELECT u FROM UserInformationEntity u WHERE u.account=:account")
    UserInformationEntity findUserInfoModelByAccountModel(@Param("account") AccountEntity accountModel);

    @Override
    @Query("SELECT u FROM UserInformationEntity u WHERE u.id=:id")
    Optional<UserInformationEntity> findById(@Param("id") Integer integer);

    @Query("SELECT u FROM UserInformationEntity u WHERE u.account.role.name IN :roles")
    List<UserInformationEntity> findAllByRoleIn(@Param("roles") List<String> roles);

    boolean existsByPhone(String phone);

    @Query("SELECT a.userInformation FROM AccountEntity a WHERE a.role.name = :roleName AND a.isActivity = true")
    Optional<UserInformationEntity> findActiveAdminByRole(@Param("roleName") String roleName);

    boolean existsByAccount_Email(String email);

    Optional<UserInformationEntity> findByAccount_Username(String username);

    @Query("SELECT u FROM UserInformationEntity u WHERE u.account.role.name = :roleName")
    Page<UserInformationEntity> findAllByRoleName(@Param("roleName") String roleName, Pageable pageable);

    @Query("SELECT u FROM UserInformationEntity u WHERE u.account.role.name = :roleName AND u.account.department.id = :departmentId")
    Page<UserInformationEntity> findAllByRoleNameAndDepartment(@Param("roleName") String roleName, @Param("departmentId") Integer departmentId, Pageable pageable);

    @Query("SELECT u FROM UserInformationEntity u WHERE LOWER(u.firstName) LIKE LOWER(CONCAT('%', :firstName, '%'))AND u.account.role.name = :roleName")
    Page<UserInformationEntity> findByFirstNameAndRoleName(@Param("firstName") String firstName, @Param("roleName") String roleName, Pageable pageable);

    @Query("SELECT u FROM UserInformationEntity u WHERE u.account.department.id = :departmentId AND LOWER(u.firstName) LIKE LOWER(CONCAT('%', :firstName, '%')) AND u.account.role.name = :roleName")
    Page<UserInformationEntity> findByDepartmentAndFirstNameAndRoleName(@Param("departmentId") Integer departmentId, @Param("firstName") String firstName, @Param("roleName") String roleName, Pageable pageable);

    @Query("SELECT u FROM UserInformationEntity u JOIN u.account a WHERE a.role.name = :roleName AND a.department = :department")
    List<UserInformationEntity> findByDepartmentAndRoleName(@Param("department") DepartmentEntity department, @Param("roleName") String roleName);

    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END " +
            "FROM UserInformationEntity u " +
            "JOIN u.account a " +
            "JOIN a.role r " +
            "WHERE u.id = :userId AND r.name = :roleName")
    boolean existsByUserIdAndRoleName(@Param("userId") Integer userId, @Param("roleName") String roleName);

    Optional<UserInformationEntity> findByFirstName(String firstName);

    Optional<UserInformationEntity> findByFirstNameAndLastName(String firstName, String lastName);

    @Query("SELECT u FROM UserInformationEntity u JOIN u.account.role r WHERE u.account.department.id = :departmentId AND r.name = 'ROLE_TUVANVIEN'")
    List<UserInformationEntity> findConsultantsByDepartmentId(Integer departmentId);

    @Query("SELECT u FROM UserInformationEntity u JOIN u.account.role r WHERE u.id = :consultantId AND r.name = 'ROLE_TUVANVIEN'")
    Optional<UserInformationEntity> findConsultantById(@Param("consultantId") Integer consultantId);

    @Query("SELECT u FROM UserInformationEntity u JOIN u.account.role r WHERE r.name = 'ROLE_ADMIN'")
    UserInformationEntity findAdmin();

    @Query("SELECT u.id FROM UserInformationEntity u WHERE u.account.email = :email")
    Integer getUserIdByEmail(@Param("email") String email);

    @Query("SELECT u.account.department.id FROM UserInformationEntity u WHERE u.id = :consultantId")
    Integer findDepartmentIdByConsultantId(@Param("consultantId") Integer consultantId);

    @Query("SELECT u FROM UserInformationEntity u WHERE u.account.department.id = :departmentId")
    List<UserInformationEntity> findAllByDepartmentId(@Param("departmentId") Integer departmentId);

    @Query("SELECT u FROM UserInformationEntity u WHERE u.account.email = :email AND u.account.department.id = :departmentId")
    Optional<UserInformationEntity> findByEmailAndDepartmentId(@Param("email") String email, @Param("departmentId") Integer departmentId);

    @Query("SELECT u.account.department FROM UserInformationEntity u WHERE u.id = :consultantId")
    Optional<DepartmentEntity> findConsultantDepartmentByConsultantId(@Param("consultantId") Integer consultantId);

    @Query("SELECT u FROM UserInformationEntity u WHERE u.account.role.name = :role")
    List<UserInformationEntity> findAllByRole(@Param("role") String role);

    @Query("SELECT u FROM UserInformationEntity u WHERE u.account.role.name = :role AND u.account.department.id = :departmentId")
    Optional<UserInformationEntity> findByRoleAndDepartment(@Param("role") String role, @Param("departmentId") Integer departmentId);

    List<UserInformationEntity> findAll();

    @Modifying
    @Query("DELETE FROM UserInformationEntity u WHERE u.account.id = :accountId")
    void deleteUserInformationByAccountId(@Param("accountId") Integer accountId);

}
