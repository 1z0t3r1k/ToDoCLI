import org.junit.jupiter.api.Test;
import ru.izo.todo.cli.ConsoleApplication;
import ru.izo.todo.taskmanager.service.TaskService;
import ru.izo.todo.taskmanager.storage.InMemoryTaskRepository;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ConsoleApplicationTest {
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-06-15T12:00:00Z"), ZoneOffset.UTC);

    @Test
    void completeUserWorkflowIsAvailableFromConsole() {
        String commands = """
                1
                Publish release
                Build an executable jar
                2026-06-20
                URGENT
                java, portfolio
                2
                7
                1
                HIGH
                8
                1
                a
                github
                5
                github
                6
                1
                DONE
                13
                11
                12
                0
                """;

        String output = runApplication(commands);

        assertTrue(output.contains("Created task #1"));
        assertTrue(output.contains("Publish release"));
        assertTrue(output.contains("Status updated"));
        assertTrue(output.contains("Completion: 100.0%"));
        assertTrue(output.contains("ARCHIVE (1)"));
    }

    @Test
    void invalidInputIsReportedWithoutCrashingTheSession() {
        String commands = """
                unknown
                3
                10
                abc
                1
                Broken date task
                Description
                tomorrow
                0
                """;

        String output = runApplication(commands);

        assertTrue(output.contains("Unknown command"));
        assertTrue(output.contains("Task id must be a number"));
        assertTrue(output.contains("Use date format yyyy-MM-dd"));
        assertTrue(output.contains("See you!"));
    }

    private String runApplication(String commands) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        TaskService service = new TaskService(new InMemoryTaskRepository(), CLOCK);
        new ConsoleApplication(service,
                new ByteArrayInputStream(commands.getBytes(StandardCharsets.UTF_8)),
                new PrintStream(bytes, true, StandardCharsets.UTF_8)).run();
        return bytes.toString(StandardCharsets.UTF_8);
    }
}
