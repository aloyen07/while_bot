package ru.aloyenz.whilebot.exceptions;

import com.vk.api.sdk.objects.messages.Message;

public class MessagedException extends RuntimeException {

    private final Message message;

    public MessagedException(Message message) {
        super(message.toString());
        this.message = message;
    }

    public Message getVKMessage() {
        return message;
    }
}
