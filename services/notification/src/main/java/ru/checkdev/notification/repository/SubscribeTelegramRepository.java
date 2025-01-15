package ru.checkdev.notification.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;
import ru.checkdev.notification.domain.SubscribeTelegram;

import java.util.List;
import java.util.Optional;

public interface SubscribeTelegramRepository extends CrudRepository<SubscribeTelegram, Long> {
    @Override
    List<SubscribeTelegram> findAll();

    Optional<SubscribeTelegram> findByUserId(int id);

    Optional<SubscribeTelegram> findByChatId(long id);

    @Modifying
    @Transactional
    @Query("delete from cd_subscribe_telegram tg where tg.chatId = ?1")
    void deleteByChatId(long chatId);

}
