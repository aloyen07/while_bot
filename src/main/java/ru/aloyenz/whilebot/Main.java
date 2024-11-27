package ru.aloyenz.whilebot;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.aloyenz.whilebot.bot.VKClient;
import ru.aloyenz.whilebot.commands.CommandManager;
import ru.aloyenz.whilebot.commands.Commands;
import ru.aloyenz.whilebot.sql.SQLManager;

import java.io.IOException;

public class Main {

    private static final Logger logger = LogManager.getLogger("Main Thread");

    private static Settings settings;
    private static SQLManager sqlManager;
    private static CommandManager manager;

    public static void main(String[] args) throws IOException {
        logger.info("WhileBot is starting");
        settings = new Settings(LogManager.getLogger("Settings Manager"));
        sqlManager = new SQLManager(settings, LogManager.getLogger("SQL Manager"));
        logger.info("Creating a new command manager and registering commands.");
        manager = new CommandManager(LogManager.getLogger("Command Manager"));
        Commands.registerAll(manager);
        new VKClient(LogManager.getLogger("VK Client"),
                LogManager.getLogger("Message Handler"));
    }

    public static Settings getSettings() {
        return settings;
    }

    public static CommandManager getManager() {
        return manager;
    }

    public static SQLManager getSQLManager() {
        return sqlManager;
    }
}