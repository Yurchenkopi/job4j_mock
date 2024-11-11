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
 * 3. Мидл
 * Класс реализует пункт меню привязки аккаунта к сервису checkDev
 * или другими словами - авторизацию в сервисе CheckDev через пару email/password,
 * полученную через регистрацию средствами телеграмм бота
 *
 * @author Pavel Yurchenko, user Pavel
 * @since 03.11.2024
 */

@AllArgsConstructor
@Slf4j
public class UnBindAction implements Action {
    private static final String ERROR_OBJECT = "error";
    private static final String MESSAGE_OBJECT= "message";
    private static final String URL_AUTH_CHECK = "/check";
    private static final String URL_AUTH_SIGN_IN = "/signIn";
    private static final String URL_AUTH_UNBIND = "/unbind";
    private static final String URL_AUTH_GET_BY_EMAIL = "/person/email";
    private final TgConfig tgConfig = new TgConfig("tg/", 8);
    private final TgAuthCallWebClint authCallWebClint;
    private final String urlSiteAuth;

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
            text = "Аккаунт отвязан";
            return new SendMessage(chatId, text);
        } else {
            return new SendMessage(chatId, mapObj.get(ERROR_OBJECT));
        }
    }

    /**
     * Метод формирует ответ пользователю.
     * Весь метод разбит на 4 этапа проверки.
     * 1. Проверка на соответствие формату Email введенного текста.
     * 2. Отправка данных в сервис Auth и если сервис не доступен сообщаем
     * 3. Если сервис доступен, получаем от него ответ и обрабатываем его.
     * 3.1 ответ при ошибке аутентификации
     * 3.2 ответ при успешной аутентификации.
     *
     * @param message Message
     * @return BotApiMethod<Message>
     */
    @Override
    public BotApiMethod<Message> callback(Message message) {
        return handle(message);
    }
}
