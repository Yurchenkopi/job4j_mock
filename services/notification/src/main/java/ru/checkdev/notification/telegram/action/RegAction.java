package ru.checkdev.notification.telegram.action;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.checkdev.notification.domain.PersonDTO;
import ru.checkdev.notification.service.SubscribeTelegramService;
import ru.checkdev.notification.telegram.config.TgConfig;
import ru.checkdev.notification.telegram.service.TgAuthCallWebClint;

import java.util.Calendar;

/**
 * 3. Мидл
 * Класс реализует пункт меню регистрации нового пользователя в телеграм бот
 *
 * @author Dmitry Stepanov, user Dmitry
 * @since 12.09.2023
 */
@AllArgsConstructor
@Slf4j
public class RegAction implements Action {
    private static final String ERROR_OBJECT = "error";
    private static final String URL_AUTH_REGISTRATION = "/registration";
    private static final String URL_AUTH_CHECK_USER = "/profiles";
    private final TgConfig tgConfig = new TgConfig("tg/", 8);
    private final TgAuthCallWebClint authCallWebClint;
    private final SubscribeTelegramService subscribeTelegramService;
    private final String urlSiteAuth;

    @Override
    public BotApiMethod<Message> handle(Message message) {
        var chatId = message.getChatId().toString();
        var text = "Введите имя пользователя и email для регистрации в формате USERNAME EMAIL :";
        return new SendMessage(chatId, text);
    }

    /**
     * Метод формирует ответ пользователю.
     * Весь метод разбит на 4 этапа проверки.
     * 1. Проверка на соответствие формату Email введенного текста.
     * 2. Отправка данных в сервис Auth и если сервис не доступен сообщаем
     * 3. Если сервис доступен, получаем от него ответ и обрабатываем его.
     * 3.1 ответ при ошибке регистрации
     * 3.2 ответ при успешной регистрации.
     *
     * @param message Message
     * @return BotApiMethod<Message>
     */
    @Override
    public BotApiMethod<Message> callback(Message message) {
        var chatId = message.getChatId().toString();
        var txtMsg = message.getText().split(" ");
        var userName = txtMsg[0];
        var email = txtMsg[1];
        var text = "";
        var sl = System.lineSeparator();

        if (!tgConfig.isEmail(email)) {
            text = "Email: " + email + " не корректный." + sl
                   + "попробуйте снова." + sl
                   + "/new";
            return new SendMessage(chatId, text);
        }

        PersonDTO personDto;
        var subscribeTg = subscribeTelegramService.findByChatId(Long.parseLong(chatId));
        if (subscribeTg.isPresent()) {
            int userId = subscribeTg.get().getUserId();
            try {
                personDto = authCallWebClint.doGet(String.format("%s/%d", URL_AUTH_CHECK_USER, userId)).block();
            } catch (Exception e) {
                log.error("WebClient /check error: {}", e.getMessage());
                text = "Сервис не доступен попробуйте позже" + sl
                        + "/bind";
                return new SendMessage(chatId, text);
            }
            if (personDto != null) {
                return new SendMessage(chatId, "Текущий аккаунт уже имеет привязку к сервису нотификации. Попробуйте восстановить пароль от текущей учетной записи /forget или отвяжите аккаунт от учетной записи и создайте новую /unbind");
            }
        }

        var password = tgConfig.getPassword();
        var person = new PersonDTO(0, userName, email, password, true, null,
                Calendar.getInstance());
        Object result;
        try {
            result = authCallWebClint.doPost(URL_AUTH_REGISTRATION, person).block();
        } catch (Exception e) {
            log.error("WebClient doPost error: {}", e.getMessage());
            text = "Сервис не доступен попробуйте позже" + sl
                   + "/start";
            return new SendMessage(chatId, text);
        }

        var mapObject = tgConfig.getObjectToMap(result);

        if (mapObject.containsKey(ERROR_OBJECT)) {
            text = "Ошибка регистрации: " + mapObject.get(ERROR_OBJECT);
            return new SendMessage(chatId, text);
        }

        text = "Вы зарегистрированы: " + sl
                + "userName: " + userName + sl
                + "Логин: " + email + sl
                + "Пароль: " + password + sl
                + urlSiteAuth;
        return new SendMessage(chatId, text);
    }
}
