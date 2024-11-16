# Pet Clinic with plain java and Quarkus ArC (DI)

Sample application [Pet Clinic](https://spring-petclinic.github.io/) from spring, but
written in plain Java with [Qarkus](https://quarkus.io) ArC DI solution

## Goals

* Learn about what Java is capable of without Spring
* Learn about Inversion of Control (IoC) / Dependency Injection
  * Quarkus Configuration is done via annotations, see https://quarkus.io/guides/cdi
  * ArC is configuring the IoC container usually _at compile time_ already using build tool plugins.
    The basic task is done in this example in `SetupArc`. See also https://dev.to/nutrymaco/using-arc-outside-quarkus-3pep
    for the idea. This generates some classes on startup and places them in the `target/classes` folder.
    This means, this solution won't work when running from jar file.

## Non-Goals

* This is not an example of how a modern WebApp should be built. It's only for learning purposes.
