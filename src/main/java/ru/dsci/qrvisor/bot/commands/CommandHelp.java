package ru.dsci.qrvisor.bot.commands;

import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.bots.AbsSender;

public class CommandHelp extends Command {

    @Override
    public void processMessage(AbsSender absSender, Message message, String[] strings) {
        message.setText("Для проверки QR-кода сертификата вакцинации сфотографируйте код и отправьте в чат");
        super.processMessage(absSender, message, strings);
    }

    public CommandHelp() {
        super("help", "Справка \\help \n");
    }

}
