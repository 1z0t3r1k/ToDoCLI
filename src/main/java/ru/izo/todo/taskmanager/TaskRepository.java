package ru.izo.todo.taskmanager;

import java.time.LocalDate;
import java.util.List;

public interface TaskRepository {
    void save(Task task);
    void deleteById(int id);
    boolean existsByExactName(String name);

    Task findById(int id);
    List<Task> findAll();
    List<Task> findByStatus(Task.TaskStatus taskStatus);
    List<Task> findByName(String name);
    List<Task> findBetweenDates(LocalDate startDate, LocalDate endDate);
}
