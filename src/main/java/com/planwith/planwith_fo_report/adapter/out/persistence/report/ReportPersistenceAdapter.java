package com.planwith.planwith_fo_report.adapter.out.persistence.report;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_report.application.report.port.out.ReportRepositoryPort;
import com.planwith.planwith_fo_report.domain.report.Report;
import com.planwith.planwith_fo_report.domain.report.TargetType;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
class ReportPersistenceAdapter implements ReportRepositoryPort {

	private final ReportJpaRepository reportJpaRepository;

	@Override
	@Transactional
	public Report save(Report report) {
		Optional<ReportJpaEntity> existing = reportJpaRepository.findByReportUuid(report.getReportUuid().toString());
		ReportJpaEntity entity = existing.orElseGet(() -> ReportJpaEntity.fromDomain(report));

		if (existing.isPresent()) {
			entity.updateFromDomain(
					report.getStatus(),
					report.getReviewerUuid() == null ? null : report.getReviewerUuid().toString(),
					report.getReviewComment(),
					report.getUpdatedAt(),
					report.getReviewedAt(),
					report.getActionedAt()
			);
		}

		ReportJpaEntity saved = reportJpaRepository.save(entity);
		log.debug("ReportPersistenceAdapter : save : 신고 저장 완료 - reportUuid={}, status={}",
				saved.getReportUuid(),
				saved.getStatus());
		return toDomain(saved);
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<Report> findByReportUuid(UUID reportUuid) {
		return reportJpaRepository.findByReportUuid(reportUuid.toString())
				.map(ReportPersistenceAdapter::toDomain);
	}

	@Override
	@Transactional(readOnly = true)
	public boolean existsByReporterAndTarget(UUID reporterUuid, TargetType targetType, UUID targetUuid) {
		return reportJpaRepository.existsByReporterUuidAndTargetTypeAndTargetUuid(
				reporterUuid.toString(),
				targetType,
				targetUuid.toString()
		);
	}

	private static Report toDomain(ReportJpaEntity entity) {
		return Report.restore(
				UUID.fromString(entity.getReportUuid()),
				UUID.fromString(entity.getReporterUuid()),
				entity.getTargetType(),
				UUID.fromString(entity.getTargetUuid()),
				entity.getReason(),
				entity.getDetail(),
				entity.getStatus(),
				entity.getReviewerUuid() == null ? null : UUID.fromString(entity.getReviewerUuid()),
				entity.getReviewComment(),
				entity.getCreatedAt(),
				entity.getUpdatedAt(),
				entity.getReviewedAt(),
				entity.getActionedAt()
		);
	}
}
