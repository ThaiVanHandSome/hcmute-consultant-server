package HcmuteConsultantServer.model.payload.mapper.admin;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import HcmuteConsultantServer.model.entity.DepartmentEntity;
import HcmuteConsultantServer.model.payload.dto.manage.ManageDepartmentDTO;

@Mapper(componentModel = "spring")
public interface DepartmentMapper {

    @Mapping(source = "id", target = "id")
    @Mapping(source = "createdAt", target = "createdAt")
    @Mapping(source = "name", target = "name")
    @Mapping(source = "description", target = "description")
    @Mapping(source = "logo", target = "logo")
    ManageDepartmentDTO mapToDTO(DepartmentEntity department);
}
