package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.service.DirectorService;

import java.util.Collection;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/directors")
public class DirectorController {
    private final DirectorService directorService;

    @PostMapping
    public Director addDirector(@Valid @RequestBody Director newDirector) {
        log.info("Получен запрос на добавление режиссера: {}", newDirector);
        return directorService.addDirector(newDirector);
    }

    @PutMapping
    public Director updateDirector(@Valid @RequestBody Director newDirector) {
        log.info("Получен запрос на обновление режиссера: {}", newDirector);
        return directorService.updateDirector(newDirector);
    }

    @GetMapping
    public Collection<Director> getAllDirectors() {
        log.info("Получен запрос на получение всех режиссеров");
        return directorService.getAllDirectors();
    }

    @GetMapping("/{id}")
    public Director getDirectorById(@PathVariable long id) {
        log.info("Получен запрос на получение режиссера по id {}", id);
        return directorService.getDirectorById(id);
    }

    @DeleteMapping("/{id}")
    public void deleteDirector(@PathVariable long id) {
        log.info("Получен запрос на удаление режиссера {}", id);
        directorService.deleteDirector(id);
    }
}