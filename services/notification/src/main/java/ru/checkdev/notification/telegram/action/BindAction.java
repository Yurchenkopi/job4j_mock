package ru.checkdev.notification.telegram.action;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.checkdev.notification.domain.PersonDTO;
import ru.checkdev.notification.telegram.config.TgConfig;
import ru.checkdev.notification.telegram.service.TgAuthCallWebClint;

import java.util.Calendar;
import java.util.Map;

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
    private static final String URL_AUTH_SIGN_IN = "/signIn";
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
                Calendar.getInstance());
        Object result;
        try {
            result = authCallWebClint.doPost(URL_AUTH_SIGN_IN, person).block();
        } catch (Exception e) {
            log.error("WebClient token error: {}", e.getMessage());
            text = "Сервис не доступен попробуйте позже" + sl
                   + "/bind";
            return new SendMessage(chatId, text);
        }

        log.info(result.toString());

        var mapObject = tgConfig.getObjectToMap(result);

        if (mapObject.containsKey(ERROR_OBJECT)) {
            text = "Ошибка аутентификации: " + mapObject.get(ERROR_OBJECT);
            return new SendMessage(chatId, text);
        }

 //       try {
 //           System.out.println(mapObject);
 //           ObjectMapper mapper = new ObjectMapper();
 //           JsonNode jsonNode = mapper.readTree((String) result);
 //           ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
 //           var session = attributes.getRequest().getSession();
 //           session.setAttribute("token", jsonNode.get("access_token").asText());
 //       } catch (Exception e) {
 //           throw new RuntimeException("Error parsing JSON response", e);
 //       }

        text = "Аккаунт привязан: " + sl
               + "Логин: " + email + sl
               + "Пароль: " + password + sl
               + urlSiteAuth;
        return new SendMessage(chatId, text);
    }
}
