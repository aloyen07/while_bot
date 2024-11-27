package ru.aloyenz.whilebot.sql;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.pool.HikariPool;
import org.apache.logging.log4j.Logger;
import ru.aloyenz.whilebot.Settings;

public class SQLManager {

    private final Logger logger;

    private HikariPool pool;

    public SQLManager(Settings settings, Logger logger) {
        //jdbc:postgresql://localhost:5432/sales
        this.logger = logger;
        logger.info("Initializing new SQL Manager");
        reloadSettings(settings);
    }

    private HikariConfig generateConfig(Settings settings) {
        String jdbcURL = "jdbc:postgresql://" + settings.getIP() + ":" + settings.getPort() + "/" + settings.getDatabase();

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(jdbcURL);
        config.setUsername(settings.getUser());
        config.setPassword(settings.getPassword());
        config.setMaximumPoolSize(settings.getConnectionCount());
        config.setSchema(settings.getSchema());

        return config;
    }

    private void recreateHikariPool(HikariConfig config) {
        pool = new HikariPool(config);
    }

    public void reloadSettings(Settings settings) {
        logger.info("Reloading settings...");
        recreateHikariPool(generateConfig(settings));
        logger.info("New HikariPool instance is created.");
    }

    public HikariPool getPool() {
        return pool;
    }
}
