package ar.edu.unsam.ddso.tp.bot.adapter.http;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriBuilder;

import ar.edu.unsam.ddso.tp.bot.exception.BotSearchException;
import ar.edu.unsam.ddso.tp.bot.port.PieceSearchPort;
import ar.edu.unsam.ddso.tp.dto.PieceResponseDTO;

@Component
public class PieceApiHttpAdapter implements PieceSearchPort {

    private final RestClient restClient;

    public PieceApiHttpAdapter(
            @Value("${bot.api.base-url:http://localhost:8080}") String baseUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    @Override
    public List<PieceResponseDTO> search(
            String name,
            String description,
            LocalDate date,
            List<String> tags) {

        try {
            List<PieceResponseDTO> pieces = restClient.get()
                    .uri(uriBuilder -> buildSearchUri(
                            uriBuilder, name, description, date, tags))
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {
                    });

            return pieces == null ? List.of() : pieces;

        } catch (RestClientException exception) {
            throw new BotSearchException(
                    "No se pudo consultar la API de búsqueda.",
                    exception);
        }
    }

    private java.net.URI buildSearchUri(
            UriBuilder uriBuilder,
            String name,
            String description,
            LocalDate date,
            List<String> tags) {

        uriBuilder.path("/pieces");

        if (name != null && !name.isBlank()) {
            uriBuilder.queryParam("name", name);
        }

        if (description != null && !description.isBlank()) {
            uriBuilder.queryParam("description", description);
        }

        if (date != null) {
            uriBuilder.queryParam("date", date);
        }

        if (tags != null && !tags.isEmpty()) {
            uriBuilder.queryParam("tag", tags.toArray());
        }

        return uriBuilder.build();
    }
}
