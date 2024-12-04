package ru.aloyenz.whilebot.sql.homework.schema;

import org.jetbrains.annotations.Nullable;

import javax.naming.OperationNotSupportedException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public abstract class Branch {

    protected Integer sqlID;
    protected final int index;
    protected final String name;
    protected final String description;

    private final List<Branch> branches = new ArrayList<>();

    public Branch(int index, String name, String description) {
        this.index = index;
        this.name = name;
        this.description = description;
    }

    public int getIndex() {
        return index;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public void addBranch(Branch branch) {
        branches.add(branch);
    }

    public List<Branch> getBranches() {
        return branches;
    }

    public abstract boolean supportsBranching();

    public abstract void createRecordInDatabase(Connection connection) throws SQLException;

    @Override
    public String toString() {
        return index + ":" + name + " - " + description + " (SQL " + sqlID + ")";
    }
}
