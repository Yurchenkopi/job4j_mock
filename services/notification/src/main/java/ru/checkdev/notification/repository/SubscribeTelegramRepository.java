package ru.checkdev.notification.repository;

import org.springframework.data.repository.CrudRepository;
import ru.checkdev.notification.domain.SubscribeTelegram;

import java.util.List;
import java.util.Optional;

public interface SubscribeTelegramRepository extends CrudRepository<SubscribeTelegram, Long> {
    @Override
    List<SubscribeTelegram> findAll();

    Optional<SubscribeTelegram> findByUserId(int id);

    Optional<SubscribeTelegram> findByChatId(long id);

}
