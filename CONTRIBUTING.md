# Contributing to Feature Toggle Service

Thank you for your interest in contributing to the Feature Toggle Service! This document provides guidelines and coding standards to maintain consistency across the codebase.

## Table of Contents

- [Code Style Guidelines](#code-style-guidelines)
- [Java Coding Standards](#java-coding-standards)
- [Project Structure](#project-structure)
- [Testing Guidelines](#testing-guidelines)
- [Git Workflow](#git-workflow)

## Code Style Guidelines

### General Principles

- Write clean, readable, and maintainable code
- Follow SOLID principles
- Keep methods short and focused on a single responsibility
- Use meaningful variable and method names
- Add comments only when necessary to explain "why", not "what"

## Java Coding Standards

### Type Declarations

**Always use explicit type declarations instead of `var`**

The `var` keyword can reduce code readability and make it harder to understand types at a glance. Always declare variables with their explicit types.

❌ **Don't do this:**
```java
final var pagedClient = clientRegistrationService.findAll(pageable);
final var toggle = toggleService.create(name, description, enabled);
```

✅ **Do this:**
```java
final Page<ClientRegistration> pagedClient = clientRegistrationService.findAll(pageable);
final Toggle toggle = toggleService.create(name, description, enabled);
```

**Rationale:**
- Improves code readability, especially in large codebases
- Makes types immediately visible without IDE assistance
- Reduces ambiguity when reviewing code on GitHub or in diffs
- Helps new team members understand the codebase faster

### Naming Conventions

- **Classes**: PascalCase (e.g., `ToggleController`, `ClientRegistration`)
- **Methods**: camelCase (e.g., `findByName`, `createToggle`)
- **Variables**: camelCase (e.g., `pagedToggle`, `attributeName`)
- **Constants**: UPPER_SNAKE_CASE (e.g., `MAX_RETRY_ATTEMPTS`, `DEFAULT_PAGE_SIZE`)
- **Packages**: lowercase (e.g., `com.fnl33.featuretoggle.service`)

### Final Variables

Use `final` for variables that should not be reassigned:

```java
final Toggle toggle = toggleService.findByName(name);
final PagedResponse<ToggleListResponse> toggles = PagedResponse.from(pagedToggle);
```

### Annotations

- Place annotations on separate lines (except for simple parameter annotations)
- Order annotations consistently: validation annotations first, then Spring annotations

```java
@GetMapping("/{name}")
public ResponseEntity<ToggleDetailResponse> getToggleByName(@PathVariable String name) {
    // method implementation
}

@PostMapping
public ResponseEntity<ToggleDetailResponse> createToggle(
        @Valid @RequestBody ToggleRequest request) {
    // method implementation
}
```

### Logging

- Use SLF4J for logging
- Use appropriate log levels:
  - `ERROR`: For errors that need immediate attention
  - `WARN`: For potentially harmful situations
  - `INFO`: For informational messages (important state changes)
  - `DEBUG`: For detailed debugging information
- Include context in log messages

```java
logger.debug("Fetching toggle by name: {}", name);
logger.info("Creating toggle: {}", request.name());
logger.error("Failed to create toggle: {}", name, exception);
```

### Exception Handling

- Use custom exceptions for business logic errors
- Let Spring handle validation exceptions through `@Valid` and `GlobalExceptionHandler`
- Always include meaningful error messages

```java
throw new ToggleNotFoundException("Toggle not found with name: " + name);
```

### DTOs and Domain Objects

- Keep domain objects (entities) separate from DTOs
- Use static factory methods for DTO conversions:

```java
public record ToggleDetailResponse(...) {
    public static ToggleDetailResponse from(Toggle toggle) {
        // conversion logic
    }
}
```

### Service Layer

- Business logic belongs in the service layer
- Controllers should be thin and only handle HTTP concerns
- Use `@Transactional` for methods that modify data

### Repository Layer

- Use Spring Data JPA query methods when possible
- For complex queries, use `@Query` annotations
- Follow naming conventions: `findByX`, `existsByX`, `deleteByX`

## Project Structure

```
server/src/main/java/com/fnl33/featuretoggle/
├── config/          # Configuration classes
├── controller/      # REST controllers
├── domain/          # JPA entities
├── dto/             # Data Transfer Objects
├── repository/      # Spring Data repositories
├── service/         # Business logic
└── FeatureToggleApplication.java
```

## Testing Guidelines

- Write unit tests for service layer logic
- Use meaningful test method names that describe what is being tested
- Follow the AAA pattern: Arrange, Act, Assert
- Aim for high test coverage on business logic

```java
@Test
void shouldReturnToggleWhenNameExists() {
    // Arrange
    Toggle expectedToggle = createTestToggle();
    
    // Act
    Toggle actualToggle = toggleService.findByName("test-toggle");
    
    // Assert
    assertEquals(expectedToggle.getName(), actualToggle.getName());
}
```

## Git Workflow

### Commit Messages

Write clear, descriptive commit messages:

```
feat: add support for toggle archiving
fix: correct validation for attribute data types
docs: update API documentation for evaluation endpoint
refactor: extract toggle evaluation logic to separate service
```

Use conventional commit prefixes:
- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation changes
- `refactor`: Code refactoring
- `test`: Adding or updating tests
- `chore`: Maintenance tasks

### Branch Naming

Use descriptive branch names:
- `feature/add-toggle-archiving`
- `fix/attribute-validation-bug`
- `refactor/extract-evaluation-service`

## Questions?

If you have questions about these guidelines or need clarification, please open an issue or reach out to the maintainers.

---

**Remember**: Consistency is key. Following these guidelines helps maintain a clean, professional codebase that's easy for everyone to work with.
