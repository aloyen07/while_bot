package ru.aloyenz.whilebot.bot;

import com.vk.api.sdk.client.TransportClient;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.GroupActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.httpclient.HttpTransportClient;
import com.vk.api.sdk.objects.GroupAuthResponse;
import com.vk.api.sdk.objects.messages.LongpollMessages;
import com.vk.api.sdk.objects.messages.Message;
import org.apache.logging.log4j.Logger;
import ru.aloyenz.whilebot.Main;
import ru.aloyenz.whilebot.Settings;

public class VKClient {

    private final Logger logger;

    private final VkApiClient client;

    public VKClient(Logger logger, Logger messageHandlerLogger) {
        this.logger = logger;

        logger.info("Creating new instance for VK client and authenticating.");

        TransportClient transportClient = new HttpTransportClient();
        VkApiClient client = new VkApiClient(transportClient);

        Settings settings = Main.getSettings();

        GroupActor actor = new GroupActor(settings.getBotGroupID(), settings.getBotAPIKey());

        this.client = client;
        logger.info("Client and actor created. Initializing message handler");
        VKMessageHandler handler = new VKMessageHandler(messageHandlerLogger, client, actor);
        handler.run();
    }
}
