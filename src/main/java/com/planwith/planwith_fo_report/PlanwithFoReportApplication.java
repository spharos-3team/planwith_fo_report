package com.planwith.planwith_fo_report;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.planwith.planwith_fo_report.config.AuthProperties;
import com.planwith.planwith_fo_report.config.DeployProperties;
import com.planwith.planwith_fo_report.config.KafkaAppProperties;
import com.planwith.planwith_fo_report.config.OutboxProperties;

@SpringBootApplication
@EnableConfigurationProperties({
		AuthProperties.class,
		DeployProperties.class,
		KafkaAppProperties.class,
		OutboxProperties.class
})
public class PlanwithFoReportApplication {

	public static void main(String[] args) {
		SpringApplication.run(PlanwithFoReportApplication.class, args);
	}

}
