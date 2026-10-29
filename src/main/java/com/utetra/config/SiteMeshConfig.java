package com.utetra.config;

import org.sitemesh.builder.SiteMeshFilterBuilder;
import org.sitemesh.config.ConfigurableSiteMeshFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * SiteMesh boc moi trang HTML vao layout chung:
 * /admin/** -> admin.jsp, /staff/** -> staff.jsp, con lai -> main.jsp.
 */
@Configuration
public class SiteMeshConfig {

    public static class UtetraSiteMeshFilter extends ConfigurableSiteMeshFilter {
        @Override
        protected void applyCustomConfiguration(SiteMeshFilterBuilder builder) {
            // SiteMesh tu them tien to /WEB-INF/decorators/ nen o day chi ghi ten file (khong co dau / o dau)
            builder.setIncludeErrorPages(true) // trang loi (403, 404) cung duoc boc layout
                   .addDecoratorPath("/admin/*", "admin.jsp")
                   .addDecoratorPath("/staff/*", "staff.jsp")
                   .addDecoratorPath("/*", "main.jsp")
                   .addExcludedPath("/webjars/*")
                   .addExcludedPath("/css/*")
                   .addExcludedPath("/js/*")
                   .addExcludedPath("/images/*")
                   .addExcludedPath("/api/*")
                   .addExcludedPath("/ws*");
        }
    }

    @Bean
    public FilterRegistrationBean<UtetraSiteMeshFilter> siteMeshFilter() {
        FilterRegistrationBean<UtetraSiteMeshFilter> registration =
                new FilterRegistrationBean<>(new UtetraSiteMeshFilter());
        registration.addUrlPatterns("/*");
        // ERROR: de trang loi (vd 404) cung duoc boc layout
        registration.setDispatcherTypes(java.util.EnumSet.of(
                jakarta.servlet.DispatcherType.REQUEST, jakarta.servlet.DispatcherType.ERROR));
        return registration;
    }
}
