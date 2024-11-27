package ru.aloyenz.whilebot;

import com.google.gson.*;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class Settings {

    public static final String FILE_PATH = "settings.json";

    private final Logger logger;

    private String botAPIKey;
    private long botGroupID;
    private String longPollVersion;
    private int waitTime;

    private String IP;
    private int port;
    private String user;
    private String password;
    private String database;
    private int connectionCount;
    private String schema;

    protected Settings(Logger logger) throws IOException {
        this.logger = logger;
        reload();
    }

    private String readOrCreateConfig() throws IOException {
        File file = new File(FILE_PATH);
        if (!file.exists()) {
            // File is not exists. Reading resource and writing defaults.
            // Get resource
            logger.warn("Could not find settings file. Writing a new default file!");
            ClassLoader classloader = Thread.currentThread().getContextClassLoader();
            InputStreamReader is = new InputStreamReader(Objects.requireNonNull(classloader
                    .getResourceAsStream(FILE_PATH)), StandardCharsets.UTF_8);

            // Creating a file
            if (!file.createNewFile()) {
                throw new IOException("Cannot create a new settings file!");
            }

            FileWriter writer = new FileWriter(file);
            int ch = is.read();

            while (ch != -1) {
                writer.write(ch);
                ch = is.read();
            }
            writer.flush();
            logger.info("New file successfully wrote.");
        }

        // Reading a file
        FileReader reader = new FileReader(FILE_PATH, StandardCharsets.UTF_8);

        StringBuilder out = new StringBuilder();
        int ch = reader.read();

        while (ch != -1) {
            out.append((char) ch);
            ch = reader.read();
        }
        logger.info("File successfully read.");

        return out.toString();
    }

    @SuppressWarnings("deprecation")
    protected void reload() throws IOException {
        String json = readOrCreateConfig();

        JsonObject object = (JsonObject) new JsonParser().parse(json);
        JsonObject element = (JsonObject) object.get("bot");

        botAPIKey = element.get("APIKEY").getAsString();
        botGroupID = element.get("GROUP_ID").getAsLong();
        longPollVersion = element.get("LONG_POLL_API_VERSION").getAsString();
        waitTime = element.get("WAIT_TIME").getAsInt();

        element = (JsonObject) object.get("database");

        IP = element.get("IP").getAsString();
        port = element.get("PORT").getAsInt();
        user = element.get("USER").getAsString();
        password = element.get("PASSWORD").getAsString();
        database = element.get("DATABASE").getAsString();
        connectionCount = element.get("CONNECTION_COUNT").getAsInt();
        schema = element.get("SCHEMA").getAsString();

        logger.info("Configuration successfully reloaded!");
    }

    // -- GETTERS -- //
    public String getBotAPIKey() {
        return botAPIKey;
    }

    public long getBotGroupID() {
        return botGroupID;
    }

    public String getLongPollVersion() {
        return longPollVersion;
    }

    public int getWaitTime() {
        return waitTime;
    }


    public String getIP() {
        return IP;
    }

    public int getPort() {
        return port;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

    public String getDatabase() {
        return database;
    }

    public int getConnectionCount() {
        return connectionCount;
    }

    public String getSchema() {
        return schema;
    }
}
