package ru.checkdev.auth.service;

import lombok.AllArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.checkdev.auth.domain.Notify;

import javax.annotation.PreDestroy;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * @author Petr Arsentev (parsentev@yandex.ru)
 * @version $Id$
 * @since 0.1
 */
@AllArgsConstructor
@Service
public class Messenger {
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public void send(Notify notify) {
        this.scheduler.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    kafkaTemplate.send("message_from_auth", notify);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @PreDestroy
    public void close() {
        this.scheduler.shutdown();
    }
}
