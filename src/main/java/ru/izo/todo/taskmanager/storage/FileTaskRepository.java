package ru.izo.todo.taskmanager.storage;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import ru.izo.todo.taskmanager.Task;
import ru.izo.todo.taskmanager.TaskRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileTaskRepository implements TaskRepository {
    private final Map<Integer, Task> tasks = new HashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    private final Path filePath;

    public FileTaskRepository(Path filePath) {
        this.filePath = filePath;
        loadFromFile();
    }

    private void loadFromFile() {
        if (Files.notExists(filePath)) {
            return;
        }

        try {
            if (Files.size(filePath) == 0) {
                return;
            }

            List<Task> loadedTasks = objectMapper.readValue(
                    filePath.toFile(),
                    new TypeReference<List<Task>>() {
                    }
            );

            tasks.clear();

            for (Task task : loadedTasks) {
                tasks.put(task.getId(), task);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void writeToFile() {
        try {
            Path parent = filePath.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }

            objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValue(filePath.toFile(), tasks.values());
        } catch (IOException e) {
            throw new IllegalStateException("Failed to write tasks to file", e);
        }
    }

    private Task validateTask(Task task, int id) {
        if (task == null) {
            throw new IllegalArgumentException("There is no task with id: " + id);
        }
        return task;
    }

    @Override
    public void save(Task task) {
        if (task == null) {
            throw new IllegalArgumentException("Task cannot be null");
        }
        tasks.put(task.getId(), task);
        writeToFile();
    }

    @Override
    public void deleteById(int id) {
        validateTask(tasks.get(id), id);
        tasks.remove(id);
        writeToFile();
    }

    @Override
    public Task findById(int id) {
        Task task = tasks.get(id);
        if (task == null) {
            throw new IllegalArgumentException("There is no task with id: " + id);
        }
        return task;
    }

    @Override
    public List<Task> findAll() {
        return new ArrayList<>(tasks.values());
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
                .filter(task -> task.getName().trim().toLowerCase().contains(name.trim().toLowerCase()))
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

        return tasks.values().stream()
                .anyMatch(task -> task.getName().equals(name));
    }

    @Override
    public int getMaxId() {
        int maxId = 0;
        for (Map.Entry<Integer, Task> entry : tasks.entrySet()) {
            if (entry.getKey() > maxId) {
                maxId = entry.getKey();
            }
        }
        return maxId;
    }

    @Override
    public List<Task> findOverdueTasks(LocalDate today) {
        if (today == null) {
            throw new IllegalArgumentException("Today date cannot be null");
        }

        return tasks.values().stream()
                .filter(task -> task.getDeadline() != null)
                .filter(task -> task.getDeadline().isBefore(today))
                .filter(task -> task.getStatus() != Task.TaskStatus.DONE)
                .toList();
    }

    @Override
    public List<Task> findByDeadline(LocalDate deadline) {
        if (deadline == null) {
            throw new IllegalArgumentException("Deadline cannot be null");
        }

        return tasks.values().stream()
                .filter(task -> deadline.equals(task.getDeadline()))
                .filter(task -> task.getStatus() != Task.TaskStatus.DONE)
                .toList();
    }
}
