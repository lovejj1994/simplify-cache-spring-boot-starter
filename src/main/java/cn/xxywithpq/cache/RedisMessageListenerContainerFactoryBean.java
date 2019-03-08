package cn.xxywithpq.cache;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;


@Slf4j
public class RedisMessageListenerContainerFactoryBean implements FactoryBean<RedisMessageListenerContainer> {

    @Autowired
    RedisConnectionFactory connectionFactory;


    public RedisMessageListenerContainerFactoryBean() {
    }

    @Override
    public RedisMessageListenerContainer getObject() {
        RedisMessageListenerContainer redisMessageListenerContainer = new RedisMessageListenerContainer();
        redisMessageListenerContainer.setConnectionFactory(connectionFactory);
        return redisMessageListenerContainer;
    }

    @Override
    public Class<?> getObjectType() {
        return RedisMessageListenerContainer.class;
    }


    @Override
    public boolean isSingleton() {
        return true;
    }


}
