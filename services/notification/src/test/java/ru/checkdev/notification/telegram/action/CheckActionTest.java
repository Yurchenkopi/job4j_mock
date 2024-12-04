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
import java.util.Collections;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CheckActionTest {
    @InjectMocks
    private CheckAction checkAction;
    @Mock
    private TgAuthCallWebClint tgAuthCallWebClient;
    @Mock
    private Message messageMock;

    @Test
    public void whenUnbindingAccountThenResponseToBind() {
        when(tgAuthCallWebClient.doGet(anyString(), anyString(), anyString())).thenReturn(Mono.empty());
        String expected = "Текущий аккаунт ещё не привязан к сервису нотификации";
        BotApiMethod<Message> rsl = checkAction.handle(messageMock);
        assertThat(rsl.toString()).contains(expected);
    }

    @Test
    public void whenBindAccountThenResponseOk() {
        var created = new Calendar.Builder()
                .set(Calendar.DAY_OF_MONTH, 23)
                .set(Calendar.MONTH, Calendar.OCTOBER)
                .set(Calendar.YEAR, 2023)
                .build();
        var personDto = new PersonDTO("username", "mail", "password", true, Collections.EMPTY_LIST, created, null);
        when(tgAuthCallWebClient.doGet(anyString(), anyString(), anyString())).thenReturn(Mono.just(personDto));
        var sl = System.lineSeparator();
        String expected = "Привязанный аккаунт: " + sl
                + "Логин: " + personDto.getUsername() + sl
                + "Email: " + personDto.getEmail();
        BotApiMethod<Message> rsl = checkAction.handle(messageMock);
        assertThat(rsl.toString()).contains(expected);
    }

    @Test
    public void whenGetResourceFailsThenReturnErrorMessage() {
        when(tgAuthCallWebClient.doGet(anyString(), anyString(), anyString())).thenThrow(new RuntimeException("Service unavailable"));
        String expected = "Сервис не доступен попробуйте позже" + System.lineSeparator() + "/bind";
        BotApiMethod<Message> rsl = checkAction.handle(messageMock);
        assertThat(rsl.toString()).contains(expected);
    }


}