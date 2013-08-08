package hello;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.social.UserIdSource;
import org.springframework.social.connect.ConnectionFactoryLocator;
import org.springframework.social.connect.ConnectionRepository;
import org.springframework.social.connect.web.ConnectController;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@Configuration
@EnableAutoConfiguration
@EnableWebMvc
@Import(FacebookConfig.class)
@ComponentScan
public class Application {

    @Bean
    public ConnectController connectController(ConnectionFactoryLocator connectionFactoryLocator, ConnectionRepository connectionRepository) {
        return new ConnectController(connectionFactoryLocator, connectionRepository);
    }

    @Bean
    public UserIdSource userIdSource() {
        return new UserIdSource() {
            @Override
            public String getUserId() {
                return "testuser";
            }
        };
    }

    /*
     * SPRING BOOTSTRAP MAIN
     */
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

} 