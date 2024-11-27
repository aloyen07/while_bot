package ru.aloyenz.whilebot.commands;

import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.objects.messages.Message;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.List;

public interface CommandExecutor {

    @Nullable
    Message execute(CommandContext context, List<String> args) throws SQLException, ApiException, ClientException;
}
