package ru.izo.todo.taskmanager;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Task {
    public enum TaskStatus { UNDONE, IN_PROGRESS, DONE }
    public enum Priority { LOW, MEDIUM, HIGH, URGENT }

    private final int id;
    private String name;
    private String description;
    private TaskStatus status;
    private Priority priority;
    private final LocalDate dateOfCreation;
    private LocalDate deadline;
    private final Set<String> tags;
    private boolean archived;
    private LocalDateTime completedAt;

    public Task(int id, String name, String description) {
        this(id, name, description, null);
    }

    public Task(int id, String name, String description, LocalDate deadline) {
        validateDeadline(deadline, LocalDate.now());
        this.id = validateId(id);
        this.name = requireText(name, "Name");
        this.description = requireText(description, "Description");
        this.status = TaskStatus.UNDONE;
        this.priority = Priority.MEDIUM;
        this.dateOfCreation = LocalDate.now();
        this.deadline = deadline;
        this.tags = new LinkedHashSet<>();
    }

    public static Task create(int id, String name, String description, LocalDate deadline,
                              Priority priority, Collection<String> tags, LocalDate today) {
        validateDeadline(deadline, Objects.requireNonNull(today, "Today cannot be null"));
        return new Task(id, name, description, TaskStatus.UNDONE, today, deadline,
                priority == null ? Priority.MEDIUM : priority, tags, false, null);
    }

    public Task(Task other) {
        this.id = other.id;
        this.name = other.name;
        this.description = other.description;
        this.status = other.status;
        this.priority = other.priority;
        this.dateOfCreation = other.dateOfCreation;
        this.deadline = other.deadline;
        this.tags = new LinkedHashSet<>(other.tags);
        this.archived = other.archived;
        this.completedAt = other.completedAt;
    }

    @JsonCreator
    public Task(
            @JsonProperty("id") int id,
            @JsonProperty("name") String name,
            @JsonProperty("description") String description,
            @JsonProperty("status") TaskStatus status,
            @JsonProperty("dateOfCreation") LocalDate dateOfCreation,
            @JsonProperty("deadline") LocalDate deadline,
            @JsonProperty("priority") Priority priority,
            @JsonProperty("tags") Collection<String> tags,
            @JsonProperty("archived") boolean archived,
            @JsonProperty("completedAt") LocalDateTime completedAt
    ) {
        this.id = validateId(id);
        this.name = requireText(name, "Name");
        this.description = requireText(description, "Description");
        this.status = Objects.requireNonNull(status, "Status cannot be null");
        this.dateOfCreation = Objects.requireNonNull(dateOfCreation, "Date of creation cannot be null");
        this.deadline = deadline;
        this.priority = priority == null ? Priority.MEDIUM : priority;
        this.tags = normalizeTags(tags);
        this.archived = archived;
        this.completedAt = completedAt;
    }

    public void markDone() { markDone(LocalDateTime.now()); }

    public void markDone(LocalDateTime completedAt) {
        changeStatus(TaskStatus.DONE);
        this.completedAt = Objects.requireNonNull(completedAt, "Completion time cannot be null");
    }

    public void markInProgress() {
        changeStatus(TaskStatus.IN_PROGRESS);
        completedAt = null;
        archived = false;
    }

    public void markUndone() {
        changeStatus(TaskStatus.UNDONE);
        completedAt = null;
        archived = false;
    }

    private void changeStatus(TaskStatus newStatus) {
        if (status == newStatus) {
            throw new IllegalStateException("Task status is already " + newStatus.name().toLowerCase(Locale.ROOT));
        }
        status = newStatus;
    }

    public void rename(String newName) { name = requireText(newName, "Name"); }
    public void changeDescription(String value) { description = requireText(value, "Description"); }

    public void changeDeadline(LocalDate newDeadline) { changeDeadline(newDeadline, LocalDate.now()); }

    public void changeDeadline(LocalDate newDeadline, LocalDate today) {
        validateDeadline(newDeadline, Objects.requireNonNull(today, "Today cannot be null"));
        deadline = newDeadline;
    }

    public void changePriority(Priority newPriority) {
        priority = Objects.requireNonNull(newPriority, "Priority cannot be null");
    }

    public void addTag(String tag) { tags.add(normalizeTag(tag)); }

    public void removeTag(String tag) {
        if (!tags.remove(normalizeTag(tag))) throw new IllegalArgumentException("Task does not have tag: " + tag);
    }

    public void archive() {
        if (status != TaskStatus.DONE) throw new IllegalStateException("Only completed tasks can be archived");
        if (archived) throw new IllegalStateException("Task is already archived");
        archived = true;
    }

    public void restore() {
        if (!archived) throw new IllegalStateException("Task is not archived");
        archived = false;
    }

    private static int validateId(int id) {
        if (id < 1) throw new IllegalArgumentException("Id must be positive");
        return id;
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(field + " cannot be blank");
        return value.trim();
    }

    private static void validateDeadline(LocalDate deadline, LocalDate today) {
        if (deadline != null && deadline.isBefore(today)) throw new IllegalArgumentException("Deadline cannot be in the past");
    }

    private static Set<String> normalizeTags(Collection<String> tags) {
        Set<String> result = new LinkedHashSet<>();
        if (tags != null) tags.forEach(tag -> result.add(normalizeTag(tag)));
        return result;
    }

    private static String normalizeTag(String tag) {
        return requireText(tag, "Tag").toLowerCase(Locale.ROOT).replace(' ', '-');
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public TaskStatus getStatus() { return status; }
    public Priority getPriority() { return priority; }
    public LocalDate getDateOfCreation() { return dateOfCreation; }
    public LocalDate getDeadline() { return deadline; }
    public Set<String> getTags() { return Set.copyOf(tags); }
    public boolean isArchived() { return archived; }
    public LocalDateTime getCompletedAt() { return completedAt; }

    @Override
    public boolean equals(Object object) { return this == object || object instanceof Task task && id == task.id; }

    @Override
    public int hashCode() { return Integer.hashCode(id); }

    @Override
    public String toString() {
        return "[%d] %-11s %-7s %s%s%s".formatted(id, status, priority, name,
                deadline == null ? "" : " | due " + deadline,
                tags.isEmpty() ? "" : " | " + tags);
    }
}
