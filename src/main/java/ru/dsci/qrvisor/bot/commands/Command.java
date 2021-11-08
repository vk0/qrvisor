package ru.dsci.qrvisor.bot.commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Arrays;

public class Command implements IBotCommand {

    protected final Logger logger = LoggerFactory.getLogger(BotCommand.class);

    private final String commandIdentifier;
    private String description;

    public Command(String commandIdentifier, String description) {
        this.commandIdentifier = commandIdentifier;
        this.description = description;
    }

    @Override
    public String getCommandIdentifier() {
        return commandIdentifier;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void processMessage(AbsSender absSender, Message message, String[] strings) {
        logger.debug(String.format(String.format("COMMAND: %s(%s)", message.getText(), Arrays.toString(strings))));
        try {
            SendMessage sendMessage = SendMessage
                    .builder()
                    .chatId(message.getChatId().toString())
                    .text(message.getText()).build();
            absSender.execute(sendMessage);
        } catch (TelegramApiException e) {
            logger.error(String.format("{sendMessage}: %s", e.getMessage(), e));
        }
    }
}
