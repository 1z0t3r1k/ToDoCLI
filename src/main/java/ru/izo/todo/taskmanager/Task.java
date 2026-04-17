package ru.izo.todo.taskmanager;

import java.time.LocalDate;
import java.util.Objects;

public class Task {
    private final int id;
    private String name;
    private String description;

    public enum TaskStatus {
        UNDONE,
        IN_PROGRESS,
        DONE
    }

    private TaskStatus status;
    private final LocalDate dateOfCreation;


    public Task(int id, String name, String description) {
        validateName(name);
        validateDescription(description);

        this.name = name;
        this.description = description;
        this.id = id;
        this.dateOfCreation = LocalDate.now();
        this.status = TaskStatus.UNDONE;
    }

    public Task(Task other) {
        this.id = other.id;
        this.name = other.name;
        this.description = other.description;
        this.status = other.status;
        this.dateOfCreation = other.dateOfCreation;
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
            """.formatted(id, name, description, status, dateOfCreation);
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
}
