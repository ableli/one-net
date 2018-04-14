package com.weyong.onenet;

import io.netty.util.ResourceLeakDetector;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

/**
 * Created by hao.li on 4/3/2018.
 */
@EnableAutoConfiguration
@ComponentScan
public class OneNetClientApplication {
    public static void main(String... args) throws Exception {
        //ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.ADVANCED);
        SpringApplication application = new SpringApplication(OneNetClientApplication.class);
        application.run();
    }
}
