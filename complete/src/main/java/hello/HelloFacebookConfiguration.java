package hello;

import javax.inject.Inject;

import org.springframework.bootstrap.SpringApplication;
import org.springframework.bootstrap.context.annotation.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.social.UserIdSource;
import org.springframework.social.config.annotation.EnableInMemoryConnectionRepository;
import org.springframework.social.connect.ConnectionFactoryLocator;
import org.springframework.social.connect.ConnectionRepository;
import org.springframework.social.connect.web.ConnectController;
import org.springframework.social.facebook.config.annotation.EnableFacebook;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.thymeleaf.spring3.SpringTemplateEngine;
import org.thymeleaf.spring3.view.ThymeleafViewResolver;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;
import org.thymeleaf.templateresolver.TemplateResolver;

@Configuration
@EnableAutoConfiguration
@EnableWebMvc
@EnableFacebook(appId="489554041098344", appSecret="1bd6efb4c36b48923970bec828d25532")
@EnableInMemoryConnectionRepository
@ComponentScan
public class HelloFacebookConfiguration {

	@Inject
	private ConnectionFactoryLocator cfl;
	
	/*
	 * SPRING SOCIAL CONFIG
	 */
	
//	@Bean
//	public InMemoryUsersConnectionRepository usersConnectionRepository() {
//		return new InMemoryUsersConnectionRepository(cfl);
//	}
//
//	@Bean
//	public ConnectionRepository connectionRepository() {
//		return usersConnectionRepository().createConnectionRepository(userIdSource().getUserId());
//	}

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
	 * THYMELEAF CONFIG
	 */

	@Bean
	public TemplateResolver templateResolver() {
		ServletContextTemplateResolver resolver = new ServletContextTemplateResolver();
		resolver.setPrefix("/");
		resolver.setSuffix(".html");
		resolver.setTemplateMode("HTML5");
		return resolver;
	}
	
	@Bean
	public SpringTemplateEngine templateEngine() {
		SpringTemplateEngine templateEngine = new SpringTemplateEngine();
		templateEngine.setTemplateResolver(templateResolver());
		return templateEngine;
	}
	
	@Bean
	public ViewResolver viewResolver() {
		ThymeleafViewResolver resolver = new ThymeleafViewResolver();
		resolver.setTemplateEngine(templateEngine());
		return resolver;
	}

	/*
	 * SPRING BOOTSTRAP MAIN
	 */
	public static void main(String[] args) {
		SpringApplication.run(HelloFacebookConfiguration.class, args);
	}

} 