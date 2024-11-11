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
public class BindAction implements Action {
    private static final String ERROR_OBJECT = "error";
    private static final String MESSAGE_OBJECT= "message";
    private static final String URL_AUTH_SIGN_IN = "/signIn";
    private static final String URL_AUTH_BIND = "/bind";
    private static final String URL_AUTH_GET_BY_EMAIL = "/person/email";
    private final TgConfig tgConfig = new TgConfig("tg/", 8);
    private final TgAuthCallWebClint authCallWebClint;
    private final String urlSiteAuth;

    @Override
    public BotApiMethod<Message> handle(Message message) {
        var chatId = message.getChatId().toString();
        var text = "Введите email и password в формате EMAIL PASSWORD :";
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
        var chatId = message.getChatId().toString();
        var txtMsg = message.getText().split(" ");
        var email = txtMsg[0];
        var password = txtMsg[1];
        var text = "";
        var sl = System.lineSeparator();

        if (!tgConfig.isEmail(email)) {
            text = "Email: " + email + " не корректный." + sl
                    + "попробуйте снова." + sl
                    + "/bind";
            return new SendMessage(chatId, text);
        }
        var person = new PersonDTO(null, email, password, true, null,
                Calendar.getInstance(), null);
        Object result;
        try {
            result = authCallWebClint.doPost(URL_AUTH_SIGN_IN, person).block();
        } catch (Exception e) {
            log.error("WebClient token error: {}", e.getMessage());
            text = "Сервис не доступен попробуйте позже" + sl
                    + "/bind";
            return new SendMessage(chatId, text);
        }

        var mapObject = tgConfig.getObjectToMap(result);

        log.info(mapObject.toString());

        if (mapObject.containsKey(ERROR_OBJECT)) {
            text = "Ошибка аутентификации: " + mapObject.get(ERROR_OBJECT);
            return new SendMessage(chatId, text);
        }

        String token = mapObject.get("token");

        PersonDTO currentPersonDTO;

        try {
            currentPersonDTO = authCallWebClint.doGetWithToken(URL_AUTH_GET_BY_EMAIL, token, email).block();
        } catch (Exception e) {
            log.error("WebClient /check error: {}", e.getMessage());
            text = "Сервис не доступен попробуйте позже" + sl
                    + "/bind";
            return new SendMessage(chatId, text);
        }

        var registeredChatId = currentPersonDTO.getChatId();

        if (registeredChatId == null) {
            person.setChatId(chatId);
            var rsl = authCallWebClint.doPost(URL_AUTH_BIND, token, person).block();
            var mapObj = tgConfig.getObjectToMap(rsl);
            if (mapObj.containsKey(MESSAGE_OBJECT)) {
                text = mapObj.get(MESSAGE_OBJECT) + sl
                        + "Логин: " + email + sl
                        + "Пароль: " + password + sl
                        + urlSiteAuth;
                return new SendMessage(chatId, text);
            } else {
                return new SendMessage(chatId, mapObj.get(ERROR_OBJECT));
            }
        }

        if (registeredChatId.equals(chatId)) {
            text = "Аккаунт уже привязан";
        } else {
            text = "К введенным учетным данным от сервиса нотификации уже привязан другой аккаунт." + sl
                    + "Для привязки текущего аккаунта выполните процедуру отвязки через старое устройство.";
        }

        return new SendMessage(chatId, text);
    }
}
