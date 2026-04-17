package ru.izo.todo.taskmanager.storage;

import ru.izo.todo.taskmanager.Task;
import ru.izo.todo.taskmanager.TaskRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class InMemoryTaskRepository implements TaskRepository {
    private final Map<Integer, Task> tasks = new HashMap<>();

    @Override
    public void save(Task task) {
        if (task == null) {
            throw new IllegalArgumentException("Task cannot be null");
        }
        tasks.put(task.getId(), task);
    }

    @Override
    public List<Task> findAll() {
        return new ArrayList<>(tasks.values());
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
        if (taskStatus == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }

        return tasks.values().stream()
                .filter(task -> task.getStatus() == taskStatus)
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
                .toList();
    }

    @Override
    public List<Task> findBetweenDates(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Dates cannot be null");
        }
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date cannot be after end date");
        }

        return tasks.values().stream()
                .filter(task ->
                        !task.getDateOfCreation().isBefore(startDate)
                                && !task.getDateOfCreation().isAfter(endDate))
                .toList();
    }

    @Override
    public boolean existsByExactName(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Name cannot be null");
        }
        if (name.isBlank()) {
            return false;
        }

        return tasks.values().stream().anyMatch(task -> task.getName().equals(name));
    }
}