package ru.aloyenz.whilebot.commands;

import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.GroupActor;
import com.vk.api.sdk.objects.callback.MessageNew;

public record CommandContext(int groupID, MessageNew message, VkApiClient client, GroupActor actor) {
}
