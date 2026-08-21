package org.sy.pickandsave.global.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.sy.pickandsave.domain.users.entity.UserRole;
import org.sy.pickandsave.domain.users.entity.User;
import org.sy.pickandsave.domain.users.entity.UserPlan;
import org.sy.pickandsave.domain.users.repository.UserRepository;

import java.util.Collections;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

	private final UserRepository userRepository;

	@Override
	public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
		OAuth2User oAuth2User = super.loadUser(userRequest);

		String registrationId = userRequest.getClientRegistration().getRegistrationId();
		String userNameAttributeName = userRequest.getClientRegistration().getProviderDetails()
				.getUserInfoEndpoint().getUserNameAttributeName();

		Map<String, Object> attributes = oAuth2User.getAttributes();
		String email = extractEmail(registrationId, attributes);

		saveOrUpdateUser(email);

		return new DefaultOAuth2User(
				Collections.singleton(new SimpleGrantedAuthority(UserRole.ROLE_USER.name())),
				attributes,
				userNameAttributeName
		);
	}

	private String extractEmail(String registrationId, Map<String, Object> attributes) {
		if ("google".equals(registrationId)) {
			return (String) attributes.get("email");
		} else if ("naver".equals(registrationId)) {
			Map<String, Object> response = (Map<String, Object>) attributes.get("response");
			return (String) response.get("email");
		} else if ("kakao".equals(registrationId)) {
			Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
			return (String) kakaoAccount.get("email");
		}
		throw new IllegalArgumentException("지원하지 않는 소셜 로그인입니다.");
	}

	private User saveOrUpdateUser(String email) {
		User user = userRepository.findByEmail(email);
		if (user == null) {
			user = User.builder()
					.email(email)
					.role(UserRole.ROLE_USER)
					.plan(UserPlan.FREE)
					.build();
			return userRepository.save(user);
		}
		return user;
	}
}