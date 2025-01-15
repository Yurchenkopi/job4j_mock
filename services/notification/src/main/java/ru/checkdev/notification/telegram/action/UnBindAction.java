package ru.checkdev.notification.telegram.action;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.checkdev.notification.service.SubscribeTelegramService;

/**
 * Класс реализует пункт меню отвязки аккаунта телеграмм от сервиса checkDev
 *
 * @author Pavel Yurchenko, user Pavel
 * @since 25.11.2024
 */

@AllArgsConstructor
@Slf4j
public class UnBindAction implements Action {
    private final SubscribeTelegramService subscribeTelegramService;

    @Override
    public BotApiMethod<Message> handle(Message message) {
        var chatId = message.getChatId().toString();
        var subscribeTg = subscribeTelegramService.findByChatId(Long.parseLong(chatId));
        if (subscribeTg.isEmpty()) {
            return new SendMessage(chatId, "Текущий аккаунт ещё не привязан к сервису нотификации");
        }
        subscribeTelegramService.delete(Long.parseLong(chatId));
        return new SendMessage(chatId, "Аккаунт был успешно отвязан от сервиса нотификации.");
    }

    @Override
    public BotApiMethod<Message> callback(Message message) {
        return handle(message);
    }
}
