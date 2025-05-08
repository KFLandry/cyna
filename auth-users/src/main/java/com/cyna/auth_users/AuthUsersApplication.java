package com.cyna.auth_users;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.cyna.auth_users")
public class AuthUsersApplication {

	public static void main(String[] args) {
		SpringApplication.run(AuthUsersApplication.class, args);
	}
}
