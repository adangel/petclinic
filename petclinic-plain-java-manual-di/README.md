# Pet Clinic with plain java and manual dependency injection

Sample application [Pet Clinic](https://spring-petclinic.github.io/) from spring, but
written in plain Java without Spring implementing manual dependency injection.

## Goals

* Learn about what Java is capable of without Spring
* Learn about Inversion of Control (IoC) / Dependency Injection
  * Beans are created now in one place in `ApplicationContext`
  * Most beans use "constructor injection". OwnerController additionally uses "setter injection" for "defaultFirstName".
  * Look at ApplicationContext - uncomment the line with `registerPrototypeBean` and observe the difference
    for instance field `counter` in `OwnerController`.
  * Tests can now inject mocks and don't need the real implementations.

## Non-Goals

* This is not an example of how a modern WebApp should be built. It's only for learning purposes.
