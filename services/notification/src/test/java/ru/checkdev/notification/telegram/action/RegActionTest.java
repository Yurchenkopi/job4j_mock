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
import ru.checkdev.notification.telegram.config.TgConfig;
import ru.checkdev.notification.telegram.service.TgAuthCallWebClint;

import java.lang.reflect.Field;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegActionTest {
    @InjectMocks
    private RegAction regAction;
    @Mock
    private TgAuthCallWebClint tgAuthCallWebClient;
    @Mock
    private SubscribeTelegramService subscribeTelegramService;
    @Mock
    private Message messageMock;

    @Test
    public void whenRequestToRegThenSendResponse() {
        String expected = "Введите имя пользователя и email для регистрации в формате USERNAME EMAIL :";
        BotApiMethod<Message> rsl = regAction.handle(messageMock);
        assertThat(rsl.toString()).contains(expected);
    }

    @Test
    public void whenEmailIsInvalidThenReturnErrorMessage() {
        String username = "username";
        String email = "invalid-email";
        String messageText = username + " " + email;
        var sl = System.lineSeparator();
        String expected = "Email: " + email + " не корректный." + sl
                + "попробуйте снова." + sl
                + "/new";
        when(messageMock.getText()).thenReturn(messageText);
        BotApiMethod<Message> rsl = regAction.callback(messageMock);
        assertThat(rsl.toString()).contains(expected);
    }

    @Test
    public void whenAccountCheckFailedThenReturnServiceUnavailableMessage() {
        var sl = System.lineSeparator();
        String expected = "Сервис не доступен попробуйте позже" + sl
                + "/bind";
        when(subscribeTelegramService.findByChatId(anyLong())).thenReturn(Optional.of(new SubscribeTelegram(5, 5L)));
        when(tgAuthCallWebClient.doGet(anyString())).thenThrow(new RuntimeException("Service unavailable"));
        when(messageMock.getChatId()).thenReturn(11111L);
        when(messageMock.getText()).thenReturn("username valid-mail@mail.com");
        BotApiMethod<Message> rsl = regAction.callback(messageMock);
        assertThat(rsl.toString()).contains(expected);
    }

    @Test
    public void whenAccountCheckedAAndIsNotRegisteredThenRegisterUser() throws NoSuchFieldException, IllegalAccessException {
        String username = "username";
        String email = "valid-email@mail.com";
        String password = "tg_password";
        TgConfig tgConfig = mock(TgConfig.class);
        var sl = System.lineSeparator();
        String expected = "Вы зарегистрированы: " + sl
                + "userName: " + username + sl
                + "Логин: " + email + sl
                + "Пароль: " + password + sl;
        PersonDTO person = new PersonDTO(0, username, email, "password", true, null, Calendar.getInstance());

        when(messageMock.getChatId()).thenReturn(11111L);
        when(messageMock.getText()).thenReturn(username + " " + email);

        when(tgConfig.isEmail(email)).thenReturn(true);
        when(tgConfig.getPassword()).thenReturn(password);
        Field tgConfigField = RegAction.class.getDeclaredField("tgConfig");
        tgConfigField.setAccessible(true);
        tgConfigField.set(regAction, tgConfig);

        when(tgAuthCallWebClient.doPost(anyString(), any(PersonDTO.class))).thenReturn(Mono.just(person));
        BotApiMethod<Message> rsl = regAction.callback(messageMock);
        assertThat(rsl.toString()).contains(expected);
    }

    @Test
    public void whenUserIsAlreadyRegisteredThenErrorMessage() {
        String username = "username";
        String email = "valid-email@mail.com";
        String expected = "Текущий аккаунт уже имеет привязку к сервису нотификации. Попробуйте восстановить пароль от текущей учетной записи /forget или отвяжите аккаунт от учетной записи и создайте новую /unbind";
        PersonDTO person = new PersonDTO(0, username, email, "password", true, null, Calendar.getInstance());
        when(messageMock.getChatId()).thenReturn(11111L);
        when(messageMock.getText()).thenReturn(username + " " + email);
        when(subscribeTelegramService.findByChatId(anyLong())).thenReturn(Optional.of(new SubscribeTelegram(5, 5L)));
        when(tgAuthCallWebClient.doGet(anyString())).thenReturn(Mono.just(person));
        BotApiMethod<Message> rsl = regAction.callback(messageMock);
        assertThat(rsl.toString()).contains(expected);
    }

    @Test
    public void whenAccountCheckedAAndIsNotRegisteredThenRegisterFailed() {
        String username = "username";
        String email = "valid-email@mail.com";
        String expected = String.format("Пользователь с почтой %s уже существует.", email);
        when(messageMock.getChatId()).thenReturn(11111L);
        when(messageMock.getText()).thenReturn(username + " " + email);
        when(tgAuthCallWebClient.doPost(anyString(), any(PersonDTO.class))).thenReturn(Mono.just(new HashMap<String, Object>() {{
            put("error", expected);
        }}));
        BotApiMethod<Message> rsl = regAction.callback(messageMock);
        assertThat(rsl.toString()).contains(expected);
    }
}