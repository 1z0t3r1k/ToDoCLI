package ru.izo.todo;

import ru.izo.todo.taskmanager.Task;
import ru.izo.todo.taskmanager.TaskRepository;
import ru.izo.todo.taskmanager.service.TaskService;
import ru.izo.todo.taskmanager.storage.InMemoryTaskRepository;

import java.time.LocalDate;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        TaskService taskService = new TaskService((TaskRepository) new InMemoryTaskRepository());
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

                        int id = taskService.createTask(name, description);
                        System.out.println("Task created with id: " + id);
                    }
                    case "2" -> System.out.println(taskService.getTasks());

                    case "3" -> {
                        System.out.print("Enter task id: ");
                        int id = Integer.parseInt(scanner.nextLine());
                        System.out.println(taskService.getTaskById(id));
                    }
                    case "4" -> {
                        System.out.print("Enter task id: ");
                        int id = Integer.parseInt(scanner.nextLine());

                        System.out.print("Enter new name: ");
                        String newName = scanner.nextLine();

                        taskService.renameTask(id, newName);
                        System.out.println("Task renamed");
                    }
                    case "5" -> {
                        System.out.print("Enter task id: ");
                        int id = Integer.parseInt(scanner.nextLine());

                        System.out.print("Enter new description: ");
                        String newDescription = scanner.nextLine();

                        taskService.changeTaskDescription(id, newDescription);
                        System.out.println("Description changed");
                    }
                    case "6" -> {
                        System.out.print("Enter task id: ");
                        int id = Integer.parseInt(scanner.nextLine());

                        taskService.markTaskDone(id);
                        System.out.println("Task marked as DONE");
                    }
                    case "7" -> {
                        System.out.print("Enter task id: ");
                        int id = Integer.parseInt(scanner.nextLine());

                        taskService.markTaskInProgress(id);
                        System.out.println("Task marked as IN_PROGRESS");
                    }
                    case "8" -> {
                        System.out.print("Enter task id: ");
                        int id = Integer.parseInt(scanner.nextLine());

                        taskService.markTaskUndone(id);
                        System.out.println("Task marked as UNDONE");
                    }
                    case "9" -> {
                        System.out.print("Enter task id: ");
                        int id = Integer.parseInt(scanner.nextLine());

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
                        System.out.print("Enter start date (yyyy-mm-dd): ");
                        LocalDate startDate = LocalDate.parse(scanner.nextLine());

                        System.out.print("Enter end date (yyyy-mm-dd): ");
                        LocalDate endDate = LocalDate.parse(scanner.nextLine());

                        System.out.println(taskService.findBetweenDates(startDate, endDate));
                    }
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
}