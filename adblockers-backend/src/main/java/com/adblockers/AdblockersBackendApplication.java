package com.adblockers;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;

@SpringBootApplication
public class AdblockersBackendApplication {

	// Supposing that the JAR file is run from the root of the adblockers-backend project
	public static final String RESOURCES_PATH = new StringBuilder()
			.append(System.getProperty("user.dir")).append(File.separator)
			.append("src").append(File.separator)
			.append("main").append(File.separator)
			.append("resources").append(File.separator)
			.toString();
	public static void main(String[] args) {
		SpringApplication.run(AdblockersBackendApplication.class, args);
	}
}
