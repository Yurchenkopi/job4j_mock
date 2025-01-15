package ru.checkdev.notification.telegram.action;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;
import reactor.core.publisher.Mono;
import ru.checkdev.notification.domain.PersonDTO;
import ru.checkdev.notification.domain.SubscribeTelegram;
import ru.checkdev.notification.service.SubscribeTelegramService;
import ru.checkdev.notification.telegram.service.TgAuthCallWebClint;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UnBindActionTest {
    @InjectMocks
    private UnBindAction unbindAction;
    @Mock
    private TgAuthCallWebClint tgAuthCallWebClient;
    @Mock
    private SubscribeTelegramService subscribeTelegramService;
    @Mock
    private Message messageMock;

    @Test
    public void whenRequestToUnbindThenReturnUserNotBoundMessage() {
        String expected = "Текущий аккаунт ещё не привязан к сервису нотификации";
        BotApiMethod<Message> rsl = unbindAction.handle(messageMock);
        assertThat(rsl.toString()).contains(expected);
    }

    @Test
    public void whenRequestToUnbindThenReturnUserUnboundSucceedMessage() {
        PersonDTO person = new PersonDTO(0, "username", "email@email.com", "password", true, null, Calendar.getInstance());
        String expected = "Аккаунт был успешно отвязан от сервиса нотификации.";
        when(subscribeTelegramService.findByChatId(anyLong())).thenReturn(Optional.of(new SubscribeTelegram(5, 5L)));
        BotApiMethod<Message> rsl = unbindAction.callback(messageMock);
        assertThat(rsl.toString()).contains(expected);
    }
}