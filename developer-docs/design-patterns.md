# Design Patterns in Galasa

This document outlines the key design patterns used in the Galasa framework architecture.

## Service Provider Interface (SPI) Pattern

Galasa makes extensive use of the Service Provider Interface pattern, which separates interface definitions from their implementations.

```mermaid
classDiagram
    class IService {
        <<interface>>
        +operation()
    }
    
    class ServiceImpl {
        +operation()
    }
    
    class Client
    
    IService <|-- ServiceImpl
    Client --> IService
```

Examples in Galasa:
- `IManager` interface with various manager implementations
- `IResultArchiveStore` interface with different storage implementations
- `IConfigurationPropertyStore` interface with different backend implementations

Benefits:
- Allows for pluggable implementations
- Enables loose coupling between components
- Facilitates testing with mock implementations

## Dependency Injection

Galasa uses dependency injection to provide components with their dependencies.

```mermaid
classDiagram
    class Framework {
        +getService()
    }
    
    class Component {
        -service
        +Component(IService)
        +operation()
    }
    
    class IService {
        <<interface>>
        +serviceOperation()
    }
    
    Framework --> Component : creates
    Framework --> IService : provides
    Component --> IService : uses
```

Examples in Galasa:
- Managers are injected with the Framework instance
- Test classes are injected with manager-provided resources
- Services are injected with their dependencies

Benefits:
- Reduces tight coupling between components
- Makes testing easier through mock dependencies
- Improves code modularity

## Annotation-Based Configuration

Galasa uses Java annotations extensively for configuration and resource declaration.

```mermaid
classDiagram
    class TestClass {
        +@Annotation field
        +@Test method()
    }
    
    class AnnotationProcessor {
        +process(TestClass)
    }
    
    TestClass --> AnnotationProcessor : processed by
```

Examples in Galasa:
- `@ZosImage` for declaring zOS image requirements
- `@HttpClient` for declaring HTTP client requirements
- `@Test` for marking test methods

Benefits:
- Declarative programming style
- Reduced boilerplate code
- Clear separation of configuration from implementation

## Manager Pattern

Galasa's manager architecture is a specialized form of the Strategy pattern, where each manager provides a specific capability.

```mermaid
classDiagram
    class IManager {
        <<interface>>
        +initialise()
        +provisionGenerate()
        +fillAnnotatedFields()
    }
    
    class AbstractManager {
        #findAnnotatedFields()
        #generateAnnotatedFields()
    }
    
    class ConcreteManager1 {
        +initialise()
        +provisionGenerate()
        +fillAnnotatedFields()
    }
    
    class ConcreteManager2 {
        +initialise()
        +provisionGenerate()
        +fillAnnotatedFields()
    }
    
    IManager <|-- AbstractManager
    AbstractManager <|-- ConcreteManager1
    AbstractManager <|-- ConcreteManager2
```

Benefits:
- Encapsulates specific functionality
- Allows for composition of capabilities
- Enables extensibility

## Observer Pattern

Galasa uses the Observer pattern for event notification.

```mermaid
classDiagram
    class IEventProducer {
        +registerListener(IEventListener)
        +unregisterListener(IEventListener)
        +fireEvent(Event)
    }
    
    class IEventListener {
        <<interface>>
        +processEvent(Event)
    }
    
    class ConcreteListener {
        +processEvent(Event)
    }
    
    IEventProducer --> IEventListener : notifies
    IEventListener <|-- ConcreteListener
```

Examples in Galasa:
- Framework events for test lifecycle notifications
- Resource status change notifications
- Run state change events

Benefits:
- Loose coupling between event producers and consumers
- Support for multiple listeners
- Asynchronous processing

## Factory Pattern

Galasa uses factory patterns to create complex objects.

```mermaid
classDiagram
    class Factory {
        +create() : Product
    }
    
    class Product {
        <<interface>>
    }
    
    class ConcreteProduct
    
    Factory --> Product : creates
    Product <|-- ConcreteProduct
```

Examples in Galasa:
- `GalasaFactory` for creating framework instances
- Manager factories for creating resources
- Test runner factories

Benefits:
- Encapsulates object creation logic
- Allows for different creation strategies
- Simplifies client code

## Composite Pattern

Galasa uses the Composite pattern for managing collections of similar objects.

```mermaid
classDiagram
    class Component {
        <<interface>>
        +operation()
    }
    
    class Leaf {
        +operation()
    }
    
    class Composite {
        -children : List~Component~
        +operation()
        +add(Component)
        +remove(Component)
    }
    
    Component <|-- Leaf
    Component <|-- Composite
    Composite o-- Component
```

Examples in Galasa:
- Multiple Result Archive Stores
- Manager collections
- Test class hierarchies

Benefits:
- Uniform treatment of individual and composite objects
- Simplified client code
- Hierarchical structures

## Adapter Pattern

Galasa uses adapters to make incompatible interfaces work together.

```mermaid
classDiagram
    class Client
    
    class TargetInterface {
        <<interface>>
        +targetOperation()
    }
    
    class Adapter {
        +targetOperation()
    }
    
    class Adaptee {
        +specificOperation()
    }
    
    Client --> TargetInterface
    TargetInterface <|-- Adapter
    Adapter --> Adaptee : adapts
```

Examples in Galasa:
- Adapters for different storage backends
- Protocol adapters in communication managers
- Legacy system integration

Benefits:
- Reuse of existing components
- Gradual migration path
- Interface compatibility

## Proxy Pattern

Galasa uses proxies to control access to objects.

```mermaid
classDiagram
    class Subject {
        <<interface>>
        +operation()
    }
    
    class RealSubject {
        +operation()
    }
    
    class Proxy {
        -realSubject : RealSubject
        +operation()
    }
    
    Subject <|-- RealSubject
    Subject <|-- Proxy
    Proxy --> RealSubject
```

Examples in Galasa:
- Security proxies for credential access
- Remote proxies for distributed resources
- Lazy-loading proxies for resource efficiency

Benefits:
- Access control
- Lazy initialization
- Remote resource representation