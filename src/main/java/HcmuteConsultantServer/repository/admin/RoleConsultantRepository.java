package HcmuteConsultantServer.repository.admin;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import HcmuteConsultantServer.model.entity.RoleConsultantEntity;

import java.util.Optional;

public interface RoleConsultantRepository extends PagingAndSortingRepository<RoleConsultantEntity, Integer>, JpaSpecificationExecutor<RoleConsultantEntity>, JpaRepository<RoleConsultantEntity, Integer> {
    Optional<RoleConsultantEntity> findByName(String name);

    boolean existsByName(String name);

}
