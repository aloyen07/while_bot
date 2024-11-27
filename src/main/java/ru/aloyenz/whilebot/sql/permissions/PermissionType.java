package ru.aloyenz.whilebot.sql.permissions;

public enum PermissionType {

    // Все привилегии записываются в 32-битное INT-число.
    // Нумерация указана в порядке от младшего бита к старшему

    USE(0, "Основные функции",
            "Позволяет использовать все пользовательские команды."),
    ADD(1, "Добавление тем",
            "Позволяет добавлять новые темы (команда \"добавить\")."),
    FORCE_RETAKE(2, "Отказ в дедлайне",
            "Позволяет отказаться от темы тогда, когда наступил дедлайн на отказ."),
    CLOSE(3, "Закрытие тем", "Позволяет закрыть тему раньше положенного срока"),
    RETAKE_OTHER(4, "Отказ от темы другим людям",
            "Позволяет сделать отказ от темы другим людям без их согласия. Не работает в дедлайне."),
    FORCE_RETAKE_OTHER(5, "Отказ от темы в дедлайне другим людям",
            "То же самое, что и \"Отказ от темы другим людям\", только работает ТОЛЬКО в дедлайне."),
    ADMINISTRATOR(30, "Администратор",
            "Имеет все привилегии (не распространяется на команды разработчика). Может выдавать и забирать привилегии других."),
    DEVELOPER(31, "Разработчик",
            "Включает в себя не только все привилегии, но и доступ к командам разработчика.");

    public static PermissionType getFromID(int id) {
        for (PermissionType type : PermissionType.values()) {
            if (type.getPos() == id) {
                return type;
            }
        }

        return null;
    }

    private final int pos;
    private final String name;
    private final String description;

    PermissionType(int pos, String name, String description) {
        this.pos = pos;
        this.name = name;
        this.description = description;
    }

    public int getPos() {
        return pos;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}
