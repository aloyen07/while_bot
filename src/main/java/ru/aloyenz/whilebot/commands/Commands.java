package ru.aloyenz.whilebot.commands;

import ru.aloyenz.whilebot.commands.impl.CheckPermissionCommand;
import ru.aloyenz.whilebot.commands.impl.GoidaCommand;
import ru.aloyenz.whilebot.commands.impl.GrantCommand;
import ru.aloyenz.whilebot.commands.impl.RevokeCommand;

public enum Commands {

    GOIDA("гойда", new GoidaCommand()),
    CHECK_PERMS("perm_level", new CheckPermissionCommand()),
    GRANT("grant", new GrantCommand()),
    REVOKE("revoke", new RevokeCommand());

    public static void registerAll(CommandManager manager) {
        for (Commands command : values()) {
            manager.registerCommand(command.getCommand(), command.getExecutor());
        }
    }

    private final String command;
    private final Command executor;

    Commands(String command, Command executor) {
        this.command = command;
        this.executor = executor;
    }

    public String getCommand() {
        return command;
    }

    public Command getExecutor() {
        return executor;
    }
}
