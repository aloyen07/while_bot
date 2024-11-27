package ru.aloyenz.whilebot.commands;

import org.jetbrains.annotations.Nullable;
import ru.aloyenz.whilebot.sql.permissions.PermissionType;

import java.util.List;

public abstract class Command {

    public abstract int argsNum();

    public String usage() {
        return usage(true);
    }

    public abstract String usage(boolean isShort);
    public abstract CommandExecutor executor();

    @Nullable
    public abstract List<PermissionType> needPermission();
}
