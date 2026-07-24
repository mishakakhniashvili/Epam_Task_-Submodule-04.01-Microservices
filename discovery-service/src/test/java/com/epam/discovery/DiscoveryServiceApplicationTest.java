package com.epam.discovery;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        properties = {
                "eureka.client.register-with-eureka=false",
                "eureka.client.fetch-registry=false"
        }
)
class DiscoveryServiceApplicationTest {

    @Test
    void contextLoads() {
    }
}
