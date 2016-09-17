package com.adblockers;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;

@SpringBootApplication
public class AdblockersBackendApplication {

	public static final String RESOURCES_PATH = new StringBuilder()
			.append(System.getProperty("user.dir"))
			.append(File.pathSeparator)
			.append("src")
			.append(File.pathSeparator)
			.append("main")
			.append(File.pathSeparator)
			.append("resources")
			.append(File.pathSeparator)
			.toString();
	public static void main(String[] args) {
		SpringApplication.run(AdblockersBackendApplication.class, args);
	}
}
