package com.isom.dataserver;

import com.isom.dataserver.common.config.DataInitializer;
import com.isom.dataserver.common.config.Knife4jConfig;
import com.isom.dataserver.common.config.SaTokenConfig;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ComponentScan(
        basePackages = "com.isom.dataserver",
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
                        DataInitializer.class,
                        Knife4jConfig.class,
                        SaTokenConfig.class
                }),
                @ComponentScan.Filter(type = FilterType.REGEX, pattern = "com\\.isom\\.dataserver\\.module\\.(alert|api|app|auth|dashboard|doc|test|user)\\.(controller|service)\\..*"),
                @ComponentScan.Filter(type = FilterType.REGEX, pattern = "com\\.isom\\.dataserver\\.module\\.datasource\\.controller\\..*"),
                @ComponentScan.Filter(type = FilterType.REGEX, pattern = "com\\.isom\\.dataserver\\.module\\.datasource\\.service\\.impl\\..*"),
                @ComponentScan.Filter(type = FilterType.REGEX, pattern = "com\\.isom\\.dataserver\\.module\\.log\\.controller\\..*")
        }
)
@MapperScan("com.isom.dataserver.**.mapper")
@EnableScheduling
@EnableAsync
public class DataServerGatewayApplication {
    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(DataServerGatewayApplication.class);
        app.setAdditionalProfiles("gateway");
        app.run(args);
    }
}
