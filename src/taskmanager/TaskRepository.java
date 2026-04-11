package taskmanager;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public interface TaskRepository {
    void save(Task task);
    List<Task> findAll();
    Task findById(int id);
    void deleteById(int id);
    List<Task> findByStatus(Task.TaskStatus taskStatus);
    List<Task> findByName(String name);
    List<Task> findBetweenDates(LocalDate startDate, LocalDate endDate);
}
