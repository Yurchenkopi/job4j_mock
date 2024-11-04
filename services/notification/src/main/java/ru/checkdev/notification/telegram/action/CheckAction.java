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
public class CheckAction implements Action {
    private static final String ERROR_OBJECT = "error";
    private static final String URL_AUTH_CHECK = "/check";
    private final TgConfig tgConfig = new TgConfig("tg/", 8);
    private final TgAuthCallWebClint authCallWebClint;
    private final String urlSiteAuth;

    private final String authToken;

    @Override
    public BotApiMethod<Message> handle(Message message) {
        var chatId = message.getChatId().toString();
        var text = "";
        var sl = System.lineSeparator();

        PersonDTO result;
        try {
            result = authCallWebClint.doGet(URL_AUTH_CHECK, authToken).block();
        } catch (Exception e) {
            log.error("WebClient /check error: {}", e.getMessage());
            text = "Сервис не доступен попробуйте позже" + sl
                    + "/check";
            return new SendMessage(chatId, text);
        }

        log.info(result.toString());

        var mapObject = tgConfig.getObjectToMap(result);

        if (mapObject.containsKey(ERROR_OBJECT)) {
            text = "Ошибка проверки пользователя: " + mapObject.get(ERROR_OBJECT);
            return new SendMessage(chatId, text);
        }

        text = "Привязанный аккаунт: " + sl
                + "Логин: " + result.getEmail() + sl
                + "Пароль: " + result.getPassword() + sl
                + urlSiteAuth;
        return new SendMessage(chatId, text);
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
