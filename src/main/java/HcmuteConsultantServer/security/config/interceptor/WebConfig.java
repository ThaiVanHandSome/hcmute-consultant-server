// package HcmuteConsultantServer.security.config.interceptor;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.web.filter.ForwardedHeaderFilter;
// import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
// import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// @Configuration
// public class WebConfig implements WebMvcConfigurer {

//     @Autowired
//     private AccountStatusInterceptor accountStatusInterceptor;

//     @Override
//     public void addInterceptors(InterceptorRegistry registry) {
//         registry.addInterceptor(accountStatusInterceptor)
//                 .addPathPatterns("/api/v1/**");
//     }
// }
