package com.piratas.controller;

import com.piratas.model.Personagem;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/jogo")
public class JogoController {

    private Personagem lua = Personagem.criarLua();

    @GetMapping("/lua")
    public Personagem getStatusLua() {
        return lua;
    }
}