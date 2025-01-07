package ru.checkdev.notification.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.checkdev.notification.domain.SubscribeTelegram;
import ru.checkdev.notification.domain.SubscribeTopic;
import ru.checkdev.notification.repository.SubscribeTelegramRepository;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class SubscribeTelegramService {

    private final SubscribeTelegramRepository repository;

    public List<SubscribeTelegram> findAll() {
        return repository.findAll();
    }

    public SubscribeTelegram save(SubscribeTelegram subscribeTelegram) {
        return repository.save(subscribeTelegram);
    }

    public SubscribeTelegram findByChatId(long chatId) {
        Optional<SubscribeTelegram> rsl = repository.findByChatId(chatId);
        return rsl.orElseGet(SubscribeTelegram::new);
    }

    public SubscribeTelegram findByUserId(int userId) {
        Optional<SubscribeTelegram> rsl = repository.findByUserId(userId);
        return rsl.orElseGet(SubscribeTelegram::new);
    }
}
