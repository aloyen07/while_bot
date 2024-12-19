package ru.aloyenz.whilebot.sql.homework;

import org.jetbrains.annotations.Nullable;
import ru.aloyenz.whilebot.Main;
import ru.aloyenz.whilebot.exceptions.RecordNotFoundException;
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

    public static void createHomework(String name, TreeBranch branch, Timestamp endsAt, Timestamp retakeDeadline, int lessonID) throws SQLException {
        Connection connection = Main.getSQLManager().getPool().getConnection();
        branch.createRecordInDatabase(connection);

        PreparedStatement ps = connection.prepareStatement("INSERT INTO homeworks (name, branches, ends_at, retake_deadline, lesson_id) VALUES (?, ?, ?, ?, ?) RETURNING id;", Statement.RETURN_GENERATED_KEYS);

        ps.setString(1, name);
        ps.setString(2, branch.toDatabaseString());
        ps.setLong(3, endsAt.getTime());
        ps.setLong(4, retakeDeadline.getTime());
        ps.setInt(5, lessonID);

        ps.executeUpdate();
        int index = -1;

        try (ResultSet rs = ps.getGeneratedKeys()) {
            if (rs.next()) {
                index = rs.getInt(1);
            }
        }

        ps.close();

        if (index == -1) {
            throw new SQLException("Index not found");
        }

        for (Homework homework : getHomeworksForLesson(lessonID)) {
            if (homework.id == index) {
                return;
            }
        }

        throw new SQLException("Homework not found!");
    }

    public static Homework getByID(int id) throws RecordNotFoundException, SQLException {
        Connection connection = Main.getSQLManager().getPool().getConnection();
        PreparedStatement ps = connection.prepareStatement("SELECT * FROM homeworks WHERE id = ?;");
        ps.setInt(1, id);

        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            return new Homework(rs, connection, true, false);
        } else {
            throw new RecordNotFoundException("homework", "ID", id);
        }
    }

    private final int id;
    private final String name;
    private final String branches;
    private final Timestamp createdAt;
    private Timestamp endsAt;
    private Timestamp retakeDeadline;
    private boolean isClosed;
    private final int lessonID;

    private TreeBranch branch;

    private boolean isFull;

    private Homework(ResultSet rs, Connection connection, boolean isClose, boolean getFull) throws SQLException {
        this.id = rs.getInt("id");
        this.name = rs.getString("name");
        this.branches = rs.getString("branches");
        this.createdAt = rs.getTimestamp("created_at");
        this.endsAt = new Timestamp(rs.getLong("ends_at"));
        this.retakeDeadline = new Timestamp(rs.getLong("retake_deadline"));
        this.isClosed = rs.getBoolean("is_closed");
        this.lessonID = rs.getInt("lesson_id");

        if (isClose) {
            connection.close();
        }

        if (getFull) {
            try {
                getBranchSchemaFromDatabase();
            } catch (RecordNotFoundException e) {
                throw new SQLException(e);
            }
        }

        isFull = getFull;
    }

    public void getBranchSchemaFromDatabase() throws SQLException, RecordNotFoundException {
        if (!isFull) {
            Connection connection = Main.getSQLManager().getPool().getConnection();

            branch = new TreeBranch(-1, "MAIN TREE", "", branches, connection);
            connection.close();

            isFull = true;
        }
    }

    @Nullable
    public TreeBranch getMainTreeBranch() {
        return branch;
    }

    public void update() throws SQLException {
        Connection connection = Main.getSQLManager().getPool().getConnection();

        PreparedStatement ps = connection.prepareStatement("UPDATE homeworks SET ends_at = ?, retake_deadline = ?," +
                "is_closed = ? WHERE id = ?;");
        ps.setLong(1, endsAt.getTime());
        ps.setLong(2, retakeDeadline.getTime());
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

    public Timestamp getRetakeDeadline() {
        return retakeDeadline;
    }

    public void setRetakeDeadline(Timestamp retakeDeadline) {
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
