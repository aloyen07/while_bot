import org.junit.jupiter.api.Test;
import ru.aloyenz.whilebot.Main;
import ru.aloyenz.whilebot.sql.permissions.Permission;
import ru.aloyenz.whilebot.sql.permissions.PermissionType;

import java.io.IOException;
import java.sql.SQLException;

public class IntTest {
    @Test
    public void testOR() {
        int x = 1 << 1;
        int y = 4;

        System.out.println(y | x);
    }

    @Test
    public void testPermission() throws SQLException, IOException {
        Main.main(null);

        Permission permission = new Permission(545859801);

        int i = permission.getPermLevel();
        int x = 1 << PermissionType.USE.getPos();
        int z = x | i;
        System.out.println(i);
        System.out.println(x);
        System.out.println(z);
    }
}
