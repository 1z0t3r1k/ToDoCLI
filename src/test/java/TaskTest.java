import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import ru.izo.todo.taskmanager.Task;
import ru.izo.todo.taskmanager.storage.FileTaskRepository;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TaskTest {
    private Task createValidTask() {
        return new Task(1, "Name", "Some description", null);
    }

    @ParameterizedTest
    @NullSource
    @EmptySource
    @ValueSource(strings = {" ", "   "})
    void constructorShouldThrowWhenNameIsInvalid(String invalidName) {
        assertThrows(IllegalArgumentException.class, () -> new Task(1, invalidName, "Some description", null));
    }

    @ParameterizedTest
    @NullSource
    @EmptySource
    @ValueSource(strings = {" ", "   "})
    void constructorShouldThrowWhenDescriptionIsInvalid(String invalidDescription) {
        assertThrows(IllegalArgumentException.class, () -> new Task(1, "Some name", invalidDescription, null));
    }

    @Test
    public void taskShouldBeDoneAfterMarkDone() {
        Task task = createValidTask();
        task.markDone();
        assertEquals(Task.TaskStatus.DONE, task.getStatus());
    }

    @Test
    public void markDoneShouldThrowWhenTaskIsAlreadyDone() {
        Task task = createValidTask();
        task.markDone();
        assertThrows(IllegalStateException.class, task::markDone);
    }

    @Test
    public void markInProgressShouldThrowWhenTaskIsAlreadyInProgress() {
        Task task = createValidTask();
        task.markInProgress();
        assertThrows(IllegalStateException.class, task::markInProgress);
    }

    @Test
    public void markUndoneShouldThrowWhenTaskIsAlreadyUndone() {
        Task task = createValidTask();
        assertThrows(IllegalStateException.class, task::markUndone);
    }

    @Test
    public void newTaskShouldHaveUndoneStatus() {
        Task task = createValidTask();
        assertEquals(Task.TaskStatus.UNDONE, task.getStatus());
    }

    @Test
    public void renameShouldChangeTaskName() {
        Task task = createValidTask();
        String newName = "New name";
        task.rename(newName);
        assertEquals(newName, task.getName());
    }

    @ParameterizedTest
    @NullSource
    @EmptySource
    @ValueSource(strings = {" ", "   "})
    public void renameShouldThrowWhenNameIsInvalid(String invalidName) {
        Task task = createValidTask();

        assertThrows(IllegalArgumentException.class, () -> task.rename(invalidName));
    }

    @Test
    public void changeDescriptionShouldChangeTaskDescription() {
        Task task = createValidTask();
        String newDescription = "New description";
        task.changeDescription(newDescription);
        assertEquals(newDescription, task.getDescription());
    }

    @ParameterizedTest
    @NullSource
    @EmptySource
    @ValueSource(strings = {" ", "   "})
    public void changeDescriptionShouldThrowWhenDescriptionIsInvalid(String invalidDescription) {
        Task task = createValidTask();

        assertThrows(IllegalArgumentException.class, () -> task.changeDescription(invalidDescription));
    }

    @Test
    public void tasksWithSameIdShouldBeEqual() {
        Task task1 = new Task(1, "Name 1", "Description 1", null);
        Task task2 = new Task(1, "Name 2", "Description 2", null);

        assertEquals(task1, task2);
    }

    @Test
    public void equalTasksShouldHaveSameHashCode() {
        Task task1 = new Task(1, "Name 1", "Description 1", null);
        Task task2 = new Task(1, "Name 2", "Description 2", null);

        assertEquals(task1.hashCode(), task2.hashCode());
    }

    @Test
    public void tasksWithDifferentIdShouldNotBeEqual() {
        Task task1 = new Task(1, "Name 1", "Description 1", null);
        Task task2 = new Task(2, "Name 1", "Description 1", null);

        assertNotEquals(task1, task2);
    }

    @Test
    public void copyConstructorShouldCreateEqualTaskWithSameFields() {
        Task originalTask = createValidTask();
        Task copiedTask = new Task(originalTask);

        assertEquals(originalTask.getId(), copiedTask.getId());
        assertEquals(originalTask.getName(), copiedTask.getName());
        assertEquals(originalTask.getDescription(), copiedTask.getDescription());
        assertEquals(originalTask.getStatus(), copiedTask.getStatus());
        assertEquals(originalTask.getDateOfCreation(), copiedTask.getDateOfCreation());
        assertEquals(originalTask, copiedTask);
    }

    @Test
    public void jsonDeserializationShouldCreateValidTask() throws IOException {
        Path tempFile = Files.createTempFile("tasks", ".json");

        ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule());
        FileTaskRepository fileTaskRepository = new FileTaskRepository(tempFile);

        Task task = createValidTask();

        fileTaskRepository.save(task);

        List<Task> loadedTasks = objectMapper.readValue(
                tempFile.toFile(),
                new TypeReference<List<Task>>() {}
        );

        assertEquals(1, loadedTasks.size());

        Task loadedTask = loadedTasks.getFirst();

        assertEquals(task.getId(), loadedTask.getId());
        assertEquals(task.getName(), loadedTask.getName());
        assertEquals(task.getDescription(), loadedTask.getDescription());
        assertEquals(task.getDateOfCreation(), loadedTask.getDateOfCreation());
        assertEquals(task.getStatus(), loadedTask.getStatus());
        assertEquals(task, loadedTask);
    }

    @Test
    public void jsonDeserializationShouldThrowWhenNameIsBlank() {
        ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule());

        String json = """
            {
              "id": 1,
              "name": "   ",
              "description": "Description",
              "status": "UNDONE",
              "dateOfCreation": "2026-04-24"
            }
            """;

        assertThrows(Exception.class, () -> objectMapper.readValue(json, Task.class));
    }

    @Test
    public void jsonDeserializationShouldThrowWhenStatusIsNull() {
        ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule());

        String json = """
            {
              "id": 1,
              "name": "Name",
              "description": "Description",
              "status": null,
              "dateOfCreation": "2026-04-24"
            }, null
            """;

        assertThrows(Exception.class, () -> objectMapper.readValue(json, Task.class));
    }

    @Test
    public void constructorShouldSetDeadline() {
        LocalDate deadline = LocalDate.now().plusDays(3);

        Task task = new Task(1, "Name", "Description", deadline);

        assertEquals(deadline, task.getDeadline());
    }

    @Test
    public void constructorShouldAllowNullDeadline() {
        Task task = new Task(1, "Name", "Description", null);

        assertNull(task.getDeadline());
    }

    @Test
    public void constructorShouldThrowWhenDeadlineIsInPast() {
        LocalDate pastDeadline = LocalDate.now().minusDays(1);

        assertThrows(IllegalArgumentException.class,
                () -> new Task(1, "Name", "Description", pastDeadline));
    }

    @Test
    public void changeDeadlineShouldChangeDeadline() {
        Task task = createValidTask();
        LocalDate deadline = LocalDate.now().plusDays(5);

        task.changeDeadline(deadline);

        assertEquals(deadline, task.getDeadline());
    }

    @Test
    public void changeDeadlineShouldAllowNullDeadline() {
        Task task = new Task(1, "Name", "Description", LocalDate.now().plusDays(5));

        task.changeDeadline(null);

        assertNull(task.getDeadline());
    }

    @Test
    public void changeDeadlineShouldThrowWhenDeadlineIsInPast() {
        Task task = createValidTask();
        LocalDate pastDeadline = LocalDate.now().minusDays(1);

        assertThrows(IllegalArgumentException.class,
                () -> task.changeDeadline(pastDeadline));
    }

}