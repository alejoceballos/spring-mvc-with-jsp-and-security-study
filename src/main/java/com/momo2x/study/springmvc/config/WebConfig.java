package com.momo2x.study.springmvc.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import static com.momo2x.study.springmvc.config.ViewPath.URL;

@Configuration
public class WebConfig {

    @Bean
    public WebMvcConfigurer webMvcConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addViewControllers(@NonNull final ViewControllerRegistry registry) {
                registry.addViewController(URL.ROOT.PATH).setViewName("index");
                registry.addViewController(URL.ROOT.OTHER).setViewName("other");

                registry.addViewController(URL.USER.MAIN).setViewName("secured/user/main");
                registry.addViewController(URL.USER.OTHER).setViewName("secured/user/other-main");

                registry.addViewController(URL.ADMIN.ADMIN).setViewName("secured/admin/admin");
                registry.addViewController(URL.ADMIN.OTHER).setViewName("secured/admin/other-admin");
            }
        };
    }

}
