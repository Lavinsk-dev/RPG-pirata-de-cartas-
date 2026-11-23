package com.piratas.controller;

import com.piratas.model.Carta;
import com.piratas.model.Personagem;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@RestController
@RequestMapping("/api/jogo")
public class JogoController {

    private Personagem lua = Personagem.criarLua();
    private Personagem inimigo = Personagem.criarInimigoTeste();
    private String ultimoLog = "A batalha começou! Escolha uma carta da Lua ou faça um Improviso Narrativo.";
    private Random random = new Random();

    @GetMapping("/estado")
    public Map<String, Object> getEstado() {
        Map<String, Object> estado = new HashMap<>();
        estado.put("lua", lua);
        estado.put("inimigo", inimigo);
        estado.put("log", ultimoLog);
        return estado;
    }

    @PostMapping("/jogar-carta/{indice}")
    public Map<String, Object> jogarCarta(@PathVariable int indice) {
        if (indice < 0 || indice >= lua.getMao().size()) {
            ultimoLog = "Carta inválida!";
            return getEstado();
        }

        Carta carta = lua.getMao().get(indice);
        if (!lua.gastarEP(carta.getCustoEP())) {
            ultimoLog = "EP insuficiente para usar " + carta.getNome() + "!";
            return getEstado();
        }

        int d20 = random.nextInt(20) + 1;
        int bonusDado = (d20 == 20) ? 5 : (d20 >= 15) ? 3 : (d20 >= 6) ? 1 : 0;

        switch (carta.getNome()) {
            case "Disparo de Pederneira":
                int danoDisparo = lua.getAtk() + carta.getValorBase() + bonusDado;
                inimigo.receberDano(danoDisparo, 0);
                ultimoLog = "🎲 d20 = " + d20 + " | Lua disparou! Causou " + danoDisparo + " de poder de ataque em " + inimigo.getNome() + "!";
                break;
            case "Estocada na Brecha":
                int danoEstocada = lua.getAtk() + carta.getValorBase() + bonusDado;
                inimigo.receberDano(danoEstocada, 3);
                ultimoLog = "🎲 d20 = " + d20 + " | Golpe preciso ignorando defesa! Poder: " + danoEstocada + "!";
                break;
            case "Defesa Protetora":
                lua.adicionarEscudo(carta.getValorBase() + bonusDado);
                ultimoLog = "🎲 d20 = " + d20 + " | Lua assumiu postura defensiva (+ " + (carta.getValorBase() + bonusDado) + " DEF temporária)!";
                break;
            case "Memória da Civilização":
                lua.adicionarBonusImproviso(carta.getValorBase());
                ultimoLog = "📜 Lua analisou as ruínas! Próximo Improviso Narrativo ganha +3 no dado!";
                break;
            case "Mecanismo Antigo":
                lua.getArmadilhasSetadas().add(carta);
                ultimoLog = "⚙️ Armadilha 'Mecanismo Antigo' setada virada para baixo no campo!";
                break;
        }

        lua.getMao().remove(indice);
        return getEstado();
    }

    @PostMapping("/improviso/{bonusMestre}")
    public Map<String, Object> improvisoNarrativo(@PathVariable int bonusMestre) {
        int d20 = random.nextInt(20) + 1;
        int bonusCarta = lua.consumirBonusImproviso();
        int total = d20 + bonusMestre + bonusCarta;

        if (total >= 15) {
            int dano = 6 + bonusMestre;
            inimigo.receberDano(dano, 2);
            ultimoLog = "🎭 IMPROVISO ÉPICO! d20 (" + d20 + ") + Mestre (" + bonusMestre + ") + Carta (" + bonusCarta + ") = " + total + "! Causou " + dano + " de dano!";
        } else {
            ultimoLog = "🎭 Improviso Narrativo: d20 (" + d20 + ") + Mestre (" + bonusMestre + ") + Carta (" + bonusCarta + ") = " + total + ". Ação realizada com sucesso moderado!";
        }
        return getEstado();
    }

    @PostMapping("/proximo-turno")
    public Map<String, Object> proximoTurno() {
        // Inimigo ataca
        if (inimigo.getPvAtual() > 0) {
            int danoInimigo = inimigo.getAtk() + random.nextInt(4);
            if (!lua.getArmadilhasSetadas().isEmpty()) {
                Carta armadilha = lua.getArmadilhasSetadas().remove(0);
                inimigo.receberDano(armadilha.getValorBase(), 99);
                ultimoLog = "💥 ARMADILHA ATIVADA (" + armadilha.getNome() + ")! Inimigo sofreu 4 de dano direto e perdeu o ataque!";
            } else {
                lua.receberDano(danoInimigo, 0);
                ultimoLog = "⚔️ Turno Inimigo: " + inimigo.getNome() + " atacou com força " + danoInimigo + "!";
            }
        }
        lua.iniciarNovoTurno();
        return getEstado();
    }

    @PostMapping("/reiniciar")
    public Map<String, Object> reiniciar() {
        lua = Personagem.criarLua();
        inimigo = Personagem.criarInimigoTeste();
        ultimoLog = "🔄 Combate reiniciado!";
        return getEstado();
    }
}