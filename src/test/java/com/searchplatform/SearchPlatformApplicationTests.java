package com.searchplatform;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.elasticsearch.uris=http://localhost:9200"
})
class SearchPlatformApplicationTests {

    @Test
    void contextLoads() {
    }
}
