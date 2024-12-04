package ru.checkdev.auth.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 3. Мидл
 * Класс реализует методы get и post для отправки сообщений через WebClient
 *
 * @author Dmitry Stepanov, user Dmitry
 * @since 12.09.2023
 */
@Service
@Slf4j
public class WebAuthCall {
    private final WebClient webClient;

    public WebAuthCall(@Value("${server.auth}") String urlAuth) {
        this.webClient = WebClient.create(urlAuth);
    }

    public Mono<String> token(String url, Map<String, String> params) {
        var requestBody = new StringBuilder();
        params.forEach((key, value) ->
                requestBody
                        .append(key)
                        .append('=')
                        .append(value)
                        .append('&')
        );
        requestBody.append("scope=any&grant_type=password");
        return webClient
                .post()
                .uri(url)
                .header("Authorization", "Basic am9iNGo6cGFzc3dvcmQ=")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromValue(requestBody.toString()))
                .retrieve()
                .bodyToMono(String.class)
                .doOnError(err -> log.error("API not found: {}", err.getMessage()));
    }


}
