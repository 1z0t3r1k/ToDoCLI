package taskmanager.storage;

import com.sun.source.util.TaskListener;
import taskmanager.Task;
import taskmanager.TaskRepository;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;


public class InMemoryTaskRepository implements TaskRepository {
    private final Map<Integer, Task> tasks = new HashMap<>();

    @Override
    public void save(Task task) {
        tasks.put(task.getId(), task);
    }

    @Override
    public List<Task> findAll() {
        return tasks.values().stream()
                .map(Task::new)
                .toList();
    }

    private Task validateTask(Task task, int id) {
        if (task == null) {
            throw new IllegalArgumentException("There is no task with id: " + id);
        }
        return task;
    }

    @Override
    public Task findById(int id) {
        return validateTask(tasks.get(id), id);
    }

    @Override
    public void deleteById(int id) {
        validateTask(tasks.get(id), id);
        tasks.remove(id);
    }

    @Override
    public List<Task> findByStatus(Task.TaskStatus taskStatus) {
        return tasks.values().stream()
                .filter(task -> task.getStatus() == taskStatus)
                .map(Task::new)
                .toList();
    }

    @Override
    public List<Task> findByName(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Name cannot be null");
        }
        if (name.isBlank()) {
            return List.of();
        }

        return tasks.values().stream()
                .filter(task -> task.getName().contains(name))
                .map(Task::new)
                .toList();
    }

    @Override
    public List<Task> findBetweenDates(LocalDate startDate, LocalDate endDate) {
        return tasks.values().stream()
                .filter(task -> !task.getDateOfCreation().isBefore(startDate) && !task.getDateOfCreation().isAfter(endDate))
                .map(Task::new)
                .toList();
    }
}
