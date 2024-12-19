package ru.aloyenz.whilebot.services.safety;

import com.vk.api.sdk.objects.callback.MessageNew;
import com.vk.api.sdk.objects.messages.Message;

public class UserFingerprint {

    private final String fingerprint;

    public UserFingerprint(Message message) {
        fingerprint = String.valueOf(message.getFromId());
    }

    public UserFingerprint(MessageNew messageNew) {
        this(messageNew.getObject().getMessage());
    }

    @Override
    public String toString() {
        return fingerprint;
    }
}
