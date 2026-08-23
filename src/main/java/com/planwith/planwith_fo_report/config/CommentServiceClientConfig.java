package com.planwith.planwith_fo_report.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class CommentServiceClientConfig {

	@Bean
	RestClient.Builder restClientBuilder() {
		return RestClient.builder();
	}
}
