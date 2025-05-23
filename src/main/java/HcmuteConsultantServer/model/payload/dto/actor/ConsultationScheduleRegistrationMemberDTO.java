package HcmuteConsultantServer.model.payload.dto.actor;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsultationScheduleRegistrationMemberDTO {
    private String userName;
    private String avatarUrl;
    private LocalDate registeredAt;
    private Boolean status;
}

