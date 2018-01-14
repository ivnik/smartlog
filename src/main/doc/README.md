# Smart Log - yet another logging framework for java

[![ci-travis](https://api.travis-ci.org/ivnik/smartlog.svg?branch=develop)](https://travis-ci.org/ivnik/smartlog)
[![codecov](https://codecov.io/gh/ivnik/smartlog/branch/develop/graph/badge.svg)](https://codecov.io/gh/ivnik/smartlog)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.ivnik/smartlog.svg)](http://search.maven.org/#artifactdetails%7Cio.github.ivnik%7Csmartlog%7C${release.version}%7C)

## About

Last version: ${release.version}

TODO

## Examples

1. Simple log method call using method name, return value and total execution time
```java
@Loggable
public static int example1() {
    return 42;
}
```
log output:
```text
12:10:03.870 [main] INFO org.smartlog.ExampleAspect - example1 - [42], trace: [] [1 ms]
```

2. Auto log uncaught exception

```java
@Loggable
public static void example2() {
    throw new RuntimeException("example uncaught exception");
}
```
log output:
```text
14:55:08.885 [main] ERROR org.smartlog.ExampleAspect - example2 - [java.lang.RuntimeException: example uncaught exception], trace: [] [0 ms]
java.lang.RuntimeException: example uncaught exception
    at org.smartlog.ExampleAspect.example2(ExampleAspect.java:21)
    at org.smartlog.ExampleAspect.main(ExampleAspect.java:61)
```

3. Custom title, result and trace

```java
import static org.smartlog.TraceFlag.MARK_TIME;
import static org.smartlog.TraceFlag.WRITE_TIME;

@Loggable
public static void example3() {
    SmartLog.title("Custom title");

    SmartLog.trace(MARK_TIME, "make request to...");
    // request remote server
    SmartLog.trace(WRITE_TIME, "got result %d", 42);
    
    SmartLog.trace("try parse");
    // parse
    SmartLog.trace("ok");

    SmartLog.result("custom result");
}
```
log output:
```text
15:07:50.918 [main] INFO org.smartlog.ExampleAspect - Custom title - [custom result], trace: [make request to...; got result 42 [2 ms]; try parse; ok] [8 ms]
```

## Getting started

### Maven
```xml
<dependencies>
    <dependency>
        <groupId>io.github.ivnik</groupId>
        <artifactId>smartlog</artifactId>
        <version>${release.version}</version>
    </dependency>
</dependencies>
```
#### Configure AspectJ for compile time weaving (TODO - add example for LTW)
```xml
<dependencies>
    <dependency>
        <groupId>org.aspectj</groupId>
        <artifactId>aspectjtools</artifactId>
        <version>${aspectj.version}</version>
    </dependency>
    <dependency>
        <groupId>io.github.ivnik</groupId>
        <artifactId>smartlog</artifactId>
        <version>${release.version}</version>
    </dependency>
</dependencies>
<plugin>
    <groupId>org.codehaus.mojo</groupId>
    <artifactId>aspectj-maven-plugin</artifactId>
    <version>1.10</version>
    <configuration>
        <complianceLevel>1.8</complianceLevel>
        <source>1.8</source>
        <target>1.8</target>
        <aspectLibraries>
            <aspectLibrary>
                <groupId>io.github.ivnik</groupId>
                <artifactId>smartlog-aop</artifactId>
                <version>${release.version}</version>
            </aspectLibrary>
        </aspectLibraries>
    </configuration>
    <executions>
        <execution>
            <goals>
                <goal>compile</goal>
                <goal>test-compile</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

### Gradle - TODO