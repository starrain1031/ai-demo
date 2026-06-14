package org.starry.aidemo.config;


import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configures MVC behavior shared by the REST API.
 */
@Configuration
public class MvcConfiguration implements WebMvcConfigurer {

    /**
     * Allows the frontend development server to call backend APIs and read download headers.
     *
     * @param registry CORS registry provided by Spring MVC
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("Content-Disposition");
    }
}
