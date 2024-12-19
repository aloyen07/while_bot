package ru.aloyenz.whilebot.sql.homework.schema;

import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.objects.users.User;
import org.jetbrains.annotations.Nullable;
import ru.aloyenz.whilebot.exceptions.RecordNotFoundException;
import ru.aloyenz.whilebot.sql.utils.VKStaticMethods;

import java.sql.*;
import java.util.regex.Pattern;

public class TreeBranch extends Branch {


    public TreeBranch(int index, String name, String description) {
        super(index, name, description);
    }

    public TreeBranch(int index, String name, String description, String dbString, Connection connection) throws SQLException, RecordNotFoundException {
        super(index, name, description);

        fill(dbString, connection);
    }

    private void fill(String dbString, Connection connection) throws SQLException, RecordNotFoundException {
        for (String dbIndex : dbString.split(";")) {
            int index = Integer.parseInt(dbIndex.substring(1));

            if (dbIndex.equals("e" + index)) { // Endpoint
                PreparedStatement ps = connection.prepareStatement("SELECT * FROM endpoints WHERE id = ?;");
                ps.setInt(1, index);

                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    int dbNum = rs.getInt("number");
                    String dbName = rs.getString("name");
                    String dbDescription = rs.getString("description");
                    int tookID = rs.getInt("took_id");

                    BranchEndpoint branchEndpoint = new BranchEndpoint(dbNum, dbName, dbDescription, tookID);
                    branchEndpoint.sqlID = index;

                    addBranch(branchEndpoint);
                    rs.close();
                    ps.close();
                } else {
                    rs.close();
                    ps.close();
                    throw new RecordNotFoundException("endpoint", "ID", index);
                }

            } else if (dbIndex.equals("b" + index)) {
                PreparedStatement ps = connection.prepareStatement("SELECT * FROM branches WHERE id = ?;");
                ps.setInt(1, index);

                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    int dbNum = rs.getInt("number");
                    String dbName = rs.getString("name");
                    String dbDescription = rs.getString("description");
                    String branches = rs.getString("branches");

                    TreeBranch branch = new TreeBranch(dbNum, dbName, dbDescription, branches, connection);
                    branch.sqlID = index;

                    addBranch(branch);
                    rs.close();
                    ps.close();
                } else {
                    rs.close();
                    ps.close();
                    throw new RecordNotFoundException("branch", "ID", index);
                }
            }
        }
    }

    @Nullable
    public Branch getBranchByID(int id) {
        for (Branch branch : getBranches()) {
            if (branch.getIndex() == id) {
                return branch;
            }
        }

        return null;
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

            if (!isMain()) {
                String ids = builder.toString();
                ids = ids.substring(0, ids.length() - 1);

                PreparedStatement ps = connection.prepareStatement("INSERT INTO branches (number, name, description, branches) VALUES  (?, ?, ?, ?) RETURNING id;", Statement.RETURN_GENERATED_KEYS);

                ps.setInt(1, index);
                ps.setString(2, name);
                ps.setString(3, description);
                ps.setString(4, ids);

                ps.executeUpdate();

                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        sqlID = generatedKeys.getInt(1);
                        ps.close();
                    } else {
                        throw new SQLException("Creating record is failed, no ID obtained.");
                    }
                }
            }
        }
    }

    public boolean isMain() {
        return index == -1 && name.equals("MAIN TREE");
    }

    public boolean isEmpty() {
        return getBranches().isEmpty();
    }

    public boolean isWrote() {
        return sqlID != null;
    }

    public String toDatabaseString() throws SQLException {
        StringBuilder builder = new StringBuilder();

        for (Branch branch : getBranches()) {
            if (branch.sqlID != null) {
                if (branch instanceof TreeBranch) {
                    builder.append("b");
                } else {
                    builder.append("e");
                }
                builder.append(branch.sqlID).append(";");
            } else {
                throw new SQLException("SQL ID for branch " + branch + " is null!");
            }
        }

        return builder.toString();
    }

    private String indexTemp;
    private static final Pattern pattern = Pattern.compile("((^\\.)|(// null$))");

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        if (isMain()) {
            indexTemp = "";
            iterate(builder, this, false);

            return builder.toString();
        } else {
            return super.toString();
        }
    }

    public boolean setTookID(int oldID, int newID) throws SQLException {
        boolean took = false;
        for (Branch branch : getBranches()) {
            if (took) {
                return true;
            }

            if (branch instanceof TreeBranch treeBranch) {
                took = took || treeBranch.setTookID(oldID, newID);
            } else if (branch instanceof BranchEndpoint branchEndpoint) {
                if (branchEndpoint.getTookID() == oldID) {
                    branchEndpoint.setTookID(newID);

                    took = true;
                    return took;
                }
            }
        }

        return took;
    }

    public boolean hasTookAny(int id) {
        boolean took = false;
        for (Branch branch : getBranches()) {
            if (took) {
                return true;
            }

            if (branch instanceof TreeBranch treeBranch) {
                took = took || treeBranch.hasTookAny(id);
            } else if (branch instanceof BranchEndpoint branchEndpoint) {
                if (branchEndpoint.getTookID() == id) {
                    took = true;
                    return took;
                }
            }
        }

        return took;
    }

    public String toExtendedString() {
        StringBuilder builder = new StringBuilder();

        if (isMain()) {
            indexTemp = "";
            iterate(builder, this, true);

            return builder.toString();
        } else {
            return super.toString();
        }
    }

    private void iterate(StringBuilder builder, TreeBranch masterBranch, boolean extended) {
        for (Branch branch : masterBranch.getBranches()) {
            if (branch instanceof BranchEndpoint) {
               appendName(builder, branch);
            } else {
                appendName(builder, branch);
                String oldIndex = indexTemp;
                if (!indexTemp.isEmpty()) {
                    indexTemp += "." + branch.getIndex();
                } else {
                    indexTemp += branch.getIndex();
                }
                iterate(builder, (TreeBranch) branch, extended);

                indexTemp = oldIndex;
            }
        }
    }

    private void appendName(StringBuilder builder, Branch branch) {
        if (branch instanceof BranchEndpoint endpoint) {
            if (!indexTemp.isBlank()) {
                builder.append(indexTemp).append(".").append(branch.index).append(" - ").append(branch.name).append(getDescription(branch));
            } else {
                builder.append(branch.index).append(" - ").append(branch.name).append(getDescription(branch));
            }
            if (endpoint.getTookID() != 0) {
                try {
                    User user = VKStaticMethods.getUserFromID(endpoint.getTookID());
                    builder.append(" (Взял ").append(user.getFirstName()).append(" ").append(user.getLastName()).append(")");
                } catch (Exception ignore) {}
            }
            builder.append("\n");
        } else {
            if (!indexTemp.isBlank()) {
                builder.append(" - ").append(indexTemp).append(".").append(branch.index).append(" - ").append(branch.name).append(getDescription(branch)).append("\n");
            } else {
                builder.append(" - ").append(branch.index).append(" - ").append(branch.name).append(getDescription(branch)).append("\n");
            }
        }
    }

    private String getDescription(Branch branch) {
        if (branch.getDescription() == null) {
            return "";
        } else {
            return " // " + branch.getDescription();
        }
    }
}
