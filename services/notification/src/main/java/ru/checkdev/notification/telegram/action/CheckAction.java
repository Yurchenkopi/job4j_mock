package ru.checkdev.notification.telegram.action;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.checkdev.notification.domain.PersonDTO;
import ru.checkdev.notification.telegram.config.TgConfig;
import ru.checkdev.notification.telegram.service.TgAuthCallWebClint;

import java.util.Calendar;

/**
 * Класс реализует пункт меню проверки привязанного аккаунта телеграмм к сервису checkDev
 *
 * @author Pavel Yurchenko, user Pavel
 * @since 25.11.2024
 */

@AllArgsConstructor
@Slf4j
public class CheckAction implements Action {
    private static final String URL_AUTH_CHECK = "/check";
    private final TgAuthCallWebClint authCallWebClint;

    @Override
    public BotApiMethod<Message> handle(Message message) {
        var chatId = message.getChatId().toString();
        var text = "";
        var sl = System.lineSeparator();

        PersonDTO result;
        try {
            result = authCallWebClint.doGet(URL_AUTH_CHECK, "chatId", chatId).block();
        } catch (Exception e) {
            log.error("WebClient /check error: {}", e.getMessage());
            text = "Сервис не доступен попробуйте позже" + sl
                    + "/bind";
            return new SendMessage(chatId, text);
        }
        if (result == null) {
            return new SendMessage(chatId, "Текущий аккаунт ещё не привязан к сервису нотификации");
        }

        text = "Привязанный аккаунт: " + sl
                + "Логин: " + result.getUsername() + sl
                + "Email: " + result.getEmail();
        return new SendMessage(chatId, text);
    }
    @Override
    public BotApiMethod<Message> callback(Message message) {
        return handle(message);
    }
}
