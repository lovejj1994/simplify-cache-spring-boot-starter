# simplify-cache-spring-boot-starter
提供一个二级缓存组件 for spring boot，第一级为 caffeine ，第二级为 redis

测试web项目在
[simplify-cache-spring-boot-starter-test](https://github.com/lovejj1994/simplify-cache-spring-boot-starter-test) 



范例配置文件 application.properties

namespace为一个命名空间，region为区域，不同区域可以有不同配置。
下面namespace为user，region为test，一级缓存(Caffeine)最大容量为100，存活时间120000毫秒，redis存活时间跟一级同步(sync-redis-ttl)

```
simplify.cache.region.test=100,120000
simplify.cache.namespace=user
simplify.cache.sync-redis-ttl.test=true
spring.redis.host=1234
spring.redis.port=6379
spring.redis.timeout=PT10S
spring.redis.password=1234
spring.redis.database=14

```
