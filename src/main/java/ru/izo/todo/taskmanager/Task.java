package ru.izo.todo.taskmanager;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.util.Objects;

public class Task {
    private final int id;
    private String name;
    private String description;
    private LocalDate deadline;

    public enum TaskStatus {
        UNDONE,
        IN_PROGRESS,
        DONE
    }

    private TaskStatus status;
    private final LocalDate dateOfCreation;

    public Task(int id, String name, String description) {
        this(id, name, description, null);
    }

    public Task(int id, String name, String description, LocalDate deadline) {
        validateName(name);
        validateDescription(description);
        validateDeadline(deadline);

        this.name = name;
        this.description = description;
        this.id = id;
        this.dateOfCreation = LocalDate.now();
        this.status = TaskStatus.UNDONE;
        this.deadline = deadline;
    }

    public Task(Task other) {
        this.id = other.id;
        this.name = other.name;
        this.description = other.description;
        this.status = other.status;
        this.dateOfCreation = other.dateOfCreation;
        this.deadline = other.deadline;
    }

    @JsonCreator
    public Task(
            @JsonProperty("id") int id,
            @JsonProperty("name") String name,
            @JsonProperty("description") String description,
            @JsonProperty("status") TaskStatus status,
            @JsonProperty("dateOfCreation") LocalDate dateOfCreation,
            @JsonProperty("deadline") LocalDate deadline
    ) {
        validateName(name);
        validateDescription(description);
        validateDeadline(deadline);

        if (status == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }

        if (dateOfCreation == null) {
            throw new IllegalArgumentException("Date of creation cannot be null");
        }

        this.id = id;
        this.name = name;
        this.description = description;
        this.status = status;
        this.dateOfCreation = dateOfCreation;
        this.deadline = deadline;
    }

    public void markDone() {
        if (status == TaskStatus.DONE) {
            throw new IllegalStateException("Task status is already done");
        }
        status = TaskStatus.DONE;
    }

    public void markInProgress() {
        if (status == TaskStatus.IN_PROGRESS) {
            throw new IllegalStateException("Task status is already in progress");
        }
        status = TaskStatus.IN_PROGRESS;
    }

    public void markUndone() {
        if (status == TaskStatus.UNDONE) {
            throw new IllegalStateException("Task status is already undone");
        }
        status = TaskStatus.UNDONE;
    }

    private void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name cannot be blank");
        }
    }

    private void validateDescription(String description) {
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("Description cannot be blank");
        }
    }

    private void validateDeadline(LocalDate deadline) {
        if (deadline != null && deadline.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Deadline cannot be in the past");
        }
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return id == task.id;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return """
            Task
              id: %d
              name: %s
              description: %s
              status: %s
              created: %s
              deadline: %s
            """.formatted(id, name, description, status, dateOfCreation, deadline);
    }

    public int getId() {
        return this.id;
    }

    public String getName() {
        return name;
    }

    public void rename(String newName) {
        validateName(newName);
        this.name = newName;
    }

    public void changeDeadline(LocalDate newDeadline) {
        validateDeadline(newDeadline);
        this.deadline = newDeadline;
    }

    public String getDescription() {
        return description;
    }

    public void changeDescription(String newDescription) {
        validateDescription(newDescription);
        this.description = newDescription;
    }

    public LocalDate getDateOfCreation() {
        return dateOfCreation;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public LocalDate getDeadline() {
        return deadline;
    }

}
