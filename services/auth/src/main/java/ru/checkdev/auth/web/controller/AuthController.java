package ru.checkdev.auth.web.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.checkdev.auth.domain.Profile;
import ru.checkdev.auth.service.AuthService;
import ru.checkdev.auth.service.PersonService;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author parsentev
 * @since 26.09.2016
 */
@RestController
@Slf4j
public class AuthController {
    private final PersonService persons;
    private final String ping = "{}";

    private final AuthService authService;

    @Autowired
    public AuthController(final PersonService persons, final AuthService authService) {
        this.persons = persons;
        this.authService = authService;
    }

    @RequestMapping("/user")
    public Principal user(Principal user) {
        return user;
    }

    @GetMapping("/ping")
    public String ping() {
        return this.ping;
    }

    @GetMapping("/auth/activated/{key}")
    public Object activated(@PathVariable String key) {
        if (this.persons.activated(key)) {
            return new Object() {
                public boolean getSuccess() {
                    return true;
                }
            };
        } else {
            return new Object() {
                public String getError() {
                    return "Notify has already activated";
                }
            };
        }
    }

    @PostMapping("/registration")
    public Object registration(@RequestBody Profile profile) {
        Optional<Profile> result = this.persons.reg(profile);
        return result.<Object>map(prs -> new Object() {
            public Profile getPerson() {
                return prs;
            }
        }).orElseGet(() -> new Object() {
            public String getError() {
                return String.format("Пользователь с почтой %s уже существует.", profile.getEmail());
            }
        });
    }

    @PostMapping("/forgot")
    public Object forgot(@RequestBody Profile profile) {
        Optional<Profile> result = this.persons.forgot(profile);
        if (result.isPresent()) {
            return new Object() {
                public String getOk() {
                    return "ok";
                }
            };
        } else {
            return new Object() {
                public String getError() {
                    return "E-mail не найден.";
                }
            };
        }
    }

    @GetMapping("/revoke")
    @ResponseStatus(HttpStatus.OK)
    public void logout(HttpServletRequest request) {

    }

    @PostMapping("/signIn")
    public Object signIn(HttpServletRequest request, @RequestBody Profile profile, @Value("${security.oauth2.tokenUri}") String oauth2url) {
        var isLogin = authService.token(
                oauth2url,
                Map.of("username", profile.getEmail(),
                        "password", profile.getPassword()));
        if (isLogin.isEmpty()) {
            return new Object() {
                public String getError() {
                    return "Проверьте введенные логин и пароль.";
                }
            };
        }
        return new Object() {
            public String getToken() {
                return isLogin;
            }
        };
    }
}
