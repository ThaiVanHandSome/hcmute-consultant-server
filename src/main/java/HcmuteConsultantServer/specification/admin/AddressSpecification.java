package HcmuteConsultantServer.specification.admin;

import org.springframework.data.jpa.domain.Specification;
import HcmuteConsultantServer.model.entity.AddressEntity;

public class AddressSpecification {

    public static Specification<AddressEntity> hasId(Integer id) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.like(root.get("id"), "%" + id + "%");
    }

    public static Specification<AddressEntity> hasLine(String line) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.like(root.get("line"), "%" + line + "%");
    }

    public static Specification<AddressEntity> hasProvince(String provinceCode) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("province").get("code"), provinceCode);
    }

    public static Specification<AddressEntity> hasDistrict(String districtCode) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("district").get("code"), districtCode);
    }

    public static Specification<AddressEntity> hasWard(String wardCode) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("ward").get("code"), wardCode);
    }
}
