package ru.aloyenz.whilebot.commands;

import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.objects.messages.Message;
import com.vk.api.sdk.queries.messages.MessagesSendQueryWithDeprecated;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import ru.aloyenz.whilebot.sql.permissions.Permission;

import java.sql.SQLException;
import java.util.*;

public class CommandManager {

    public static final String PREFIX = "+";

    private final HashMap<String, Command> commands;

    private final Random random = new Random();
    private final Logger logger;

    public CommandManager(Logger logger) {
        commands = new HashMap<>();
        this.logger = logger;
        logger.info("Command manager is initialized!");
    }

    public void registerCommand(String prefix, Command command) {
        commands.put(prefix, command);
        logger.info("Registered a new command {} with {} arguments", prefix, command.argsNum());
    }

    @Nullable
    private Message wrapWithUsageError(Command command, @Nullable Message message) {
        Message out;
        if (message == null) {
            if (!command.usage().isEmpty()) {
                out = new Message().setText("Неверное использование команды!\n" + command.usage());
            } else {
                return null;
            }
        } else {
            out = message;
        }

        return out.setRandomId(random.nextInt());
    }

    public void executeCommand(CommandContext context) throws ClientException, ApiException {
        String text = context.message().getObject().getMessage().getText();
        if (text.toLowerCase().startsWith(PREFIX)) {
            context.message().getObject().getMessage().setText(text.substring(1));
            text = text.substring(1);

            List<String> args = new ArrayList<>();
            Collections.addAll(args, text.split(" "));

            Command command = commands.get(args.getFirst().toLowerCase());
            if (command != null) {
                Message output;

                try {
                    // Creating a reply
                    Permission permission = Permission.forMessageNewAuthor(context.message());
                    if (permission.hasPermissions(command.needPermission())) {
                        if (command.argsNum() == args.size() - 1 || command.argsNum() < 0) {
                            logger.debug("User {} used command {}", context.message().getObject().getMessage().getFromId(),
                                    args.getFirst().toLowerCase());
                            args.removeFirst();
                            try {
                                output = wrapWithUsageError(command, command.executor().execute(context, args));
                            } catch (SQLException e) {
                                output = new Message().setText("При исполнении Вашей команды возникла внутренняя ошибка. Обратитесь к администратору.");
                                logger.throwing(Level.ERROR, e);
                            }
                        } else {
                            output = wrapWithUsageError(command, null);
                        }
                    } else {
                        output = new Message().setText("Вы не имеете прав на исполнение данной команды!");
                    }
                } catch (SQLException e) {
                    output = new Message().setText("При исполнении Вашей команды возникла внутренняя ошибка. Обратитесь к администратору.");
                    logger.throwing(Level.ERROR, e);
                }


                // Sending reply
                if (output != null) {
                    List<Integer> fwdInts = new ArrayList<>();
                    if (output.getFwdMessages() != null) {
                        output.getFwdMessages().forEach((msg) -> fwdInts.add(msg.getId()));
                    }

                    if (output.getRandomId() == null) {
                        output.setRandomId(random.nextInt());
                    }

                    MessagesSendQueryWithDeprecated query = context.client().messages()
                            .sendDeprecated(context.actor())
                            .randomId(output.getRandomId())
                            .message(output.getText())
                            .forwardMessages(fwdInts)
                            .peerId(context.message().getObject().getMessage().getPeerId());

                    if (output.getKeyboard() != null) {
                        query.keyboard(output.getKeyboard());
                    }

                    if (output.getReplyMessage() != null) {
                        query.replyTo(output.getReplyMessage().getId());
                    }

                    if (output.getAttachments() != null) {
                        output.getAttachments().forEach((att) -> query.attachment(att.toString()));
                    }

                    query.execute();
                }
            }

        }
    }
}
