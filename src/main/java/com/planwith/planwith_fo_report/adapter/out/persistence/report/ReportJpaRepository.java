package com.planwith.planwith_fo_report.adapter.out.persistence.report;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.planwith.planwith_fo_report.domain.report.TargetType;

interface ReportJpaRepository extends JpaRepository<ReportJpaEntity, Long> {

	Optional<ReportJpaEntity> findByReportUuid(String reportUuid);

	boolean existsByReporterUuidAndTargetTypeAndTargetUuid(
			String reporterUuid,
			TargetType targetType,
			String targetUuid
	);
}
