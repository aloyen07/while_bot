package ru.aloyenz.whilebot.sql.homework;

import ru.aloyenz.whilebot.Main;
import ru.aloyenz.whilebot.exceptions.RecordNotFoundException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Lesson {

    public static Lesson createLesson(String shortName, String name) throws RecordNotFoundException, SQLException {
        Connection connection = Main.getSQLManager().getPool().getConnection();

        PreparedStatement ps = connection.prepareStatement("INSERT INTO lessons (short_name, name) VALUES (?, ?);");
        ps.setString(1, shortName);
        ps.setString(2, name);

        ps.executeUpdate();
        ps.close();

        return lessonFor(name, connection, true);
    }

    public static List<Lesson> getLessons() throws SQLException, RecordNotFoundException {
        Connection connection = Main.getSQLManager().getPool().getConnection();

        PreparedStatement ps = connection.prepareStatement("SELECT id FROM lessons;");
        ResultSet rs = ps.executeQuery();

        List<Lesson> lessons = new ArrayList<>();
        while (rs.next()) {
            lessons.add(lessonFor(rs.getInt(1), connection, false));
        }

        return lessons;
    }

    public static Lesson lessonFor(int id) throws SQLException, RecordNotFoundException {
        Connection connection = Main.getSQLManager().getPool().getConnection();

        return lessonFor(id, connection, true);
    }

    public static Lesson lessonFor(String name) throws SQLException, RecordNotFoundException {
        Connection connection = Main.getSQLManager().getPool().getConnection();

        return lessonFor(name, connection, true);
    }

    private static Lesson lessonFor(int id, Connection connection, boolean isClose) throws SQLException, RecordNotFoundException {
        PreparedStatement ps = connection.prepareStatement("SELECT * FROM lessons WHERE id = ?;");
        ps.setInt(1, id);

        ResultSet rs = ps.executeQuery();

        try {
            return new Lesson(rs, connection, ps, isClose);
        } catch (RecordNotFoundException e) {
            throw new RecordNotFoundException("lesson", "id", id);
        }
    }

    private static Lesson lessonFor(String name, Connection connection, boolean isClose) throws SQLException, RecordNotFoundException {
        PreparedStatement ps = connection.prepareStatement("SELECT * FROM lessons WHERE LOWER(name) = ? OR LOWER(short_name) = ?;");
        ps.setString(1, name.toLowerCase());
        ps.setString(2, name.toLowerCase());

        ResultSet rs = ps.executeQuery();

        try {
            return new Lesson(rs, connection, ps, isClose);
        } catch (RecordNotFoundException e) {
            throw new RecordNotFoundException("lesson", "name", name);
        }
    }

    private final int id;
    private final String shortName;
    private final String name;
    private boolean isClosed;

    private Lesson(ResultSet rs, Connection connection, PreparedStatement ps, boolean isClose) throws SQLException, RecordNotFoundException {

        if (rs.next()) {
            this.id = rs.getInt("id");
            this.shortName = rs.getString("short_name");
            this.name = rs.getString("name");
            this.isClosed = rs.getBoolean("is_closed");
            if (isClose) {
                connection.close();
            }
        } else {
            if (isClose) {
                connection.close();
            }
            throw new RecordNotFoundException();
        }

        rs.close();
        ps.close();

    }

    public String getShortName() {
        return shortName;
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public boolean isClosed() {
        return isClosed;
    }

    public void setClosedNoUpdate(boolean closed) {
        isClosed = closed;
    }

    

    public List<Homework> getHomeworks() throws SQLException {
        return Homework.getHomeworksForLesson(id);
    }

    public void updateInDatabase() throws SQLException {
        Connection connection = Main.getSQLManager().getPool().getConnection();
        PreparedStatement ps = connection.prepareStatement("UPDATE lessons SET is_closed = ? WHERE id = ?;");

        ps.setBoolean(1, isClosed);
        ps.setInt(2, id);

        ps.executeUpdate();

        ps.close();
        connection.close();
    }

    @Override
    public String toString() {
        return name + " (" + shortName + ") ID: " + id + ".";
    }
}
