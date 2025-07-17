package com.zheng.aicommunitybackend;

import com.huaban.analysis.jieba.JiebaSegmenter;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.crypto.SecretKey;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@SpringBootTest
class AiCommunityBackendApplicationTests {

    @Test
    void contextLoads() {
        String content = "我我我我我我我我爱中国";
        JiebaSegmenter jiebaSegmenter = new JiebaSegmenter();
        List<String> list = jiebaSegmenter.sentenceProcess(content);
        Map<String, Long> map = list.stream().collect(Collectors.groupingBy(
                item -> item,
                Collectors.counting()
        ));
        for(String key : map.keySet()){
            System.out.println("key:" + key + " value:" + map.get(key));
        }
    }

}
