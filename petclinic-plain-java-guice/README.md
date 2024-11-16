# Pet Clinic with plain java and Guice for DI

Sample application [Pet Clinic](https://spring-petclinic.github.io/) from spring, but
written in plain Java with [Guice](https://github.com/google/guice) for dependency injection.

## Goals

* Learn about what Java is capable of without Spring
* Learn about Inversion of Control (IoC) / Dependency Injection using [Guice](https://github.com/google/guice)
  * Guice returns a new instance each time it supplies a value. (https://github.com/google/guice/wiki/Scopes)
  * See `ApplicationModule` for the configuration now - and see the `@Inject` annotations
    * Database is explicitly marked as `@Singleton`
    * All other beans use the default scope - test it with OwnerController

## Non-Goals

* This is not an example of how a modern WebApp should be built. It's only for learning purposes.
