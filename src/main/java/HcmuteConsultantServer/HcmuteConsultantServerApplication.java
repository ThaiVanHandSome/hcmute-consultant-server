package HcmuteConsultantServer;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SpringBootApplication
@EnableScheduling
public class HcmuteConsultantServerApplication {
    private static final Logger logger = LoggerFactory.getLogger(HcmuteConsultantServerApplication.class);

    public static void main(String[] args) {
        Dotenv dotenv = null;

        // Luôn đặt RAILWAY_ENV=true để ứng dụng luôn chạy ở chế độ Railway
        System.setProperty("RAILWAY_ENV", "true");
        boolean isRailwayEnv = true;

        logger.info("Running in Railway environment. Using environment variables.");
        // Log các biến môi trường (chỉ tên, không log giá trị vì lý do bảo mật)
        logger.info("Environment variables available: DB_URL={}, DB_USERNAME={}, JWT_SECRET={}, etc.",
                System.getenv("DB_URL") != null ? "set" : "not set",
                System.getenv("DB_USERNAME") != null ? "set" : "not set",
                System.getenv("JWT_SECRET") != null ? "set" : "not set");

        SpringApplication app = new SpringApplication(HcmuteConsultantServerApplication.class);
        ConfigurableEnvironment environment = new StandardEnvironment();

        environment.getSystemProperties().put("SERVER_PORT",
                System.getenv("SERVER_PORT") != null ? System.getenv("SERVER_PORT") : "8080");

        environment.getSystemProperties().put("spring.datasource.url",
                System.getenv("DB_URL") != null ? System.getenv("DB_URL") : "");

        environment.getSystemProperties().put("spring.datasource.username",
                System.getenv("DB_USERNAME") != null ? System.getenv("DB_USERNAME") : "");

        environment.getSystemProperties().put("spring.datasource.password",
                System.getenv("DB_PASSWORD") != null ? System.getenv("DB_PASSWORD") : "");

        environment.getSystemProperties().put("jwt.secret",
                System.getenv("JWT_SECRET") != null ? System.getenv("JWT_SECRET") : "");

        environment.getSystemProperties().put("spring.mail.host",
                System.getenv("MAIL_HOST") != null ? System.getenv("MAIL_HOST") : "smtp.gmail.com");

        environment.getSystemProperties().put("spring.mail.port",
                System.getenv("MAIL_PORT") != null ? System.getenv("MAIL_PORT") : "587");

        environment.getSystemProperties().put("spring.mail.username",
                System.getenv("MAIL_USERNAME") != null ? System.getenv("MAIL_USERNAME") : "");

        environment.getSystemProperties().put("spring.mail.password",
                System.getenv("MAIL_PASSWORD") != null ? System.getenv("MAIL_PASSWORD") : "");

        environment.getSystemProperties().put("base.url",
                System.getenv("BASE_URL") != null ? System.getenv("BASE_URL") : "");

        environment.getSystemProperties().put("spring.security.oauth2.client.registration.google.client-id",
                System.getenv("GOOGLE_CLIENT_ID") != null ? System.getenv("GOOGLE_CLIENT_ID") : "");

        environment.getSystemProperties().put("spring.security.oauth2.client.registration.google.client-secret",
                System.getenv("GOOGLE_CLIENT_SECRET") != null ? System.getenv("GOOGLE_CLIENT_SECRET") : "");

        environment.getSystemProperties().put("spring.security.oauth2.client.registration.google.redirect-uri",
                System.getenv("REDIRECT_URI") != null ? System.getenv("REDIRECT_URI") : "");

        environment.getSystemProperties().put("app.oauth2.authorizedRedirectUris",
                System.getenv("AUTHORIZED_REDIRECT_URI") != null ? System.getenv("AUTHORIZED_REDIRECT_URI") : "");

        app.setEnvironment(environment);
        app.run(args);
    }
}


