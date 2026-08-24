package com.planwith.planwith_fo_report.adapter.in.web.auth;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class GatewayReportAuthorizationFilter extends OncePerRequestFilter {

	static final String AUTH_USER_ID = "X-Auth-User-Id";
	static final String AUTH_ROLES = "X-Auth-Roles";
	static final String MEMBER_UUID_ALIAS = "X-Member-Uuid";
	private static final String API_PREFIX = "/api/planwith-fo-report/";

	private final boolean enabled;

	public GatewayReportAuthorizationFilter(
			@Value("${app.gateway.auth-header-mapping-enabled:true}") boolean enabled
	) {
		this.enabled = enabled;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		if (!enabled || !requiresAuthentication(request)) {
			filterChain.doFilter(request, response);
			return;
		}
		String memberUuid = request.getHeader(AUTH_USER_ID);
		if (memberUuid == null || memberUuid.isBlank()) {
			log.warn("GatewayReportAuthorizationFilter : doFilterInternal : 인증 회원 헤더 누락");
			writeError(response, HttpServletResponse.SC_UNAUTHORIZED, "UNAUTHENTICATED_MEMBER", "로그인 회원 정보가 없습니다.");
			return;
		}
		if (requiresAdmin(request) && !hasAdminRole(request.getHeader(AUTH_ROLES))) {
			log.warn("GatewayReportAuthorizationFilter : doFilterInternal : 신고 관리 권한 부족 - memberUuid={}", memberUuid);
			writeError(response, HttpServletResponse.SC_FORBIDDEN, "ADMIN_REQUIRED", "관리자 권한이 필요합니다.");
			return;
		}
		filterChain.doFilter(new TrustedIdentityRequest(request), response);
	}

	private boolean requiresAuthentication(HttpServletRequest request) {
		String path = request.getRequestURI();
		return path.startsWith(API_PREFIX)
				&& !path.equals(API_PREFIX + "deploy-check")
				&& !path.equals(API_PREFIX + "login");
	}

	private boolean requiresAdmin(HttpServletRequest request) {
		String path = request.getRequestURI();
		return ("GET".equals(request.getMethod()) && path.startsWith(API_PREFIX + "reports/"))
				|| path.endsWith("/workflow");
	}

	private boolean hasAdminRole(String roles) {
		if (roles == null) {
			return false;
		}
		for (String role : roles.split(",")) {
			if ("ADMIN".equalsIgnoreCase(role.trim()) || "ROLE_ADMIN".equalsIgnoreCase(role.trim())) {
				return true;
			}
		}
		return false;
	}

	private void writeError(HttpServletResponse response, int status, String code, String message) throws IOException {
		response.setStatus(status);
		response.setCharacterEncoding("UTF-8");
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.getWriter().write("{\"status\":" + status + ",\"code\":\"" + code + "\",\"message\":\"" + message + "\"}");
	}

	private static final class TrustedIdentityRequest extends HttpServletRequestWrapper {
		private TrustedIdentityRequest(HttpServletRequest request) {
			super(request);
		}

		@Override
		public String getHeader(String name) {
			if (MEMBER_UUID_ALIAS.equalsIgnoreCase(name)) {
				return super.getHeader(AUTH_USER_ID);
			}
			return super.getHeader(name);
		}

		@Override
		public Enumeration<String> getHeaders(String name) {
			String value = getHeader(name);
			return value == null ? Collections.emptyEnumeration() : Collections.enumeration(Collections.singleton(value));
		}
	}
}
