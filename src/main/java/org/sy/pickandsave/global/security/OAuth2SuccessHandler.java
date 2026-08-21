package org.sy.pickandsave.global.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.sy.pickandsave.domain.users.entity.User;
import org.sy.pickandsave.domain.users.repository.UserRepository;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

	private final JwtTokenProvider jwtTokenProvider;
	private final UserRepository userRepository;

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
			throws IOException, ServletException {

		OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
		String email = oAuth2User.getAttribute("email");

		User user = userRepository.findByEmail(email);

		if (user != null) {
			String accessToken = jwtTokenProvider.createToken(user.getId(), user.getRole().name());

			// 프론트엔드 연동 리다이렉트 URL (개발 환경에 맞춰 수정 가능)
			String targetUrl = "http://localhost:3000/oauth2/redirect?token=" + accessToken;
			getRedirectStrategy().sendRedirect(request, response, targetUrl);
		}
	}
}