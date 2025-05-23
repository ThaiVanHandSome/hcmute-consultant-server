package HcmuteConsultantServer.security.oauth2;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import HcmuteConsultantServer.model.entity.AccountEntity;
import HcmuteConsultantServer.model.entity.RoleEntity;
import HcmuteConsultantServer.model.entity.UserInformationEntity;
import HcmuteConsultantServer.model.exception.Exceptions;
import HcmuteConsultantServer.repository.admin.AccountRepository;
import HcmuteConsultantServer.repository.admin.RoleRepository;
import HcmuteConsultantServer.repository.admin.UserRepository;
import HcmuteConsultantServer.security.authentication.UserPrincipal;
import HcmuteConsultantServer.security.authentication.UserPrinciple;
import HcmuteConsultantServer.security.oauth2.user.OAuth2UserInfo;
import HcmuteConsultantServer.security.oauth2.user.OAuth2UserInfoFactory;

import java.util.Arrays;
import java.util.Optional;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository; // Thêm UserRepository để lưu thông tin UserInformationEntity

    @Override
    public OAuth2User loadUser(OAuth2UserRequest oAuth2UserRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(oAuth2UserRequest);

        try {
            return processOAuth2User(oAuth2UserRequest, oAuth2User);
        } catch (AuthenticationException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new InternalAuthenticationServiceException(ex.getMessage(), ex.getCause());
        }
    }

    private OAuth2User processOAuth2User(OAuth2UserRequest oAuth2UserRequest, OAuth2User oAuth2User) {
        OAuth2UserInfo oAuth2UserInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(oAuth2UserRequest.getClientRegistration().getRegistrationId(), oAuth2User.getAttributes());

        if (StringUtils.isEmpty(oAuth2UserInfo.getEmail())) {
            throw new OAuth2AuthenticationProcessingException("Email not found from OAuth2 provider");
        }

        Optional<AccountEntity> accountOptional = accountRepository.findByEmail(oAuth2UserInfo.getEmail());
        AccountEntity account;
        if (accountOptional.isPresent()) {
            account = accountOptional.get();
            if (!account.getProvider().equals(AuthProvider.valueOf(oAuth2UserRequest.getClientRegistration().getRegistrationId()))) {
                throw new OAuth2AuthenticationProcessingException("Looks like you're signed up with " +
                        account.getProvider() + " account. Please use your " + account.getProvider() +
                        " account to login.");
            }
            account = updateExistingAccount(account, oAuth2UserInfo);
            UserInformationEntity userInformation = account.getUserInformation();
            return UserPrinciple.create(userInformation, oAuth2User.getAttributes());
        } else {
            account = registerNewAccount(oAuth2UserRequest, oAuth2UserInfo);
            UserInformationEntity userInformation = account.getUserInformation();
            return UserPrinciple.create(userInformation, oAuth2User.getAttributes());        }
    }




    private AccountEntity registerNewAccount(OAuth2UserRequest oAuth2UserRequest, OAuth2UserInfo oAuth2UserInfo) {
        AccountEntity account = new AccountEntity();

        account.setProvider(AuthProvider.valueOf(oAuth2UserRequest.getClientRegistration().getRegistrationId()));
        account.setProviderId(oAuth2UserInfo.getId());
        account.setEmail(oAuth2UserInfo.getEmail());
        account.setUsername(oAuth2UserInfo.getName());

        String[] nameParts = oAuth2UserInfo.getName().split(" ");
        String firstname = nameParts.length > 1 ? String.join(" ", Arrays.copyOfRange(nameParts, 0, nameParts.length - 1)) : nameParts[0];
        String lastname = nameParts[nameParts.length - 1];

        UserInformationEntity userInformation = new UserInformationEntity();
        userInformation.setFirstName(firstname);
        userInformation.setLastName(lastname);
        userInformation.setAvatarUrl(oAuth2UserInfo.getImageUrl());
        userInformation.setAccount(account);

        account.setUserInformation(userInformation);

        RoleEntity role = roleRepository.findById(4).orElseThrow(() -> new Exceptions.ErrorException("Role not found with id 4"));
        account.setRole(role);
        accountRepository.save(account);
        userRepository.save(userInformation);

        return account;
    }

    private AccountEntity updateExistingAccount(AccountEntity existingAccount, OAuth2UserInfo oAuth2UserInfo) {
        existingAccount.setUsername(oAuth2UserInfo.getName());

        if (existingAccount.getUserInformation() != null) {
            existingAccount.getUserInformation().setFirstName(oAuth2UserInfo.getName());
            existingAccount.getUserInformation().setAvatarUrl(oAuth2UserInfo.getImageUrl());
        }

        accountRepository.save(existingAccount);

        return existingAccount;
    }
}
