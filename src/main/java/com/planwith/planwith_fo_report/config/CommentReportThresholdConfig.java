package com.planwith.planwith_fo_report.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.planwith.planwith_fo_report.domain.report.CommentReportThreshold;

@Configuration
class CommentReportThresholdConfig {

	@Bean
	CommentReportThreshold commentReportThreshold(CommentReportThresholdProperties properties) {
		return CommentReportThreshold.of(properties.hideThreshold());
	}
}
