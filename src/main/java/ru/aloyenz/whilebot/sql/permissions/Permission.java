package ru.aloyenz.whilebot.sql.permissions;

import com.vk.api.sdk.objects.callback.MessageNew;
import com.vk.api.sdk.objects.messages.Message;
import org.jetbrains.annotations.Nullable;
import ru.aloyenz.whilebot.Main;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class Permission {

    public static Permission forID(long userID) throws SQLException {
        return new Permission(userID);
    }

    public static Permission forMessageAuthor(Message message) throws SQLException {
        return forID(message.getFromId());
    }

    public static Permission forMessageNewAuthor(MessageNew messageNew) throws SQLException {
        return forMessageAuthor(messageNew.getObject().getMessage());
    }

    private int permLevel = 0;
    private final long userID;

    public Permission(long userID) throws SQLException {
        Connection connection = Main.getSQLManager().getPool().getConnection();

        PreparedStatement ps = connection.prepareStatement("SELECT permission_level FROM users WHERE user_id = ?;");
        ps.setLong(1, userID);

        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            this.permLevel = rs.getInt(1);
        }

        ps.close();
        connection.close();

        this.userID = userID;
    }

    public boolean hasPermission(PermissionType type) {
        int needPerm = 1 << type.getPos();

        // 001101
        // 001000 <- Need
        // 001000 <- OUT
        // If equals - true
        if ((needPerm & permLevel) == needPerm) {
            return true;
        }

        if (!type.equals(PermissionType.DEVELOPER)) { // Security check
            return hasAdminAccess();
        } else {
            return false;
        }
    }

    public boolean hasPermissions(@Nullable List<PermissionType> types) {
        boolean out = true;

        if (types != null) {
            for (PermissionType type : types) {
                out = out && hasPermission(type);
            }
        }

        return out;
    }


    public boolean hasAdminAccess() {
        int developerPerm = 1 << 31;     // Developer permissions
        int administratorPerm = 1 << 30; // Administrator permission

        return ((developerPerm & permLevel) == developerPerm) || ((administratorPerm & permLevel) == administratorPerm);
    }

    public void addPermissionNoUpdate(PermissionType type) {
        int needPerm = 1 << type.getPos();

        this.permLevel = needPerm | permLevel;
    }

    public void removePermissionNoUpdate(PermissionType type) {
        int needPerm = ~(1 << type.getPos());

        this.permLevel = permLevel & needPerm;
    }

    public void addPermissionNoUpdate(List<PermissionType> types) {
        for (PermissionType type : types) {
            addPermissionNoUpdate(type);
        }
    }

    public void removePermissionNoUpdate(List<PermissionType> types) {
        for (PermissionType type : types) {
            removePermissionNoUpdate(type);
        }
    }

    public void setPermissionNoUpdate(int permLevel) {
        this.permLevel = permLevel;
    }

    public void setPermission(int permLevel) throws SQLException {
        this.permLevel = permLevel;
        updatePermissionInDatabase();
    }

    public void addPermission(PermissionType type) throws SQLException {
        addPermissionNoUpdate(type);
        updatePermissionInDatabase();
    }

    public void removePermission(PermissionType type) throws SQLException {
        removePermissionNoUpdate(type);
        updatePermissionInDatabase();
    }

    public void addPermission(List<PermissionType> types) throws SQLException {
        addPermissionNoUpdate(types);
        updatePermissionInDatabase();
    }

    public void removePermission(List<PermissionType> types) throws SQLException {
        removePermissionNoUpdate(types);
        updatePermissionInDatabase();
    }

    public int getPermLevel() {
        return permLevel;
    }

    public void updatePermissionInDatabase() throws SQLException {
        Connection connection = Main.getSQLManager().getPool().getConnection();

        PreparedStatement ps = connection.prepareStatement("UPDATE users SET permission_level = ? WHERE user_id = ?;");
        ps.setInt(1, permLevel);
        ps.setLong(2, userID);

        ps.executeUpdate();

        ps.close();
        connection.close();
    }
}
