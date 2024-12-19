import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.objects.users.User;
import org.junit.jupiter.api.Test;
import ru.aloyenz.whilebot.Main;
import ru.aloyenz.whilebot.sql.utils.VKStaticMethods;

import java.io.IOException;

public class StaticMethodTest {

    @Test
    public void test() throws InterruptedException, ClientException, ApiException {

        Thread thread = new Thread(() -> {
            try {
                Main.main(new String[0]);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        thread.start();

        while (!Main.isInitialized()) {
            // Waiting for init
            Thread.sleep(10);
        }

        User user = VKStaticMethods.getUserFromID(1);
        System.out.println(user);
        System.out.println(user.getFirstName() + " " + user.getLastName());
    }
}
