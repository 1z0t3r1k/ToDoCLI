import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import ru.izo.todo.taskmanager.Task;
import ru.izo.todo.taskmanager.storage.InMemoryTaskRepository;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class InMemoryTaskRepositoryTest {
    @Test
    public void saveShouldThrowWhenTaskIsNull() {
        InMemoryTaskRepository taskRepository = new InMemoryTaskRepository();
        assertThrows(IllegalArgumentException.class, () -> taskRepository.save(null));
    }

    @Test
    public void saveShouldStoreTask() {
        InMemoryTaskRepository taskRepository = new InMemoryTaskRepository();
        Task task = new Task(1, "Name", "Description");

        taskRepository.save(task);

        assertEquals(task, taskRepository.findById(1));
    }

    @Test
    public void findByIdShouldThrowWhenTaskDoesNotExist() {
        InMemoryTaskRepository taskRepository = new InMemoryTaskRepository();
        assertThrows(IllegalArgumentException.class, () -> taskRepository.findById(1));
    }

    @Test
    public void findByIdShouldReturnTaskAfterSave() {
        InMemoryTaskRepository taskRepository = new InMemoryTaskRepository();
        Task task = new Task(1, "Name", "Description");

        taskRepository.save(task);

        assertEquals(task, taskRepository.findById(1));
    }

    @Test
    public void deleteByIdShouldThrowWhenTaskDoesNotExist() {
        InMemoryTaskRepository taskRepository = new InMemoryTaskRepository();
        assertThrows(IllegalArgumentException.class, () -> taskRepository.deleteById(1));
    }

    @Test
    public void deleteByIdShouldMakeTaskUnavailableById() {
        InMemoryTaskRepository taskRepository = new InMemoryTaskRepository();
        Task task = new Task(1, "Name", "Description");

        taskRepository.save(task);
        taskRepository.deleteById(1);

        assertThrows(IllegalArgumentException.class, () -> taskRepository.findById(1));
    }

    @Test
    public void findByStatusShouldThrowWhenStatusIsNull() {
        InMemoryTaskRepository taskRepository = new InMemoryTaskRepository();

        assertThrows(IllegalArgumentException.class, () -> taskRepository.findByStatus(null));
    }

    @Test
    public void findByStatusShouldFilterTasksByStatus() {
        InMemoryTaskRepository taskRepository = new InMemoryTaskRepository();

        Task task1 = new Task(1, "Name 1", "Description 1");
        Task task2 = new Task(2, "Name 2", "Description 2");
        Task task3 = new Task(3, "Name 3", "Description 3");
        Task task4 = new Task(4, "Name 4", "Description 4");
        Task task5 = new Task(5, "Name 5", "Description 5");
        Task task6 = new Task(6, "Name 6", "Description 6");

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
        InMemoryTaskRepository taskRepository = new InMemoryTaskRepository();
        assertThrows(IllegalArgumentException.class, () -> taskRepository.findByName(null));
    }

    @Test
    public void findByNameShouldFilterTasksByName() {
        InMemoryTaskRepository taskRepository = new InMemoryTaskRepository();

        Task task1 = new Task(1, "Name 1", "Description 1");
        Task task2 = new Task(2, "Name 1", "Description 2");
        Task task3 = new Task(3, "Name 1", "Description 3");
        Task task4 = new Task(4, "Name 2", "Description 4");
        Task task5 = new Task(5, "Name 2", "Description 5");
        Task task6 = new Task(6, "Name 3", "Description 6");

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
        InMemoryTaskRepository taskRepository = new InMemoryTaskRepository();

        Task task1 = new Task(1, "Learn Java", "Description 1");
        Task task2 = new Task(2, "Java Collections", "Description 2");
        Task task3 = new Task(3, "Spring Boot", "Description 3");

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
        InMemoryTaskRepository repository = new InMemoryTaskRepository();

        assertThrows(IllegalArgumentException.class,
                () -> repository.findBetweenDates(null, LocalDate.now()));
    }

    @Test
    public void findBetweenDatesShouldThrowWhenEndDateIsNull() {
        InMemoryTaskRepository repository = new InMemoryTaskRepository();

        assertThrows(IllegalArgumentException.class,
                () -> repository.findBetweenDates(LocalDate.now(), null));
    }

    @Test
    public void findBetweenDatesShouldThrowWhenStartDateIsAfterEndDate() {
        InMemoryTaskRepository repository = new InMemoryTaskRepository();

        LocalDate startDate = LocalDate.now().plusDays(1);
        LocalDate endDate = LocalDate.now();

        assertThrows(IllegalArgumentException.class,
                () -> repository.findBetweenDates(startDate, endDate));
    }

    @Test
    public void findBetweenDatesShouldReturnTasksInsideInclusiveRange() {
        InMemoryTaskRepository repository = new InMemoryTaskRepository();

        Task task1 = new Task(1, "Task 1", "Description 1");
        Task task2 = new Task(2, "Task 2", "Description 2");

        repository.save(task1);
        repository.save(task2);

        LocalDate today = LocalDate.now();

        List<Task> filtered = repository.findBetweenDates(today, today);

        assertEquals(2, filtered.size());
        assertTrue(filtered.contains(task1));
        assertTrue(filtered.contains(task2));
    }

    @Test
    public void findBetweenDatesShouldReturnEmptyListWhenNoTasksMatch() {
        InMemoryTaskRepository repository = new InMemoryTaskRepository();

        Task task = new Task(1, "Task 1", "Description 1");
        repository.save(task);

        LocalDate futureDate = LocalDate.now().plusDays(10);

        List<Task> filtered = repository.findBetweenDates(futureDate, futureDate);

        assertTrue(filtered.isEmpty());
    }

    @Test
    public void existsByExactNameShouldThrowWhenNameIsNull() {
        InMemoryTaskRepository repository = new InMemoryTaskRepository();

        assertThrows(IllegalArgumentException.class,
                () -> repository.existsByExactName(null));
    }

    @Test
    public void existsByExactNameShouldReturnFalseWhenNameIsInvalid() {
        InMemoryTaskRepository repository = new InMemoryTaskRepository();

        assertFalse(repository.existsByExactName(""));
        assertFalse(repository.existsByExactName("   "));
    }

    @Test
    public void existsByExactNameShouldReturnTrueWhenExactNameExists() {
        InMemoryTaskRepository repository = new InMemoryTaskRepository();

        Task task = new Task(1, "Learn Java", "Description");
        repository.save(task);

        assertTrue(repository.existsByExactName("Learn Java"));
    }

    @Test
    public void existsByExactNameShouldReturnFalseWhenOnlyPartialMatchExists() {
        InMemoryTaskRepository repository = new InMemoryTaskRepository();

        Task task = new Task(1, "Learn Java", "Description");
        repository.save(task);

        assertFalse(repository.existsByExactName("Java"));
    }

    @Test
    public void existsByExactNameShouldReturnFalseWhenNameDoesNotExist() {
        InMemoryTaskRepository repository = new InMemoryTaskRepository();

        Task task = new Task(1, "Learn Java", "Description");
        repository.save(task);

        assertFalse(repository.existsByExactName("Spring"));
    }
}
