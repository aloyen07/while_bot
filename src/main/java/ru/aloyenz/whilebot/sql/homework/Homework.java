package ru.aloyenz.whilebot.sql.homework;

import ru.aloyenz.whilebot.Main;
import ru.aloyenz.whilebot.sql.homework.schema.TreeBranch;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Homework {

    public static List<Homework> getHomeworksForLesson(int lessonID) throws SQLException {
        List<Homework> homeworks = new ArrayList<>();

        Connection connection = Main.getSQLManager().getPool().getConnection();
        PreparedStatement ps = connection.prepareStatement("SELECT * FROM homeworks WHERE lesson_id = ?;");
        ps.setInt(1, lessonID);
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            homeworks.add(new Homework(rs, connection, false, false));
        }

        ps.close();
        connection.close();

        return homeworks;
    }

    public static Homework createHomework(String name, TreeBranch branch, Timestamp endsAt, int lessonID) throws SQLException {
        Connection connection = Main.getSQLManager().getPool().getConnection();
        PreparedStatement ps = connection.prepareStatement("INSERT INTO homeworks (name, branches, ends_at, lesson_id) VALUES (?, ?, ?, ?);");

        ps.setString(1, name);
        ps.setString(2, branch.toDatabaseString());
        ps.setTimestamp(3, endsAt);
        ps.setInt(4, lessonID);

        ps.executeUpdate();
        ps.close();

        return (Homework) getHomeworksForLesson(lessonID);
    }

    public static Homework createHomework(String name, TreeBranch branch, Timestamp endsAt, Lesson lesson) throws SQLException {
        return createHomework(name, branch, endsAt, lesson.getId());
    }

    private final int id;
    private final String name;
    private final String branches;
    private final Timestamp createdAt;
    private Timestamp endsAt;
    private int retakeDeadline;
    private boolean isClosed;
    private final int lessonID;

    private Homework(ResultSet rs, Connection connection, boolean isClose, boolean getFull) throws SQLException {

        this.id = rs.getInt("id");
        this.name = rs.getString("name");
        this.branches = rs.getString("branches");
        this.createdAt = rs.getTimestamp("created_at");
        this.endsAt = rs.getTimestamp("ends_at");
        this.retakeDeadline = rs.getInt("retake_deadline");
        this.isClosed = rs.getBoolean("is_closed");
        this.lessonID = rs.getInt("lesson_id");

        if (isClose) {
            connection.close();
        }

        if (getFull) {
            getBranchSchemaFromDatabase();
        }
    }

    public void getBranchSchemaFromDatabase() {

    }

    public void update() throws SQLException {
        Connection connection = Main.getSQLManager().getPool().getConnection();

        PreparedStatement ps = connection.prepareStatement("UPDATE homeworks SET ends_at = ?, retake_deadlime = ?," +
                "is_closed = ? WHERE id = ?;");
        ps.setTimestamp(1, endsAt);
        ps.setInt(2, retakeDeadline);
        ps.setBoolean(3, isClosed);
        ps.setInt(4, id);

        ps.executeUpdate();
        ps.close();
        connection.close();
    }


    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getBranches() {
        return branches;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public Timestamp getEndsAt() {
        return endsAt;
    }

    public void setEndsAt(Timestamp endsAt) {
        this.endsAt = endsAt;
    }

    public int getRetakeDeadline() {
        return retakeDeadline;
    }

    public void setRetakeDeadline(int retakeDeadline) {
        this.retakeDeadline = retakeDeadline;
    }

    public boolean isClosed() {
        return isClosed;
    }

    public void setClosed(boolean closed) {
        isClosed = closed;
    }

    public int getLessonID() {
        return lessonID;
    }
}
