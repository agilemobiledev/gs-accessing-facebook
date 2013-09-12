This guide walks you through the process of creating a simple web application that accesses Facebook data. 

What you'll build
-----------------

You'll build a web application that accesses data from a Facebook user profile, as well as profile data from that user's Facebook friends.

What you'll need
----------------

 - About 15 minutes
 - An application ID and secret obtained from [registering an application with Facebook][gs-register-facebook-app].
 - A favorite text editor or IDE
 - [JDK 6][jdk] or later
 - [Gradle 1.7+][gradle] or [Maven 3.0+][mvn]
 - You can also import the code from this guide as well as view the web page directly into [Spring Tool Suite (STS)][gs-sts] and work your way through it from there.

[jdk]: http://www.oracle.com/technetwork/java/javase/downloads/index.html
[gradle]: http://www.gradle.org/
[mvn]: http://maven.apache.org/download.cgi
[gs-sts]: /guides/gs/sts


How to complete this guide
--------------------------

Like all Spring's [Getting Started guides](/guides/gs), you can start from scratch and complete each step, or you can bypass basic setup steps that are already familiar to you. Either way, you end up with working code.

To **start from scratch**, move on to [Set up the project](#scratch).

To **skip the basics**, do the following:

 - [Download][zip] and unzip the source repository for this guide, or clone it using [Git][u-git]:
`git clone https://github.com/spring-guides/gs-accessing-facebook.git`
 - cd into `gs-accessing-facebook/initial`.
 - Jump ahead to [Enable Facebook](#initial).

**When you're finished**, you can check your results against the code in `gs-accessing-facebook/complete`.
[zip]: https://github.com/spring-guides/gs-accessing-facebook/archive/master.zip
[u-git]: /understanding/Git


<a name="scratch"></a>
Set up the project
------------------

First you set up a basic build script. You can use any build system you like when building apps with Spring, but the code you need to work with [Gradle](http://gradle.org) and [Maven](https://maven.apache.org) is included here. If you're not familiar with either, refer to [Building Java Projects with Gradle](/guides/gs/gradle/) or [Building Java Projects with Maven](/guides/gs/maven).

### Create the directory structure

In a project directory of your choosing, create the following subdirectory structure; for example, with `mkdir -p src/main/java/hello` on *nix systems:

    └── src
        └── main
            └── java
                └── hello


### Create a Gradle build file
Below is the [initial Gradle build file](https://github.com/spring-guides/gs-accessing-facebook/blob/master/initial/build.gradle). But you can also use Maven. The pom.xml file is included [right here](https://github.com/spring-guides/gs-accessing-facebook/blob/master/initial/pom.xml). If you are using [Spring Tool Suite (STS)][gs-sts], you can import the guide directly.

`build.gradle`
```gradle
buildscript {
    repositories {
        maven { url "http://repo.springsource.org/libs-snapshot" }
        mavenLocal()
    }
}

apply plugin: 'java'
apply plugin: 'eclipse'
apply plugin: 'idea'

jar {
    baseName = 'gs-accessing-facebook'
    version =  '0.1.0'
}

repositories {
    mavenCentral()
    maven { url "http://repo.springsource.org/libs-snapshot" }
}

dependencies {
    compile("org.springframework.boot:spring-boot-starter-web:0.5.0.M2")
    compile("org.springframework.social:spring-social-facebook:1.1.0.BUILD-SNAPSHOT")
    compile("org.springframework.security:spring-security-crypto:3.1.4.RELEASE")
    compile("org.thymeleaf:thymeleaf-spring3:2.0.16")
    testCompile("junit:junit:4.11")
}

task wrapper(type: Wrapper) {
    gradleVersion = '1.7'
}
```
    
[gs-sts]: /guides/gs/sts    

> **Note:** This guide is using [Spring Boot](/guides/gs/spring-boot/).


<a name="initial"></a>
Enable Facebook
---------------

Before you can fetch a user's data from Facebook, you need to set up the Spring configuration. Here's a configuration class that contains what you need to enable Facebook in your application:

`src/main/java/hello/FacebookConfig.java`
```java
package hello;

import org.springframework.context.annotation.Bean;
import org.springframework.social.UserIdSource;
import org.springframework.social.config.annotation.EnableInMemoryConnectionRepository;
import org.springframework.social.connect.ConnectionFactoryLocator;
import org.springframework.social.connect.ConnectionRepository;
import org.springframework.social.connect.web.ConnectController;
import org.springframework.social.facebook.config.annotation.EnableFacebook;

@EnableFacebook(appId="someAppId", appSecret="shhhhhh!!!")
@EnableInMemoryConnectionRepository
public class FacebookConfig {

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

} 
```

Because the application will be accessing Facebook data, `FacebookConfig` is annotated with [`@EnableFacebook`][@EnableFacebook]. Notice that, as shown here, the `appId` and `appSecret` attributes have fake values. For the code to work, [obtain a real application ID and secret][gs-register-facebook-app] and replace these fake values with the real values given to you by Facebook.

After a user authorizes your application to access their Facebook data, Spring Social creates a connection. That connection will need to be saved in a connection repository for long-term use.
For the purposes of testing and for small sample applications, such as the one in this guide, an in-memory connection repository is sufficient. Notice that `FacebookConfig` is annotated with `@EnableInMemoryConnectionRepository`. 

For real applications, you need to select a more persistent
option. You can use [`@EnableJdbcConnectionRepository`][@EnableJdbcConnectionRepository] to persist connections to a relational database.

Within the `FacebookConfig`'s body, two beans are declared: `ConnectController` and `UserIdSource`.

Obtaining user authorization from Facebook involves a "dance" of redirects between the application and Facebook. This process is formally known as [OAuth][u-oauth]'s _Resource Owner Authorization_. Don't worry if you don't know much about OAuth. Spring Social's [`ConnectController`][ConnectController] takes care of the OAuth dance for you.

Notice that `ConnectController` is created by injecting a [`ConnectionFactoryLocator`][ConnectionFactoryLocator] and a [`ConnectionRepository`][ConnectionRepository] via the constructor. You won't need to explicitly declare these beans, however. The `@EnableFacebook` annotation makes sure that a `ConnectionFactoryLocator` bean is created, and the `@EnableInMemoryConnectionRepository` annotation creates an in-memory implementation of `ConnectionRepository`.

Connections represent a three-way agreement among a user, an application, and an API provider such as Facebook. Facebook and the application itself are readily identifiable. You identify the current user with the `UserIdSource` bean. 

Here, the `UserIdSource` bean is defined by an inner-class that always returns "testuser" as the user ID. The sample application has only one user. In a real application, you probably want to create an implementation of `UserIdSource` that determines the user ID from the currently authenticated user, perhaps by consulting with an [`Authentication`][Authentication] obtained from Spring Security's [`SecurityContext`][SecurityContext]).

Create connection status views
------------------------------

Although much of what `ConnectController` does involves redirecting to Facebook and handling a redirect from Facebook, it also shows connection status when a GET request to /connect is made. It defers to a view named connect/{provider}IDConnect when no existing connection is available and to connect/{providerId}Connected when a connection exists for the provider. In this case, *provider ID* is "facebook".

`ConnectController` does not define its own connection views, so you need to create them. First, here's a Thymeleaf view to be shown when no connection to Facebook exists:

`src/main/resources/templates/connect/facebookConnect.html`
```html
<html>
	<head>
		<title>Hello Facebook</title>
	</head>
	<body>
		<h3>Connect to Facebook</h3>
		
		<form action="/connect/facebook" method="POST">
			<div class="formInfo">
				<p>You aren't connected to Facebook yet. Click the button to connect Spring Social Showcase with your Facebook account.</p>
			</div>
			<p><button type="submit">Connect to Facebook</button></p>
		</form>
	</body>
</html>
```

The form on this view will POST to /connect/facebook, which is handled by `ConnectController` and will kick off the OAuth authorization code flow.

Here's the view to be displayed when a connection exists:

`src/main/resources/templates/connect/facebookConnected.html`
```html
<html>
	<head>
		<title>Hello Facebook</title>
	</head>
	<body>
		<h3>Connected to Facebook</h3>
		
		<p>
			You are now connected to your Facebook account.
			Click <a href="/">here</a> to see your Facebook friends.
		</p>		
	</body>
</html>
```


Fetch Facebook data
-------------------

With Facebook configured in your application, you now can write a Spring MVC controller that fetches data for the user who authorized the application and presents it in the browser. `HelloController` is just such a controller:

`src/main/java/hello/HelloController.java`
```java
package hello;

import javax.inject.Inject;

import org.springframework.social.facebook.api.Facebook;
import org.springframework.social.facebook.api.FacebookProfile;
import org.springframework.social.facebook.api.PagedList;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/")
public class HelloController {

    private Facebook facebook;

    @Inject
    public HelloController(Facebook facebook) {
        this.facebook = facebook;
    }

    @RequestMapping(method=RequestMethod.GET)
    public String helloFacebook(Model model) {
        if (!facebook.isAuthorized()) {
            return "redirect:/connect/facebook";
        }

        model.addAttribute(facebook.userOperations().getUserProfile());
        PagedList<FacebookProfile> friends = facebook.friendOperations().getFriendProfiles();
        model.addAttribute("friends", friends);

        return "hello";
    }

}
```

`HelloController` is created by injecting a `Facebook` object into its constructor. The `Facebook` object is a reference to Spring Social's Facebook API binding.

The `helloFacebook()` method is annotated with `@RequestMapping` to indicate that it should handle GET requests for the root path (/). The first thing the method does is check whether the user has authorized the application to access the user's Facebook data. If not, the user is redirected to `ConnectController` with the option to kick off the authorization process.

If the user has authorized the application to access Facebook data, the application fetches the user's profile as well as profile data for the user's friends that is visible to that user. The data is placed into the model to be displayed by the view identified as "hello".

Speaking of the "hello" view, here it is as a Thymeleaf template:

`src/main/resources/templates/hello.html`
```html
<html>
	<head>
		<title>Hello Facebook</title>
	</head>
	<body>
		<h3>Hello, <span th:text="${facebookProfile.name}">Some User</span>!</h3>
		
		<h4>These are your friends:</h4>
		
		<ul>
			<li th:each="friend:${friends}" th:text="${friend.name}">Friend</li>
		</ul>
	</body>
</html>
```


Make the application executable
-------------------------------

Although it is possible to package this service as a traditional _web application archive_ or [WAR][u-war] file for deployment to an external application server, the simpler approach demonstrated below creates a _standalone application_. You package everything in a single, executable JAR file, driven by a good old Java `main()` method. And along the way, you use Spring's support for embedding the [Tomcat][u-tomcat] servlet container as the HTTP runtime, instead of deploying to an external instance.

### Create an Application class

`src/main/java/hello/Application.java`
```java
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

@Configuration
@EnableAutoConfiguration
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
```

The `main()` method defers to the [`SpringApplication`][] helper class, providing `Application.class` as an argument to its `run()` method. This tells Spring to read the annotation metadata from `Application` and to manage it as a component in the _[Spring application context][u-application-context]_.

The `@ComponentScan` annotation tells Spring to search recursively through the `hello` package and its children for classes marked directly or indirectly with Spring's [`@Component`][] annotation. This directive ensures that Spring finds and registers the `GreetingController`, because it is marked with `@Controller`, which in turn is a kind of `@Component` annotation.

The `@Import` annotation tells Spring to import additional Java configuration. Here it is asking Spring to import the `FacebookConfig` class where you enabled Facebook in your application.

The [`@EnableAutoConfiguration`][] annotation switches on reasonable default behaviors based on the content of your classpath. For example, because the application depends on the embeddable version of Tomcat (tomcat-embed-core.jar), a Tomcat server is set up and configured with reasonable defaults on your behalf. And because the application also depends on Spring MVC (spring-webmvc.jar), a Spring MVC [`DispatcherServlet`][] is configured and registered for you — no `web.xml` necessary! Auto-configuration is a powerful, flexible mechanism. See the [API documentation][`@EnableAutoConfiguration`] for further details.

### Build an executable JAR
Now that your `Application` class is ready, you simply instruct the build system to create a single, executable jar containing everything. This makes it easy to ship, version, and deploy the service as an application throughout the development lifecycle, across different environments, and so forth.

Below are the Gradle steps, but if you are using Maven, you can find the updated pom.xml [right here](https://github.com/spring-guides/gs-accessing-facebook/blob/master/complete/pom.xml) and build it by typing `mvn clean package`.

Update your Gradle `build.gradle` file's `buildscript` section, so that it looks like this:

```groovy
buildscript {
    repositories {
        maven { url "http://repo.springsource.org/libs-snapshot" }
        mavenLocal()
    }
    dependencies {
        classpath("org.springframework.boot:spring-boot-gradle-plugin:0.5.0.M2")
    }
}
```

Further down inside `build.gradle`, add the following to the list of applied plugins:

```groovy
apply plugin: 'spring-boot'
```
You can see the final version of `build.gradle` [right here]((https://github.com/spring-guides/gs-accessing-facebook/blob/master/complete/build.gradle).

The [Spring Boot gradle plugin][spring-boot-gradle-plugin] collects all the jars on the classpath and builds a single "über-jar", which makes it more convenient to execute and transport your service.
It also searches for the `public static void main()` method to flag as a runnable class.

Now run the following command to produce a single executable JAR file containing all necessary dependency classes and resources:

```sh
$ ./gradlew build
```

If you are using Gradle, you can run the JAR by typing:

```sh
$ java -jar build/libs/gs-accessing-facebook-0.1.0.jar
```

If you are using Maven, you can run the JAR by typing:

```sh
$ java -jar target/gs-accessing-facebook-0.1.0.jar
```

[spring-boot-gradle-plugin]: https://github.com/SpringSource/spring-boot/tree/master/spring-boot-tools/spring-boot-gradle-plugin

> **Note:** The procedure above will create a runnable JAR. You can also opt to [build a classic WAR file](/guides/gs/convert-jar-to-war/) instead.

Run the service
-------------------
If you are using Gradle, you can run your service at the command line this way:

```sh
$ ./gradlew clean build && java -jar build/libs/gs-accessing-facebook-0.1.0.jar
```

> **Note:** If you are using Maven, you can run your service by typing `mvn clean package && java -jar target/gs-accessing-facebook-0.1.0.jar`.


```
... app starts up ...
```

Once the application starts up, point your web browser to http://localhost:8080. No connection is established yet, so this screen prompts you to connect with Facebook:

![No connection to Facebook exists yet.](images/connect.png)

When you click the **Connect to Facebook** button, the browser is redirected to Facebook for authorization:

![Facebook needs your permission to allow the application to access your data.](images/fbauth.png)

Click "Okay" to grant permission for the sample application to access your public profile and list of friends.

Once permission is granted, Facebook redirects the browser back to the application. A connection is created and stored in the connection repository. You should see this page indicating that a connection was successful:

![A connection with Facebook has been created.](images/connected.png)

Click the link on the connection status page, and you are taken to the home page. This time, now that a connection has been created, you see your name on Facebook and a list of your friends. Although only names are shown here, the application retrieves profile data for the user and for the user's friends. What is in the friends' profile data depends on the security settings for each individual friend, ranging from public data only to the complete profile.

![Guess noone told you life was gonna be this way.](images/friends.png)


Summary
-------
Congratulations! You have developed a simple web application that obtains user authorization to fetch data from Facebook. The application connects the user to Facebook through Spring Social, retrieves data from the user's Facebook profile, and also fetches profile data from the user's Facebook friends. 

[u-war]: /understanding/WAR
[u-tomcat]: /understanding/Tomcat
[u-application-context]: /understanding/application-context
[`SpringApplication`]: http://docs.spring.io/spring-boot/docs/0.5.0.M3/api/org/springframework/boot/SpringApplication.html
[`@Component`]: http://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/stereotype/Component.html
[`@EnableAutoConfiguration`]: http://docs.spring.io/spring-boot/docs/0.5.0.M3/api/org/springframework/boot/autoconfigure/EnableAutoConfiguration.html
[`DispatcherServlet`]: http://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/web/servlet/DispatcherServlet.html
[gs-register-facebook-app]: /guides/gs/register-facebook-app
[@EnableFacebook]: http://docs.spring.io/spring-social-facebook/docs/1.1.x/api/org/springframework/social/facebook/config/annotation/EnableFacebook.html
[@EnableJdbcConnectionRepository]: http://docs.spring.io/spring-social/docs/1.1.x/api/org/springframework/social/config/annotation/EnableJdbcConnectionRepository.html
[u-oauth]: /understanding/OAuth
[ConnectController]: http://docs.spring.io/spring-social/docs/1.1.x/api/org/springframework/social/connect/web/ConnectController.html
[ConnectionFactoryLocator]: http://docs.spring.io/spring-social/docs/1.1.x/api/org/springframework/social/connect/ConnectionFactoryLocator.html
[ConnectionRepository]: http://docs.spring.io/spring-social/docs/1.1.x/api/org/springframework/social/connect/ConnectionRepository.html
[Authentication]: http://docs.spring.io/spring-security/site/docs/3.2.x/apidocs/org/springframework/security/core/Authentication.html
[SecurityContext]: http://docs.spring.io/spring-security/site/docs/3.2.x/apidocs/org/springframework/security/core/context/SecurityContext.html
