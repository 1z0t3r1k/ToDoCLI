# ToDoCLI

ToDoCLI is a local-first task manager written with Java Core. It keeps data in a human-readable JSON file and offers an interactive terminal interface for planning, searching, prioritising, and reviewing work.

## Highlights

- tasks with deadlines, priorities, tags, and explicit workflow states;
- smart views for today's, overdue, and archived work;
- case-insensitive search, filtering, and deterministic sorting;
- dashboard with completion and workload statistics;
- safe JSON persistence through atomic file replacement;
- layered architecture with dependency inversion and an injectable clock;
- unit and integration tests, coverage reporting, and an executable JAR.

The application deliberately does not use Spring or another application framework. Runtime dependencies are limited to Jackson for JSON persistence.

## Requirements

- JDK 21+
- Maven 3.9+

## Run

```shell
mvn clean package
java -jar target/todocli.jar
```

Tasks are stored in `data/tasks.json`. To use another file:

```shell
java -jar target/todocli.jar --data=/path/to/tasks.json
```

## Quality checks

```shell
mvn verify
```

The command runs the test suite, enforces at least 70% line coverage, and creates a JaCoCo report in `target/site/jacoco/index.html`.

## Architecture

```text
CLI (input/output)
        |
TaskService (use cases, validation, queries, statistics)
        |
TaskRepository (port)
        |
FileTaskRepository / InMemoryTaskRepository (adapters)
```

Domain objects do not know about the terminal or filesystem. `TaskService` accepts a `Clock`, so date-sensitive behaviour is deterministic in tests. The file adapter writes a temporary file first and then replaces the storage file, reducing the chance of corruption after an interrupted write.

## Technologies demonstrated

Java 21, collections and streams, records, enums, `java.time`, NIO, exceptions, generics, JSON serialization, repository pattern, dependency injection without a framework, JUnit 5, Maven, and JaCoCo.

## License

MIT — see [LICENSE](LICENSE).
