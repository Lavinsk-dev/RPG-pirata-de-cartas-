package com.piratas.controller;

import com.piratas.model.Carta;
import com.piratas.model.Personagem;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/jogo")
@CrossOrigin(origins = "*")
@SuppressWarnings({"unchecked", "rawtypes"})
public class JogoController {

    private final Map herois = new LinkedHashMap();
    private final List inimigos = new ArrayList();
    private final List ordemTurnos = new ArrayList();
    private int indiceTurnoAtual = 0;
    private int numeroRodada = 1;
    private String faseJogo = "EXPLORACAO";

    private final List logs = new ArrayList();
    private final List armadilhasAtivas = new ArrayList();
    private final List cartasNaMesa = new ArrayList();
    private final Set improvisosUsadosBatalha = new HashSet();
    private Map solicitacaoImprovisoAtiva = null;
    private Map ultimoEventoVisual = new LinkedHashMap();
    private long contadorEventos = 0;

    private String cenarioAtual = "porto";
    private String cenarioTitulo = "Porto Pirata — Docas da Alvorada";
    private final Random random = new Random();

    public JogoController() {
        inicializarJogo();
    }

    private void inicializarJogo() {
        herois.clear();
        inimigos.clear();
        ordemTurnos.clear();
        logs.clear();
        armadilhasAtivas.clear();
        cartasNaMesa.clear();
        improvisosUsadosBatalha.clear();
        solicitacaoImprovisoAtiva = null;
        ultimoEventoVisual.clear();
        indiceTurnoAtual = 0;
        numeroRodada = 1;
        faseJogo = "EXPLORACAO";

        Personagem lua = new Personagem("lua", "Lua", "Olhos de Águia", "Atiradora", "/imagens/lua.jpg", 18, 4, 1, 5);
        lua.getBaralho().add(new Carta("c1", "Disparo de Precisão", "ATAQUE", 2, 4, "CRITICO_VELOZ", "Tiro certeiro de longa distância com chance de dano crítico.", "/imagens/carta_lua.jpg"));
        lua.getBaralho().add(new Carta("c2", "Rajada de Pólvora", "ATAQUE", 3, 6, "QUEIMADURA", "Disparo explosivo que atravessa parte da defesa inimiga.", ""));
        lua.getBaralho().add(new Carta("c3", "Cortina de Fumaça", "DEFESA", 1, 3, "ESQUIVA", "Concede +3 de Escudo e +1 VLC no turno.", ""));
        lua.getBaralho().add(new Carta("c4", "Fio Invisível de Pólvora", "ARMADILHA", 2, 5, "EXPLOSAO_OCULTA", "Carta oculta: detona quando um inimigo atacar, causando 5 de dano.", ""));
        lua.getBaralho().add(new Carta("c5", "Olhar do Horizonte", "SUPORTE", 1, 2, "FOCO", "Cura 2 PV, dá +2 ATK no turno e +2 de bônus para Improviso.", ""));

        Personagem kyan = new Personagem("kyan", "Kyan", "O Lobo Branco", "Carpinteiro", "/imagens/kyan.jpg", 20, 3, 2, 3);
        kyan.getBaralho().add(new Carta("k1", "Corte Duplo dos Machados", "ATAQUE", 2, 3, "GOLPE_DUPLO", "Investida dupla veloz com chance de segundo corte (+3 dano).", "/imagens/carta_kyan.jpg"));
        kyan.getBaralho().add(new Carta("k2", "Martelo & Cinzel", "ATAQUE", 1, 2, "QUEBRA_DEFESA", "Impacto de construtor que abala a guarda inimiga.", ""));
        kyan.getBaralho().add(new Carta("k3", "Barricada de Carpinteiro", "DEFESA", 2, 4, "FORTIFICAR", "Ergue vigas reforçadas: ganha +4 Escudo e +1 DEF no turno.", ""));
        kyan.getBaralho().add(new Carta("k4", "Estrepes e Vigas Ocultas", "ARMADILHA", 2, 4, "CONTRA_ATAQUE", "Carta oculta: estrutura desaba no atacante, bloqueando e causando 4 de dano.", ""));
        kyan.getBaralho().add(new Carta("k5", "Pulso Rúnico Latente", "SUPORTE", 2, 3, "CURA_RUNICA", "As runas aquecem: cura 3 PV, +2 ATK e +2 de bônus para Improviso.", ""));

        Personagem zafira = new Personagem("zafira", "Zafira", "Chama Escarlate", "Combatente", "/imagens/zafira.jpg", 22, 4, 2, 2);
        zafira.getBaralho().add(new Carta("z1", "Sopro Dracônico", "ATAQUE", 3, 6, "CHAMAS", "Labaredas ancestrais que incineram o oponente (+3 dano em crítico).", "/imagens/carta_zafira.jpg"));
        zafira.getBaralho().add(new Carta("z2", "Garra Rubra", "ATAQUE", 1, 3, "SANGRAMENTO", "Investida feroz corpo-a-corpo.", ""));
        zafira.getBaralho().add(new Carta("z3", "Escamas de Obsidiana", "DEFESA", 2, 4, "BLOQUEIO_TOTAL", "Endurece a postura e recebe +4 de Escudo.", ""));
        zafira.getBaralho().add(new Carta("z4", "Rastro de Brasas", "ARMADILHA", 2, 5, "CHAMAS_OCULTAS", "Carta oculta: incendeia o oponente ao atacar.", ""));
        zafira.getBaralho().add(new Carta("z5", "Fúria Ancestral", "SUPORTE", 2, 3, "FURIA", "Desperta o sangue dracônico: cura 3 PV, +3 ATK e +2 para Improviso.", ""));

        herois.put("lua", lua);
        herois.put("kyan", kyan);
        herois.put("zafira", zafira);

        adicionarInimigoInterno("Capitão Morgano — Corsário", 45, 5, 2, 4, "corsario");
        recalcularOrdemTurnos();
        registrarLog("🧭 Tripulação desembarcou em " + cenarioTitulo + ". Fase de Exploração ativa!");
    }

    private void registrarEventoVisual(String tipo, String efeitoEspecial, String autorId, String autorNome,
                                       String alvoId, String alvoNome, String cartaNome,
                                       String valorDestaque, String mensagem) {
        contadorEventos++;
        Map ev = new LinkedHashMap();
        ev.put("id", contadorEventos);
        ev.put("tipo", tipo);
        ev.put("efeitoEspecial", efeitoEspecial != null ? efeitoEspecial : "");
        ev.put("autorId", autorId != null ? autorId : "");
        ev.put("autor", autorNome != null ? autorNome : "");
        ev.put("alvoId", alvoId != null ? alvoId : "");
        ev.put("alvo", alvoNome != null ? alvoNome : "");
        ev.put("cartaNome", cartaNome != null ? cartaNome : "");
        ev.put("valorDestaque", valorDestaque != null ? valorDestaque : "");
        ev.put("mensagem", mensagem != null ? mensagem : "");
        this.ultimoEventoVisual = ev;
    }

    private void adicionarCartaMesa(String autor, String alvo, String cartaNome, String tipo, String efeito) {
        Map c = new LinkedHashMap();
        c.put("autor", autor);
        c.put("alvo", alvo);
        c.put("nome", cartaNome);
        c.put("tipo", tipo);
        c.put("efeito", efeito);
        cartasNaMesa.add(c);
        if (cartasNaMesa.size() > 5) cartasNaMesa.remove(0);
    }

    private List gerarBaralhoInimigo(String arquetipo, int atk) {
        List deck = new ArrayList();
        switch (arquetipo) {
            case "atirador" -> {
                deck.add(new Carta("ia1", "Tiro de Mosquete", "ATAQUE", 1, atk + 1, "TIRO", "Disparo rápido de longa distância.", ""));
                deck.add(new Carta("ia2", "Saraivada de Canhão", "ATAQUE", 2, atk + 4, "CANHAO", "Bala explosiva que devasta o convés.", ""));
                deck.add(new Carta("ia3", "Cobertura de Barris", "DEFESA", 1, 4, "ESCUDO", "Protege-se atrás da barricada (+4 Escudo).", ""));
                deck.add(new Carta("ia4", "Mira Telescópica", "SUPORTE", 1, 2, "BUFF", "Ganha +3 ATK para o próximo disparo.", ""));
            }
            case "fera" -> {
                deck.add(new Carta("if1", "Mordida Abissal", "ATAQUE", 1, atk + 2, "MORDIDA", "Investida selvagem com presas.", ""));
                deck.add(new Carta("if2", "Esmagamento de Tentáculo", "ATAQUE", 2, atk + 4, "ESMAGAR", "Golpe colossal que treme o navio.", ""));
                deck.add(new Carta("if3", "Carapaça Marinheira", "DEFESA", 1, 5, "ESCUDO", "Endurece escamas (+5 Escudo).", ""));
                deck.add(new Carta("if4", "Regeneração das Profundezas", "SUPORTE", 1, 5, "CURA", "Recupera 6 PV e ruge (+1 ATK).", ""));
            }
            case "arcano" -> {
                deck.add(new Carta("im1", "Lança de Água Negra", "ATAQUE", 1, atk + 1, "MAGIA", "Projétil amaldiçoado das marés.", ""));
                deck.add(new Carta("im2", "Maelstrom Sombrio", "ATAQUE", 2, atk + 4, "TEMPESTADE", "Vórtice arcano de alta pressão.", ""));
                deck.add(new Carta("im3", "Barreira de Névoa", "DEFESA", 1, 4, "ESCUDO", "Vórtice protetor (+4 Escudo).", ""));
                deck.add(new Carta("im4", "Drenar Vitalidade", "SUPORTE", 1, 3, "DRENO", "Absorve energia: cura 5 PV e +2 ATK.", ""));
            }
            default -> {
                deck.add(new Carta("ic1", "Sabre Corsário", "ATAQUE", 1, atk, "CORTE", "Ataque direto com lâmina pesada.", ""));
                deck.add(new Carta("ic2", "Golpe Traiçoeiro", "ATAQUE", 2, atk + 3, "CRITICO", "Estocada brutal nas brechas da guarda.", ""));
                deck.add(new Carta("ic3", "Guarda de Ferro", "DEFESA", 1, 4, "ESCUDO", "Ergue defesa corsária (+4 Escudo).", ""));
                deck.add(new Carta("ic4", "Grito de Abordagem", "SUPORTE", 1, 2, "FURIA", "Recupera 4 PV e ganha +2 ATK.", ""));
            }
        }
        return deck;
    }

    private void adicionarInimigoInterno(String nome, int pvMax, int atk, int def, int vlc, String arquetipo) {
        String id = "ini_" + (inimigos.size() + 1) + "_" + (random.nextInt(900) + 100);
        Map ini = new LinkedHashMap();
        ini.put("id", id);
        ini.put("nome", nome);
        ini.put("pv", pvMax);
        ini.put("pvMax", pvMax);
        ini.put("atk", atk);
        ini.put("def", def);
        ini.put("vlc", vlc);
        ini.put("escudo", 0);
        ini.put("arquetipo", arquetipo);
        ini.put("baralho", gerarBaralhoInimigo(arquetipo, atk));
        inimigos.add(ini);
    }

    private void recalcularOrdemTurnos() {
        String idAtual = null;
        if (!ordemTurnos.isEmpty() && indiceTurnoAtual < ordemTurnos.size()) {
            Map atual = (Map) ordemTurnos.get(indiceTurnoAtual);
            idAtual = atual.get("id").toString();
        }

        List novaFila = new ArrayList();
        for (Object obj : herois.values()) {
            Personagem p = (Personagem) obj;
            if (p.getPv() > 0) {
                Map item = new LinkedHashMap();
                item.put("id", p.getId());
                item.put("nome", p.getNome());
                item.put("tipo", "HEROI");
                item.put("vlc", Math.max(1, p.getVlc()));
                item.put("imagemUrl", p.getImagemUrl());
                novaFila.add(item);
            }
        }
        for (Object obj : inimigos) {
            Map ini = (Map) obj;
            if ((int) ini.get("pv") > 0) {
                Map item = new LinkedHashMap();
                item.put("id", ini.get("id"));
                item.put("nome", ini.get("nome"));
                item.put("tipo", "INIMIGO");
                item.put("vlc", Math.max(1, (int) ini.get("vlc")));
                item.put("imagemUrl", "");
                novaFila.add(item);
            }
        }

        novaFila.sort((a, b) -> {
            Map mA = (Map) a;
            Map mB = (Map) b;
            return Integer.compare((int) mB.get("vlc"), (int) mA.get("vlc"));
        });
        ordemTurnos.clear();
        ordemTurnos.addAll(novaFila);

        if (ordemTurnos.isEmpty()) {
            indiceTurnoAtual = 0;
        } else if (idAtual != null) {
            int idxEncontrado = 0;
            for (int i = 0; i < ordemTurnos.size(); i++) {
                Map m = (Map) ordemTurnos.get(i);
                if (m.get("id").equals(idAtual)) {
                    idxEncontrado = i;
                    break;
                }
            }
            indiceTurnoAtual = idxEncontrado;
        } else {
            indiceTurnoAtual = 0;
        }
    }

    private Map getTurnoAtualObj() {
        if (ordemTurnos.isEmpty()) recalcularOrdemTurnos();
        if (ordemTurnos.isEmpty()) return Map.of("id", "", "nome", "Ninguém", "tipo", "HEROI");
        if (indiceTurnoAtual >= ordemTurnos.size()) indiceTurnoAtual = 0;
        return (Map) ordemTurnos.get(indiceTurnoAtual);
    }

    private String getNomeTurnoAtual() {
        return getTurnoAtualObj().get("nome").toString();
    }

    @GetMapping("/estado")
    public Map getEstado() {
        recalcularOrdemTurnos();
        Map res = new LinkedHashMap();
        res.put("faseJogo", faseJogo);
        res.put("cenario", cenarioAtual);
        res.put("cenarioTitulo", cenarioTitulo);
        res.put("numeroRodada", numeroRodada);
        res.put("ordemTurnos", ordemTurnos);
        res.put("turnoAtual", getTurnoAtualObj());
        res.put("herois", new ArrayList(herois.values()));
        res.put("inimigos", inimigos);
        res.put("armadilhasAtivas", armadilhasAtivas);
        res.put("cartasNaMesa", cartasNaMesa);
        res.put("improvisosUsadosBatalha", new ArrayList(improvisosUsadosBatalha));
        res.put("solicitacaoImprovisoAtiva", solicitacaoImprovisoAtiva);
        res.put("ultimoEventoVisual", ultimoEventoVisual);
        res.put("logs", logs);
        return res;
    }

    @PostMapping("/iniciar-combate")
    public Map iniciarCombate(@RequestBody(required = false) Map payload) {
        if (inimigos.isEmpty()) {
            adicionarInimigoInterno("Capitão Morgano — Corsário", 45, 5, 2, 4, "corsario");
        }
        recalcularOrdemTurnos();
        cartasNaMesa.clear();
        improvisosUsadosBatalha.clear();
        solicitacaoImprovisoAtiva = null;
        this.faseJogo = "COMBATE";
        this.numeroRodada = 1;
        this.indiceTurnoAtual = 0;

        String iniciadorId = (payload != null && payload.get("iniciadorId") != null)
                ? payload.get("iniciadorId").toString()
                : "";

        if (!iniciadorId.isEmpty() && herois.containsKey(iniciadorId)) {
            for (int i = 0; i < ordemTurnos.size(); i++) {
                Map item = (Map) ordemTurnos.get(i);
                if (iniciadorId.equals(item.get("id"))) {
                    Map primeiro = (Map) ordemTurnos.remove(i);
                    ordemTurnos.add(0, primeiro);
                    indiceTurnoAtual = 0;
                    break;
                }
            }
            Personagem iniP = (Personagem) herois.get(iniciadorId);
            String msg = "⚔️ EMBOSCADA! " + iniP.getNome() + " puxou o combate e age primeiro!";
            registrarLog(msg);
            registrarEventoVisual("SUPORTE", "FOCO", iniP.getId(), iniP.getNome(), "", "", "Emboscada Surpresa", "1º Turno!", msg);
        } else {
            String msg = "⚔️ COMBATE INICIADO! Ordem por VLC. Turno de: " + getNomeTurnoAtual();
            registrarLog(msg);
            registrarEventoVisual("SUPORTE", "FOCO", "", getNomeTurnoAtual(), "", "", "Batalha Iniciada", "Iniciativa VLC", msg);
        }

        return Map.of("ok", true, "turnoAtual", getTurnoAtualObj());
    }

    @PostMapping("/voltar-exploracao")
    public Map voltarExploracao() {
        this.faseJogo = "EXPLORACAO";
        cartasNaMesa.clear();
        improvisosUsadosBatalha.clear();
        solicitacaoImprovisoAtiva = null;
        for (Object obj : herois.values()) {
            Personagem p = (Personagem) obj;
            p.setEp(p.getEpMax());
            p.setAtkTemp(0);
            p.setDefTemp(0);
            p.setEscudo(0);
        }
        registrarLog("🧭 Combate encerrado! Tripulação retornou à Fase de Exploração.");
        return Map.of("ok", true);
    }

    @PostMapping("/jogar")
    public Map jogarCarta(@RequestBody Map payload) {
        String personagemId = payload.get("personagemId").toString();
        String cartaId = payload.get("cartaId").toString();
        String inimigoAlvoId = payload.getOrDefault("inimigoAlvoId", "").toString();

        Map turno = getTurnoAtualObj();
        if (!personagemId.equals(turno.get("id"))) {
            return Map.of("erro", "Aguarde sua vez! Turno atual: " + turno.get("nome"));
        }

        Personagem p = (Personagem) herois.get(personagemId);
        if (p == null) return Map.of("erro", "Personagem não encontrado.");

        Carta carta = p.getBaralho().stream().filter(c -> c.getId().equals(cartaId)).findFirst().orElse(null);
        if (carta == null) return Map.of("erro", "Carta não encontrada.");
        if (p.getEp() < carta.getCustoEp()) return Map.of("erro", "Energia (EP) insuficiente!");

        p.setEp(p.getEp() - carta.getCustoEp());
        int rolagemInterna = random.nextInt(20) + 1;

        String resumo;
        String alvoId = "";
        String alvoNome = "";
        String efeitoTexto = "";

        switch (carta.getTipo()) {
            case "ATAQUE" -> {
                Map alvoIni = null;
                for (Object obj : inimigos) {
                    Map i = (Map) obj;
                    if (i.get("id").equals(inimigoAlvoId) && (int) i.get("pv") > 0) {
                        alvoIni = i;
                        break;
                    }
                }
                if (alvoIni == null) {
                    for (Object obj : inimigos) {
                        Map i = (Map) obj;
                        if ((int) i.get("pv") > 0) {
                            alvoIni = i;
                            break;
                        }
                    }
                }

                if (alvoIni == null) return Map.of("erro", "Não há inimigos vivos na arena!");

                alvoId = alvoIni.get("id").toString();
                alvoNome = alvoIni.get("nome").toString();
                int defInimigo = (int) alvoIni.get("def");
                int escudoIni = (int) alvoIni.getOrDefault("escudo", 0);
                int poder = carta.getPoderTotal() + p.getAtk() + p.getAtkTemp();

                boolean critico = false;
                if ("GOLPE_DUPLO".equals(carta.getEfeitoEspecial()) && rolagemInterna >= 14) { poder += 3; critico = true; }
                if ("CRITICO_VELOZ".equals(carta.getEfeitoEspecial()) && rolagemInterna >= 14) { poder += 2; critico = true; }
                if ("CHAMAS".equals(carta.getEfeitoEspecial()) && rolagemInterna >= 15) { poder += 3; critico = true; }

                int danoTotal = Math.max(1, poder - defInimigo);
                if (escudoIni > 0) {
                    int absorvido = Math.min(escudoIni, danoTotal);
                    alvoIni.put("escudo", escudoIni - absorvido);
                    danoTotal -= absorvido;
                }
                int pvRestante = Math.max(0, (int) alvoIni.get("pv") - danoTotal);
                alvoIni.put("pv", pvRestante);

                efeitoTexto = "-" + danoTotal + " PV" + (critico ? " CRÍTICO!" : "");
                resumo = "⚔️ " + p.getNome() + " atacou " + alvoNome + " com [" + carta.getNome() + "] (" + efeitoTexto + ")!";
                if (pvRestante == 0) resumo += " 💀 Derrotado!";
            }
            case "DEFESA" -> {
                int ganhoEscudo = carta.getPoderTotal();
                p.setEscudo(p.getEscudo() + ganhoEscudo);
                p.setDefTemp(p.getDefTemp() + 1);
                alvoId = p.getId();
                alvoNome = p.getNome();
                efeitoTexto = "+" + ganhoEscudo + " Escudo";
                resumo = "🛡️ " + p.getNome() + " ativou [" + carta.getNome() + "] (" + efeitoTexto + ")!";
            }
            case "ARMADILHA" -> {
                Map trap = new LinkedHashMap();
                trap.put("donoId", p.getId());
                trap.put("donoNome", p.getNome());
                trap.put("cartaNome", carta.getNome());
                trap.put("poder", carta.getPoderTotal());
                armadilhasAtivas.add(trap);
                alvoNome = "Emboscada Oculta";
                efeitoTexto = carta.getPoderTotal() + " Dano Oculto";
                resumo = "🪤 " + p.getNome() + " armou uma Carta Oculta na mesa!";
            }
            default -> {
                int cura = carta.getPoderTotal();
                p.setPv(Math.min(p.getPvMax(), p.getPv() + cura));
                p.setAtkTemp(p.getAtkTemp() + 2);
                p.setBonusImproviso(p.getBonusImproviso() + 2);
                alvoId = p.getId();
                alvoNome = p.getNome();
                efeitoTexto = "+" + cura + " PV, +2 ATK, +2 Improviso";
                resumo = "✨ " + p.getNome() + " usou [" + carta.getNome() + "] (" + efeitoTexto + ")!";
            }
        }

        adicionarCartaMesa(p.getNome(), alvoNome, carta.getNome(), carta.getTipo(), efeitoTexto);
        registrarEventoVisual(carta.getTipo(), carta.getEfeitoEspecial(), p.getId(), p.getNome(),
                              alvoId, alvoNome, carta.getNome(), efeitoTexto, resumo);
        recalcularOrdemTurnos();
        registrarLog(resumo);
        return Map.of("ok", true, "mensagem", resumo);
    }

    @PostMapping("/mestre/jogar-carta-inimigo")
    public Map mestreJogarCartaInimigo(@RequestBody Map payload) {
        String inimigoId = payload.get("inimigoId").toString();
        String cartaId = payload.get("cartaId").toString();
        String alvoId = payload.get("alvoId").toString();

        Map turno = getTurnoAtualObj();
        if (!inimigoId.equals(turno.get("id"))) {
            return Map.of("erro", "Não é a vez deste inimigo! Turno atual: " + turno.get("nome"));
        }

        Map iniAtivo = null;
        for (Object obj : inimigos) {
            Map i = (Map) obj;
            if (i.get("id").equals(inimigoId)) {
                iniAtivo = i;
                break;
            }
        }
        if (iniAtivo == null) return Map.of("erro", "Inimigo não encontrado.");

        Personagem alvo = (Personagem) herois.get(alvoId);
        if (alvo == null) return Map.of("erro", "Selecione um pirata alvo.");

        List deck = (List) iniAtivo.get("baralho");
        Carta carta = null;
        for (Object cObj : deck) {
            Carta c = (Carta) cObj;
            if (c.getId().equals(cartaId)) {
                carta = c;
                break;
            }
        }
        if (carta == null) return Map.of("erro", "Carta inimiga inválida.");

        String autorNome = iniAtivo.get("nome").toString();
        String msg;
        String alvoFinalId;
        String alvoFinalNome;
        String efeitoTexto;

        if ("DEFESA".equals(carta.getTipo())) {
            int esc = (int) iniAtivo.getOrDefault("escudo", 0) + carta.getValorBase();
            iniAtivo.put("escudo", esc);
            alvoFinalId = inimigoId;
            alvoFinalNome = autorNome;
            efeitoTexto = "+" + carta.getValorBase() + " Escudo";
            msg = "🛡️ INIMIGO: " + autorNome + " usou [" + carta.getNome() + "] (" + efeitoTexto + ")!";
        } else if ("SUPORTE".equals(carta.getTipo())) {
            int novoPv = Math.min((int) iniAtivo.get("pvMax"), (int) iniAtivo.get("pv") + 5);
            iniAtivo.put("pv", novoPv);
            iniAtivo.put("atk", (int) iniAtivo.get("atk") + 2);
            alvoFinalId = inimigoId;
            alvoFinalNome = autorNome;
            efeitoTexto = "+5 PV, +2 ATK";
            msg = "💀 INIMIGO: " + autorNome + " usou [" + carta.getNome() + "] (" + efeitoTexto + ")!";
        } else {
            if (!armadilhasAtivas.isEmpty()) {
                Map trap = (Map) armadilhasAtivas.remove(0);
                int danoTrap = (int) trap.get("poder");
                iniAtivo.put("pv", Math.max(0, (int) iniAtivo.get("pv") - danoTrap));
                alvoFinalId = inimigoId;
                alvoFinalNome = autorNome;
                efeitoTexto = "Armadilha -" + danoTrap + " PV!";
                msg = "💥 ARMADILHA ATIVADA! [" + trap.get("cartaNome") + "] de " + trap.get("donoNome") + " causou " + danoTrap + " de dano em " + autorNome + "!";
            } else {
                int dano = Math.max(1, carta.getValorBase() - (alvo.getDef() + alvo.getDefTemp()));
                if (alvo.getEscudo() > 0) {
                    int absorvido = Math.min(alvo.getEscudo(), dano);
                    alvo.setEscudo(alvo.getEscudo() - absorvido);
                    dano -= absorvido;
                }
                alvo.setPv(Math.max(0, alvo.getPv() - dano));
                alvoFinalId = alvo.getId();
                alvoFinalNome = alvo.getNome();
                efeitoTexto = "-" + dano + " PV";
                msg = "🩸 ATAQUE INIMIGO! " + autorNome + " atacou " + alvo.getNome() + " com [" + carta.getNome() + "] (" + efeitoTexto + ")!";
            }
        }

        adicionarCartaMesa(autorNome, alvoFinalNome, carta.getNome(), "INIMIGO", efeitoTexto);
        registrarEventoVisual("INIMIGO", carta.getEfeitoEspecial(), inimigoId, autorNome,
                              alvoFinalId, alvoFinalNome, carta.getNome(), efeitoTexto, msg);
        recalcularOrdemTurnos();
        registrarLog(msg);
        return Map.of("ok", true, "mensagem", msg);
    }

    // JOGADOR SOLICITA IMPROVISO NARRATIVO (1 VEZ POR BATALHA)
    @PostMapping("/solicitar-improviso")
    public Map solicitarImproviso(@RequestBody Map payload) {
        String personagemId = payload.get("personagemId").toString();
        String descricao = payload.getOrDefault("descricao", "Ação Criativa").toString();
        Personagem p = (Personagem) herois.get(personagemId);
        if (p == null) return Map.of("erro", "Pirata inválido.");

        if ("COMBATE".equals(faseJogo) && improvisosUsadosBatalha.contains(personagemId)) {
            return Map.of("erro", p.getNome() + " já utilizou seu Improviso Narrativo nesta batalha!");
        }

        if ("COMBATE".equals(faseJogo)) {
            improvisosUsadosBatalha.add(personagemId);
        }

        Map req = new LinkedHashMap();
        req.put("personagemId", p.getId());
        req.put("personagemNome", p.getNome());
        req.put("descricao", descricao);
        req.put("bonusAcumulado", p.getBonusImproviso());
        this.solicitacaoImprovisoAtiva = req;

        String msg = "🎭 " + p.getNome() + " solicitou Improviso Narrativo: \"" + descricao + "\"" +
                     (p.getBonusImproviso() > 0 ? " (Possui +" + p.getBonusImproviso() + " de bônus de cartas!)" : "");
        registrarLog(msg);
        return Map.of("ok", true, "solicitacao", req);
    }

    // MESTRE RESOLVE O IMPROVISO COM O VALOR DO D20 FÍSICO + BÔNUS DE CARTAS
    @PostMapping("/mestre/improviso")
    public Map calcularImproviso(@RequestBody Map payload) {
        String personagemId = payload.get("personagemId").toString();
        int dadoFisico = Integer.parseInt(payload.get("dadoFisico").toString());
        String atributo = payload.getOrDefault("atributo", "VLC").toString();
        String descricao = payload.getOrDefault("descricao", "Ação de Improviso").toString();

        Personagem p = (Personagem) herois.get(personagemId);
        if (p == null) return Map.of("erro", "Pirata inválido.");

        int bonusAtributo = switch (atributo) {
            case "ATK" -> p.getAtk() + p.getAtkTemp();
            case "DEF" -> p.getDef() + p.getDefTemp();
            case "VLC" -> p.getVlc();
            default -> 0;
        };
        int bonusCartas = p.getBonusImproviso();
        int total = dadoFisico + bonusAtributo + bonusCartas;
        p.setBonusImproviso(0);
        this.solicitacaoImprovisoAtiva = null;

        String sucesso = total >= 15 ? "✅ SUCESSO ÉPICO!" : (total >= 10 ? "☑️ SUCESSO!" : "❌ FALHA!");
        String msg = "🎲 Improviso de " + p.getNome() + " [" + descricao + "]: d20 (" + dadoFisico +
                     ") + " + atributo + " (" + bonusAtributo + ")" +
                     (bonusCartas > 0 ? " + Cartas (+" + bonusCartas + ")" : "") +
                     " = " + total + " → " + sucesso;
        registrarEventoVisual("SUPORTE", "FOCO", p.getId(), p.getNome(), p.getId(), p.getNome(),
                              "Improviso: " + descricao, "Total: " + total, msg);
        registrarLog(msg);
        return Map.of("ok", true, "mensagem", msg, "total", total);
    }

    // MESTRE AJUSTA PV OU ATRIBUTOS (BUFFS/DEBUFFS) DE JOGADORES OU INIMIGOS
    @PostMapping("/mestre/ajustar-entidade")
    public Map ajustarEntidade(@RequestBody Map payload) {
        String alvoId = payload.get("alvoId").toString();
        String tipoAjuste = payload.get("tipoAjuste").toString(); // pv, pvMax, atk, def, vlc
        int valor = Integer.parseInt(payload.getOrDefault("valor", 0).toString());

        // 1. Verifica se é um Herói
        if (herois.containsKey(alvoId)) {
            Personagem p = (Personagem) herois.get(alvoId);
            String msg;
            switch (tipoAjuste) {
                case "pv" -> {
                    p.setPv(Math.max(0, Math.min(p.getPvMax(), p.getPv() + valor)));
                    msg = (valor >= 0 ? "💚 Mestre curou +" : "💔 Mestre aplicou ") + valor + " PV em " + p.getNome() + " (" + p.getPv() + "/" + p.getPvMax() + ").";
                    registrarEventoVisual(valor >= 0 ? "SUPORTE" : "INIMIGO", "FOCO", "", "Mestre", p.getId(), p.getNome(),
                                          valor >= 0 ? "Cura Narrativa" : "Dano Narrativo", (valor >= 0 ? "+" : "") + valor + " PV", msg);
                }
                case "pvMax" -> {
                    p.setPv(p.getPvMax());
                    p.setEp(p.getEpMax());
                    msg = "✨ " + p.getNome() + " teve PV e EP restaurados ao máximo!";
                    registrarEventoVisual("SUPORTE", "FOCO", "", "Mestre", p.getId(), p.getNome(), "Restauração Total", "PV & EP MÁX", msg);
                }
                case "atk" -> {
                    p.setAtkTemp(p.getAtkTemp() + valor);
                    msg = (valor >= 0 ? "⚔️ Buff em " : "🔻 Debuff em ") + p.getNome() + ": " + (valor >= 0 ? "+" : "") + valor + " ATK no combate!";
                }
                case "def" -> {
                    p.setDefTemp(p.getDefTemp() + valor);
                    msg = (valor >= 0 ? "🛡️ Buff em " : "🔻 Debuff em ") + p.getNome() + ": " + (valor >= 0 ? "+" : "") + valor + " DEF no combate!";
                }
                case "vlc" -> {
                    p.setVlc(Math.max(1, p.getVlc() + valor));
                    msg = (valor >= 0 ? "💨 Buff em " : "🔻 Debuff em ") + p.getNome() + ": " + (valor >= 0 ? "+" : "") + valor + " VLC (Nova VLC: " + p.getVlc() + ")!";
                }
                default -> msg = "Ajuste aplicado.";
            }
            recalcularOrdemTurnos();
            registrarLog(msg);
            return Map.of("ok", true);
        }

        // 2. Verifica se é um Inimigo
        for (Object obj : inimigos) {
            Map ini = (Map) obj;
            if (alvoId.equals(ini.get("id"))) {
                String nomeIni = ini.get("nome").toString();
                String msg;
                switch (tipoAjuste) {
                    case "pv" -> {
                        int novoPv = Math.max(0, Math.min((int) ini.get("pvMax"), (int) ini.get("pv") + valor));
                        ini.put("pv", novoPv);
                        msg = (valor >= 0 ? "💚 Mestre curou +" : "💔 Mestre aplicou ") + valor + " PV em " + nomeIni + " (" + novoPv + "/" + ini.get("pvMax") + ").";
                        registrarEventoVisual(valor >= 0 ? "SUPORTE" : "ATAQUE", "FOCO", "", "Mestre", alvoId, nomeIni,
                                              valor >= 0 ? "Regeneração" : "Impacto Narrativo", (valor >= 0 ? "+" : "") + valor + " PV", msg);
                    }
                    case "pvMax" -> {
                        ini.put("pv", ini.get("pvMax"));
                        msg = "✨ " + nomeIni + " teve PV restaurado ao máximo!";
                    }
                    case "atk" -> {
                        int novoAtk = Math.max(1, (int) ini.get("atk") + valor);
                        ini.put("atk", novoAtk);
                        msg = (valor >= 0 ? "⚔️ Buff em " : "🔻 Debuff em ") + nomeIni + ": " + (valor >= 0 ? "+" : "") + valor + " ATK (Atual: " + novoAtk + ")!";
                    }
                    case "def" -> {
                        int novoDef = Math.max(0, (int) ini.get("def") + valor);
                        ini.put("def", novoDef);
                        msg = (valor >= 0 ? "🛡️ Buff em " : "🔻 Debuff em ") + nomeIni + ": " + (valor >= 0 ? "+" : "") + valor + " DEF (Atual: " + novoDef + ")!";
                    }
                    case "vlc" -> {
                        int novoVlc = Math.max(1, (int) ini.get("vlc") + valor);
                        ini.put("vlc", novoVlc);
                        msg = (valor >= 0 ? "💨 Buff em " : "🔻 Debuff em ") + nomeIni + ": " + (valor >= 0 ? "+" : "") + valor + " VLC (Atual: " + novoVlc + ")!";
                    }
                    default -> msg = "Ajuste aplicado.";
                }
                recalcularOrdemTurnos();
                registrarLog(msg);
                return Map.of("ok", true);
            }
        }

        return Map.of("erro", "Alvo não encontrado.");
    }

    @PostMapping("/turno")
    public Map encerrarTurno(@RequestBody(required = false) Map payload) {
        if (ordemTurnos.isEmpty()) recalcularOrdemTurnos();
        if (ordemTurnos.isEmpty()) return Map.of("ok", true);

        cartasNaMesa.clear();
        indiceTurnoAtual++;
        if (indiceTurnoAtual >= ordemTurnos.size()) {
            recalcularOrdemTurnos();
            indiceTurnoAtual = 0;
            numeroRodada++;
            for (Object obj : herois.values()) {
                Personagem p = (Personagem) obj;
                p.setEp(p.getEpMax());
            }
            registrarLog("🔄 Rodada " + numeroRodada + "! Energia (EP) restaurada. Turno de: " + getNomeTurnoAtual());
        } else {
            registrarLog("⏭️ Turno passado! Agora é a vez de: " + getNomeTurnoAtual());
        }
        return Map.of("ok", true, "turnoAtual", getTurnoAtualObj());
    }

    @PostMapping("/loja")
    public Map comprarLoja(@RequestBody Map payload) {
        Personagem p = (Personagem) herois.get(payload.get("personagemId"));
        String item = String.valueOf(payload.get("item"));
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
    public Map mudarCenario(@RequestBody Map payload) {
        this.cenarioAtual = String.valueOf(payload.getOrDefault("cenario", "porto"));
        this.cenarioTitulo = switch (cenarioAtual) {
            case "conves" -> "Convés do Galeão — Tempestade em Alto-Mar";
            case "ruinas" -> "Ilha das Ruínas — Santuário Esquecido";
            case "taverna" -> "Taverna Subterrânea — Porto dos Renegados";
            default -> "Porto Pirata — Docas da Alvorada";
        };
        registrarLog("🗺️ Cenário alterado para: " + cenarioTitulo);
        return Map.of("ok", true);
    }

    @PostMapping("/mestre/novo-inimigo")
    public Map novoInimigo(@RequestBody Map payload) {
        String nome = payload.getOrDefault("nome", "Corsário Sombrio").toString();
        int pv = Integer.parseInt(payload.getOrDefault("pv", 35).toString());
        int atk = Integer.parseInt(payload.getOrDefault("atk", 4).toString());
        int def = Integer.parseInt(payload.getOrDefault("def", 2).toString());
        int vlc = Integer.parseInt(payload.getOrDefault("vlc", 3).toString());
        String arquetipo = payload.getOrDefault("arquetipo", "corsario").toString();
        boolean limparAnteriores = Boolean.parseBoolean(payload.getOrDefault("limpar", "false").toString());

        if (limparAnteriores) inimigos.clear();
        adicionarInimigoInterno(nome, pv, atk, def, vlc, arquetipo);
        recalcularOrdemTurnos();
        registrarLog("💀 Oponente preparado: " + nome + " (PV " + pv + " | VLC " + vlc + ")!");
        return Map.of("ok", true);
    }

    @PostMapping("/mestre/heroi")
    public Map ajustarHeroi(@RequestBody Map payload) {
        Personagem p = (Personagem) herois.get(payload.get("personagemId"));
        if (p == null) return Map.of("erro", "Herói inválido.");
        String acao = payload.get("acao").toString();
        switch (acao) {
            case "ouro" -> { p.setOuro(p.getOuro() + 15); registrarLog("💰 Mestre concedeu +15g para " + p.getNome()); }
            case "nivel" -> {
                p.setNivel(p.getNivel() + 1);
                p.setPvMax(p.getPvMax() + 4);
                p.setPv(p.getPvMax());
                p.setAtk(p.getAtk() + 1);
                p.setVlc(p.getVlc() + 1);
                recalcularOrdemTurnos();
                registrarLog("⭐ " + p.getNome() + " subiu para o Nível " + p.getNivel() + " (+1 ATK, +1 VLC)!");
            }
            case "fruta" -> {
                String fNome = payload.getOrDefault("frutaNome", "Fruta Mística").toString();
                p.setFrutaNome(fNome);
                p.setAtk(p.getAtk() + 2);
                registrarLog("🍎 " + p.getNome() + " despertou [" + fNome + "] (+2 ATK permanente)!");
            }
            case "bonusDado" -> {
                p.setBonusImproviso(p.getBonusImproviso() + 3);
                registrarLog("🎲 " + p.getNome() + " recebeu +3 de bônus para Improviso!");
            }
        }
        return Map.of("ok", true);
    }

    @PostMapping("/mestre/restaurar-save")
    public Map restaurarSave(@RequestBody Map save) {
        if (save == null || !save.containsKey("herois")) return Map.of("erro", "Save inválido.");
        if (save.containsKey("cenario")) this.cenarioAtual = save.get("cenario").toString();
        if (save.containsKey("cenarioTitulo")) this.cenarioTitulo = save.get("cenarioTitulo").toString();

        if (save.get("inimigos") instanceof List) {
            this.inimigos.clear();
            List listaIni = (List) save.get("inimigos");
            for (Object obj : listaIni) {
                Map m = (Map) obj;
                String arq = String.valueOf(m.getOrDefault("arquetipo", "corsario"));
                int atk = Integer.parseInt(m.get("atk").toString());
                m.put("baralho", gerarBaralhoInimigo(arq, atk));
                this.inimigos.add(m);
            }
        }
        if (save.get("herois") instanceof List) {
            List listaHerois = (List) save.get("herois");
            for (Object itemH : listaHerois) {
                Map dadosH = (Map) itemH;
                Personagem p = (Personagem) herois.get(dadosH.get("id").toString());
                if (p != null) {
                    p.setNivel(Integer.parseInt(dadosH.get("nivel").toString()));
                    p.setOuro(Integer.parseInt(dadosH.get("ouro").toString()));
                    p.setPv(Integer.parseInt(dadosH.get("pv").toString()));
                    p.setPvMax(Integer.parseInt(dadosH.get("pvMax").toString()));
                    p.setEp(Integer.parseInt(dadosH.get("ep").toString()));
                    p.setEpMax(Integer.parseInt(dadosH.get("epMax").toString()));
                    p.setAtk(Integer.parseInt(dadosH.get("atk").toString()));
                    p.setDef(Integer.parseInt(dadosH.get("def").toString()));
                    p.setVlc(Integer.parseInt(dadosH.getOrDefault("vlc", p.getVlc()).toString()));
                    p.setEscudo(Integer.parseInt(dadosH.get("escudo").toString()));
                    p.setFrutaNome(String.valueOf(dadosH.getOrDefault("frutaNome", "")));
                    if (dadosH.get("baralho") instanceof List) {
                        List listaCartas = (List) dadosH.get("baralho");
                        for (Object itemC : listaCartas) {
                            Map cDados = (Map) itemC;
                            p.getBaralho().stream()
                                .filter(c -> c.getId().equals(cDados.get("id").toString()))
                                .findFirst()
                                .ifPresent(c -> c.setNivel(Integer.parseInt(cDados.get("nivel").toString())));
                        }
                    }
                }
            }
        }
        recalcularOrdemTurnos();
        registrarLog("📖 Crônica restaurada! Progresso da tripulação carregado.");
        return Map.of("ok", true);
    }

    @PostMapping("/mestre/resetar")
    public Map resetar() {
        inicializarJogo();
        return Map.of("ok", true);
    }

    private void registrarLog(String texto) {
        logs.add(0, texto);
        if (logs.size() > 35) logs.remove(logs.size() - 1);
    }
}
