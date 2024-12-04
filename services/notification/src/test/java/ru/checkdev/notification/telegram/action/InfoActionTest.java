package ru.checkdev.notification.telegram.action;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ExtendWith(MockitoExtension.class)
class InfoActionTest {
    private final InfoAction infoAction = new InfoAction(List.of(
            "/start", "/new", "/check", "/bind", "/unbind"));
    @Mock
    private Message messageMock;

    @Test
    public void whenRequestInfoThenReturnInfoMessage() {
        String sl = System.lineSeparator();
        String expected = "Выберите действие:" + sl
                + "/start" + sl
                + "/new" + sl
                + "/check" + sl
                + "/bind" + sl
                + "/unbind" + sl;
        BotApiMethod<Message> rsl = infoAction.handle(messageMock);
        assertThat(rsl.toString()).contains(expected);
    }
}