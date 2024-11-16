# Pet Clinic with plain java and Spring Annotations for DI

Sample application [Pet Clinic](https://spring-petclinic.github.io/) from spring, but
written in plain Java with [Spring Framework](https://spring.io/projects/spring-framework) for DI.

## Goals

* Learn about what Java is capable of without Spring
* Learn about Inversion of Control (IoC) / Dependency Injection
  * We only use the core technology "dependency injection" from Spring
  * We use standard jakarta inject annotations, see https://docs.spring.io/spring-framework/reference/core/beans/standard-annotations.html
  * We use Java-based container configuration instead of XML, see https://docs.spring.io/spring-framework/reference/core/beans/java.html
    along with ComponentScan: `ApplicationConfiguration`
  * Default scope is singleton - see class OwnerController to switch to prototype
  * You can mix jakarta CDI annotations and Spring annotations, e.g. OwnerRepository uses Spring annotations,
    while the other beans use jakarta annotations.

## Non-Goals

* This is not an example of how a modern WebApp should be built. It's only for learning purposes.
