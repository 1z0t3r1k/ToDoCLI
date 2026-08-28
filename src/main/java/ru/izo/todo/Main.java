package ru.izo.todo;

import ru.izo.todo.cli.ConsoleApplication;
import ru.izo.todo.taskmanager.service.TaskService;
import ru.izo.todo.taskmanager.storage.FileTaskRepository;

import java.nio.file.Path;

public final class Main {
    private Main() {
    }

    public static void main(String[] args) {
        Path storage = resolveStorage(args);
        TaskService service = new TaskService(new FileTaskRepository(storage));
        new ConsoleApplication(service, System.in, System.out).run();
    }

    private static Path resolveStorage(String[] args) {
        for (String argument : args) {
            if (argument.startsWith("--data=")) {
                String value = argument.substring("--data=".length()).trim();
                if (value.isEmpty()) throw new IllegalArgumentException("--data requires a file path");
                return Path.of(value);
            }
        }
        return Path.of("data", "tasks.json");
    }
}
