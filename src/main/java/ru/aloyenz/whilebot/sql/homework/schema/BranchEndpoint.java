package ru.aloyenz.whilebot.sql.homework.schema;

import ru.aloyenz.whilebot.Main;

import java.sql.*;
import java.util.List;

public class BranchEndpoint extends Branch {

    private int tookID;

    public BranchEndpoint(int index, String name, String description, int tookID) {
        super(index, name, description);

        this.tookID = tookID;
    }

    public BranchEndpoint(int index, String name, String description) {
        this(index, name, description, 0);
    }

    @Override
    public List<Branch> getBranches() {
        throw new RuntimeException("Operation getBranches not supported for endpoint!");
    }

    @Override
    public boolean supportsBranching() {
        return false;
    }

    public int getTookID() {
        return tookID;
    }

    public void setTookID(int tookID) throws SQLException {
        this.tookID = tookID;

        Connection connection = Main.getSQLManager().getPool().getConnection();
        PreparedStatement ps = connection.prepareStatement("UPDATE endpoints SET took_id = ? WHERE id = ?;");
        ps.setInt(1, tookID);
        ps.setInt(2, sqlID);

        ps.executeUpdate();

        ps.close();
        connection.close();
    }

    @Override
    public void createRecordInDatabase(Connection connection) throws SQLException {
        PreparedStatement ps = connection.prepareStatement("INSERT INTO endpoints (number, name, description) VALUES (?, ?, ?) RETURNING id;", Statement.RETURN_GENERATED_KEYS);
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
