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

    private final PersonService personService;

    @Value("${security.oauth2.tokenUri}")
    String oauth2url;

    @Autowired
    public AuthController(final PersonService persons, final AuthService authService, final PersonService personService)
    {
        this.persons = persons;
        this.authService = authService;
        this.personService = personService;
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
    public Object signIn(HttpServletRequest req,
                                       @RequestBody Profile profile) {
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
        log.info(isLogin);
        req.getSession().setAttribute("Authorization", isLogin);
        log.info(req.getSession().getAttributeNames().nextElement());
        log.info(req.getSession().getAttribute("Authorization").toString());
        return new Object() {
            public String getToken() {
                return isLogin;
            }
        };
    }

    @GetMapping("/check")
    public Profile check(HttpServletRequest req, Principal user) {
        Optional<Profile> optPerson = Optional.empty();
        log.info(req.getSession().getAttributeNames().nextElement());
        String token = req.getSession().getAttribute("Authorization").toString();
        log.info(token);
        if (token != null) {
            optPerson = personService.findByEmail(user.getName());
            log.info(optPerson.get().toString());
        }
        if (optPerson.isEmpty()) {
            return null;
        }
        log.info(optPerson.get().toString());
        return optPerson.get();
    }

}
