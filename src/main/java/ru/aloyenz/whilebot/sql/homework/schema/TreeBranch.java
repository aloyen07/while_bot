package ru.aloyenz.whilebot.sql.homework.schema;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TreeBranch extends Branch {

    private boolean isWrote = false;

    public TreeBranch(int index, String name, String description) {
        super(index, name, description);
    }

    @Override
    public boolean supportsBranching() {
        return true;
    }

    @Override
    public void createRecordInDatabase(Connection connection) throws SQLException {
        if (getBranches().isEmpty()) {
            System.out.println("WARN: branch " + this + " is empty!");
        } else {
            StringBuilder builder = new StringBuilder();
            for (Branch branch : getBranches()) {
                branch.createRecordInDatabase(connection);
                if (branch instanceof TreeBranch) {
                    builder.append("b");
                } else {
                    builder.append("e");
                }
                builder.append(branch.sqlID).append(";");
            }
            String ids = builder.toString();
            ids = ids.substring(0, ids.length() - 1);

            PreparedStatement ps = connection.prepareStatement("INSERT INTO branches (number, name, description, branches) VALUES  (?, ?, ?, ?);");

            ps.setInt(1, index);
            ps.setString(2, name);
            ps.setString(3, description);
            ps.setString(4, ids);

            ps.executeUpdate();

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    sqlID = generatedKeys.getInt(1);
                    ps.close();
                }
                else {
                    throw new SQLException("Creating record is failed, no ID obtained.");
                }
            }
        }

        isWrote = true;
    }

    public boolean isEmpty() {
        return getBranches().isEmpty();
    }

    public boolean isWrote() {
        return isWrote;
    }

    public String toDatabaseString() {
        // TODO
        return "";
    }
}
