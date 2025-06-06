package HcmuteConsultantServer.service.interfaces.admin;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import HcmuteConsultantServer.model.payload.dto.manage.ManageAddressDTO;
import HcmuteConsultantServer.model.payload.request.AddressRequest;

public interface IAdminAdressService {

    ManageAddressDTO createAddress(AddressRequest addressRequest);

    ManageAddressDTO updateAddress(Integer id, AddressRequest addressRequest);

    void deleteAddressById(Integer id);

    ManageAddressDTO getAddressById(Integer id);

    Page<ManageAddressDTO> getAddressByAdmin(Integer id, String line, String provinceCode, String districtCode, String wardCode, Pageable pageable);
}
