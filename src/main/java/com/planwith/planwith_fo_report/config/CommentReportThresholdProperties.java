package com.planwith.planwith_fo_report.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.comment-report")
public record CommentReportThresholdProperties(
		int hideThreshold
) {
}
