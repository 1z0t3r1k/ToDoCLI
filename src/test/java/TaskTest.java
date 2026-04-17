import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import ru.izo.todo.taskmanager.Task;

import static org.junit.jupiter.api.Assertions.*;

public class TaskTest {
    private Task createValidTask() {
        return new Task(1, "Name", "Some description");
    }

    @ParameterizedTest
    @NullSource
    @EmptySource
    @ValueSource(strings = {" ", "   "})
    void constructorShouldThrowWhenNameIsInvalid(String invalidName) {
        assertThrows(IllegalArgumentException.class, () -> new Task(1, invalidName, "Some description"));
    }

    @ParameterizedTest
    @NullSource
    @EmptySource
    @ValueSource(strings = {" ", "   "})
    void constructorShouldThrowWhenDescriptionIsInvalid(String invalidDescription) {
        assertThrows(IllegalArgumentException.class, () -> new Task(1, "Some name", invalidDescription));
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
        Task task1 = new Task(1, "Name 1", "Description 1");
        Task task2 = new Task(1, "Name 2", "Description 2");

        assertEquals(task1, task2);
    }

    @Test
    public void equalTasksShouldHaveSameHashCode() {
        Task task1 = new Task(1, "Name 1", "Description 1");
        Task task2 = new Task(1, "Name 2", "Description 2");

        assertEquals(task1.hashCode(), task2.hashCode());
    }

    @Test
    public void tasksWithDifferentIdShouldNotBeEqual() {
        Task task1 = new Task(1, "Name 1", "Description 1");
        Task task2 = new Task(2, "Name 1", "Description 1");

        assertNotEquals(task1, task2);
    }
}