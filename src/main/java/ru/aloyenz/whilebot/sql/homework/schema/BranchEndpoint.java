package ru.aloyenz.whilebot.sql.homework.schema;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class BranchEndpoint extends Branch {

    public BranchEndpoint(int index, String name, String description) {
        super(index, name, description);
    }

    @Override
    public List<Branch> getBranches() {
        throw new RuntimeException("Operation getBranches not supported for endpoint!");
    }

    @Override
    public boolean supportsBranching() {
        return false;
    }

    @Override
    public void createRecordInDatabase(Connection connection) throws SQLException {
        PreparedStatement ps = connection.prepareStatement("INSERT INTO endpoints (number, name, description) VALUES (?, ?, ?);");
        ps.setInt(1, index);
        ps.setString(2, name);
        ps.setString(3, description);

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
}
