package ar.edu.unsam.ddso.tp.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import ar.edu.unsam.ddso.tp.dto.PieceCreateRequestDTO;
import ar.edu.unsam.ddso.tp.dto.PieceResponseDTO;
import ar.edu.unsam.ddso.tp.service.PieceService;
import jakarta.validation.Valid;


@RestController
@RequestMapping("/pieces")
public class PieceController {

    private final PieceService pieceService;

    public PieceController(PieceService pieceService) {
        this.pieceService = pieceService;
    }

    @GetMapping
    public List<PieceResponseDTO> search(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) LocalDate date,
            @RequestParam(name = "tag", required = false) List<String> tags) {
        return pieceService.search(name, description, date, tags);
    }

    @GetMapping("/{id}")
    public PieceResponseDTO findById(@PathVariable Long id) {
        return pieceService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PieceResponseDTO create(@Valid @RequestBody PieceCreateRequestDTO request) {
        return pieceService.create(request);
    }
}
