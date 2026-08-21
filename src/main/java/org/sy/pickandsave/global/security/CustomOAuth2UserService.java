package org.sy.pickandsave.global.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.sy.pickandsave.domain.users.entity.AuthProvider;
import org.sy.pickandsave.domain.users.entity.User;
import org.sy.pickandsave.domain.users.entity.UserPlan;
import org.sy.pickandsave.domain.users.entity.UserRole;
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

		// 1. 소셜 로그인 정보 추출
		String email = extractEmail(registrationId, attributes);
		String nickname = extractNickname(registrationId, attributes);
		String providerId = extractProviderId(registrationId, attributes);

		// 2. DB 저장 또는 업데이트
		saveOrUpdateUser(email, nickname, registrationId, providerId);

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
			// 네이버는 response 키 안에 최상위 데이터가 들어있음
			Map<String, Object> response = (Map<String, Object>) attributes.get("response");
			if (response != null && response.get("email") != null) {
				return (String) response.get("email");
			}
			return attributes.get("id") + "@naver.com"; // 예외 방지용 대체값
		} else if ("kakao".equals(registrationId)) {
			Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
			return (String) kakaoAccount.get("email");
		}
		throw new IllegalArgumentException("지원하지 않는 소셜 로그인입니다.");
	}

	private String extractNickname(String registrationId, Map<String, Object> attributes) {
		if ("google".equals(registrationId)) {
			return (String) attributes.get("name");
		} else if ("naver".equals(registrationId)) {
			Map<String, Object> response = (Map<String, Object>) attributes.get("response");
			if (response != null) {
				// 네이버는 name 또는 nickname 필드를 제공
				if (response.get("name") != null) {
					return (String) response.get("name");
				} else if (response.get("nickname") != null) {
					return (String) response.get("nickname");
				}
			}
			return "NaverUser";
		} else if ("kakao".equals(registrationId)) {
			Map<String, Object> properties = (Map<String, Object>) attributes.get("properties");
			return properties != null ? (String) properties.get("nickname") : "KakaoUser";
		}
		return "User";
	}

	private String extractProviderId(String registrationId, Map<String, Object> attributes) {
		if ("google".equals(registrationId)) {
			return (String) attributes.get("sub");
		} else if ("naver".equals(registrationId)) {
			Map<String, Object> response = (Map<String, Object>) attributes.get("response");
			if (response != null) {
				return (String) response.get("id"); // 네이버의 유저 고유 ID
			}
		} else if ("kakao".equals(registrationId)) {
			return String.valueOf(attributes.get("id"));
		}
		return null;
	}

	private User saveOrUpdateUser(String email, String nickname, String provider, String providerId) {
		User user = userRepository.findByEmail(email);

		String finalNickname =
				(nickname != null && !nickname.isBlank())
						? nickname
						: "User_" + System.currentTimeMillis() % 10000;

		// String -> AuthProvider Enum 변환 시 대문자 변환 적용
		AuthProvider authProvider = AuthProvider.valueOf(provider.toUpperCase());

		if (user == null) {
			user = User.builder()
					.email(email)
					.nickname(finalNickname)
					.provider(authProvider) // 대문자로 변환된 Enum 전달 (GOOGLE, NAVER, KAKAO)
					.providerId(providerId)
					.role(UserRole.ROLE_USER)
					.plan(UserPlan.FREE)
					.build();
			return userRepository.save(user);
		}
		return user;
	}
}