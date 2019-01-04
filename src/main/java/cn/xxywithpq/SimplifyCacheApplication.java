package cn.xxywithpq;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties
public class SimplifyCacheApplication {

    public static void main(String[] args) {
        SpringApplication.run(SimplifyCacheApplication.class, args);
    }

//    @Bean
//    RedissonClient redissonClient() {
//        Config config = new Config();
//        SingleServerConfig serverConfig = config.useSingleServer()
//                .setAddress("redis://" + redisProperties.getHost() + ":" + redisProperties.getPort())
//                .setTimeout((int) (redisProperties.getTimeout().getSeconds() * 1000))
//                .setDatabase(redisProperties.getDatabase());
//
//        if (StringUtils.isNotBlank(redisProperties.getPassword())) {
//            serverConfig.setPassword(redisProperties.getPassword());
//        }
//        return Redisson.create(config);
//    }
}

