package ar.edu.unsam.ddso.tp.bot.adapter.telegram;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.util.DefaultLongPollingUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import ar.edu.unsam.ddso.tp.bot.exception.BotSearchException;
import ar.edu.unsam.ddso.tp.bot.service.BotSearchService;

@Component
@ConditionalOnProperty(prefix = "telegram.bot", name = "enabled", havingValue = "true")
public class TelegramBotAdapter extends DefaultLongPollingUpdateConsumer {

    private static final String SEARCH_COMMAND = "/buscar";

    private static final Pattern SEARCH_PATTERN =
        Pattern.compile("(name|description|date|tag)=(?:\"([^\"]*)\"|(\\S+))");

    private static final String INVALID_SEARCH_MESSAGE =
            "El formato de búsqueda no es válido. "
            + "Escribí /help para ver los comandos disponibles.";

    private static final String HELP_MESSAGE = """
            Hola. Soy el bot de búsqueda del Archivo Digital.

            Podés buscar piezas utilizando:

            /buscar name=carta
            /buscar description=manuscrita
            /buscar date=1978-06-25
            /buscar tag=historia
            /buscar name="carta familiar" tag=historia

            Podés combinar varios filtros.
            """;

    private final TelegramClient telegramClient;
    private final BotSearchService botSearchService;

    public TelegramBotAdapter(
            @Value("${telegram.bot.token}") String botToken,
            BotSearchService botSearchService) {

        this.telegramClient = new OkHttpTelegramClient(botToken);
        this.botSearchService = botSearchService;
    }

    @Override
    public void consume(Update update) {

        if (!update.hasMessage() || !update.getMessage().hasText()) {
            return;
        }

        String message = update.getMessage().getText();
        String response;

        if (message.equals("/start") || message.equals("/help")) {
            response = HELP_MESSAGE;
        } else if (message.equals(SEARCH_COMMAND)
                || message.startsWith(SEARCH_COMMAND + " ")) {
            response = search(message);
        } else {
            return;
        }

        sendMessage(
                String.valueOf(update.getMessage().getChatId()),
                response);
    }

    private String search(String message) {

        String arguments = message.substring(SEARCH_COMMAND.length()).trim();

        String name = null;
        String description = null;
        LocalDate date = null;
        List<String> tags = new ArrayList<>();

        Matcher matcher = SEARCH_PATTERN.matcher(arguments);

        int posicion = 0;

        try {
            while (matcher.find()) {

                String textoAnterior = arguments.substring(posicion, matcher.start());

                if (!textoAnterior.isBlank()) {
                    return INVALID_SEARCH_MESSAGE;
                }

                String value = matcher.group(2);

                if (value == null) {
                    value = matcher.group(3);
                }

                if (value.isBlank()) {
                    return INVALID_SEARCH_MESSAGE;
                }

                switch (matcher.group(1)) {
                    case "name" -> name = value;
                    case "description" -> description = value;
                    case "date" -> date = LocalDate.parse(value);
                    case "tag" -> tags.add(value);
                    default -> throw new IllegalStateException(
                            "Valor inesperado: " + matcher.group(1));
                }

                posicion = matcher.end();
            }

            if (!arguments.substring(posicion).isBlank()) {
                return INVALID_SEARCH_MESSAGE;
            }

        } catch (DateTimeParseException exception) {
            return "La fecha no tiene un formato válido. Usá YYYY-MM-DD.";
        }

        if (name == null && description == null && date == null && tags.isEmpty()) {
            return INVALID_SEARCH_MESSAGE;
        }

        try {
            return botSearchService.search(name, description, date, tags);
        } catch (BotSearchException exception) {
            return "No se pudo realizar la búsqueda porque el servicio no está disponible. "
                    + "Intentá nuevamente más tarde.";
        }
    }

    private void sendMessage(String chatId, String text) {

        SendMessage message = new SendMessage(chatId, text);

        try {
            telegramClient.execute(message);
        } catch (TelegramApiException exception) {
            throw new IllegalStateException(
                    "No se pudo enviar el mensaje de Telegram.",
                    exception);
        }
    }
}
