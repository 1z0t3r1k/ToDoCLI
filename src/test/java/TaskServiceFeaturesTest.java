import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import ru.izo.todo.taskmanager.Task;
import ru.izo.todo.taskmanager.service.TaskService;
import ru.izo.todo.taskmanager.service.TaskStatistics;
import ru.izo.todo.taskmanager.storage.FileTaskRepository;
import ru.izo.todo.taskmanager.storage.InMemoryTaskRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TaskServiceFeaturesTest {
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-06-15T12:00:00Z"), ZoneOffset.UTC);

    @Test
    void taskCanBePlannedWithPriorityAndNormalizedTags() {
        TaskService service = new TaskService(new InMemoryTaskRepository(), CLOCK);

        int id = service.createTask("Release CLI", "Prepare version 1.0",
                LocalDate.of(2026, 6, 20), Task.Priority.URGENT, List.of("Open Source", "Java"));

        Task task = service.getTaskById(id);
        assertEquals(Task.Priority.URGENT, task.getPriority());
        assertEquals(List.of(task), service.findByTag("open source"));
        assertEquals(List.of(task), service.search("java"));
        assertEquals(List.of(task), service.search("version 1.0"));
    }

    @Test
    void completedTasksCanBeArchivedAndAreExcludedFromActiveViews() {
        TaskService service = new TaskService(new InMemoryTaskRepository(), CLOCK);
        int id = service.createTask("Write docs", "Add examples");
        service.markTaskDone(id);

        assertEquals(1, service.archiveCompletedTasks());

        assertTrue(service.getTasks().isEmpty());
        assertEquals(1, service.getArchivedTasks().size());
        assertNotNull(service.getTaskById(id).getCompletedAt());
    }

    @Test
    void dashboardUsesInjectedClockForOverdueWork() {
        InMemoryTaskRepository repository = new InMemoryTaskRepository();
        Task overdue = new Task(1, "Old task", "Still relevant", Task.TaskStatus.IN_PROGRESS,
                LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 10),
                Task.Priority.HIGH, List.of("work"), false, null);
        repository.save(overdue);
        TaskService service = new TaskService(repository, CLOCK);

        TaskStatistics statistics = service.getStatistics();

        assertEquals(1, statistics.total());
        assertEquals(1, statistics.inProgress());
        assertEquals(1, statistics.overdue());
        assertEquals(0.0, statistics.completionRate());
    }

    @Test
    void oldJsonWithAnOverdueDeadlineRemainsLoadable(@TempDir Path directory) throws IOException {
        Path storage = directory.resolve("tasks.json");
        Files.writeString(storage, """
                [{
                  "id": 1,
                  "name": "Legacy task",
                  "description": "Created by an older version",
                  "status": "UNDONE",
                  "dateOfCreation": "2020-01-01",
                  "deadline": "2020-01-02"
                }]
                """);

        Task loaded = new FileTaskRepository(storage).findById(1);

        assertEquals(Task.Priority.MEDIUM, loaded.getPriority());
        assertEquals(LocalDate.of(2020, 1, 2), loaded.getDeadline());
        assertTrue(loaded.getTags().isEmpty());
    }

    @Test
    void repositoryDoesNotLeakMutableStoredState() {
        InMemoryTaskRepository repository = new InMemoryTaskRepository();
        repository.save(new Task(1, "Safe copy", "Repository boundary"));

        repository.findById(1).markDone();

        assertEquals(Task.TaskStatus.UNDONE, repository.findById(1).getStatus());
    }
}
