package org.humancellatlas.ingest.ingestbroker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@SpringBootApplication
public class IngestBrokerApplication {

	public static void main(String[] args) {
		SpringApplication.run(IngestBrokerApplication.class, args);
	}
}
