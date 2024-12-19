package ru.aloyenz.whilebot.bot;

import com.vk.api.sdk.client.GsonHolder;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.GroupActor;
import com.vk.api.sdk.events.CallbackEvent;
import com.vk.api.sdk.events.longpoll.GroupLongPollApi;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.objects.callback.*;
import com.vk.api.sdk.objects.callback.longpoll.responses.GetLongPollEventsResponse;
import com.vk.api.sdk.objects.callback.messages.CallbackMessage;
import com.vk.api.sdk.objects.groups.LongPollServer;
import com.vk.api.sdk.objects.groups.responses.GetLongPollServerResponse;
import com.vk.api.sdk.objects.messages.Message;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import ru.aloyenz.whilebot.Main;
import ru.aloyenz.whilebot.commands.CommandContext;
import ru.aloyenz.whilebot.exceptions.ConnectionClosedException;

import java.net.ConnectException;
import java.net.URI;
import java.util.Random;
import java.util.concurrent.Executors;


public class VKMessageHandler extends GroupLongPollApi {

    public static final int RECONNECT_TIME = 1000;
    public static final int MAX_RECONNECTS = 5;

    private final Logger logger;

    private final VkApiClient client;
    private final GroupActor actor;

    private final int waitTime;


    public VKMessageHandler(Logger logger, VkApiClient client, GroupActor actor){
        super(client, actor, Main.getSettings().getWaitTime());
        this.waitTime = Main.getSettings().getWaitTime();
        this.logger = logger;
        this.client = client;
        this.actor = actor;
        logger.info("Initialized new message handler.");
    }

    @Override
    public String parse(CallbackMessage message) {
        String objectToDeserialize = message.getObject().toString();
        if (message.getType() == null) {
            return null;
        }

        switch (message.getType()) {
            case MESSAGE_NEW:
            case MESSAGE_DENY:
            case MESSAGE_REPLY:
            case MESSAGE_EDIT:
            case MESSAGE_ALLOW: {
                objectToDeserialize = "{ \"object\": " + objectToDeserialize + "}";
            }
            default: {
                //ignored
            }
        }

        CallbackEvent event = GSON.fromJson(objectToDeserialize, message.getType().getType());

        return switch (message.getType()) {
            case CONFIRMATION -> {
                confirmation();
                yield "OK";
            }
            case MESSAGE_NEW -> {
                messageNew(message.getGroupId(), (MessageNew) event);
                yield "OK";
            }
            case MESSAGE_REPLY -> {
                messageReply(message.getGroupId(), (MessageReply) event);
                yield "OK";
            }
            case MESSAGE_EDIT -> {
                messageEdit(message.getGroupId(), (MessageEdit) event);
                yield "OK";
            }
            case MESSAGE_ALLOW -> {
                messageAllow(message.getGroupId(), (MessageAllow) event);
                yield "OK";
            }
            case MESSAGE_DENY -> {
                messageDeny(message.getGroupId(), (MessageDeny) event);
                yield "OK";
            }
            default -> null;
        };
    }

    @Override
    public void messageNew(Integer groupId, MessageNew message) {
        try {
            Main.getManager().executeCommand(new CommandContext(groupId, message, client, actor));
        } catch (ApiException | ClientException e) {
            throw new RuntimeException(e);
        }
    }

    private LongPollServer getLongPollServer(GroupActor actor) {
        try {

            GetLongPollServerResponse response = client
                    .groupsLongPoll()
                    .getLongPollServer(actor, actor.getGroupId())
                    .execute();

            return new LongPollServer()
                    .setKey(response.getKey())
                    .setTs(response.getTs())
                    .setServer(response.getServer());
        } catch (ApiException | ClientException e) {
            logger.throwing(Level.ERROR, e);
            return null;
        }
    }

    boolean isRunning;

    private void handleUpdates(LongPollServer lpServer) throws ConnectionClosedException, InterruptedException {
        int reconnects = 0;
        while (lpServer == null && reconnects <= MAX_RECONNECTS) {
            Thread.sleep(RECONNECT_TIME);
            reconnects += 1;
            logger.error("Getting LongPoll server was failed. Trying again... Attempt: {}", reconnects);
            lpServer = getLongPollServer(actor);
        }

        if (lpServer == null) {
            throw new RuntimeException("Could not connect to LongPollServer, sorry :c");
        } else {
            logger.info("Connection to LongPoll server established!");
        }

        isRunning = true;
        try {
            logger.info("LongPoll handler started to handle events");
            Main.handleInitEvent();
            GetLongPollEventsResponse eventsResponse;
            String timestamp = lpServer.getTs();
            while (isRunning) {
                eventsResponse = client.longPoll()
                        .getEvents(lpServer.getServer(), lpServer.getKey(), timestamp)
                        .waitTime(waitTime)
                        .execute();
                eventsResponse.getUpdates().forEach(e -> parse(new GsonHolder().getGson().fromJson(e, CallbackMessage.class)));
                timestamp = eventsResponse.getTs();
            }
            logger.info("LongPoll handler stopped to handle events");
        } catch (ApiException | ClientException e) {
            /*
            Actually instead of GetLongPollEventsResponse there might be returned error like:
            {"failed":1,"ts":30} or {"failed":2}, but it directly handled in execute() method.
            There are 2 ways: deserialize manually response from string OR do reconnection in each
            error case. There is second way - keep use typed object and reconnect when any error.
            */
            logger.error("Getting LongPoll events was failed", e);
            throw new ConnectionClosedException();
        }
        isRunning = false;
    }

    @Override
    protected void run(GroupActor actor) {
        Executors.newSingleThreadExecutor().execute(
                () -> {
                    try {
                        LongPollServer lpServer = getLongPollServer(actor);
                        handleUpdates(lpServer);
                    } catch (ConnectionClosedException e) {
                        run(actor);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
        );
    }

    @Override
    public void run() {
        run(actor);
    }
}
