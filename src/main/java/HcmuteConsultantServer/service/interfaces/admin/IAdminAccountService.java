package HcmuteConsultantServer.service.interfaces.admin;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import HcmuteConsultantServer.model.payload.dto.manage.ManageAccountDTO;
import HcmuteConsultantServer.model.payload.dto.manage.ManageActivityDTO;
import HcmuteConsultantServer.model.payload.dto.manage.UpdateAccountDTO;
import HcmuteConsultantServer.model.payload.dto.manage.UpdateActivityDTO;

import java.time.LocalDate;
import java.util.Optional;

public interface IAdminAccountService {

    Page<ManageAccountDTO> getAccountByAdmin(String email, String username, Boolean isOnline, Optional<LocalDate> startDate, Optional<LocalDate> endDate, Boolean isActivity, Pageable pageable);

    ManageAccountDTO getAccountById(Integer id);

    public ManageAccountDTO updateAccount(Integer id, UpdateAccountDTO accountRequest);
    public ManageActivityDTO updateActivity(Integer id, UpdateActivityDTO accountRequest);


    }
