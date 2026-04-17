package ru.izo.todo.taskmanager.service;

import ru.izo.todo.taskmanager.Task;
import ru.izo.todo.taskmanager.TaskRepository;

import java.time.LocalDate;
import java.util.List;

public class TaskService {
    private final TaskRepository repository;
    private int nextId = 1;

    public TaskService(TaskRepository repository) {
        this.repository = repository;
    }

    public int createTask(String name, String description) {
        Task task = new Task(nextId, name, description);
        repository.save(task);
        nextId++;
        return task.getId();
    }

    public List<Task> getTasks() {
        return repository.findAll();
    }

    public int tasksSize() {
        return this.getTasks().size();
    }

    public Task getTaskById(int id) {
        return repository.findById(id);
    }

    public void deleteTaskById(int id) {
        repository.deleteById(id);
    }

    public void renameTask(int id, String newName) {
        Task task = getTaskById(id);
        task.rename(newName);
    }

    public void changeTaskDescription(int id, String newDescription) {
        Task task = getTaskById(id);
        task.changeDescription(newDescription);
    }

    public void markTaskDone(int id) {
        Task task = getTaskById(id);
        task.markDone();
    }

    public void markTaskInProgress(int id) {
        Task task = getTaskById(id);
        task.markInProgress();
    }

    public void markTaskUndone(int id) {
        Task task = getTaskById(id);
        task.markUndone();
    }

    public List<Task> findByStatus(Task.TaskStatus taskStatus) {
        return repository.findByStatus(taskStatus);
    }

    public List<Task> findByName(String name) {
        return repository.findByName(name);
    }

    public List<Task> findBetweenDates(LocalDate startDate, LocalDate endDate) {
        return repository.findBetweenDates(startDate, endDate);
    }

    public boolean existsByExactName(String name) {
        return repository.existsByExactName(name);
    }
}
