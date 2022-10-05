package com.zbinyds.reggie.config;


import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.zbinyds.reggie.commen.JacksonObjectMapper;
import com.zbinyds.reggie.intercept.LoginIntercept;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * @author zbinyds
 * @time 2022/08/16 10:46
 * <p>
 * 自定义配置类，用于配置项目中的其他配置项。
 */

@Configuration
public class ReggieConfig implements WebMvcConfigurer {
    /**
     * 员工未登录设置拦截器进行拦截
     *
     * @param registry
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LoginIntercept())
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/employee/login",
                        "/backend/**",
                        "/front/**",
                        "/user/sendMsg",
                        "/user/login"
                );
    }

    /**
     * mybatis-plus分页插件
     *
     * @return
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor mybatisPlusInterceptor = new MybatisPlusInterceptor();
        mybatisPlusInterceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return mybatisPlusInterceptor;
    }

    /**
     * 扩展SpringMVC的消息转换器。即底层使用jackson，而不使用默认的消息转换器。
     * 作用：
     *      将controller中方法的返回值，转换为json对象。因为Long类型id字段在进行传输的时候会导致精度丢失，
     *      所以我们直接将对象转成json，这样id字段值就会变成String类型，这样就能解决精度丢失问题。
     * @param converters
     */
    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        // 创建对象转换器
        MappingJackson2HttpMessageConverter messageConverter = new MappingJackson2HttpMessageConverter();
        // 设置对象转换器，底层使用Jackson将Java对象转换为Json
        messageConverter.setObjectMapper(new JacksonObjectMapper());
        // 将上面的消息转换器对象追加到SpringMVC框架的消息转换器容器中，并将索引设置为0（优先使用）。
        converters.add(0, messageConverter);
    }
}
