package com.adblockers;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AdblockersBackendApplication {

	public static final String RESOURCES_PATH = System.getProperty("user.dir") + "/src/main/resources/";
	public static void main(String[] args) {
		SpringApplication.run(AdblockersBackendApplication.class, args);
	}
}
