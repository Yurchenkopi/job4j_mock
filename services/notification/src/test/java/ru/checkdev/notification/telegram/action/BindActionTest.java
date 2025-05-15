package ru.checkdev.notification.telegram.action;

import org.junit.jupiter.api.BeforeEach;
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
class BindActionTest {
    @InjectMocks
    private BindAction bindAction;
    @Mock
    private TgAuthCallWebClint tgAuthCallWebClient;
    @Mock
    private SubscribeTelegramService subscribeTelegramService;
    @Mock
    private Message messageMock;
    private final String urlSiteAuth = "http://localhost:8080/login";
    @BeforeEach
    public void setUp() {
        bindAction = new BindAction(tgAuthCallWebClient, subscribeTelegramService, urlSiteAuth);
    }

    @Test
    public void whenRequestToBindThenSendResponse() {
        String expected = "Введите email и password в формате EMAIL PASSWORD :";
        BotApiMethod<Message> rsl = bindAction.handle(messageMock);
        assertThat(rsl.toString()).contains(expected);
    }

    @Test
    public void whenEmailIsInvalidThenReturnErrorMessage() {
        String email = "invalid-email";
        String messageText = email + " password";
        var sl = System.lineSeparator();
        String expected = "Email: " + email + " не корректный." + sl
                + "попробуйте снова." + sl
                + "/bind";
        when(messageMock.getChatId()).thenReturn(12345L);
        when(messageMock.getText()).thenReturn(messageText);
        BotApiMethod<Message> rsl = bindAction.callback(messageMock);
        assertThat(rsl.toString()).contains(expected);
    }

    @Test
    public void whenAuthenticationFailsThenReturnServiceUnavailableMessage() {
        String email = "valid-email@example.com";
        String password = "password";
        PersonDTO person = new PersonDTO(0, "username", email, password, true, null, Calendar.getInstance());
        String messageText = email + " " + password;
        var sl = System.lineSeparator();
        String expected = "Сервис не доступен попробуйте позже" + sl
                + "/bind";
        when(messageMock.getChatId()).thenReturn(12345L);
        when(messageMock.getText()).thenReturn(messageText);
        when(tgAuthCallWebClient.doPost(anyString(), any(PersonDTO.class))).thenThrow(new RuntimeException("Service unavailable"));
        BotApiMethod<Message> rsl = bindAction.callback(messageMock);
        assertThat(rsl.toString()).contains(expected);
    }

    @Test
    public void whenAuthenticationSucceedsAndUserNotBoundThenBindUser() {
        String email = "valid-email@example.com";
        String password = "password";
        PersonDTO person = new PersonDTO(0, "username", email, password, true, null, Calendar.getInstance());
        String messageText = email + " " + password;
        var sl = System.lineSeparator();
        String expected = "Аккаунт был успешно привязан к сервису нотификации." + sl
                + "Логин: " + email + sl
                + "Пароль: " + password + sl
                + urlSiteAuth;
        when(messageMock.getChatId()).thenReturn(12345L);
        when(messageMock.getText()).thenReturn(messageText);
        when(tgAuthCallWebClient.doPost(anyString(), any(PersonDTO.class))).thenReturn(Mono.just(new HashMap<String, Object>() {{
            put("token", "some-token");
        }}));
        when(tgAuthCallWebClient.doGetWithToken(anyString(), anyString(), anyString())).thenReturn(Mono.just(person));
        when(subscribeTelegramService.findByUserId(anyInt())).thenReturn(Optional.empty());

        BotApiMethod<Message> rsl = bindAction.callback(messageMock);
        assertThat(rsl.toString()).contains(expected);
    }

    @Test
    public void whenUserIsAlreadyBoundThenReturnAlreadyBoundMessage() {
        String email = "valid-email@example.com";
        String password = "password";
        Long chatId = 12345L;
        PersonDTO person = new PersonDTO(5, "username", email, password, true, null, Calendar.getInstance());
        String messageText = email + " " + password;
        String expected = "Аккаунт уже привязан";
        when(messageMock.getChatId()).thenReturn(chatId);
        when(messageMock.getText()).thenReturn(messageText);
        when(tgAuthCallWebClient.doPost(anyString(), any(PersonDTO.class))).thenReturn(Mono.just(new HashMap<String, Object>() {{
            put("token", "some-token");
        }}));
        when(tgAuthCallWebClient.doGetWithToken(anyString(), anyString(), anyString())).thenReturn(Mono.just(person));
        when(subscribeTelegramService.findByUserId(anyInt())).thenReturn(Optional.of(new SubscribeTelegram(person.getId(), chatId)));
        BotApiMethod<Message> rsl = bindAction.callback(messageMock);
        assertThat(rsl.toString()).contains(expected);
    }

    @Test
    public void whenUserIsAlreadyBoundThenReturnUnboundMessage() {
        String email = "valid-email@example.com";
        String password = "password";
        Long currentChatId = 12345L;
        Long registeredChatId = 67890L;
        PersonDTO person = new PersonDTO(0, "username", email, password, true, null, Calendar.getInstance());
        String messageText = email + " " + password;
        String sl = System.lineSeparator();
        String expected = "К введенным учетным данным от сервиса нотификации уже привязан другой аккаунт." + sl
                + "Для привязки текущего аккаунта выполните процедуру отвязки через старое устройство.";
        when(messageMock.getChatId()).thenReturn(currentChatId);
        when(messageMock.getText()).thenReturn(messageText);
        when(tgAuthCallWebClient.doPost(anyString(), any(PersonDTO.class))).thenReturn(Mono.just(new HashMap<String, Object>() {{
            put("token", "some-token");
        }}));
        when(tgAuthCallWebClient.doGetWithToken(anyString(), anyString(), anyString())).thenReturn(Mono.just(person));
        when(subscribeTelegramService.findByUserId(anyInt())).thenReturn(Optional.of(new SubscribeTelegram(person.getId(), registeredChatId)));        BotApiMethod<Message> rsl = bindAction.callback(messageMock);
        assertThat(rsl.toString()).contains(expected);
    }
}