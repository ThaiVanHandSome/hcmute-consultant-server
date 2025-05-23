package HcmuteConsultantServer.model.payload.mapper.admin;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import HcmuteConsultantServer.model.entity.ProvinceEntity;
import HcmuteConsultantServer.model.payload.dto.manage.ManageProvinceDTO;

@Mapper(componentModel = "spring")
public interface ProvinceMapper {

    @Mapping(source = "code", target = "code")
    @Mapping(source = "name", target = "name")
    @Mapping(source = "nameEn", target = "nameEn")
    @Mapping(source = "fullName", target = "fullName")
    @Mapping(source = "fullNameEn", target = "fullNameEn")
    @Mapping(source = "codeName", target = "codeName")
    ManageProvinceDTO mapToDTO(ProvinceEntity province);
}
