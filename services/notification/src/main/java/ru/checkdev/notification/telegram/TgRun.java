package ru.checkdev.notification.telegram;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import ru.checkdev.notification.domain.SubscribeTelegram;
import ru.checkdev.notification.service.SubscribeTelegramService;
import ru.checkdev.notification.telegram.action.*;
import ru.checkdev.notification.telegram.service.TgAuthCallWebClint;

import java.util.List;
import java.util.Map;

/**
 * 3. Мидл
 * Инициализация телеграм бот,
 * username = берем из properties
 * token = берем из properties
 *
 * @author Dmitry Stepanov, user Dmitry
 * @since 12.09.2023
 */
@Component
@Slf4j
public class TgRun {
    private final TgAuthCallWebClint tgAuthCallWebClint;
    private final SubscribeTelegramService subscribeTelegramService;
    @Value("${tg.username}")
    private String username;
    @Value("${tg.token}")
    private String token;

    @Value("${security.oauth2.tokenUri}")
    private String oauth2Token;

    @Value("${server.site.url.login}")
    private String urlSiteAuth;

    public TgRun(TgAuthCallWebClint tgAuthCallWebClint, SubscribeTelegramService subscribeTelegramService) {
        this.tgAuthCallWebClint = tgAuthCallWebClint;
        this.subscribeTelegramService = subscribeTelegramService;
    }

    @Bean
    public void initTg() {
        Map<String, Action> actionMap = Map.of(
                "/start", new InfoAction(List.of(
                        "/start", "/new", "/check", "/bind", "/unbind")),
                "/new", new RegAction(tgAuthCallWebClint, subscribeTelegramService, urlSiteAuth),
                "/check", new CheckAction(tgAuthCallWebClint, subscribeTelegramService),
                "/bind", new BindAction(tgAuthCallWebClint, urlSiteAuth),
                "/unbind", new UnBindAction(tgAuthCallWebClint)
        );
        try {
            BotMenu menu = new BotMenu(actionMap, username, token);

            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(menu);
        } catch (TelegramApiException e) {
            log.error("Telegram bot: {}, ERROR {}", username, e.getMessage());
        }
    }
}
