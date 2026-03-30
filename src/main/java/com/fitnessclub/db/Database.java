package com.fitnessclub.db;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public final class Database {
    private static final String DB_DIR = "data";
    private static final String DB_FILE = "fitness_club.db";
    private static Connection connection;

    private Database() {}

    public static synchronized Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                Path dir = Path.of(DB_DIR);
                if (!Files.exists(dir)) {
                    Files.createDirectories(dir);
                }
            } catch (Exception e) {
                throw new SQLException("Не удалось создать каталог БД", e);
            }
            String url = "jdbc:sqlite:" + DB_DIR + "/" + DB_FILE;
            connection = DriverManager.getConnection(url);
            connection.createStatement().execute("PRAGMA foreign_keys = ON");
            initSchema(connection);
        }
        return connection;
    }

    private static void initSchema(Connection c) throws SQLException {
        try (Statement st = c.createStatement()) {
            st.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT NOT NULL UNIQUE,
                    password TEXT NOT NULL,
                    full_name TEXT NOT NULL,
                    role TEXT NOT NULL CHECK(role IN ('ADMIN','TRAINER','CLIENT')),
                    client_id INTEGER
                )
                """);
            st.execute("""
                CREATE TABLE IF NOT EXISTS clients (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL,
                    age INTEGER NOT NULL,
                    gender TEXT NOT NULL,
                    phone TEXT NOT NULL,
                    email TEXT NOT NULL,
                    user_id INTEGER UNIQUE REFERENCES users(id)
                )
                """);
            st.execute("""
                CREATE TABLE IF NOT EXISTS exercises (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL UNIQUE,
                    description TEXT
                )
                """);
            st.execute("""
                CREATE TABLE IF NOT EXISTS training_schedules (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    trainer_user_id INTEGER NOT NULL REFERENCES users(id),
                    client_id INTEGER NOT NULL REFERENCES clients(id),
                    start_time TEXT NOT NULL,
                    end_time TEXT NOT NULL,
                    notes TEXT
                )
                """);
            st.execute("""
                CREATE TABLE IF NOT EXISTS training_plans (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    client_id INTEGER NOT NULL REFERENCES clients(id),
                    trainer_user_id INTEGER NOT NULL REFERENCES users(id),
                    name TEXT NOT NULL,
                    created_at TEXT NOT NULL
                )
                """);
            st.execute("""
                CREATE TABLE IF NOT EXISTS plan_exercises (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    plan_id INTEGER NOT NULL REFERENCES training_plans(id) ON DELETE CASCADE,
                    exercise_id INTEGER NOT NULL REFERENCES exercises(id),
                    sets INTEGER NOT NULL,
                    reps INTEGER NOT NULL,
                    sort_order INTEGER NOT NULL DEFAULT 0
                )
                """);
            st.execute("""
                CREATE TABLE IF NOT EXISTS progress_log (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    plan_exercise_id INTEGER NOT NULL REFERENCES plan_exercises(id) ON DELETE CASCADE,
                    log_date TEXT NOT NULL,
                    completed_sets INTEGER NOT NULL,
                    completed_reps INTEGER NOT NULL,
                    notes TEXT
                )
                """);
            st.execute("""
                CREATE TABLE IF NOT EXISTS attendance (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    schedule_id INTEGER NOT NULL REFERENCES training_schedules(id) ON DELETE CASCADE,
                    client_id INTEGER NOT NULL REFERENCES clients(id),
                    attended INTEGER NOT NULL DEFAULT 1,
                    recorded_at TEXT NOT NULL
                )
                """);
        }
        seedIfEmpty(c);
    }

    private static void seedIfEmpty(Connection c) throws SQLException {
        try (var rs = c.createStatement().executeQuery("SELECT COUNT(*) FROM users")) {
            if (rs.next() && rs.getInt(1) > 0) {
                return;
            }
        }
        try (Statement st = c.createStatement()) {
            st.executeUpdate("""
                INSERT INTO clients (name, age, gender, phone, email, user_id)
                VALUES ('Иван Демо', 28, 'М', '+79001234567', 'ivan@example.com', NULL)
                """);
            st.executeUpdate("""
                INSERT INTO users (username, password, full_name, role, client_id)
                VALUES
                ('admin', 'admin', 'Администратор', 'ADMIN', NULL),
                ('trainer1', 'trainer', 'Тренер Петров', 'TRAINER', NULL),
                ('client1', 'client', 'Иван Демо', 'CLIENT', 1)
                """);
            st.executeUpdate("UPDATE clients SET user_id = 3 WHERE id = 1");
            st.executeUpdate("""
                INSERT INTO exercises (name, description) VALUES
                ('Жим лёжа', 'Грудь, трицепс'),
                ('Приседания', 'Ноги, ягодицы'),
                ('Становая тяга', 'Спина, задняя поверхность бедра'),
                ('Подтягивания', 'Спина'),
                ('Планка', 'Кор')
                """);
            st.executeUpdate("""
                INSERT INTO training_schedules (trainer_user_id, client_id, start_time, end_time, notes)
                VALUES (2, 1, '2026-03-31T10:00', '2026-03-31T11:00', 'Силовая'),
                (2, 1, '2026-04-02T10:00', '2026-04-02T11:00', 'Кардио')
                """);
            st.executeUpdate("""
                INSERT INTO training_plans (client_id, trainer_user_id, name, created_at)
                VALUES (1, 2, 'Базовый план', date('now'))
                """);
            st.executeUpdate("""
                INSERT INTO plan_exercises (plan_id, exercise_id, sets, reps, sort_order) VALUES
                (1, 1, 3, 10, 0),
                (1, 2, 4, 12, 1)
                """);
            st.executeUpdate("""
                INSERT INTO progress_log (plan_exercise_id, log_date, completed_sets, completed_reps, notes)
                VALUES (1, date('now','-2 day'), 3, 10, 'Норма'),
                (2, date('now','-1 day'), 4, 11, 'Устал')
                """);
            st.executeUpdate("""
                INSERT INTO attendance (schedule_id, client_id, attended, recorded_at)
                VALUES (1, 1, 1, datetime('now','-1 day'))
                """);
        }
    }
}
