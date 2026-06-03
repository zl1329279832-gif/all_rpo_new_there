package com.wms.config;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.servlet.mvc.method.RequestMappingInfoHandlerMapping;
import springfox.documentation.spring.web.plugins.WebMvcRequestHandlerProvider;

import java.lang.reflect.Field;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
public class SpringfoxFixConfig {

    @Bean
    public BeanPostProcessor springfoxHandlerProviderBeanPostProcessor() {
        return new BeanPostProcessor() {
            @Override
            public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
                if (bean instanceof WebMvcRequestHandlerProvider) {
                    try {
                        Field field = ReflectionUtils.findField(WebMvcRequestHandlerProvider.class, "handlerMappings");
                        if (field != null) {
                            field.setAccessible(true);
                            List<RequestMappingInfoHandlerMapping> handlerMappings =
                                    (List<RequestMappingInfoHandlerMapping>) field.get(bean);
                            if (handlerMappings != null) {
                                List<RequestMappingInfoHandlerMapping> filtered = handlerMappings.stream()
                                        .filter(mapping -> mapping.getPatternParser() == null)
                                        .collect(Collectors.toList());
                                field.set(bean, filtered);
                            }
                        }
                    } catch (Exception e) {
                        // ignore
                    }
                }
                return bean;
            }
        };
    }
}
