package HcmuteConsultantServer.model.payload.dto.actor;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EmailDTO {
    private Integer id;
    private String email;
    private String name;
    private String avatarUrl;
}
