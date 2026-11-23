package com.piratas.controller;

import com.piratas.model.Carta;
import com.piratas.model.Personagem;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@RestController
@RequestMapping("/api/jogo")
@CrossOrigin(origins = "*")
public class JogoController {

    private final Map<String, Personagem> herois = new LinkedHashMap<>();
    private final Map<String, Object> inimigo = new LinkedHashMap<>();
    private final List<java.util.Map<String, Object>> armadilhasAtivas = new ArrayList<>();
    private final List<String> logs = new ArrayList<>();
    private String cenarioAtual = "porto";
    private String cenarioTitulo = "Porto Pirata — Docas da Alvorada";

    public JogoController() {
        inicializarDados();
    }

    private void inicializarDados() {
        herois.clear();
        armadilhasAtivas.clear();
        logs.clear();

        // 1. LUA - A Flor de Sangue
        Personagem lua = new Personagem("lua", "Lua", "A Flor de Sangue", "Arqueóloga", "/imagens/lua.jpg", 20, 4, 2, 2);
        lua.getBaralho().add(new Carta("lua_1", "Disparo de Precisão", "ATAQUE", 2, 4, "PRECISAO", "Tiro com pistola de cano longo. +2 no d20 se houver armadilha ou análise.", "/imagens/carta_lua.jpg"));
        lua.getBaralho().add(new Carta("lua_2", "Estocada e Chute de Salto", "ATAQUE", 1, 2, "AGIL_EP", "Faca + golpe com salto de cano longo. Recupera 1 EP se d20 >= 12.", ""));
        lua.getBaralho().add(new Carta("lua_3", "Leitura de Ruínas", "SUPORTE", 1, 2, "BUFF_ATK", "Analisa falhas antigas no alvo. Concede +2 ATK para o grupo no turno.", ""));
        lua.getBaralho().add(new Carta("lua_4", "Fio de Escavação", "ARMADILHA", 2, 4, "ESTACAS", "Carta oculta: prende o atacante, bloqueia 4 de dano e causa 4 de dano.", ""));
        lua.getBaralho().add(new Carta("lua_5", "Fragmento do Códice", "SUPORTE", 2, 3, "CURA_DEF", "Anotações da civilização perdida. Cura 3 PV de todos e dá +1 DEF.", ""));
        herois.put("lua", lua);

        // 2. KYAN - O Lobo Branco
        Personagem kyan = new Personagem("kyan", "Kyan", "O Lobo Branco", "Carpinteiro / Pirata", "/imagens/kyan.jpg", 20, 3, 2, 3);
        kyan.getBaralho().add(new Carta("kyan_1", "Corte Duplo dos Machados", "ATAQUE", 2, 3, "DUPLO", "Golpe veloz (VLC 3). Se d20 >= 14, desfere segundo corte (+3 dano).", ""));
        kyan.getBaralho().add(new Carta("kyan_2", "Martelo & Cinzel", "ATAQUE", 1, 2, "QUEBRA_DEF", "Impacto de construtor que abala a guarda inimiga.", ""));
        kyan.getBaralho().add(new Carta("kyan_3", "Barricada de Carpinteiro", "DEFESA", 2, 4, "ESCUDO_DEF", "Ergue vigas reforçadas: ganha +4 Escudo e +1 DEF no turno.", ""));
        kyan.getBaralho().add(new Carta("kyan_4", "Estrepes e Vigas Ocultas", "ARMADILHA", 2, 4, "DESABAMENTO", "Carta oculta: estrutura desaba no atacante, bloqueando 4 e causando 4 de dano.", ""));
        kyan.getBaralho().add(new Carta("kyan_5", "Pulso Rúnico Latente", "SUPORTE", 2, 3, "CURA_ATK", "As runas do machado aquecem com seus batimentos: cura 3 PV e dá +2 ATK.", "/imagens/carta_kyan.jpg"));
        herois.put("kyan", kyan);

        // 3. ZAFIRA D'ÁVILLA - A Marcada pelo Dragão
        Personagem zafira = new Personagem("zafira", "Zafira D'Ávilla", "A Marcada pelo Dragão", "Navegadora / Pirata", "/imagens/zafira.jpg", 20, 3, 3, 2);
        zafira.getBaralho().add(new Carta("zafira_1", "Dança das Lâminas", "ATAQUE", 2, 3, "ESQUIVA_ATK", "Golpe giratório com leques laminados. Causa dano e ganha +1 DEF.", "/imagens/carta_zafira.jpg"));
        zafira.getBaralho().add(new Carta("zafira_2", "Corrente de Vento", "SUPORTE", 1, 2, "VENTO_EP", "Lê os ventos como sua mãe: restaura +2 EP para todos os aliados.", ""));
        zafira.getBaralho().add(new Carta("zafira_3", "Guarda do Dragão", "DEFESA", 2, 4, "REFLETE", "Fecha os leques reforçados: ganha +4 Escudo e causa 2 de dano imediato.", ""));
        zafira.getBaralho().add(new Carta("zafira_4", "Agulhas & Névoa Náutica", "ARMADILHA", 2, 4, "VENENO", "Carta oculta: some na fumaça, bloqueia 4 de dano e dispara agulhas (4 dano).", ""));
        zafira.getBaralho().add(new Carta("zafira_5", "Bússola Estelar", "SUPORTE", 2, 3, "CURA_SORTE", "Traça a rota perfeita: cura 3 PV e concede +2 de bônus na próxima rolagem.", ""));
        herois.put("zafira", zafira);

        inimigo.put("nome", "Capitão Morgano — Corsário de Ferro");
        inimigo.put("pv", 45);
        inimigo.put("pvMax", 45);
        inimigo.put("atk", 5);
        inimigo.put("def", 2);

        registrarLog("⚓ A tripulação desembarcou em Porto Pirata. Escolha seu personagem!");
    }

    private void registrarLog(String msg) {
        logs.add(0, msg);
        if (logs.size() > 25) {
            logs.remove(logs.size() - 1);
        }
    }

    @GetMapping("/estado")
    public Map<String, Object> getEstado() {
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("cenario", cenarioAtual);
        resp.put("cenarioTitulo", cenarioTitulo);
        resp.put("inimigo", inimigo);
        resp.put("herois", new ArrayList<>(herois.values()));
        resp.put("armadilhasAtivas", armadilhasAtivas);
        resp.put("logs", logs);
        return resp;
    }

    @PostMapping("/jogar")
    public Map<String, Object> jogarCarta(@RequestBody Map<String, String> payload) {
        String pId = payload.get("personagemId");
        String cId = payload.get("cartaId");
        Personagem h = herois.get(pId);
        if (h == null) return Map.of("erro", "Personagem não encontrado");

        Carta carta = h.getBaralho().stream().filter(c -> c.getId().equals(cId)).findFirst().orElse(null);
        if (carta == null) return Map.of("erro", "Carta não encontrada");

        if (h.getEp() < carta.getCustoEp()) {
            return Map.of("erro", "Energia (EP) insuficiente! Encerre o turno para recuperar.");
        }

        h.setEp(h.getEp() - carta.getCustoEp());
        int d20 = ThreadLocalRandom.current().nextInt(1, 21);
        int improviso = h.getBonusImproviso();
        h.setBonusImproviso(0);

        if ("PRECISAO".equals(carta.getEfeitoEspecial()) && !armadilhasAtivas.isEmpty()) {
            improviso += 2;
        }

        int rolagemFinal = d20 + improviso;
        int poder = carta.getPoderTotal();
        String resumo = "";

        switch (carta.getTipo()) {
            case "ATAQUE": {
                int atkTotal = h.getAtk() + h.getAtkTemp();
                int defInimigo = (int) inimigo.get("def");
                int bonusCritico = (rolagemFinal >= 18) ? 3 : (rolagemFinal == 1 ? -2 : 0);
                int bonusExtra = 0;

                if ("DUPLO".equals(carta.getEfeitoEspecial()) && rolagemFinal >= 14) {
                    bonusExtra += 3;
                }
                if ("AGIL_EP".equals(carta.getEfeitoEspecial()) && rolagemFinal >= 12) {
                    h.setEp(Math.min(h.getEpMax(), h.getEp() + 1));
                }
                if ("ESQUIVA_ATK".equals(carta.getEfeitoEspecial())) {
                    h.setDefTemp(h.getDefTemp() + 1);
                }

                int dano = Math.max(1, poder + atkTotal + bonusCritico + bonusExtra - defInimigo);
                int pvInimigo = Math.max(0, (int) inimigo.get("pv") - dano);
                inimigo.put("pv", pvInimigo);

                resumo = String.format("⚔️ %s usou [%s] (1d20=%d%s) e causou %d de dano em %s!",
                        h.getNome(), carta.getNome(), d20,
                        improviso > 0 ? "+" + improviso : "", dano, inimigo.get("nome"));
                break;
            }
            case "DEFESA": {
                int escudoGanho = poder + h.getDef() + (rolagemFinal >= 15 ? 2 : 0);
                h.setEscudo(h.getEscudo() + escudoGanho);
                if ("ESCUDO_DEF".equals(carta.getEfeitoEspecial())) {
                    h.setDefTemp(h.getDefTemp() + 1);
                }
                if ("REFLETE".equals(carta.getEfeitoEspecial())) {
                    int pvInimigo = Math.max(0, (int) inimigo.get("pv") - 2);
                    inimigo.put("pv", pvInimigo);
                }
                resumo = String.format("🛡️ %s ativou [%s] (1d20=%d) e ganhou +%d de Escudo!",
                        h.getNome(), carta.getNome(), d20, escudoGanho);
                break;
            }
            case "ARMADILHA": {
                Map<String, Object> trap = new LinkedHashMap<>();
                trap.put("id", UUID.randomUUID().toString());
                trap.put("donoId", h.getId());
                trap.put("donoNome", h.getNome());
                trap.put("nome", carta.getNome());
                trap.put("poder", poder);
                armadilhasAtivas.add(trap);
                resumo = String.format("🪤 %s posicionou uma carta virada para baixo na mesa!", h.getNome());
                break;
            }
            case "SUPORTE": {
                if ("BUFF_ATK".equals(carta.getEfeitoEspecial())) {
                    herois.values().forEach(aliado -> aliado.setAtkTemp(aliado.getAtkTemp() + 2));
                    resumo = String.format("📜 %s usou [%s]: +2 ATK para toda a tripulação neste turno!", h.getNome(), carta.getNome());
                } else if ("VENTO_EP".equals(carta.getEfeitoEspecial())) {
                    herois.values().forEach(aliado -> aliado.setEp(Math.min(aliado.getEpMax(), aliado.getEp() + 2)));
                    resumo = String.format("🌬️ %s usou [%s]: +2 EP restaurados para todos os aliados!", h.getNome(), carta.getNome());
                } else {
                    h.setPv(Math.min(h.getPvMax(), h.getPv() + poder));
                    if ("CURA_DEF".equals(carta.getEfeitoEspecial())) h.setDefTemp(h.getDefTemp() + 1);
                    if ("CURA_ATK".equals(carta.getEfeitoEspecial())) h.setAtkTemp(h.getAtkTemp() + 2);
                    if ("CURA_SORTE".equals(carta.getEfeitoEspecial())) h.setBonusImproviso(h.getBonusImproviso() + 2);
                    resumo = String.format("✨ %s ativou [%s] (1d20=%d) e recuperou %d PV!", h.getNome(), carta.getNome(), d20, poder);
                }
                break;
            }
        }

        registrarLog(resumo);
        return Map.of("ok", true, "d20", d20, "mensagem", resumo);
    }

    @PostMapping("/turno")
    public Map<String, Object> encerrarTurno() {
        for (Personagem h : herois.values()) {
            h.setEp(h.getEpMax());
            h.setAtkTemp(0);
            h.setDefTemp(0);
        }
        registrarLog("🔄 Novo Turno! Energia (EP) restaurada para toda a tripulação.");
        return Map.of("ok", true);
    }

    @PostMapping("/loja")
    public Map<String, Object> comprarLoja(@RequestBody Map<String, String> payload) {
        Personagem h = herois.get(payload.get("personagemId"));
        String item = payload.get("item");
        if (h == null) return Map.of("erro", "Herói inválido");

        if ("pocao".equals(item)) {
            if (h.getOuro() < 15) return Map.of("erro", "Ouro insuficiente (15 ouros)");
            h.setOuro(h.getOuro() - 15);
            h.setPv(Math.min(h.getPvMax(), h.getPv() + 6));
            registrarLog("🧪 " + h.getNome() + " comprou Elixir Pirata (+6 PV).");
        } else if ("tonico".equals(item)) {
            if (h.getOuro() < 15) return Map.of("erro", "Ouro insuficiente (15 ouros)");
            h.setOuro(h.getOuro() - 15);
            h.setEp(Math.min(h.getEpMax(), h.getEp() + 2));
            registrarLog("⚡ " + h.getNome() + " comprou Tônico de Fôlego (+2 EP).");
        } else if ("forja".equals(item)) {
            if (h.getOuro() < 25) return Map.of("erro", "Ouro insuficiente (25 ouros)");
            h.setOuro(h.getOuro() - 25);
            Carta primeira = h.getBaralho().get(0);
            primeira.setNivel(primeira.getNivel() + 1);
            registrarLog("🔨 " + h.getNome() + " aprimorou [" + primeira.getNome() + "] para Nv." + primeira.getNivel() + "!");
        }
        return Map.of("ok", true);
    }

    // --- ROTAS EXCLUSIVAS DO MESTRE ---

    @PostMapping("/mestre/cenario")
    public Map<String, Object> mudarCenario(@RequestBody Map<String, String> payload) {
        this.cenarioAtual = payload.getOrDefault("cenario", "porto");
        this.cenarioTitulo = payload.getOrDefault("titulo", "Porto Pirata");
        registrarLog("🗺️ O Mestre alterou o cenário para: " + this.cenarioTitulo);
        return Map.of("ok", true);
    }

    @PostMapping("/mestre/inimigo")
    public Map<String, Object> configurarInimigo(@RequestBody Map<String, Object> payload) {
        if (payload.containsKey("nome")) inimigo.put("nome", payload.get("nome"));
        if (payload.containsKey("pvMax")) {
            int max = Integer.parseInt(payload.get("pvMax").toString());
            inimigo.put("pvMax", max);
            inimigo.put("pv", max);
        }
        if (payload.containsKey("atk")) inimigo.put("atk", Integer.parseInt(payload.get("atk").toString()));
        if (payload.containsKey("def")) inimigo.put("def", Integer.parseInt(payload.get("def").toString()));
        registrarLog("👹 Novo inimigo em campo: " + inimigo.get("nome") + " (PV " + inimigo.get("pvMax") + ")");
        return Map.of("ok", true);
    }

    @PostMapping("/mestre/ataque-inimigo")
    public Map<String, Object> ataqueInimigo(@RequestBody Map<String, String> payload) {
        Personagem alvo = herois.get(payload.get("alvoId"));
        if (alvo == null) return Map.of("erro", "Alvo não encontrado");

        int atkInimigo = (int) inimigo.get("atk");
        int d20 = ThreadLocalRandom.current().nextInt(1, 21);
        int danoBruto = atkInimigo + (d20 >= 17 ? 3 : 0);

        // Verifica Armadilha virada para baixo!
        if (!armadilhasAtivas.isEmpty()) {
            Map<String, Object> trap = armadilhasAtivas.remove(0);
            int poderTrap = (int) trap.get("poder");
            int pvInimigo = Math.max(0, (int) inimigo.get("pv") - poderTrap);
            inimigo.put("pv", pvInimigo);
            danoBruto = Math.max(0, danoBruto - poderTrap);
            registrarLog(String.format("💥 ARMADILHA REVELADA! [%s] de %s ativou: causou %d de dano ao inimigo e bloqueou %d do ataque!",
                    trap.get("nome"), trap.get("donoNome"), poderTrap, poderTrap));
        }

        int defesaTotal = alvo.getDef() + alvo.getDefTemp();
        int danoFinal = Math.max(0, danoBruto - defesaTotal);

        if (alvo.getEscudo() > 0 && danoFinal > 0) {
            int absorvido = Math.min(alvo.getEscudo(), danoFinal);
            alvo.setEscudo(alvo.getEscudo() - absorvido);
            danoFinal -= absorvido;
        }

        alvo.setPv(Math.max(0, alvo.getPv() - danoFinal));
        registrarLog(String.format("🩸 %s atacou %s (1d20=%d) causando %d de dano!", inimigo.get("nome"), alvo.getNome(), d20, danoFinal));
        return Map.of("ok", true);
    }

    @PostMapping("/mestre/improviso")
    public Map<String, Object> darImproviso(@RequestBody Map<String, Object> payload) {
        Personagem h = herois.get(payload.get("alvoId").toString());
        int bonus = Integer.parseInt(payload.get("bonus").toString());
        if (h != null) {
            h.setBonusImproviso(h.getBonusImproviso() + bonus);
            registrarLog("🎭 Improviso Narrativo: " + h.getNome() + " recebeu +" + bonus + " para a próxima rolagem!");
        }
        return Map.of("ok", true);
    }

    @PostMapping("/mestre/evoluir")
    public Map<String, Object> evoluirHeroi(@RequestBody Map<String, Object> payload) {
        Personagem h = herois.get(payload.get("alvoId").toString());
        String acao = payload.get("acao").toString();
        if (h == null) return Map.of("erro", "Herói inválido");

        switch (acao) {
            case "nivel":
                h.setNivel(h.getNivel() + 1);
                h.setPvMax(h.getPvMax() + 3);
                h.setPv(h.getPvMax());
                if (h.getNivel() % 2 == 0) h.setAtk(h.getAtk() + 1);
                else h.setDef(h.getDef() + 1);
                registrarLog("⭐ LEVEL UP! " + h.getNome() + " alcançou o Nível " + h.getNivel() + "!");
                break;
            case "ouro":
                int qtd = Integer.parseInt(payload.getOrDefault("valor", 15).toString());
                h.setOuro(h.getOuro() + qtd);
                registrarLog("💰 " + h.getNome() + " recebeu +" + qtd + " moedas de ouro!");
                break;
            case "carta":
                String cartaId = payload.get("cartaId").toString();
                h.getBaralho().stream().filter(c -> c.getId().equals(cartaId)).findFirst().ifPresent(c -> {
                    c.setNivel(c.getNivel() + 1);
                    registrarLog("📜 Carta evoluída! [" + c.getNome() + "] de " + h.getNome() + " agora é Nv." + c.getNivel() + "!");
                });
                break;
            case "fruta":
                String fNome = payload.get("frutaNome").toString();
                String fDesc = payload.get("frutaDescricao").toString();
                h.setFrutaNome(fNome);
                h.setFrutaDescricao(fDesc);
                h.setAtk(h.getAtk() + 1);
                h.setEpMax(h.getEpMax() + 1);
                h.setEp(h.getEpMax());
                registrarLog("🍎 PODER DESPERTADO! " + h.getNome() + " consumiu a " + fNome + "!");
                break;
            case "curar":
                h.setPv(h.getPvMax());
                h.setEp(h.getEpMax());
                registrarLog("💚 " + h.getNome() + " teve PV e EP totalmente restaurados pelo Mestre.");
                break;
        }
        return Map.of("ok", true);
    }

    @PostMapping("/mestre/resetar")
    public Map<String, Object> resetarBatalha() {
        for (Personagem h : herois.values()) {
            h.setPv(h.getPvMax());
            h.setEp(h.getEpMax());
            h.setEscudo(0);
            h.setAtkTemp(0);
            h.setDefTemp(0);
        }
        armadilhasAtivas.clear();
        inimigo.put("pv", inimigo.get("pvMax"));
        registrarLog("🕊️ Batalha reiniciada! Níveis, evoluções e frutas foram preservados.");
        return Map.of("ok", true);
    }
}
