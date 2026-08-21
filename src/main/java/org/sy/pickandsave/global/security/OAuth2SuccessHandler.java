package org.sy.pickandsave.global.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import org.sy.pickandsave.domain.users.entity.User;
import org.sy.pickandsave.domain.users.repository.UserRepository;

import java.io.IOException;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

	private final JwtTokenProvider jwtTokenProvider;
	private final UserRepository userRepository;

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
			throws IOException, ServletException {
		OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

		// CustomOAuth2UserService에서 구한 이메일 등으로 유저 조회
		// (소셜 제공자별 키값 처리 또는 attributes 활용)
		String email = extractEmailFromOAuth2User(oAuth2User);
		User user = userRepository.findByEmail(email);

		if (user == null) {
			throw new IllegalStateException("해당 이메일로 가입된 유저를 찾을 수 없습니다: " + email);
		}

		// JWT AccessToken 생성
		String token = jwtTokenProvider.createToken(user.getId(), user.getRole().name());

		// 프론트엔드로 리다이렉트 (토큰을 Query Parameter로 전달)
		String targetUrl = UriComponentsBuilder.fromUriString("http://localhost:3000/oauth2/redirect")
				.queryParam("token", token)
				.build().toUriString();

		getRedirectStrategy().sendRedirect(request, response, targetUrl);
	}

	private String extractEmailFromOAuth2User(OAuth2User oAuth2User) {
		Map<String, Object> attributes = oAuth2User.getAttributes();

		// 1. 네이버인 경우 (response 내부 확인)
		if (attributes.containsKey("response")) {
			Map<String, Object> response = (Map<String, Object>) attributes.get("response");
			if (response != null && response.get("email") != null) {
				return (String) response.get("email");
			}
			return response.get("id") + "@naver.com";
		}

		// 2. 카카오인 경우 (kakao_account 내부 확인)
		if (attributes.containsKey("kakao_account")) {
			Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
			if (kakaoAccount != null && kakaoAccount.get("email") != null) {
				return (String) kakaoAccount.get("email");
			}
			return attributes.get("id") + "@kakao.com";
		}

		// 3. 구글 등 일반적인 경우
		return (String) attributes.get("email");
	}
}