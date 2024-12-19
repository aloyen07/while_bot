package ru.aloyenz.whilebot.sql.utils;

import java.sql.SQLException;

public interface ConsumerWithSQLException<T> {

    void accept(T arg) throws SQLException;
}
