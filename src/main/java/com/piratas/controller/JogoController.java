package com.piratas.controller;

import com.piratas.model.Carta;
import com.piratas.model.Personagem;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/jogo")
@CrossOrigin(origins = "*")
public class JogoController {

    private final Map<String, Personagem> herois = new LinkedHashMap<>();
    private final Map<String, Object> inimigo = new LinkedHashMap<>();
    private final List<Carta> baralhoInimigo = new ArrayList<>();
    private final List<String> logs = new ArrayList<>();
    private final List<java.util.Map<String, Object>> armadilhasAtivas = new ArrayList<>();
    private String cenarioAtual = "porto";
    private String cenarioTitulo = "Porto Pirata — Docas da Alvorada";
    private final Random random = new Random();

    public JogoController() {
        inicializarJogo();
    }

    private void inicializarJogo() {
        herois.clear();
        logs.clear();
        armadilhasAtivas.clear();
        baralhoInimigo.clear();

        Personagem lua = new Personagem("lua", "Lua", "Olhos de Águia", "Atiradora", "/imagens/lua.jpg", 18, 4, 1, 5);
        lua.getBaralho().add(new Carta("c1", "Disparo de Precisão", "ATAQUE", 2, 4, "CRITICO_VELOZ", "Tiro certeiro. Se VLC > DEF inimiga, +2 de dano.", "/imagens/carta_lua.jpg"));
        lua.getBaralho().add(new Carta("c2", "Rajada de Pólvora", "ATAQUE", 3, 6, "QUEIMADURA", "Disparo explosivo que atravessa parte da defesa.", ""));
        lua.getBaralho().add(new Carta("c3", "Cortina de Fumaça", "DEFESA", 1, 3, "ESQUIVA", "Concede +3 de Escudo e +1 VLC no turno.", ""));
        lua.getBaralho().add(new Carta("c4", "Fio Invisível de Pólvora", "ARMADILHA", 2, 5, "EXPLOSAO_OCULTA", "Carta oculta: detona quando o inimigo atacar, causando 5 de dano.", ""));
        lua.getBaralho().add(new Carta("c5", "Olhar do Horizonte", "SUPORTE", 1, 2, "FOCO", "Aumenta +2 ATK neste turno e +1 em testes de d20.", ""));

        Personagem kyan = new Personagem("kyan", "Kyan", "O Lobo Branco", "Carpinteiro", "/imagens/kyan.jpg", 20, 3, 2, 3);
        kyan.getBaralho().add(new Carta("k1", "Corte Duplo dos Machados", "ATAQUE", 2, 3, "GOLPE_DUPLO", "Golpe veloz (VLC 3). Se d20 >= 14, desfere segundo corte (+3 dano).", "/imagens/carta_kyan.jpg"));
        kyan.getBaralho().add(new Carta("k2", "Martelo & Cinzel", "ATAQUE", 1, 2, "QUEBRA_DEFESA", "Impacto de construtor que abala a guarda inimiga.", ""));
        kyan.getBaralho().add(new Carta("k3", "Barricada de Carpinteiro", "DEFESA", 2, 4, "FORTIFICAR", "Ergue vigas reforçadas: ganha +4 Escudo e +1 DEF no turno.", ""));
        kyan.getBaralho().add(new Carta("k4", "Estrepes e Vigas Ocultas", "ARMADILHA", 2, 4, "CONTRA_ATAQUE", "Carta oculta: estrutura desaba no atacante, bloqueando 4 e causando 4 de dano.", ""));
        kyan.getBaralho().add(new Carta("k5", "Pulso Rúnico Latente", "SUPORTE", 2, 3, "CURA_RUNICA", "As runas do machado aquecem com seus batimentos: cura 3 PV e dá +2 ATK.", ""));

        Personagem zafira = new Personagem("zafira", "Zafira", "Chama Escarlate", "Combatente", "/imagens/zafira.jpg", 22, 4, 2, 2);
        zafira.getBaralho().add(new Carta("z1", "Sopro Dracônico", "ATAQUE", 3, 6, "CHAMAS", "Chamas ancestrais envolvem o alvo com poder devastador.", "/imagens/carta_zafira.jpg"));
        zafira.getBaralho().add(new Carta("z2", "Garra Rubra", "ATAQUE", 1, 3, "SANGRAMENTO", "Investida feroz corpo-a-corpo.", ""));
        zafira.getBaralho().add(new Carta("z3", "Escamas de Obsidiana", "DEFESA", 2, 4, "BLOQUEIO_TOTAL", "Endurece a postura e recebe +4 de Escudo.", ""));
        zafira.getBaralho().add(new Carta("z4", "Rastro de Brasas", "ARMADILHA", 2, 5, "CHAMAS_OCULTAS", "Carta oculta: incendeia o oponente ao ser atacada.", ""));
        zafira.getBaralho().add(new Carta("z5", "Fúria Ancestral", "SUPORTE", 2, 3, "FURIA", "Desperta o sangue dracônico: +3 ATK no turno.", ""));

        herois.put("lua", lua);
        herois.put("kyan", kyan);
        herois.put("zafira", zafira);

        configurarInimigo("Capitão Morgano — Corsário de Ferro", 45, 5, 2);
        registrarLog("⚓ A batalha começou em " + cenarioTitulo + "!");
    }

    private void configurarInimigo(String nome, int pvMax, int atk, int def) {
        inimigo.clear();
        inimigo.put("nome", nome);
        inimigo.put("pv", pvMax);
        inimigo.put("pvMax", pvMax);
        inimigo.put("atk", atk);
        inimigo.put("def", def);
        inimigo.put("escudo", 0);

        baralhoInimigo.clear();
        baralhoInimigo.add(new Carta("ini1", "Sabre Corsário", "ATAQUE", 1, atk, "CORTE", "Ataque direto com lâmina pesada.", ""));
        baralhoInimigo.add(new Carta("ini2", "Disparo de Canhão", "ATAQUE", 2, atk + 3, "IMPACTO", "Tiro de pólvora que estilhaça o convés.", ""));
        baralhoInimigo.add(new Carta("ini3", "Guarda de Ferro", "DEFESA", 1, 4, "ESCUDO", "Ergue defesa corsária (+4 Escudo).", ""));
        baralhoInimigo.add(new Carta("ini4", "Saque & Intimidação", "SUPORTE", 1, 2, "FURIA_INIMIGA", "Recupera 4 PV e ganha +2 ATK.", ""));
    }

    private void registrarLog(String texto) {
        logs.add(0, texto);
        if (logs.size() > 35) logs.remove(logs.size() - 1);
    }

    @GetMapping("/estado")
    public Map<String, Object> getEstado() {
        Map<String, Object> res = new LinkedHashMap<>();
        res.put("cenario", cenarioAtual);
        res.put("cenarioTitulo", cenarioTitulo);
        res.put("herois", new ArrayList<>(herois.values()));
        res.put("inimigo", inimigo);
        res.put("baralhoInimigo", baralhoInimigo);
        res.put("armadilhasAtivas", armadilhasAtivas);
        res.put("logs", logs);
        return res;
    }

    @PostMapping("/jogar")
    public Map<String, Object> jogarCarta(@RequestBody Map<String, String> payload) {
        Personagem p = herois.get(payload.get("personagemId"));
        String cartaId = payload.get("cartaId");
        if (p == null) return Map.of("erro", "Personagem não encontrado.");

        Carta carta = p.getBaralho().stream().filter(c -> c.getId().equals(cartaId)).findFirst().orElse(null);
        if (carta == null) return Map.of("erro", "Carta não encontrada.");
        if (p.getEp() < carta.getCustoEp()) return Map.of("erro", "Energia (EP) insuficiente!");

        p.setEp(p.getEp() - carta.getCustoEp());
        int d20 = random.nextInt(20) + 1 + p.getBonusImproviso();
        p.setBonusImproviso(0);

        String resumo;
        switch (carta.getTipo()) {
            case "ATAQUE" -> {
                int defInimigo = (int) inimigo.get("def");
                int escudoIni = (int) inimigo.getOrDefault("escudo", 0);
                int poder = carta.getPoderTotal() + p.getAtk() + p.getAtkTemp();
                if ("GOLPE_DUPLO".equals(carta.getEfeitoEspecial()) && d20 >= 14) poder += 3;
                if ("CRITICO_VELOZ".equals(carta.getEfeitoEspecial()) && p.getVlc() > defInimigo) poder += 2;
                if (d20 >= 19) poder += 3;
                int danoTotal = Math.max(1, poder - defInimigo);
                if (escudoIni > 0) {
                    int absorvido = Math.min(escudoIni, danoTotal);
                    inimigo.put("escudo", escudoIni - absorvido);
                    danoTotal -= absorvido;
                }
                int pvAtual = Math.max(0, (int) inimigo.get("pv") - danoTotal);
                inimigo.put("pv", pvAtual);
                resumo = "⚔️ " + p.getNome() + " usou [" + carta.getNome() + "] (1d20=" + d20 + ") e causou " + danoTotal + " de dano!";
            }
            case "DEFESA" -> {
                int ganhoEscudo = carta.getPoderTotal();
                p.setEscudo(p.getEscudo() + ganhoEscudo);
                p.setDefTemp(p.getDefTemp() + 1);
                resumo = "🛡️ " + p.getNome() + " ativou [" + carta.getNome() + "] e ganhou +" + ganhoEscudo + " de Escudo!";
            }
            case "ARMADILHA" -> {
                Map<String, Object> trap = new LinkedHashMap<>();
                trap.put("donoId", p.getId());
                trap.put("donoNome", p.getNome());
                trap.put("cartaNome", carta.getNome());
                trap.put("poder", carta.getPoderTotal());
                armadilhasAtivas.add(trap);
                resumo = "🪤 " + p.getNome() + " posicionou uma Carta Oculta na mesa!";
            }
            default -> {
                int cura = carta.getPoderTotal();
                p.setPv(Math.min(p.getPvMax(), p.getPv() + cura));
                p.setAtkTemp(p.getAtkTemp() + 2);
                resumo = "✨ " + p.getNome() + " usou [" + carta.getNome() + "], recuperou " + cura + " PV e ganhou +2 ATK!";
            }
        }

        registrarLog(resumo);
        return Map.of("ok", true, "mensagem", resumo, "d20", d20);
    }

    @PostMapping("/mestre/jogar-carta-inimigo")
    public Map<String, Object> mestreJogarCartaInimigo(@RequestBody Map<String, String> payload) {
        String cartaId = payload.get("cartaId");
        String alvoId = payload.get("alvoId");
        Personagem alvo = herois.get(alvoId);
        if (alvo == null) return Map.of("erro", "Selecione um pirata alvo.");

        Carta carta = baralhoInimigo.stream().filter(c -> c.getId().equals(cartaId)).findFirst().orElse(null);
        if (carta == null) return Map.of("erro", "Carta inimiga inválida.");

        String msg;
        if ("DEFESA".equals(carta.getTipo())) {
            int esc = (int) inimigo.getOrDefault("escudo", 0) + carta.getValorBase();
            inimigo.put("escudo", esc);
            msg = "🛡️ " + inimigo.get("nome") + " usou [" + carta.getNome() + "] (+4 Escudo)!";
        } else if ("SUPORTE".equals(carta.getTipo())) {
            int novoPv = Math.min((int) inimigo.get("pvMax"), (int) inimigo.get("pv") + 4);
            inimigo.put("pv", novoPv);
            inimigo.put("atk", (int) inimigo.get("atk") + 2);
            msg = "💀 " + inimigo.get("nome") + " usou [" + carta.getNome() + "] (+4 PV, +2 ATK)!";
        } else {
            if (!armadilhasAtivas.isEmpty()) {
                Map<String, Object> trap = armadilhasAtivas.remove(0);
                int danoTrap = (int) trap.get("poder");
                inimigo.put("pv", Math.max(0, (int) inimigo.get("pv") - danoTrap));
                msg = "💥 ARMADILHA ATIVADA! [" + trap.get("cartaNome") + "] de " + trap.get("donoNome") + " causou " + danoTrap + " de dano!";
            } else {
                int d20 = random.nextInt(20) + 1;
                int dano = Math.max(1, carta.getValorBase() + (d20 >= 18 ? 3 : 0) - (alvo.getDef() + alvo.getDefTemp()));
                if (alvo.getEscudo() > 0) {
                    int absorvido = Math.min(alvo.getEscudo(), dano);
                    alvo.setEscudo(alvo.getEscudo() - absorvido);
                    dano -= absorvido;
                }
                alvo.setPv(Math.max(0, alvo.getPv() - dano));
                msg = "🩸 " + inimigo.get("nome") + " jogou [" + carta.getNome() + "] em " + alvo.getNome() + " (1d20=" + d20 + ") causando " + dano + " de dano!";
            }
        }
        registrarLog(msg);
        return Map.of("ok", true, "mensagem", msg);
    }

    @PostMapping("/turno")
    public Map<String, Object> encerrarTurno() {
        for (Personagem p : herois.values()) {
            p.setEp(p.getEpMax());
            p.setAtkTemp(0);
            p.setDefTemp(0);
        }
        registrarLog("🔄 Novo Turno! Energia (EP) restaurada para toda a tripulação.");
        return Map.of("ok", true);
    }

    @PostMapping("/loja")
    public Map<String, Object> comprarLoja(@RequestBody Map<String, String> payload) {
        Personagem p = herois.get(payload.get("personagemId"));
        String item = payload.get("item");
        if (p == null) return Map.of("erro", "Personagem inválido.");

        if ("pocao".equals(item)) {
            if (p.getOuro() < 15) return Map.of("erro", "Ouro insuficiente (15g).");
            p.setOuro(p.getOuro() - 15);
            p.setPv(Math.min(p.getPvMax(), p.getPv() + 6));
            registrarLog("🧪 " + p.getNome() + " bebeu um Elixir (+6 PV).");
        } else if ("tonico".equals(item)) {
            if (p.getOuro() < 15) return Map.of("erro", "Ouro insuficiente (15g).");
            p.setOuro(p.getOuro() - 15);
            p.setEp(Math.min(p.getEpMax() + 2, p.getEp() + 2));
            registrarLog("⚡ " + p.getNome() + " tomou Tônico de Fôlego (+2 EP).");
        } else if ("forja".equals(item)) {
            if (p.getOuro() < 25) return Map.of("erro", "Ouro insuficiente (25g).");
            p.setOuro(p.getOuro() - 25);
            Carta c = p.getBaralho().get(0);
            c.setNivel(c.getNivel() + 1);
            registrarLog("🔨 " + p.getNome() + " forjou [" + c.getNome() + "] para Nível " + c.getNivel() + "!");
        }
        return Map.of("ok", true);
    }

    @PostMapping("/mestre/cenario")
    public Map<String, Object> mudarCenario(@RequestBody Map<String, String> payload) {
        this.cenarioAtual = payload.getOrDefault("cenario", "porto");
        this.cenarioTitulo = switch (cenarioAtual) {
            case "conves" -> "Convés do Galeão — Tempestade em Alto-Mar";
            case "ruinas" -> "Ilha das Ruínas — Santuário Esquecido";
            case "taverna" -> "Taverna Subterrânea — Porto dos Renegados";
            default -> "Porto Pirata — Docas da Alvorada";
        };
        registrarLog("🗺️ O Mestre alterou o cenário para: " + cenarioTitulo);
        return Map.of("ok", true);
    }

    @PostMapping("/mestre/novo-inimigo")
    public Map<String, Object> novoInimigo(@RequestBody Map<String, Object> payload) {
        String nome = payload.getOrDefault("nome", "Corsário Misterioso").toString();
        int pv = Integer.parseInt(payload.getOrDefault("pv", 40).toString());
        int atk = Integer.parseInt(payload.getOrDefault("atk", 4).toString());
        int def = Integer.parseInt(payload.getOrDefault("def", 2).toString());
        configurarInimigo(nome, pv, atk, def);
        registrarLog("💀 Novo oponente surgiu: " + nome + " (PV " + pv + ")!");
        return Map.of("ok", true);
    }

    @PostMapping("/mestre/heroi")
    public Map<String, Object> ajustarHeroi(@RequestBody Map<String, Object> payload) {
        Personagem p = herois.get(payload.get("personagemId"));
        if (p == null) return Map.of("erro", "Herói inválido.");
        String acao = payload.get("acao").toString();
        switch (acao) {
            case "ouro" -> { p.setOuro(p.getOuro() + 15); registrarLog("💰 Mestre concedeu +15g para " + p.getNome()); }
            case "nivel" -> {
                p.setNivel(p.getNivel() + 1);
                p.setPvMax(p.getPvMax() + 4);
                p.setPv(p.getPvMax());
                p.setAtk(p.getAtk() + 1);
                registrarLog("⭐ " + p.getNome() + " subiu para o Nível " + p.getNivel() + "!");
            }
            case "fruta" -> {
                String fNome = payload.getOrDefault("frutaNome", "Fruta Mística").toString();
                p.setFrutaNome(fNome);
                p.setAtk(p.getAtk() + 2);
                registrarLog("🍎 " + p.getNome() + " despertou [" + fNome + "] (+2 ATK permanente)!");
            }
            case "curar" -> { p.setPv(p.getPvMax()); p.setEp(p.getEpMax()); registrarLog("✨ " + p.getNome() + " teve PV e EP restaurados!"); }
        }
        return Map.of("ok", true);
    }

    @PostMapping("/mestre/restaurar-save")
    @SuppressWarnings("unchecked")
    public Map<String, Object> restaurarSave(@RequestBody Map<String, Object> save) {
        if (save == null || !save.containsKey("herois")) return Map.of("erro", "Save inválido.");
        if (save.containsKey("cenario")) this.cenarioAtual = save.get("cenario").toString();
        if (save.containsKey("cenarioTitulo")) this.cenarioTitulo = save.get("cenarioTitulo").toString();

        if (save.get("inimigo") instanceof Map) {
            this.inimigo.clear();
            this.inimigo.putAll((Map<String, Object>) save.get("inimigo"));
        }
        if (save.get("herois") instanceof List) {
            for (Map<String, Object> dadosH : (List>) save.get("herois")) {
                Personagem p = herois.get(dadosH.get("id").toString());
                if (p != null) {
                    p.setNivel(Integer.parseInt(dadosH.get("nivel").toString()));
                    p.setOuro(Integer.parseInt(dadosH.get("ouro").toString()));
                    p.setPv(Integer.parseInt(dadosH.get("pv").toString()));
                    p.setPvMax(Integer.parseInt(dadosH.get("pvMax").toString()));
                    p.setEp(Integer.parseInt(dadosH.get("ep").toString()));
                    p.setEpMax(Integer.parseInt(dadosH.get("epMax").toString()));
                    p.setAtk(Integer.parseInt(dadosH.get("atk").toString()));
                    p.setDef(Integer.parseInt(dadosH.get("def").toString()));
                    p.setEscudo(Integer.parseInt(dadosH.get("escudo").toString()));
                    p.setFrutaNome(String.valueOf(dadosH.getOrDefault("frutaNome", "")));
                    if (dadosH.get("baralho") instanceof List) {
                        for (Map<String, Object> cDados : (List>) dadosH.get("baralho")) {
                            p.getBaralho().stream()
                                .filter(c -> c.getId().equals(cDados.get("id").toString()))
                                .findFirst()
                                .ifPresent(c -> c.setNivel(Integer.parseInt(cDados.get("nivel").toString())));
                        }
                    }
                }
            }
        }
        registrarLog("📖 Crônica restaurada! Progresso da tripulação carregado.");
        return Map.of("ok", true);
    }

    @PostMapping("/mestre/resetar")
    public Map<String, Object> resetar() {
        inicializarJogo();
        return Map.of("ok", true);
    }
}
