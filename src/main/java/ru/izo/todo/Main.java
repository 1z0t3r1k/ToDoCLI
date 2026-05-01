package ru.izo.todo;

import ru.izo.todo.taskmanager.Task;
import ru.izo.todo.taskmanager.service.TaskService;
import ru.izo.todo.taskmanager.storage.FileTaskRepository;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Scanner;

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class Main {
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static void main(String[] args) {
        Path filePath = Path.of("data", "tasks.json");
        TaskService taskService = new TaskService(new FileTaskRepository(filePath));
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("\n=== TASK MANAGER ===");
            System.out.println("1. Create task");
            System.out.println("2. Show all tasks");
            System.out.println("3. Get task by id");
            System.out.println("4. Rename task");
            System.out.println("5. Change description");
            System.out.println("6. Mark task DONE");
            System.out.println("7. Mark task IN_PROGRESS");
            System.out.println("8. Mark task UNDONE");
            System.out.println("9. Delete task by id");
            System.out.println("10. Find by status");
            System.out.println("11. Find by name");
            System.out.println("12. Find between dates");
            System.out.println("13. Change deadline");
            System.out.println("14. Delete deadline");
            System.out.println("15. Find overdue tasks");
            System.out.println("16. Find tasks for today");
            System.out.println("0. Exit");
            System.out.print("Choose action: ");

            String input = scanner.nextLine();

            try {
                switch (input) {
                    case "1" -> {
                        System.out.print("Enter task name: ");
                        String name = scanner.nextLine();

                        if (taskService.existsByExactName(name)) {
                            System.out.printf(
                                    "Task with name <%s> already exists. Create anyway? (Y/N): ",
                                    name
                            );

                            String answer = scanner.nextLine().trim().toUpperCase();
                            if (!answer.equals("Y")) {
                                System.out.println("Task creation cancelled");
                                break;
                            }
                        }

                        System.out.print("Enter task description: ");
                        String description = scanner.nextLine();

                        LocalDate deadline = readOptionalDate(
                                scanner,
                                "Enter task deadline (yyyy-MM-dd) or press Enter to skip: "
                        );

                        int id = taskService.createTask(name, description, deadline);
                        System.out.println("Task created with id: " + id);
                    }
                    case "2" -> System.out.println(taskService.getTasks());

                    case "3" -> {
                        int id = readInt(scanner, "Enter task id: ");
                        System.out.println(taskService.getTaskById(id));
                    }
                    case "4" -> {
                        int id = readInt(scanner, "Enter task id: ");

                        System.out.print("Enter new name: ");
                        String newName = scanner.nextLine();

                        taskService.renameTask(id, newName);
                        System.out.println("Task renamed");
                    }
                    case "5" -> {
                        int id = readInt(scanner, "Enter task id: ");

                        System.out.print("Enter new description: ");
                        String newDescription = scanner.nextLine();

                        taskService.changeTaskDescription(id, newDescription);
                        System.out.println("Description changed");
                    }
                    case "6" -> {
                        int id = readInt(scanner, "Enter task id: ");

                        taskService.markTaskDone(id);
                        System.out.println("Task marked as DONE");
                    }
                    case "7" -> {
                        int id = readInt(scanner, "Enter task id: ");

                        taskService.markTaskInProgress(id);
                        System.out.println("Task marked as IN_PROGRESS");
                    }
                    case "8" -> {
                        int id = readInt(scanner, "Enter task id: ");

                        taskService.markTaskUndone(id);
                        System.out.println("Task marked as UNDONE");
                    }
                    case "9" -> {
                        int id = readInt(scanner, "Enter task id: ");

                        taskService.deleteTaskById(id);
                        System.out.println("Task deleted");
                    }
                    case "10" -> {
                        System.out.println("Choose status: UNDONE / IN_PROGRESS / DONE");
                        String statusInput = scanner.nextLine().toUpperCase();
                        Task.TaskStatus status = Task.TaskStatus.valueOf(statusInput);
                        System.out.println(taskService.findByStatus(status));
                    }
                    case "11" -> {
                        System.out.print("Enter text for search: ");
                        String name = scanner.nextLine();
                        System.out.println(taskService.findByName(name));
                    }
                    case "12" -> {
                        LocalDate startDate = readRequiredDate(scanner, "Enter start date (yyyy-MM-dd): ");
                        LocalDate endDate = readRequiredDate(scanner, "Enter end date (yyyy-MM-dd): ");

                        System.out.println(taskService.findBetweenDates(startDate, endDate));
                    }
                    case "13" -> {
                        int id = readInt(scanner, "Enter task id: ");
                        LocalDate newDeadline = readRequiredDate(scanner, "Enter task deadline (yyyy-MM-dd): ");
                        taskService.changeDeadline(id, newDeadline);
                    }
                    case "14" -> {
                        int id = readInt(scanner, "Enter task id: ");
                        taskService.changeDeadline(id, null);
                    }

                    case "15" -> System.out.println(taskService.findOverdueTasks());

                    case "16" -> System.out.println(taskService.findTasksForToday());

                    case "0" -> {
                        System.out.println("Goodbye!");
                        scanner.close();
                        return;
                    }
                    default -> System.out.println("Unknown command");
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private static LocalDate readRequiredDate(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);

            String input = scanner.nextLine().trim();

            if (input.isEmpty()) {
                System.out.println("Date is required. Use yyyy-MM-dd, for example: 2026-04-24");
                continue;
            }

            try {
                return LocalDate.parse(input, DATE_FORMATTER);
            } catch (DateTimeParseException e) {
                System.out.println("Invalid date format. Use yyyy-MM-dd, for example: 2026-04-24");
            }
        }
    }

    private static LocalDate readOptionalDate(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);

            String input = scanner.nextLine().trim();

            if (input.isEmpty()) {
                return null;
            }

            try {
                return LocalDate.parse(input, DATE_FORMATTER);
            } catch (DateTimeParseException e) {
                System.out.println("Invalid date format. Use yyyy-MM-dd, for example: 2026-04-24");
            }
        }
    }

    private static int readInt(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);

            String input = scanner.nextLine().trim();

            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Invalid number. Enter integer value, for example: 1");
            }
        }
    }
}