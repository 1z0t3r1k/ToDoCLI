import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import ru.izo.todo.taskmanager.Task;
import ru.izo.todo.taskmanager.service.TaskService;
import ru.izo.todo.taskmanager.storage.InMemoryTaskRepository;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TaskServiceTest {
    private TaskService taskService;

    @BeforeEach
    void setUp() {
        InMemoryTaskRepository taskRepository = new InMemoryTaskRepository();
        taskService = new TaskService(taskRepository);
    }

    @ParameterizedTest
    @NullSource
    @EmptySource
    @ValueSource(strings = {" ", "   "})
    public void createTaskShouldThrowWhenNameIsInvalid(String invalidName) {
        assertThrows(IllegalArgumentException.class,
                () -> taskService.createTask(invalidName, "Description 1", null));
    }

    @ParameterizedTest
    @NullSource
    @EmptySource
    @ValueSource(strings = {" ", "   "})
    public void createTaskShouldThrowWhenDescriptionIsInvalid(String invalidDescription) {
        assertThrows(IllegalArgumentException.class,
                () -> taskService.createTask("Name", invalidDescription, null));
    }

    @ParameterizedTest
    @NullSource
    @EmptySource
    @ValueSource(strings = {" ", "   "})
    public void createTaskShouldNotIncrementIdAfterFailedCreation(String invalidName) {
        taskService.createTask("Name 1", "Description 1", null);
        int nextTaskId = taskService.getTasks().getFirst().getId() + 1;
        assertThrows(IllegalArgumentException.class,
                () -> taskService.createTask(invalidName, "Description 3", null));
        taskService.createTask("Name 2", "Description 2, null", null);

        assertEquals(nextTaskId, taskService.findByName("Name 2").getFirst().getId());
    }

    @Test
    public void deleteTaskByIdShouldThrowWhenTaskDoesNotExist() {
        assertThrows(IllegalArgumentException.class, () -> taskService.deleteTaskById(999));
    }

    @Test
    public void deleteTaskByIdShouldRemoveOnlyTaskWithGivenId() {
        int id1 = taskService.createTask("Name 1", "Description 1", null);
        int id2 = taskService.createTask("Name 2", "Description 2", null);
        int id3 = taskService.createTask("Name 3", "Description 3", null);

        taskService.deleteTaskById(id1);

        assertThrows(IllegalArgumentException.class, () -> taskService.getTaskById(id1));
        assertDoesNotThrow(() -> taskService.getTaskById(id2));
        assertDoesNotThrow(() -> taskService.getTaskById(id3));
        assertEquals(2, taskService.getTasksSize());
    }

    @Test
    public void renameTaskShouldThrowWhenTaskDoesntExist() {
        assertThrows(IllegalArgumentException.class, () -> taskService.renameTask(999, "New name"));
    }

    @Test
    public void renameTaskShouldRenameTaskWithGivenId() {
        int id = taskService.createTask("Name 1", "Description 1", null);
        String newName = "New name";
        taskService.renameTask(id, newName);
        assertEquals(newName, taskService.getTaskById(id).getName());
    }

    @Test
    public void changeDescriptionShouldThrowWhenTaskDoesntExist() {
        assertThrows(IllegalArgumentException.class, () -> taskService.changeTaskDescription(999, "New description"));
    }

    @Test
    public void changeDescriptionShouldChangeDescriptionWithGivenId() {
        int id = taskService.createTask("Name 1", "Description 1", null);
        String newDescription = "New description";

        taskService.changeTaskDescription(id, newDescription);

        assertEquals(newDescription, taskService.getTaskById(id).getDescription());
    }

    @Test
    public void markTaskDoneShouldSetDoneStatus() {
        int id = taskService.createTask("Name 1", "Description 1", null);

        taskService.markTaskDone(id);

        assertEquals(Task.TaskStatus.DONE, taskService.getTaskById(id).getStatus());
    }

    @Test
    public void markTaskInProgressShouldSetInProgressStatus() {
        int id = taskService.createTask("Name 1", "Description 1", null);

        taskService.markTaskInProgress(id);

        assertEquals(Task.TaskStatus.IN_PROGRESS, taskService.getTaskById(id).getStatus());
    }

    @Test
    public void markTaskUndoneShouldSetUndoneStatus() {
        int id = taskService.createTask("Name 1", "Description 1", null);
        taskService.markTaskDone(id);

        taskService.markTaskUndone(id);

        assertEquals(Task.TaskStatus.UNDONE, taskService.getTaskById(id).getStatus());
    }

    @Test
    public void markTaskDoneShouldThrowWhenTaskDoesNotExist() {
        assertThrows(IllegalArgumentException.class, () -> taskService.markTaskDone(999));
    }

    @Test
    public void markTaskInProgressShouldThrowWhenTaskDoesNotExist() {
        assertThrows(IllegalArgumentException.class, () -> taskService.markTaskInProgress(999));
    }

    @Test
    public void markTaskUndoneShouldThrowWhenTaskDoesNotExist() {
        assertThrows(IllegalArgumentException.class, () -> taskService.markTaskUndone(999));
    }

    @Test
    public void createTaskShouldCreateTaskWithDeadline() {
        LocalDate deadline = LocalDate.now().plusDays(3);

        int id = taskService.createTask("Name", "Description", deadline);

        Task task = taskService.getTaskById(id);

        assertEquals(deadline, task.getDeadline());
    }

    @Test
    public void createTaskShouldAllowNullDeadline() {
        int id = taskService.createTask("Name", "Description", null);

        Task task = taskService.getTaskById(id);

        assertNull(task.getDeadline());
    }

    @Test
    public void createTaskShouldThrowWhenDeadlineIsInPast() {
        LocalDate pastDeadline = LocalDate.now().minusDays(1);

        assertThrows(IllegalArgumentException.class,
                () -> taskService.createTask("Name", "Description", pastDeadline));
    }

    @Test
    public void changeDeadlineShouldChangeTaskDeadline() {
        int id = taskService.createTask("Name", "Description", null);
        LocalDate deadline = LocalDate.now().plusDays(5);

        taskService.changeDeadline(id, deadline);

        assertEquals(deadline, taskService.getTaskById(id).getDeadline());
    }

    @Test
    public void changeDeadlineShouldAllowNullDeadline() {
        int id = taskService.createTask("Name", "Description", LocalDate.now().plusDays(5));

        taskService.changeDeadline(id, null);

        assertNull(taskService.getTaskById(id).getDeadline());
    }

    @Test
    public void changeDeadlineShouldThrowWhenDeadlineIsInPast() {
        int id = taskService.createTask("Name", "Description", null);
        LocalDate pastDeadline = LocalDate.now().minusDays(1);

        assertThrows(IllegalArgumentException.class,
                () -> taskService.changeDeadline(id, pastDeadline));
    }

    @Test
    public void changeDeadlineShouldThrowWhenTaskDoesNotExist() {
        LocalDate deadline = LocalDate.now().plusDays(3);

        assertThrows(IllegalArgumentException.class,
                () -> taskService.changeDeadline(999, deadline));
    }

    @Test
    public void findTasksForTodayShouldReturnTasksWithTodayDeadline() {
        LocalDate today = LocalDate.now();

        int id1 = taskService.createTask("Task 1", "Description 1", today);
        int id2 = taskService.createTask("Task 2", "Description 2", today);
        int id3 = taskService.createTask("Task 3", "Description 3", today.plusDays(1));
        int id4 = taskService.createTask("Task 4", "Description 4", null);

        List<Task> result = taskService.findTasksForToday();

        assertEquals(2, result.size());
        assertTrue(result.contains(taskService.getTaskById(id1)));
        assertTrue(result.contains(taskService.getTaskById(id2)));
        assertFalse(result.contains(taskService.getTaskById(id3)));
        assertFalse(result.contains(taskService.getTaskById(id4)));
    }
}
