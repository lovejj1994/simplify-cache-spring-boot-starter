package cn.xxywithpq;

import cn.xxywithpq.cache.CachesJsonUtil;
import com.alibaba.fastjson.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SimplifyCacheApplicationTests {

    @Autowired
    CachesJsonUtil cachesJsonUtil;

    @Test
    public void contextLoads() throws InterruptedException {
        JSONObject test = cachesJsonUtil.get("test", "111", JSONObject.class);

        if (null == test) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("222", 333);
            cachesJsonUtil.set("test", "111", jsonObject);
        }
        Thread.sleep(2000);
//        再取一遍
        JSONObject test2 = cachesJsonUtil.get("test", "111", JSONObject.class);

//        删除
        cachesJsonUtil.delete("test", "111");
    }

}

