package asegroup1.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author Greg Mitten
 * gregoryamitten@gmail.com
 */

@SpringBootApplication
@EnableAutoConfiguration
@ComponentScan
public class Application {
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
}
