package com.isom.dataserver;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@ComponentScan(
        basePackages = "com.isom.dataserver",
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.REGEX, pattern = "com\\.isom\\.dataserver\\.module\\.gateway\\.(auth|chain|controller|handler|log|ratelimit|route)\\..*")
        }
)
@MapperScan("com.isom.dataserver.**.mapper")
@EnableAsync
public class DataServerAdminApplication {
    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(DataServerAdminApplication.class);
        app.setAdditionalProfiles("admin");
        app.run(args);
    }
}
