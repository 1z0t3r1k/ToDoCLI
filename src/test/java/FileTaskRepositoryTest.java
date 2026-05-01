import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import ru.izo.todo.taskmanager.Task;
import ru.izo.todo.taskmanager.storage.FileTaskRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class FileTaskRepositoryTest {
    private JsonNode findTaskById(JsonNode root, int id) {
        for (JsonNode taskNode : root) {
            if (taskNode.get("id").asInt() == id) {
                return taskNode;
            }
        }

        throw new AssertionError("Task with id " + id + " was not found in JSON");
    }

    @Test
    public void fileRepositoryShouldLoadSavedTasksFromFile() throws IOException {
        Path tempFile = Files.createTempFile("tasks", ".json");

        FileTaskRepository repository1 = new FileTaskRepository(tempFile);

        Task task1 = new Task(1, "Name 1", "Description 1", null);
        Task task2 = new Task(2, "Name 2", "Description 2", null);
        task2.markInProgress();

        repository1.save(task1);
        repository1.save(task2);

        FileTaskRepository repository2 = new FileTaskRepository(tempFile);

        Task loadedTask1 = repository2.findById(1);
        Task loadedTask2 = repository2.findById(2);

        assertEquals("Name 1", loadedTask1.getName());
        assertEquals(Task.TaskStatus.UNDONE, loadedTask1.getStatus());

        assertEquals("Name 2", loadedTask2.getName());
        assertEquals(Task.TaskStatus.IN_PROGRESS, loadedTask2.getStatus());

        assertEquals(task1.getDateOfCreation(), loadedTask1.getDateOfCreation());
        assertEquals(task2.getDateOfCreation(), loadedTask2.getDateOfCreation());
    }

    @Test
    public void saveShouldSaveCorrectData() throws IOException {
        Path tempFile = Files.createTempFile("tasks", ".json");

        FileTaskRepository repository = new FileTaskRepository(tempFile);

        Task task1 = new Task(1, "Name 1", "Description 1", null);
        Task task2 = new Task(2, "Name 2", "Description 2", null);

        task1.markInProgress();
        task2.markDone();

        repository.save(task1);
        repository.save(task2);

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode root = objectMapper.readTree(tempFile.toFile());

        assertTrue(root.isArray());
        assertEquals(2, root.size());

        JsonNode taskWithId1 = findTaskById(root, 1);
        JsonNode taskWithId2 = findTaskById(root, 2);

        assertEquals("Name 1", taskWithId1.get("name").asText());
        assertEquals("Description 1", taskWithId1.get("description").asText());
        assertEquals("IN_PROGRESS", taskWithId1.get("status").asText());

        assertEquals("Name 2", taskWithId2.get("name").asText());
        assertEquals("Description 2", taskWithId2.get("description").asText());
        assertEquals("DONE", taskWithId2.get("status").asText());
    }

    @Test
    public void deleteByIdShouldRemoveTaskFromFile() throws IOException {
        Path tempFile = Files.createTempFile("tasks", ".json");

        FileTaskRepository fileTaskRepository = new FileTaskRepository(tempFile);

        Task task1 = new Task(1, "Name 1", "Description 1", null);
        Task task2 = new Task(2, "Name 2", "Description 2", null);

        fileTaskRepository.save(task1);
        fileTaskRepository.save(task2);

        fileTaskRepository.deleteById(1);

        FileTaskRepository reloadedTaskRepository = new FileTaskRepository(tempFile);

        assertThrows(IllegalArgumentException.class, () -> reloadedTaskRepository.findById(1));
        assertEquals(task2, reloadedTaskRepository.findById(2));
    }

    @Test
    public void fileRepositoryShouldSaveAndLoadTaskWithDeadline() throws IOException {
        Path tempFile = Files.createTempFile("tasks", ".json");

        FileTaskRepository repositoryBeforeReload = new FileTaskRepository(tempFile);

        LocalDate deadline = LocalDate.now().plusDays(3);
        Task task = new Task(1, "Name", "Description", deadline);

        repositoryBeforeReload.save(task);

        FileTaskRepository repositoryAfterReload = new FileTaskRepository(tempFile);

        Task loadedTask = repositoryAfterReload.findById(1);

        assertEquals(task.getId(), loadedTask.getId());
        assertEquals(task.getName(), loadedTask.getName());
        assertEquals(task.getDescription(), loadedTask.getDescription());
        assertEquals(task.getStatus(), loadedTask.getStatus());
        assertEquals(task.getDateOfCreation(), loadedTask.getDateOfCreation());
        assertEquals(deadline, loadedTask.getDeadline());
    }

    @Test
    public void findByDeadlineShouldWorkAfterReload() throws IOException {
        Path tempFile = Files.createTempFile("tasks", ".json");

        LocalDate deadline = LocalDate.now().plusDays(2);

        FileTaskRepository repositoryBeforeReload = new FileTaskRepository(tempFile);

        Task task1 = new Task(1, "Task 1", "Description 1", deadline);
        Task task2 = new Task(2, "Task 2", "Description 2", deadline);
        Task task3 = new Task(3, "Task 3", "Description 3", LocalDate.now().plusDays(5));

        repositoryBeforeReload.save(task1);
        repositoryBeforeReload.save(task2);
        repositoryBeforeReload.save(task3);

        FileTaskRepository repositoryAfterReload = new FileTaskRepository(tempFile);

        List<Task> result = repositoryAfterReload.findByDeadline(deadline);

        assertEquals(2, result.size());
        assertTrue(result.contains(task1));
        assertTrue(result.contains(task2));
        assertFalse(result.contains(task3));
    }

    @Test
    public void findOverdueTasksShouldWorkAfterReload() throws IOException {
        Path tempFile = Files.createTempFile("tasks", ".json");

        LocalDate deadline = LocalDate.now();
        LocalDate today = LocalDate.now().plusDays(1);

        FileTaskRepository repositoryBeforeReload = new FileTaskRepository(tempFile);

        Task overdueTask = new Task(1, "Task 1", "Description 1", deadline);
        Task notOverdueTask = new Task(2, "Task 2", "Description 2", today);
        Task withoutDeadlineTask = new Task(3, "Task 3", "Description 3", null);

        repositoryBeforeReload.save(overdueTask);
        repositoryBeforeReload.save(notOverdueTask);
        repositoryBeforeReload.save(withoutDeadlineTask);

        FileTaskRepository repositoryAfterReload = new FileTaskRepository(tempFile);

        List<Task> result = repositoryAfterReload.findOverdueTasks(today);

        assertEquals(1, result.size());
        assertTrue(result.contains(overdueTask));
        assertFalse(result.contains(notOverdueTask));
        assertFalse(result.contains(withoutDeadlineTask));
    }

    @Test
    public void findByDeadlineShouldNotReturnDoneTasksAfterReload() throws IOException {
        Path tempFile = Files.createTempFile("tasks", ".json");

        LocalDate deadline = LocalDate.now().plusDays(2);

        FileTaskRepository repositoryBeforeReload = new FileTaskRepository(tempFile);

        Task undoneTask = new Task(1, "Task 1", "Description 1", deadline);
        Task doneTask = new Task(2, "Task 2", "Description 2", deadline);
        doneTask.markDone();

        repositoryBeforeReload.save(undoneTask);
        repositoryBeforeReload.save(doneTask);

        FileTaskRepository repositoryAfterReload = new FileTaskRepository(tempFile);

        List<Task> result = repositoryAfterReload.findByDeadline(deadline);

        assertEquals(1, result.size());
        assertTrue(result.contains(undoneTask));
        assertFalse(result.contains(doneTask));
    }
}
