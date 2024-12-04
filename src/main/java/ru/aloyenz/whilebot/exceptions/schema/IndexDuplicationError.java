package ru.aloyenz.whilebot.exceptions.schema;

import ru.aloyenz.whilebot.sql.homework.schema.Branch;
import ru.aloyenz.whilebot.sql.homework.schema.TreeBranch;

public class IndexDuplicationError extends RuntimeException {

    public IndexDuplicationError(TreeBranch masterBranch, Branch duplicationBranch) {
        super("Дубликат индекса " + duplicationBranch.getIndex() + " в ветке с именем " + duplicationBranch.getName() + " внутри ветки " + masterBranch.getName() + " (ID мастер-ветки: " + masterBranch.getIndex() + ").");
    }
}
