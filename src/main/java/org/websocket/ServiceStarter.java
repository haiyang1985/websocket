package org.websocket;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;

/**
 * @author hy_gu on 2019/3/8
 **/
@SpringBootApplication
public class ServiceStarter extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(ServiceStarter.class);
    }

    public static void main(String[] args) {
        SpringApplication.run(ServiceStarter.class);
    }
}
