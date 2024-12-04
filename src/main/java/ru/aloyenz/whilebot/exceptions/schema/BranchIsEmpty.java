package ru.aloyenz.whilebot.exceptions.schema;

import ru.aloyenz.whilebot.sql.homework.schema.TreeBranch;

public class BranchIsEmpty extends RuntimeException {
    public BranchIsEmpty(int line, TreeBranch branch) {
        super("Ветвь с именем \"" + branch.getName() + "\" (ID: " + branch.getIndex() + ") пуста. Ошибка находится рядом со строкой " + line);

    }
}
