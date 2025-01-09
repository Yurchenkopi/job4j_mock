package ru.checkdev.notification.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.checkdev.notification.domain.SubscribeTelegram;
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

    public void delete(long chatId) {
        repository.deleteByChatId(chatId);
    }

    public Optional<SubscribeTelegram> findByChatId(long chatId) {
        return repository.findByChatId(chatId);
    }

    public Optional<SubscribeTelegram> findByUserId(int userId) {
        return repository.findByUserId(userId);
    }
}
