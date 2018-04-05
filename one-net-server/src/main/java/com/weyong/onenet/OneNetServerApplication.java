package com.weyong.onenet;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Created by hao.li on 4/3/2018.
 */
@EnableAutoConfiguration
public class OneNetServerApplication {
    public static void main(String... args) throws Exception {
        SpringApplication application = new SpringApplication(OneNetServerApplication.class);
        application.run();
    }
}
