
package ar.edu.unsam.ddso.tp.bot.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import ar.edu.unsam.ddso.tp.bot.adapter.telegram.TelegramBotAdapter;

@Configuration
@ConditionalOnProperty(
        prefix = "telegram.bot",
        name = "enabled",
        havingValue = "true")
public class TelegramBotConfiguration {

    @Bean
    public TelegramBotsLongPollingApplication telegramBotsApplication(
            @Value("${telegram.bot.token}") String botToken,
            TelegramBotAdapter telegramBotAdapter)
            throws TelegramApiException {

        TelegramBotsLongPollingApplication application =
                new TelegramBotsLongPollingApplication();

        application.registerBot(botToken, telegramBotAdapter);

        return application;
    }
}
