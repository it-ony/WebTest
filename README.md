# Webtest

A library to run selenium tests from [TestNG](https://testng.org/doc/).

# Features
                    
* Run tests in parallel
* Run tests local / against a selenium grid / browserstack
* Create a test report with screenshots and logs
* Publish results to browserstack
* Access localhost from browserstack 
* mobile emulation
                         
# Setting up  your project

Create a new mvn project and the following to your pom.xml

```xml
<project>
    
    <!-- load webtest dependency from github-->
    <repositories>
        <repository>
            <id>github-public</id>
            <url>https://public:&#103;hp_T4mRAMhpfaCBfjmp7zRAG1OvRwSa2p20dv6P@maven.pkg.github.com/it-ony/webtest
            </url>
        </repository>
    </repositories>
    
    <dependencies>
        <dependency>
            <groupId>com.onfido.qa.webdriver</groupId>
            <artifactId>webtest</artifactId>
            <version>0.1.0-SNAPSHOT</version>
            <scope>compile</scope>
        </dependency>
        <dependency>
            <groupId>org.testng</groupId>
            <artifactId>testng</artifactId>
            <version>7.3.0</version>
            <scope>test</scope>
        </dependency>
    </dependencies>
</project>
```

Create your first test class in the `src/test/java` directory named `<MyTest>IT.java`.

```java
public class GithubIT extends WebTest  {

    public class FooIT extends WebTest {

        @Test()
        public void testGithubCodeSearch() {
            driver().get("https://github.com/");

            verifyPage(GithubMainPage.class)
                    .search("it-ony/webtest")
                    .clickFirstResult();
        }
    }
}
```

and create the PageObjects that we want to use. The PageObject needs to extend from Page and can 
overwrite the `verifyPage` method. By doing we can make sure that we landed on the correct page.

```java
public static class GithubMainPage extends Page {

    public static final By SEARCH = By.cssSelector(".header-search-input");

    public GithubMainPage(Driver driver) {
        super(driver);
    }

    public GithubSearchPage search(String searchTerm) {
        driver.waitFor.clickable(SEARCH).sendKeys(searchTerm + Keys.ENTER);
        return new GithubSearchPage(driver);
    }

    @Override
    protected void verifyPage(Driver driver) {
        super.verifyPage(driver);

        driver.waitFor.visibility(SEARCH);
    }
}
```

```java
public static class GithubSearchPage extends Page {

    public static final By CODE_SEARCH_RESULTS = By.cssSelector(".codesearch-results");

    public GithubSearchPage(Driver driver) {
        super(driver);
    }

    @Override
    protected void verifyPage(Driver driver) {
        super.verifyPage(driver);

        driver.waitFor.visibility(CODE_SEARCH_RESULTS);
    }

    public void clickFirstResult() {
        driver.waitFor.clickable(new ByChained(CODE_SEARCH_RESULTS, By.cssSelector("li a"))).click();
    }
}
```
               
From the above examples you see that clicking an element or even typing in a search field should
be done in conjunction of waiting for the element to be clickable. By doing so, we make sure that 
the element is on the page displayed and can be interacted with.

# Executing the tests

## Properties

Webtest comes with the ability to load properties from properties files in the resource folder. Properties
can either be set with the `-Dmyproperty=myvalue` syntax of mvn or via the property files.  

The resource folder is scanned and properties are taken in the following order:

* local
* ENVIRONMENT-REGION
* REGION
* ENVIRONMENT
* common

where `ENVIRONMENT` and `REGION` are based on the system properties named `environment` and `region`. 
By default the environment is `DEV` and region is `EU`.

A best practice for local testing is to create a `local.properties` file and put the local test 
properties into it.

Properties support a templating syntax, e.g. you can put into your `common.properties`

```properties
host=defaulthost.com
url=https://${host}/some/resource
apiEndpoint=https://${host}/api/v2
```

and instead of repeating yourself you can simply overwrite the `url` and `apiEndpoint` by setting the 
`url` in your local.properties file to e.g. `localhost:8080`. `url` will be for the test run then 
`https://localhost:8081/some/resource` and `apiEndpoint` will be `https://localhost:8081/api/v2`. 
                                                                       
## choosing the browser, version and platform 

The browser can be either chooses with the property `browser`. A fallback to `chrome` is given by the framework.
The version is set by `browserVersion` property and the platform can be specified by setting the `platform` property
to a [valid value](https://github.com/SeleniumHQ/selenium/blob/trunk/java/src/org/openqa/selenium/Platform.java).

As an alternative way all the values can be overwritten with the `@Browser` annotation on a method or the test class.

## running tests against local browsers

For debugging tests and developing test code running code locally is key. You can see and inspect the 
web app while stepping through the test code. Right now you can run tests against Firefox and Chrome locally.

To run local a property `local=true` needs to be specified. For Chrome you need to install a 
[chromedriver](https://chromedriver.chromium.org/) that matches your chrome version. For Firefox you need
[geckodriver](https://firefox-source-docs.mozilla.org/testing/geckodriver/index.html#)

For chrome you need to define the path to chromedriver either via the `browserPath` or `webdriver.chrome.driver`
property. If not defined it tries to find the driver executable in your path.

## running tests against a grid

You can run your tests a selenium grid by defining a property named `gridUrl`. The property `local` needs
to be `false`.

## running against browserstack

[Browserstack](https://www.browserstack.com/) is a selenium grid hosting company. Webtest is prepared for browserstack.
Just define `browserstack.username` and `browserstack.accessKey` as system properties or `BROWSERSTACK_USERNAME` and 
`BROWSERSTACK_ACCESS_KEY` as environment variables.
                             
If you need to your local system from browserstack set the property `browserstack.local=true` and it will create a tunnel
between browserstack and your system executing the tests. 

The `BrowserStackListener` takes care of setting the test results. To make use of it annotate your test class 

```java
@Listeners({BrowserStackListener.class})
public class MyTestIT extends WebTest {
    ...
}
```

## running with the mvn failsafe plugin

Extend your pom.xml with the maven-failsafe-plugin

```xml
<project>
    
    <properties>
        <threadCount>5</threadCount>
    </properties>
    
    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-failsafe-plugin</artifactId>
                <version>2.22.2</version>
                <executions>
                    <execution>
                        <goals>
                            <goal>integration-test</goal>
                            <goal>verify</goal>
                        </goals>
                    </execution>
                </executions>
                <configuration>
                    <properties>
                        <property>
                            <name>configfailurepolicy</name>
                            <value>continue</value>
                        </property>
                    </properties>
                    <parallel>methods</parallel>
                    <threadCount>${threadCount}</threadCount>
                    <forkCount>0</forkCount>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

Then execute 

```shell
mvn -DthreadCount=10 -Dbrowser=chrome clean verify
```

You can set properties with the `-D` option. Those are taken with priority to the ones defined in your property files.

## running from your IDE

To ease debugging and running tests most IDEs can execute test methods directly from the IDE. 
Give [Intellij Idea](https://www.jetbrains.com/idea/) if you like.

# ScreenshotListener

Webtest comes with Screenshot listener giving you details for failed tests. Annotate your testng tests to include the 
`ScreenshotListener` and find your reports under the target directory after the test run.

```java

@Listeners({ScreenshotListener.class})
public class MyTestIT extends WebTest {
    ...
}
```

# Emulating mobile devices

To emulate a mobile device use the `@Browser` annotation. You can define the `width` and `height` of the screen as well as the 
`pixelRatio`. You can disallow emulation by setting `allowEmulation=false` and define the `device` and the `osVersion. 

Mobile emulation is only available on chrome (and most likely on edge).

# Switch easily between environments

A best pratice is to run your tests against local and testing them before the commit against a remote grid that your CI system 
is testing against. You can do this easily by having different property files for the environments.

In your `local.properties` file only put
```properties
environment=_local
#environment=_remote
```

Create a `_local.properties` 

```properties
local=true
```

and a `_remote.properties` file 

```properties
local=false
browserstack.username=
browserstack.accessKey=
browserstack.local=true
```
                       
and set your properties. You can easily toggle now between the environments by setting the desired environment in the `local.properties` file.



