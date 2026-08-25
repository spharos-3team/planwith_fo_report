package com.planwith.planwith_fo_report.adapter.in.web.auth;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class GatewayReportAuthorizationFilterTest {

	private final GatewayReportAuthorizationFilter filter = new GatewayReportAuthorizationFilter(true);

	@Test
	void allowsAuthenticatedMemberRequest() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/planwith-fo-report/reports/comments/id");
		request.addHeader(GatewayReportAuthorizationFilter.AUTH_USER_ID, "11111111-1111-1111-1111-111111111111");
		MockHttpServletResponse response = new MockHttpServletResponse();

		filter.doFilter(request, response, new MockFilterChain());

		assertThat(response.getStatus()).isEqualTo(200);
	}

	@Test
	void rejectsMissingAuthentication() throws Exception {
		MockHttpServletResponse response = new MockHttpServletResponse();
		filter.doFilter(new MockHttpServletRequest("POST", "/api/planwith-fo-report/reports"), response, new MockFilterChain());
		assertThat(response.getStatus()).isEqualTo(401);
	}

	@Test
	void rejectsNonAdminWorkflowRequest() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/planwith-fo-report/reports/id/workflow");
		request.addHeader(GatewayReportAuthorizationFilter.AUTH_USER_ID, "11111111-1111-1111-1111-111111111111");
		request.addHeader(GatewayReportAuthorizationFilter.AUTH_ROLES, "ROLE_USER");
		MockHttpServletResponse response = new MockHttpServletResponse();
		filter.doFilter(request, response, new MockFilterChain());
		assertThat(response.getStatus()).isEqualTo(403);
	}
}
