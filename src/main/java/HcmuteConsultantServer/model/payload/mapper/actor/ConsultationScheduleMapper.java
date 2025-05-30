package HcmuteConsultantServer.model.payload.mapper.actor;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import HcmuteConsultantServer.model.entity.ConsultationScheduleEntity;
import HcmuteConsultantServer.model.entity.UserInformationEntity;
import HcmuteConsultantServer.model.payload.dto.actor.ConsultationScheduleDTO;
import HcmuteConsultantServer.model.payload.dto.manage.ManageConsultantScheduleDTO;

@Mapper(componentModel = "spring")
public interface ConsultationScheduleMapper {

    @Mapping(source = "id", target = "id")
    @Mapping(source = "department.id", target = "department.id")
    @Mapping(source = "department.name", target = "department.name")
    @Mapping(source = "title", target = "title")
    @Mapping(source = "content", target = "content")
    @Mapping(source = "consultationDate", target = "consultationDate")
    @Mapping(source = "consultationTime", target = "consultationTime")
    @Mapping(source = "location", target = "location")
    @Mapping(source = "link", target = "link")
    @Mapping(source = "mode", target = "mode")
    @Mapping(source = "statusPublic", target = "statusPublic")
    @Mapping(source = "statusConfirmed", target = "statusConfirmed")
    @Mapping(source = "consultant", target = "consultantName", qualifiedByName = "mapConsultantName")
    @Mapping(source = "type", target = "type")
    @Mapping(source = "user.id", target = "createdBy")
    @Mapping(source = "user", target = "name", qualifiedByName = "mapUserName")
    @Mapping(source = "createdAt", target = "createdAt")
    ConsultationScheduleDTO mapToDTO(ConsultationScheduleEntity schedule);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "title", target = "title")
    @Mapping(source = "content", target = "content")
    @Mapping(source = "consultationDate", target = "consultationDate")
    @Mapping(source = "consultationTime", target = "consultationTime")
    @Mapping(source = "location", target = "location")
    @Mapping(source = "link", target = "link")
    @Mapping(source = "mode", target = "mode")
    @Mapping(source = "statusPublic", target = "statusPublic")
    @Mapping(source = "statusConfirmed", target = "statusConfirmed")
    @Mapping(source = "createdBy", target = "created_by")
    @Mapping(source = "type", target = "type")
    ManageConsultantScheduleDTO mapToDTOs(ConsultationScheduleEntity schedule);

    @Named("mapConsultantName")
    default String mapConsultantName(UserInformationEntity consultant) {
        if (consultant == null) return null;
        return consultant.getLastName() + " " + consultant.getFirstName();
    }

    @Named("mapUserName")
    default String mapUserName(UserInformationEntity user) {
        if (user == null) return null;
        return user.getLastName() + " " + user.getFirstName();
    }
}
