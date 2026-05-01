import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ru.izo.todo.taskmanager.Task;
import ru.izo.todo.taskmanager.storage.InMemoryTaskRepository;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class InMemoryTaskRepositoryTest {
    private InMemoryTaskRepository taskRepository;

    @BeforeEach
    void setUp() {
        taskRepository = new InMemoryTaskRepository();
    }

    @Test
    public void saveShouldThrowWhenTaskIsNull() {
        assertThrows(IllegalArgumentException.class, () -> taskRepository.save(null));
    }

    @Test
    public void saveShouldStoreTask() {
        Task task = new Task(1, "Name", "Description", null);

        taskRepository.save(task);

        assertEquals(task, taskRepository.findById(1));
    }

    @Test
    public void findByIdShouldThrowWhenTaskDoesNotExist() {
        assertThrows(IllegalArgumentException.class, () -> taskRepository.findById(1));
    }

    @Test
    public void findByIdShouldReturnTaskAfterSave() {
        Task task = new Task(1, "Name", "Description", null);

        taskRepository.save(task);

        assertEquals(task, taskRepository.findById(1));
    }

    @Test
    public void deleteByIdShouldThrowWhenTaskDoesNotExist() {
        assertThrows(IllegalArgumentException.class, () -> taskRepository.deleteById(1));
    }

    @Test
    public void deleteByIdShouldMakeTaskUnavailableById() {
        Task task = new Task(1, "Name", "Description", null);

        taskRepository.save(task);
        taskRepository.deleteById(1);

        assertThrows(IllegalArgumentException.class, () -> taskRepository.findById(1));
    }

    @Test
    public void findByStatusShouldThrowWhenStatusIsNull() {

        assertThrows(IllegalArgumentException.class, () -> taskRepository.findByStatus(null));
    }

    @Test
    public void findByStatusShouldFilterTasksByStatus() {
        Task task1 = new Task(1, "Name 1", "Description 1", null);
        Task task2 = new Task(2, "Name 2", "Description 2", null);
        Task task3 = new Task(3, "Name 3", "Description 3", null);
        Task task4 = new Task(4, "Name 4", "Description 4", null);
        Task task5 = new Task(5, "Name 5", "Description 5", null);
        Task task6 = new Task(6, "Name 6", "Description 6", null);

        taskRepository.save(task1);
        taskRepository.save(task2);
        taskRepository.save(task3);
        taskRepository.save(task4);
        taskRepository.save(task5);
        taskRepository.save(task6);

        taskRepository.findById(1).markDone();
        taskRepository.findById(2).markInProgress();
        taskRepository.findById(3).markInProgress();
        taskRepository.findById(4).markInProgress();
        taskRepository.findById(5).markDone();

        List<Task> filtered = taskRepository.findByStatus(Task.TaskStatus.IN_PROGRESS);

        assertEquals(3, filtered.size());
        assertTrue(filtered.contains(task2));
        assertTrue(filtered.contains(task3));
        assertTrue(filtered.contains(task4));
        assertFalse(filtered.contains(task1));
        assertFalse(filtered.contains(task5));
        assertFalse(filtered.contains(task6));
    }

    @Test
    public void findByNameShouldThrowWhenNameIsNull() {
        assertThrows(IllegalArgumentException.class, () -> taskRepository.findByName(null));
    }

    @Test
    public void findByNameShouldFilterTasksByName() {
        Task task1 = new Task(1, "Name 1", "Description 1", null);
        Task task2 = new Task(2, "Name 1", "Description 2", null);
        Task task3 = new Task(3, "Name 1", "Description 3", null);
        Task task4 = new Task(4, "Name 2", "Description 4", null);
        Task task5 = new Task(5, "Name 2", "Description 5", null);
        Task task6 = new Task(6, "Name 3", "Description 6", null);

        taskRepository.save(task1);
        taskRepository.save(task2);
        taskRepository.save(task3);
        taskRepository.save(task4);
        taskRepository.save(task5);
        taskRepository.save(task6);

        List<Task> filtered = taskRepository.findByName("Name 1");

        assertEquals(3, filtered.size());
        assertTrue(filtered.contains(task1));
        assertTrue(filtered.contains(task2));
        assertTrue(filtered.contains(task3));
        assertFalse(filtered.contains(task4));
        assertFalse(filtered.contains(task5));
        assertFalse(filtered.contains(task6));
    }

    @Test
    public void findByNameShouldReturnTasksContainingSearchText() {
        Task task1 = new Task(1, "Learn Java", "Description 1", null);
        Task task2 = new Task(2, "Java Collections", "Description 2", null);
        Task task3 = new Task(3, "Spring Boot", "Description 3", null);

        taskRepository.save(task1);
        taskRepository.save(task2);
        taskRepository.save(task3);

        List<Task> filtered = taskRepository.findByName("Java");

        assertEquals(2, filtered.size());
        assertTrue(filtered.contains(task1));
        assertTrue(filtered.contains(task2));
        assertFalse(filtered.contains(task3));
    }

    @Test
    public void findBetweenDatesShouldThrowWhenStartDateIsNull() {
        assertThrows(IllegalArgumentException.class, () -> taskRepository.findBetweenDates(null, LocalDate.now()));
    }

    @Test
    public void findBetweenDatesShouldThrowWhenEndDateIsNull() {
        assertThrows(IllegalArgumentException.class, () -> taskRepository.findBetweenDates(LocalDate.now(), null));
    }

    @Test
    public void findBetweenDatesShouldThrowWhenStartDateIsAfterEndDate() {
        LocalDate startDate = LocalDate.now().plusDays(1);
        LocalDate endDate = LocalDate.now();

        assertThrows(IllegalArgumentException.class, () -> taskRepository.findBetweenDates(startDate, endDate));
    }

    @Test
    public void findBetweenDatesShouldReturnTasksInsideInclusiveRange() {
        Task task1 = new Task(1, "Task 1", "Description 1", null);
        Task task2 = new Task(2, "Task 2", "Description 2", null);

        taskRepository.save(task1);
        taskRepository.save(task2);

        LocalDate today = LocalDate.now();

        List<Task> filtered = taskRepository.findBetweenDates(today, today);

        assertEquals(2, filtered.size());
        assertTrue(filtered.contains(task1));
        assertTrue(filtered.contains(task2));
    }

    @Test
    public void findBetweenDatesShouldReturnEmptyListWhenNoTasksMatch() {
        Task task = new Task(1, "Task 1", "Description 1", null);
        taskRepository.save(task);

        LocalDate futureDate = LocalDate.now().plusDays(10);

        List<Task> filtered = taskRepository.findBetweenDates(futureDate, futureDate);

        assertTrue(filtered.isEmpty());
    }

    @Test
    public void existsByExactNameShouldThrowWhenNameIsNull() {
        assertThrows(IllegalArgumentException.class, () -> taskRepository.existsByExactName(null));
    }

    @Test
    public void existsByExactNameShouldReturnFalseWhenNameIsInvalid() {
        assertFalse(taskRepository.existsByExactName(""));
        assertFalse(taskRepository.existsByExactName("   "));
    }

    @Test
    public void existsByExactNameShouldReturnTrueWhenExactNameExists() {
        Task task = new Task(1, "Learn Java", "Description", null);
        taskRepository.save(task);

        assertTrue(taskRepository.existsByExactName("Learn Java"));
    }

    @Test
    public void existsByExactNameShouldReturnFalseWhenOnlyPartialMatchExists() {
        Task task = new Task(1, "Learn Java", "Description", null);
        taskRepository.save(task);

        assertFalse(taskRepository.existsByExactName("Java"));
    }

    @Test
    public void existsByExactNameShouldReturnFalseWhenNameDoesNotExist() {
        Task task = new Task(1, "Learn Java", "Description", null);
        taskRepository.save(task);

        assertFalse(taskRepository.existsByExactName("Spring"));
    }
}
