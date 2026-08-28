package ru.izo.todo.cli;

import ru.izo.todo.taskmanager.Task;
import ru.izo.todo.taskmanager.service.TaskService;
import ru.izo.todo.taskmanager.service.TaskStatistics;

import java.io.InputStream;
import java.io.PrintStream;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

public final class ConsoleApplication {
    private final TaskService service;
    private final Scanner input;
    private final PrintStream output;

    public ConsoleApplication(TaskService service, InputStream input, PrintStream output) {
        this.service = service;
        this.input = new Scanner(input);
        this.output = output;
    }

    public void run() {
        output.println("ToDoCLI - plan clearly, finish consistently");
        boolean running = true;
        while (running && input.hasNextLine()) {
            printMenu();
            String command = prompt("Choose: ");
            try {
                running = execute(command);
            } catch (IllegalArgumentException | IllegalStateException exception) {
                output.println("! " + exception.getMessage());
            }
        }
        output.println("See you!");
    }

    private boolean execute(String command) {
        switch (command) {
            case "1" -> createTask();
            case "2" -> printTasks("ACTIVE TASKS", service.getTasks());
            case "3" -> printTasks("TODAY", service.findTasksForToday());
            case "4" -> printTasks("OVERDUE", service.findOverdueTasks());
            case "5" -> printTasks("SEARCH", service.search(promptRequired("Text: ")));
            case "6" -> changeStatus();
            case "7" -> service.changePriority(readId(), readPriority("New priority"));
            case "8" -> editTags();
            case "9" -> editTask();
            case "10" -> deleteTask();
            case "11" -> output.println("Archived: " + service.archiveCompletedTasks());
            case "12" -> printTasks("ARCHIVE", service.getArchivedTasks());
            case "13" -> printStatistics();
            case "0", "exit", "quit" -> { return false; }
            default -> output.println("Unknown command. Enter a number from the menu.");
        }
        return true;
    }

    private void createTask() {
        String name = promptRequired("Title: ");
        if (service.existsByExactName(name)
                && !prompt("A task with this title exists. Create another? [y/N]: ")
                .equalsIgnoreCase("y")) {
            output.println("Cancelled.");
            return;
        }
        String description = promptRequired("Description: ");
        LocalDate deadline = readOptionalDate("Deadline [yyyy-MM-dd, Enter to skip]: ");
        Task.Priority priority = readPriority("Priority");
        List<String> tags = readTags("Tags [comma-separated, Enter to skip]: ");
        int id = service.createTask(name, description, deadline, priority, tags);
        output.println("Created task #" + id);
    }

    private void changeStatus() {
        int id = readId();
        Task.TaskStatus status = readEnum(Task.TaskStatus.class,
                "Status " + Arrays.toString(Task.TaskStatus.values()) + ": ");
        switch (status) {
            case UNDONE -> service.markTaskUndone(id);
            case IN_PROGRESS -> service.markTaskInProgress(id);
            case DONE -> service.markTaskDone(id);
        }
        output.println("Status updated.");
    }

    private void editTags() {
        int id = readId();
        String action = prompt("Add or remove? [a/r]: ").toLowerCase(Locale.ROOT);
        String tag = promptRequired("Tag: ");
        if (action.equals("a")) service.addTag(id, tag);
        else if (action.equals("r")) service.removeTag(id, tag);
        else throw new IllegalArgumentException("Expected 'a' or 'r'");
        output.println("Tags updated.");
    }

    private void editTask() {
        int id = readId();
        Task task = service.getTaskById(id);
        String name = prompt("Title [" + task.getName() + "]: ");
        String description = prompt("Description [" + task.getDescription() + "]: ");
        String deadline = prompt("Deadline [yyyy-MM-dd, '-' to clear, Enter to keep]: ");
        if (!name.isBlank()) service.renameTask(id, name);
        if (!description.isBlank()) service.changeTaskDescription(id, description);
        if (deadline.equals("-")) service.changeDeadline(id, null);
        else if (!deadline.isBlank()) service.changeDeadline(id, parseDate(deadline));
        output.println("Task updated.");
    }

    private void deleteTask() {
        int id = readId();
        Task task = service.getTaskById(id);
        if (prompt("Delete '" + task.getName() + "'? [y/N]: ").equalsIgnoreCase("y")) {
            service.deleteTaskById(id);
            output.println("Deleted.");
        } else output.println("Cancelled.");
    }

    private void printTasks(String title, List<Task> tasks) {
        output.println("\n" + title + " (" + tasks.size() + ")");
        if (tasks.isEmpty()) {
            output.println("No tasks found.");
            return;
        }
        output.printf(Locale.ROOT, "%-4s %-12s %-8s %-12s %-20s %s%n", "ID", "STATUS", "PRIORITY", "DEADLINE", "TAGS", "TITLE");
        output.println("-".repeat(82));
        tasks.forEach(task -> output.printf(Locale.ROOT, "%-4d %-12s %-8s %-12s %-20s %s%n",
                task.getId(), task.getStatus(), task.getPriority(),
                task.getDeadline() == null ? "-" : task.getDeadline(),
                task.getTags().isEmpty() ? "-" : String.join(",", task.getTags()), task.getName()));
    }

    private void printStatistics() {
        TaskStatistics statistics = service.getStatistics();
        output.printf(Locale.ROOT, "%nDASHBOARD%nActive: %d | To do: %d | In progress: %d | Done: %d%n",
                statistics.total(), statistics.undone(), statistics.inProgress(), statistics.done());
        output.printf(Locale.ROOT, "Overdue: %d | Archived: %d | Completion: %.1f%%%n",
                statistics.overdue(), statistics.archived(), statistics.completionRate());
    }

    private void printMenu() {
        output.println("""

                1 Add task       2 List active     3 Due today
                4 Overdue        5 Search          6 Change status
                7 Set priority   8 Edit tags       9 Edit task
               10 Delete        11 Archive done   12 View archive
               13 Dashboard      0 Exit
                """);
    }

    private int readId() {
        try { return Integer.parseInt(promptRequired("Task id: ")); }
        catch (NumberFormatException exception) { throw new IllegalArgumentException("Task id must be a number"); }
    }

    private Task.Priority readPriority(String label) {
        return readEnum(Task.Priority.class, label + " " + Arrays.toString(Task.Priority.values()) + " [MEDIUM]: ", Task.Priority.MEDIUM);
    }

    private LocalDate readOptionalDate(String label) {
        String value = prompt(label);
        return value.isBlank() ? null : parseDate(value);
    }

    private LocalDate parseDate(String value) {
        try { return LocalDate.parse(value); }
        catch (DateTimeParseException exception) { throw new IllegalArgumentException("Use date format yyyy-MM-dd"); }
    }

    private List<String> readTags(String label) {
        String value = prompt(label);
        return value.isBlank() ? List.of() : Arrays.stream(value.split(",")).map(String::trim).toList();
    }

    private <E extends Enum<E>> E readEnum(Class<E> type, String label) {
        return readEnum(type, label, null);
    }

    private <E extends Enum<E>> E readEnum(Class<E> type, String label, E defaultValue) {
        String value = prompt(label);
        if (value.isBlank() && defaultValue != null) return defaultValue;
        try { return Enum.valueOf(type, value.trim().toUpperCase(Locale.ROOT)); }
        catch (IllegalArgumentException exception) { throw new IllegalArgumentException("Unknown " + type.getSimpleName() + ": " + value); }
    }

    private String promptRequired(String label) {
        String value = prompt(label);
        if (value.isBlank()) throw new IllegalArgumentException("Value cannot be blank");
        return value;
    }

    private String prompt(String label) {
        output.print(label);
        return input.hasNextLine() ? input.nextLine().trim() : "";
    }
}
