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
        this.nextId = repository.getMaxId() + 1;
    }

    public int createTask(String name, String description) {
        return createTask(name, description, null);
    }

    public int createTask(String name, String description, LocalDate deadline) {
        Task task = new Task(nextId, name, description, deadline);
        repository.save(task);
        nextId++;
        return task.getId();
    }

    public List<Task> getTasks() {
        return repository.findAll();
    }

    public int getTasksSize() {
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
        repository.save(task);
    }

    public void changeTaskDescription(int id, String newDescription) {
        Task task = getTaskById(id);
        task.changeDescription(newDescription);
        repository.save(task);
    }

    public void markTaskDone(int id) {
        Task task = getTaskById(id);
        task.markDone();
        repository.save(task);
    }

    public void markTaskInProgress(int id) {
        Task task = getTaskById(id);
        task.markInProgress();
        repository.save(task);
    }

    public void markTaskUndone(int id) {
        Task task = getTaskById(id);
        task.markUndone();
        repository.save(task);
    }

    public void changeDeadline(int id, LocalDate newDeadline) {
        Task task = getTaskById(id);
        task.changeDeadline(newDeadline);
        repository.save(task);
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

    public List<Task> findOverdueTasks() {
        return repository.findOverdueTasks(LocalDate.now());
    }

    public List<Task> findTasksForToday() {
        return repository.findByDeadline(LocalDate.now());
    }

    public boolean existsByExactName(String name) {
        return repository.existsByExactName(name);
    }
}
