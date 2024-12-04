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
import ru.checkdev.notification.telegram.service.TgAuthCallWebClint;

import java.util.Calendar;
import java.util.HashMap;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UnBindActionTest {
    @InjectMocks
    private UnBindAction unbindAction;
    @Mock
    private TgAuthCallWebClint tgAuthCallWebClient;
    @Mock
    private Message messageMock;

    @Test
    public void whenRequestToUnbindThenReturnServiceUnavailableMessage() {
        var sl = System.lineSeparator();
        String expected = "Сервис не доступен попробуйте позже" + sl
                + "/bind";
        when(tgAuthCallWebClient.doGet(anyString(), anyString(), anyString())).thenThrow(new RuntimeException("Service unavailable"));
        BotApiMethod<Message> rsl = unbindAction.handle(messageMock);
        assertThat(rsl.toString()).contains(expected);
    }

    @Test
    public void whenRequestToUnbindThenReturnUserNotBoundMessage() {
        String expected = "Текущий аккаунт ещё не привязан к сервису нотификации";
        when(tgAuthCallWebClient.doGet(anyString(), anyString(), anyString())).thenReturn(Mono.empty());
        BotApiMethod<Message> rsl = unbindAction.handle(messageMock);
        assertThat(rsl.toString()).contains(expected);
    }

    @Test
    public void whenRequestToUnbindThenReturnUserUnboundSucceedMessage() {
        PersonDTO person = new PersonDTO("username", "email@email.com", "password", true, null, Calendar.getInstance(), null);
        String expected = "Аккаунт был успешно отвязан от сервиса нотификации.";
        when(tgAuthCallWebClient.doGet(anyString(), anyString(), anyString())).thenReturn(Mono.just(person));
        when(tgAuthCallWebClient.doPost(anyString(), any(PersonDTO.class))).thenReturn(Mono.just(new HashMap<String, Object>() {{
            put("message", expected);
        }}));
        BotApiMethod<Message> rsl = unbindAction.callback(messageMock);
        assertThat(rsl.toString()).contains(expected);
    }

    @Test
    public void whenRequestToUnbindThenReturnUserUnboundErrorMessage() {
        PersonDTO person = new PersonDTO("username", "email@email.com", "password", true, null, Calendar.getInstance(), null);
        String expected = "Ошибка: аккаунт не был отвязан. Повторите попытку позднее.";
        when(tgAuthCallWebClient.doGet(anyString(), anyString(), anyString())).thenReturn(Mono.just(person));
        when(tgAuthCallWebClient.doPost(anyString(), any(PersonDTO.class))).thenReturn(Mono.just(new HashMap<String, Object>() {{
            put("error", expected);
        }}));
        BotApiMethod<Message> rsl = unbindAction.callback(messageMock);
        assertThat(rsl.toString()).contains(expected);
    }
}