import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import ru.izo.todo.taskmanager.Task;
import ru.izo.todo.taskmanager.service.TaskService;
import ru.izo.todo.taskmanager.storage.InMemoryTaskRepository;

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
                () -> taskService.createTask(invalidName, "Description 1"));
    }

    @ParameterizedTest
    @NullSource
    @EmptySource
    @ValueSource(strings = {" ", "   "})
    public void createTaskShouldThrowWhenDescriptionIsInvalid(String invalidDescription) {
        assertThrows(IllegalArgumentException.class,
                () -> taskService.createTask("Name", invalidDescription));
    }

    @ParameterizedTest
    @NullSource
    @EmptySource
    @ValueSource(strings = {" ", "   "})
    public void createTaskShouldNotIncrementIdAfterFailedCreation(String invalidName) {
        taskService.createTask("Name 1", "Description 1");
        int nextTaskId = taskService.getTasks().getFirst().getId() + 1;
        assertThrows(IllegalArgumentException.class,
                () -> taskService.createTask(invalidName, "Description 3"));
        taskService.createTask("Name 2", "Description 2");

        assertEquals(nextTaskId, taskService.findByName("Name 2").getFirst().getId());
    }

    @Test
    public void deleteTaskByIdShouldThrowWhenTaskDoesNotExist() {
        assertThrows(IllegalArgumentException.class, () -> taskService.deleteTaskById(999));
    }

    @Test
    public void deleteTaskByIdShouldRemoveOnlyTaskWithGivenId() {
        int id1 = taskService.createTask("Name 1", "Description 1");
        int id2 = taskService.createTask("Name 2", "Description 2");
        int id3 = taskService.createTask("Name 3", "Description 3");

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
        int id = taskService.createTask("Name 1", "Description 1");
        String newName = "New name";
        taskService.renameTask(id, newName);
        assertEquals(newName, taskService.getTaskById(id).getName());
    }

    @Test
    public void changeDescriptionShouldThrowWhenTaskDoesntExist() {
        assertThrows(IllegalArgumentException.class, () -> taskService.changeTaskDescription(999, "New description"));
    }

    @Test
    public void changeDescriptionShouldRenameTaskWithGivenId() {
        int id = taskService.createTask("Name 1", "Description 1");
        String newDescription = "New name";
        taskService.changeTaskDescription(id, newDescription);
        assertEquals(newDescription, taskService.getTaskById(id).getDescription());
    }

    @Test
    public void markTaskDoneShouldSetDoneStatus() {
        int id = taskService.createTask("Name 1", "Description 1");

        taskService.markTaskDone(id);

        assertEquals(Task.TaskStatus.DONE, taskService.getTaskById(id).getStatus());
    }

    @Test
    public void markTaskInProgressShouldSetInProgressStatus() {
        int id = taskService.createTask("Name 1", "Description 1");

        taskService.markTaskInProgress(id);

        assertEquals(Task.TaskStatus.IN_PROGRESS, taskService.getTaskById(id).getStatus());
    }

    @Test
    public void markTaskUndoneShouldSetUndoneStatus() {
        int id = taskService.createTask("Name 1", "Description 1");
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
}
