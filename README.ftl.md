<#assign project_id="gs-accessing-facebook">

# Getting Started: Accessing Facebook Data

What you'll build
-----------------

This guide walks you through creating a simple web application that accesses data from a Facebook user profile, as well as profile data from that user's Facebook friends.

What you'll need
----------------

 - About 15 minutes
 - An application ID and secret obtained from [registering an application with Facebook][register-facebook-app].
 - <@prereq_editor_jdk_buildtools/>


## <@how_to_complete_this_guide jump_ahead='Enable Facebook'/>


<a name="scratch"></a>
Set up the project
------------------

<@build_system_intro/>

<@create_directory_structure_hello/>

### Create a Maven POM

    <@snippet path="pom.xml" prefix="initial"/>

<@bootstrap_starter_pom_disclaimer/>


<a name="initial"></a>
Enable Facebook
---------------

Before you can fetch a user's data from Facebook, you need to set up the Spring configuration. Here's a configuration class that contains what you need to enable Facebook in your application:

    <@snippet path="src/main/java/hello/FacebookConfig.java" prefix="complete"/>

Because the application will be accessing Facebook data, `FacebookConfig` is annotated with [`@EnableFacebook`][@EnableFacebook]. Notice that, as shown here, the `appId` and `appSecret` attributes have fake values. For the code to work, [obtain a real application ID and secret][register-facebook-app] and replace these fake values with the real values given to you by Facebook.

After a user authorizes your application to access their Facebook data, Spring Social creates a connection. That connection will need to be saved in a connection repository for long-term use.
For the purposes of testing and for small sample applications, such as the one in this guide, an in-memory connection repository is sufficient. Notice that `FacebookConfig` is annotated with [`@EnableInMemoryConnectionRepository`][@EnableInMemoryConnectionRepository]. 

For real applications, you need to select a more persistent
option. You can use [`@EnableJdbcConnectionRepository`][@EnableJdbcConnectionRepository] to persist connections to a relational database.

Within the `FacebookConfig`'s body, two beans are declared: `ConnectController` and `UserIdSource`.

Obtaining user authorization from Facebook involves a "dance" of redirects between the application and Facebook. This process is formally known as [OAuth][oauth]'s _Resource Owner Authorization_. Don't worry if you don't know much about OAuth. Spring Social's [`ConnectController`][ConnectController] takes care of the OAuth dance for you.

Notice that `ConnectController` is created by injecting a [`ConnectionFactoryLocator`][ConnectionFactoryLocator] and a [`ConnectionRepository`][ConnectionRepository] via the constructor. You won't need to explicitly declare these beans, however. The `@EnableFacebook` annotation makes sure that a `ConnectionFactoryLocator` bean is created, and the `@EnableInMemoryConnectionRepository` annotation creates an in-memory implementation of `ConnectionRepository`.

Connections represent a three-way agreement among a user, an application, and an API provider such as Facebook. Facebook and the application itself are readily identifiable. You identify the current user with the `UserIdSource` bean. 

Here, the `UserIdSource` bean is defined by an inner-class that always returns "testuser" as the user ID. The sample application has only one user. In a real application, you probably want to create an implementation of `UserIdSource` that determines the user ID from the currently authenticated user, perhaps by consulting with an [`Authentication`][Authentication] obtained from Spring Security's [`SecurityContext`][SecurityContext]).

### Create connection status views

Although much of what `ConnectController` does involves redirecting to Facebook and handling a redirect from Facebook, it also shows connection status when a GET request to /connect is made. It defers to a view named connect/{provider}IDConnect when no existing connection is available and to connect/{providerId}Connected when a connection exists for the provider. In this case, *provider ID* is "facebook".

`ConnectController` does not define its own connection views, so you need to create them. First, here's a Thymeleaf view to be shown when no connection to Facebook exists:

    <@snippet path="src/main/resources/templates/connect/facebookConnect.html" prefix="complete"/>

The form on this view will POST to /connect/facebook, which is handled by `ConnectController` and will kick off the OAuth authorization code flow.

Here's the view to be displayed when a connection exists:

    <@snippet path="src/main/resources/templates/connect/facebookConnected.html" prefix="complete"/>


Fetch Facebook data
-------------------

With Facebook configured in your application, you now can write a Spring MVC controller that fetches data for the user who authorized the application and presents it in the browser. `HelloController` is just such a controller:

    <@snippet path="src/main/java/hello/HelloController.java" prefix="complete"/>

`HelloController` is created by injecting a `Facebook` object into its constructor. The `Facebook` object is a reference to Spring Social's Facebook API binding.

The `helloFacebook()` method is annotated with `@RequestMapping` to indicate that it should handle GET requests for the root path (/). The first thing the method does is check whether the user has authorized the application to access the user's Facebook data. If not, the user is redirected to `ConnectController` with the option to kick off the authorization process.

If the user has authorized the application to access Facebook data, the application fetches the user's profile as well as profile data for the user's friends that is visible to that user. The data is placed into the model to be displayed by the view identified as "hello".

Speaking of the "hello" view, here it is as a Thymeleaf template:

    <@snippet path="src/main/resources/templates/hello.html" prefix="complete"/>


Make the application executable
-------------------------------

Although it is possible to package this service as a traditional _web application archive_ or [WAR][u-war] file for deployment to an external application server, the simpler approach demonstrated below creates a _standalone application_. You package everything in a single, executable JAR file, driven by a good old Java `main()` method. And along the way, you use Spring's support for embedding the [Tomcat][u-tomcat] servlet container as the HTTP runtime, instead of deploying to an external instance.

### Create a main class

    <@snippet path="src/main/java/hello/Application.java" prefix="complete"/>

The `main()` method defers to the [`SpringApplication`][] helper class, providing `Application.class` as an argument to its `run()` method. This tells Spring to read the annotation metadata from `Application` and to manage it as a component in the _[Spring application context][u-application-context]_.

The `@ComponentScan` annotation tells Spring to search recursively through the `hello` package and its children for classes marked directly or indirectly with Spring's [`@Component`][] annotation. This directive ensures that Spring finds and registers the `GreetingController`, because it is marked with `@Controller`, which in turn is a kind of `@Component` annotation.

The `@Import` annotation tells Spring to import additional Java configuration. Here it is asking Spring to import the `FacebookConfig` class where you enabled Facebook in your application.

The [`@EnableAutoConfiguration`][] annotation switches on reasonable default behaviors based on the content of your classpath. For example, because the application depends on the embeddable version of Tomcat (tomcat-embed-core.jar), a Tomcat server is set up and configured with reasonable defaults on your behalf. And because the application also depends on Spring MVC (spring-webmvc.jar), a Spring MVC [`DispatcherServlet`][] is configured and registered for you — no `web.xml` necessary! Auto-configuration is a powerful, flexible mechanism. See the [API documentation][`@EnableAutoConfiguration`] for further details.

### <@build_an_executable_jar/>


Run the service
---------------

Now you can run the application from the jar as well, and distribute that as an executable artifact:

```sh
$ java -jar target/${project_id}-0.1.0.jar

... app starts up ...
```

Once the application starts up, point your web browser to http://localhost:8080. No connection is established yet, so this screen prompts you to connect with Facebook:

![No connection to Facebook exists yet.](images/connect.png)

When you click the **Connect to Facebook** button, the browser is redirected to Facebook for authorization:

![Facebook needs your permission to allow the application to access your data.](images/fbauth.png)

Click "Okay" to grant permission for the sample application to access your public profile and list of friends.

Once permission is granted, Facebook redirects the browser back to the application. A connection is created and stored in the connection repository. You should see this page indicating that a connection was successful:

![A connection with Facebook has been created.](images/connected.png)

Click the link on the connection status page, and you are taken to the home page. This time, now that a connection has been created, you see your name on Facebook and a list of your friends. Although only names are shown here, the application retrieves profile data for the user and for the user's friends. What is in the friends' profile data depends on the security settings for each individual friend, ranging from public data only up to the complete profile.

![Guess noone told you life was gonna be this way.](images/friends.png)


Summary
-------
Congratulations! You have developed a simple web application that obtains user authorization to fetch data from Facebook. The application connects the user to Facebook through Spring Social, retrieves data from the user's Facebook profile, and also fetches profile data from the user's Facebook friends. 

[u-war]: /understanding/war
[u-tomcat]: /understanding/tomcat
[u-application-context]: /understanding/application-context
[`SpringApplication`]: http://static.springsource.org/spring-bootstrap/docs/0.5.0.BUILD-SNAPSHOT/javadoc-api/org/springframework/bootstrap/SpringApplication.html
[`@Component`]: http://static.springsource.org/spring/docs/current/javadoc-api/org/springframework/stereotype/Component.html
[`@EnableAutoConfiguration`]: http://static.springsource.org/spring-bootstrap/docs/0.5.0.BUILD-SNAPSHOT/javadoc-api/org/springframework/bootstrap/context/annotation/SpringApplication.html
[`DispatcherServlet`]: http://static.springsource.org/spring/docs/current/javadoc-api/org/springframework/web/servlet/DispatcherServlet.html
[register-facebook-app]: /gs-register-facebook-app/README.md
[@EnableFacebook]: http://static.springsource.org/spring-social-facebook/docs/1.1.x/api/org/springframework/social/facebook/config/annotation/EnableFacebook.html
[@EnableInMemoryConnectionRepository]: TODO
[@EnableJdbcConnectionRepository]: http://static.springsource.org/spring-social/docs/1.1.x/api/org/springframework/social/config/annotation/EnableJdbcConnectionRepository.html
[oauth]: /understanding-oauth
[ConnectController]: http://static.springsource.org/spring-social/docs/1.1.x/api/org/springframework/social/connect/web/ConnectController.html
[ConnectionFactoryLocator]: http://static.springsource.org/spring-social/docs/1.1.x/api/org/springframework/social/connect/ConnectionFactoryLocator.html
[ConnectionRepository]: http://static.springsource.org/spring-social/docs/1.1.x/api/org/springframework/social/connect/ConnectionRepository.html
[Authentication]: http://static.springsource.org/spring-security/site/docs/3.2.x/apidocs/org/springframework/security/core/Authentication.html
[SecurityContext]: http://static.springsource.org/spring-security/site/docs/3.2.x/apidocs/org/springframework/security/core/context/SecurityContext.html
