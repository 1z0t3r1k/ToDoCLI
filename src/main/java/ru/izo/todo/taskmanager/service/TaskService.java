package ru.izo.todo.taskmanager.service;

import ru.izo.todo.taskmanager.Task;
import ru.izo.todo.taskmanager.TaskRepository;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class TaskService {
    private static final Comparator<Task> PLANNING_ORDER = Comparator
            .comparing(Task::getPriority, Comparator.reverseOrder())
            .thenComparing(Task::getDeadline, Comparator.nullsLast(Comparator.naturalOrder()))
            .thenComparingInt(Task::getId);

    private final TaskRepository repository;
    private final Clock clock;
    private int nextId;

    public TaskService(TaskRepository repository) {
        this(repository, Clock.systemDefaultZone());
    }

    public TaskService(TaskRepository repository, Clock clock) {
        this.repository = Objects.requireNonNull(repository, "Repository cannot be null");
        this.clock = Objects.requireNonNull(clock, "Clock cannot be null");
        nextId = repository.getMaxId() + 1;
    }

    public int createTask(String name, String description) {
        return createTask(name, description, null);
    }

    public int createTask(String name, String description, LocalDate deadline) {
        return createTask(name, description, deadline, Task.Priority.MEDIUM, List.of());
    }

    public int createTask(String name, String description, LocalDate deadline,
                          Task.Priority priority, Collection<String> tags) {
        Task task = Task.create(nextId, name, description, deadline, priority, tags, today());
        repository.save(task);
        nextId++;
        return task.getId();
    }

    public List<Task> getTasks() {
        return repository.findAll().stream()
                .filter(task -> !task.isArchived())
                .sorted(PLANNING_ORDER)
                .toList();
    }

    public List<Task> getArchivedTasks() {
        return repository.findAll().stream().filter(Task::isArchived).sorted(PLANNING_ORDER).toList();
    }

    public int getTasksSize() { return repository.findAll().size(); }
    public Task getTaskById(int id) { return repository.findById(id); }
    public void deleteTaskById(int id) { repository.deleteById(id); }

    public void renameTask(int id, String name) { update(id, task -> task.rename(name)); }
    public void changeTaskDescription(int id, String value) { update(id, task -> task.changeDescription(value)); }
    public void changeDeadline(int id, LocalDate deadline) { update(id, task -> task.changeDeadline(deadline, today())); }
    public void changePriority(int id, Task.Priority priority) { update(id, task -> task.changePriority(priority)); }
    public void addTag(int id, String tag) { update(id, task -> task.addTag(tag)); }
    public void removeTag(int id, String tag) { update(id, task -> task.removeTag(tag)); }
    public void markTaskDone(int id) { update(id, task -> task.markDone(LocalDateTime.now(clock))); }
    public void markTaskInProgress(int id) { update(id, Task::markInProgress); }
    public void markTaskUndone(int id) { update(id, Task::markUndone); }
    public void archiveTask(int id) { update(id, Task::archive); }
    public void restoreTask(int id) { update(id, Task::restore); }

    public int archiveCompletedTasks() {
        List<Task> completed = repository.findAll().stream()
                .filter(task -> task.getStatus() == Task.TaskStatus.DONE && !task.isArchived()).toList();
        completed.forEach(task -> update(task.getId(), Task::archive));
        return completed.size();
    }

    public List<Task> findByStatus(Task.TaskStatus status) {
        Objects.requireNonNull(status, "Status cannot be null");
        return active(repository.findByStatus(status));
    }

    public List<Task> findByName(String name) { return active(repository.findByName(name)); }
    public List<Task> findBetweenDates(LocalDate start, LocalDate end) { return active(repository.findBetweenDates(start, end)); }
    public List<Task> findOverdueTasks() { return active(repository.findOverdueTasks(today())); }
    public List<Task> findTasksForToday() { return active(repository.findByDeadline(today())); }

    public List<Task> findByPriority(Task.Priority priority) {
        Objects.requireNonNull(priority, "Priority cannot be null");
        return getTasks().stream().filter(task -> task.getPriority() == priority).toList();
    }

    public List<Task> findByTag(String tag) {
        String normalized = requireSearchText(tag).toLowerCase(Locale.ROOT).replace(' ', '-');
        return getTasks().stream().filter(task -> task.getTags().contains(normalized)).toList();
    }

    public List<Task> search(String text) {
        String query = requireSearchText(text).toLowerCase(Locale.ROOT);
        return getTasks().stream().filter(task ->
                task.getName().toLowerCase(Locale.ROOT).contains(query)
                        || task.getDescription().toLowerCase(Locale.ROOT).contains(query)
                        || task.getTags().stream().anyMatch(tag -> tag.contains(query))).toList();
    }

    public TaskStatistics getStatistics() {
        List<Task> all = repository.findAll();
        long active = all.stream().filter(task -> !task.isArchived()).count();
        long done = all.stream().filter(task -> !task.isArchived())
                .filter(task -> task.getStatus() == Task.TaskStatus.DONE).count();
        return new TaskStatistics(
                active,
                countActive(all, Task.TaskStatus.UNDONE),
                countActive(all, Task.TaskStatus.IN_PROGRESS),
                done,
                findOverdueTasks().size(),
                all.size() - active,
                active == 0 ? 0 : done * 100.0 / active);
    }

    public boolean existsByExactName(String name) { return repository.existsByExactName(name); }

    private long countActive(List<Task> tasks, Task.TaskStatus status) {
        return tasks.stream().filter(task -> !task.isArchived() && task.getStatus() == status).count();
    }

    private List<Task> active(List<Task> tasks) {
        return tasks.stream().filter(task -> !task.isArchived()).sorted(PLANNING_ORDER).toList();
    }

    private LocalDate today() { return LocalDate.now(clock); }

    private String requireSearchText(String text) {
        if (text == null || text.isBlank()) throw new IllegalArgumentException("Search text cannot be blank");
        return text.trim();
    }

    private void update(int id, TaskChange change) {
        Task task = repository.findById(id);
        change.apply(task);
        repository.save(task);
    }

    @FunctionalInterface
    private interface TaskChange { void apply(Task task); }
}
