package com.planwith.planwith_fo_report.application.report.port.out;

import java.util.Optional;
import java.util.UUID;

import com.planwith.planwith_fo_report.domain.report.Report;
import com.planwith.planwith_fo_report.domain.report.TargetType;

public interface ReportRepositoryPort {

	Report save(Report report);

	Optional<Report> findByReportUuid(UUID reportUuid);

	boolean existsByReporterAndTarget(UUID reporterUuid, TargetType targetType, UUID targetUuid);
}
