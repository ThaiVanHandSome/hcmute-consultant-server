package HcmuteConsultantServer.model.payload.mapper.admin;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import HcmuteConsultantServer.model.entity.DistrictEntity;
import HcmuteConsultantServer.model.payload.dto.manage.ManageDistrictDTO;

@Mapper(componentModel = "spring")
public interface DistrictMapper {

    @Mapping(source = "code", target = "code")
    @Mapping(source = "name", target = "name")
    @Mapping(source = "nameEn", target = "nameEn")
    @Mapping(source = "fullName", target = "fullName")
    @Mapping(source = "fullNameEn", target = "fullNameEn")
    @Mapping(source = "codeName", target = "codeName")
    @Mapping(source = "province.code", target = "provinceCode")
    ManageDistrictDTO mapToDTO(DistrictEntity district);
}
