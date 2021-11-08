package ru.dsci.qrvisor.bot;

import ru.dsci.qrvisor.bot.commands.CommandHelp;
import ru.dsci.qrvisor.bot.commands.CommandStart;
import ru.dsci.qrvisor.core.CertTools;
import ru.dsci.qrvisor.core.URLTools;
import ru.dsci.qrvisor.core.dtos.CertDto;
import ru.dsci.qrvisor.core.exceptions.UserException;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.telegram.telegrambots.extensions.bots.commandbot.TelegramLongPollingCommandBot;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class BotProcessor extends TelegramLongPollingCommandBot {

    private final static BotSettings botSettings = BotSettings.getInstance();
    private static BotProcessor instance;
    private final TelegramBotsApi telegramBotsApi;
    private List<String> registeredCommands = new ArrayList<>();

    public void sendMessage(Long chatId, String message) {
        try {
            SendMessage sendMessage = SendMessage
                    .builder()
                    .chatId(chatId.toString())
                    .text(message).build();
            execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error(String.format("{sendMessage}: %s", e.getMessage()));
        }
    }

    private MessageType getMessageType(Update update) throws UserException {
        MessageType messageType = null;
        try {
            if (update.getMessage().getPhoto() != null)
                messageType = MessageType.PHOTO;
            else if (update.getMessage().getText() != null)
                messageType = (update.getMessage().getText().matches("^/[\\w]*$")) ?
                        MessageType.COMMAND :
                        MessageType.TEXT;
            if (messageType == null)
                throw new IllegalArgumentException(update.toString());
            return messageType;
        } catch (RuntimeException e) {
            log.error(String.format("{getMessageType}: %s", e.getMessage()));
            throw new UserException("Неподдерживаемый тип сообщения");
        }
    }

    private void processText(Update update) throws TelegramApiException {
        getRegisteredCommand("help").processMessage(this, update.getMessage(), null);
    }

    private String getFileUrl(String fileId) throws IOException {
        String fileUrl = String.format("https://api.telegram.org/bot%s/getFile?file_id=%s",
                botSettings.getToken(),
                fileId);
        JSONObject jsonObject = URLTools.readJsonFromUrl(fileUrl);
        fileUrl = String.format("https://api.telegram.org/file/bot%s/%s",
                botSettings.getToken(),
                jsonObject.get("file_path"));
        return fileUrl;
    }

    private void processPhoto(Update update) throws TelegramApiException, IOException, UserException {
        CertDto certDto;
        List<PhotoSize> photoSizes = update.getMessage().getPhoto();
        String fileUrl = getFileUrl(update.getMessage().getPhoto().get(photoSizes.size() - 1).getFileId());
        certDto = CertTools.getCertData(fileUrl);
        sendMessage(update.getMessage().getChatId(), certDto.toString());
    }

    @Override
    public String getBotUsername() {
        return botSettings.getUserName();
    }

    @Override
    protected void processInvalidCommandUpdate(Update update) {
        String command = update.getMessage().getText().substring(1);
        sendMessage(
                update.getMessage().getChatId()
                , String.format("Некорректная команда [%s], доступные команды: %s"
                        , command
                        , registeredCommands.toString()));
    }

    @Override
    public void processNonCommandUpdate(Update update) {
        if (update.hasMessage()) {
            try {
                MessageType messageType = getMessageType(update);
                switch (messageType) {
                    case COMMAND:
                        processInvalidCommandUpdate(update);
                        break;
                    case PHOTO:
                        processPhoto(update);
                        break;
                    case TEXT:
                        processText(update);
                        break;
                }
            } catch (UserException e) {
                sendMessage(update.getMessage().getChatId(), e.getMessage());
            } catch (TelegramApiException | RuntimeException | IOException e) {
                log.error(String.format("Received message processing error: %s", e.getMessage()));
                sendMessage(update.getMessage().getChatId(), "Ошибка обработки сообщения");
            }
        }
    }

    @Override
    public String getBotToken() {
        return botSettings.getToken();
    }

    @Override
    public void onRegister() {
        super.onRegister();
    }

    private void setRegisteredCommands() {
        registeredCommands = getRegisteredCommands()
                .stream()
                .map(IBotCommand::getCommandIdentifier)
                .collect(Collectors.toList());
    }

    public void registerBot() {
        try {
            telegramBotsApi.registerBot(this);
        } catch (TelegramApiException e) {
            throw new RuntimeException("Telegram API initialization error: " + e.getMessage());
        }
    }

    {
        try {
            telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
            registerBot();
            register(new CommandStart());
            register(new CommandHelp());
            setRegisteredCommands();
        } catch (TelegramApiException e) {
            throw new RuntimeException("Telegram Bot initialization error: " + e.getMessage());
        }
    }

    public static BotProcessor getInstance() {
        if (instance == null)
            instance = new BotProcessor();
        return instance;
    }

    public BotProcessor() {
        super();
    }
}