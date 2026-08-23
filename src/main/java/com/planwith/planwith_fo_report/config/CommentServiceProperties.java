package com.planwith.planwith_fo_report.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.comment-service")
public record CommentServiceProperties(
		String baseUrl,
		String reportContextPath
) {
}
