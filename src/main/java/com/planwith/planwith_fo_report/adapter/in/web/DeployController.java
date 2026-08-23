package com.planwith.planwith_fo_report.adapter.in.web;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.planwith.planwith_fo_report.adapter.in.web.auth.AuthService;
import com.planwith.planwith_fo_report.adapter.in.web.dto.LoginRequest;
import com.planwith.planwith_fo_report.adapter.in.web.dto.LoginResponse;
import com.planwith.planwith_fo_report.config.DeployProperties;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Validated
@RestController
@RequestMapping("/api/planwith-fo-report")
@Tag(name = "planwith-fo-report", description = "Server notebook deploy verification API")
public class DeployController {

	private final AuthService authService;
	private final DeployProperties deployProperties;

	public DeployController(AuthService authService, DeployProperties deployProperties) {
		this.authService = authService;
		this.deployProperties = deployProperties;
	}

	// 배포 확인
	@GetMapping("/deploy-check")
	@Operation(summary = "Deploy check", description = "Returns a marker string to verify push-deploy on the server notebook.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Service is running")
	})
	public ResponseEntity<Map<String, String>> deployCheck() {
		log.info("DeployController : GET deployCheck : 배포 확인 요청");
		return ResponseEntity.ok(Map.of(
				"service", "planwith-fo-report",
				"marker", deployProperties.marker(),
				"message", "planwith-fo-report deploy pipeline ok"
		));
	}

	// 배포 확인용 로그인
	@PostMapping("/login")
	@Operation(summary = "Login", description = "Authenticates a user with an ID and password.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Login succeeded"),
			@ApiResponse(responseCode = "400", description = "Invalid request"),
			@ApiResponse(responseCode = "401", description = "Invalid credentials")
	})
	public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
		log.info("DeployController : POST login : 배포 확인용 로그인 요청");
		return ResponseEntity.ok(authService.login(request));
	}
}
