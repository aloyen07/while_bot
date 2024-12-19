package ru.aloyenz.whilebot;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.aloyenz.whilebot.bot.VKClient;
import ru.aloyenz.whilebot.commands.CommandManager;
import ru.aloyenz.whilebot.commands.Commands;
import ru.aloyenz.whilebot.services.SafeConfirmationService;
import ru.aloyenz.whilebot.sql.SQLManager;

import java.io.IOException;

public class Main {

    private static final Logger logger = LogManager.getLogger("Main Thread");

    public static Logger getLogger(String name) {
        return LogManager.getLogger(name);
    }

    public static VKClient getVKClient() {
        return vkClient;
    }

    public static void handleInitEvent() {
        initialized = true;
    }

    public static boolean isInitialized() {
        return initialized;
    }

    private static SafeConfirmationService confirmationService;
    private static Settings settings;
    private static SQLManager sqlManager;
    private static CommandManager manager;
    private static VKClient vkClient;
    private static boolean initialized = false;

    public static void main(String[] args) throws IOException {
        logger.info("WhileBot is starting");
        confirmationService = new SafeConfirmationService();
        initSettings();
        initSQL();
        manager = new CommandManager(LogManager.getLogger("Command Manager"));
        Commands.registerAll(manager);
        vkClient = new VKClient(LogManager.getLogger("VK Client"),
                LogManager.getLogger("Message Handler"));
    }

    public static void initSettings() throws IOException {
        settings = new Settings(LogManager.getLogger("Settings Manager"));
    }

    public static void initSQL() {
        sqlManager = new SQLManager(settings, LogManager.getLogger("SQL Manager"));
        logger.info("Creating a new command manager and registering commands.");
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

    public static SafeConfirmationService getConfirmationService() {
        return confirmationService;
    }
}