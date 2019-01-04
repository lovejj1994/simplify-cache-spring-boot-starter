package cn.xxywithpq;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties
public class SimplifyCacheApplication {

    public static void main(String[] args) {
        SpringApplication.run(SimplifyCacheApplication.class, args);
    }

}

