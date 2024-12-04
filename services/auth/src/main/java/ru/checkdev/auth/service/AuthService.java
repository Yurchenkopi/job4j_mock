package ru.checkdev.auth.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@Service
@Slf4j
@AllArgsConstructor
public class AuthService {
    private WebAuthCall webAuthCall;

    public String token(String url, Map<String, String> params) {
        ObjectMapper mapper = new ObjectMapper();
        String result = "";
        try {
            result = mapper.readTree(
                    webAuthCall.token(url, params).block()
            ).get("access_token").asText();
        } catch (Exception e) {
            log.error("Get token from service Auth error: {}", e.getMessage());
        }
        return result;
    }

}
