package ru.izo.todo.taskmanager.service;

public record TaskStatistics(
        long total,
        long undone,
        long inProgress,
        long done,
        long overdue,
        long archived,
        double completionRate
) {
}
