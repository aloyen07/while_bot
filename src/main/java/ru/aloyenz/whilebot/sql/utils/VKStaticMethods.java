package ru.aloyenz.whilebot.sql.utils;

import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.objects.users.User;
import ru.aloyenz.whilebot.Main;

public class VKStaticMethods {

    public static User getUserFromID(int id) throws ClientException, ApiException {
        return Main.getVKClient().getUserFromID(id);
    }
}
