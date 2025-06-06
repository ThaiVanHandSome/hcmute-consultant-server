package HcmuteConsultantServer.model.payload.dto.actor;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemberDTO {
    private Integer id;
    private String name;
    private String avatarUrl;
    private boolean sender;
}

