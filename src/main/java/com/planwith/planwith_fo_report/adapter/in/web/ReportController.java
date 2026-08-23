package com.planwith.planwith_fo_report.adapter.in.web;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.planwith.planwith_fo_report.adapter.in.web.dto.CreateReportRequest;
import com.planwith.planwith_fo_report.adapter.in.web.dto.ReportResponse;
import com.planwith.planwith_fo_report.adapter.in.web.dto.ReviewReportRequest;
import com.planwith.planwith_fo_report.application.report.port.in.CreateReportUseCase;
import com.planwith.planwith_fo_report.application.report.port.in.GetReportUseCase;
import com.planwith.planwith_fo_report.application.report.port.in.ReviewReportUseCase;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/planwith-fo-report/reports")
@Tag(name = "report", description = "신고 접수 및 워크플로우 API")
public class ReportController {

	private final CreateReportUseCase createReportUseCase;
	private final GetReportUseCase getReportUseCase;
	private final ReviewReportUseCase reviewReportUseCase;

	// 신고 생성
	@PostMapping
	@Operation(summary = "신고 생성", description = "STORY 또는 COMMENT를 target_uuid 기준으로 신고한다.")
	public ResponseEntity<ReportResponse> createReport(@Valid @RequestBody CreateReportRequest request) {
		log.info("ReportController : POST createReport : 신고 생성 요청");
		ReportResponse response = ReportResponse.from(createReportUseCase.createReport(request.toCommand()));
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	// 신고 단건 조회
	@GetMapping("/{reportUuid}")
	@Operation(summary = "신고 조회", description = "reportUuid로 신고를 조회한다.")
	public ResponseEntity<ReportResponse> getReport(@PathVariable UUID reportUuid) {
		log.info("ReportController : GET getReport : 신고 조회 요청");
		return ResponseEntity.ok(ReportResponse.from(getReportUseCase.getReport(reportUuid)));
	}

	// 신고 워크플로우 처리
	@PostMapping("/{reportUuid}/workflow")
	@Operation(summary = "신고 워크플로우 처리", description = "RECEIVED → REVIEWING → APPROVED|REJECTED → ACTIONED 상태를 전이한다.")
	public ResponseEntity<ReportResponse> reviewReport(
			@PathVariable UUID reportUuid,
			@Valid @RequestBody ReviewReportRequest request
	) {
		log.info("ReportController : POST reviewReport : 신고 워크플로우 처리 요청");
		return ResponseEntity.ok(ReportResponse.from(reviewReportUseCase.reviewReport(request.toCommand(reportUuid))));
	}
}
