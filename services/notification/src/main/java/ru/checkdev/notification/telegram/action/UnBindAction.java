package ru.checkdev.notification.telegram.action;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.checkdev.notification.domain.PersonDTO;
import ru.checkdev.notification.telegram.config.TgConfig;
import ru.checkdev.notification.telegram.service.TgAuthCallWebClint;

/**
 * Класс реализует пункт меню отвязки аккаунта телеграмм от сервиса checkDev
 *
 * @author Pavel Yurchenko, user Pavel
 * @since 25.11.2024
 */

@AllArgsConstructor
@Slf4j
public class UnBindAction implements Action {
    private static final String ERROR_OBJECT = "error";
    private static final String MESSAGE_OBJECT= "message";
    private static final String URL_AUTH_CHECK = "/check";
    private static final String URL_AUTH_UNBIND = "/unbind";
    private final TgConfig tgConfig = new TgConfig("tg/", 8);
    private final TgAuthCallWebClint authCallWebClint;

    @Override
    public BotApiMethod<Message> handle(Message message) {
        var chatId = message.getChatId().toString();
        var text = "";
        var sl = System.lineSeparator();
        PersonDTO personDTO;
        try {
            personDTO = authCallWebClint.doGet(URL_AUTH_CHECK, "chatId", chatId).block();
        } catch (Exception e) {
            log.error("WebClient /check error: {}", e.getMessage());
            text = "Сервис не доступен попробуйте позже" + sl
                    + "/bind";
            return new SendMessage(chatId, text);
        }

        if (personDTO == null) {
            return new SendMessage(chatId, "Текущий аккаунт ещё не привязан к сервису нотификации");
        }

        personDTO.setChatId(null);
        var rsl = authCallWebClint.doPost(URL_AUTH_UNBIND, personDTO).block();
        var mapObj = tgConfig.getObjectToMap(rsl);
        if (mapObj.containsKey(MESSAGE_OBJECT)) {
            return new SendMessage(chatId, mapObj.get(MESSAGE_OBJECT));
        } else {
            return new SendMessage(chatId, mapObj.get(ERROR_OBJECT));
        }
    }

    @Override
    public BotApiMethod<Message> callback(Message message) {
        return handle(message);
    }
}
