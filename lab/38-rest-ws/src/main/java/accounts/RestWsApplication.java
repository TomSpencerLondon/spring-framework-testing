package accounts;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Import;

import config.AppConfig;

@SpringBootApplication
@Import(AppConfig.class)
@EntityScan("rewards.internal")
public class RestWsApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(RestWsApplication.class)
                .headless(false)
                .run(args);
    }
    
}
