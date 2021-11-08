package ru.dsci.qrvisor.bot.commands;

import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.bots.AbsSender;

public class CommandStart extends Command {

    @Override
    public void processMessage(AbsSender absSender, Message message, String[] strings) {
        message.setText("Добро пожаловать! \n"
                + "Вас приветствует бот @QRVisor, у меня простая цель - верификация сертификатов вакцинации. \n"
                + "Внимание! Все QR-коды проверяются на подделку. \n"
                + "Для проверки сфотографируйте код и отправьте в чат. \n"
                + "Начнём?"
        );
        super.processMessage(absSender, message, strings);
    }

    public CommandStart() {
        super("start", "Запуск бота");
    }
}
