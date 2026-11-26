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

    private final List logs = new ArrayList();
    private final List armadilhasAtivas = new ArrayList();
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
        indiceTurnoAtual = 0;
        numeroRodada = 1;

        Personagem lua = new Personagem("lua", "Lua", "Olhos de Águia", "Atiradora", "/imagens/lua.jpg", 18, 4, 1, 5);
        lua.getBaralho().add(new Carta("c1", "Disparo de Precisão", "ATAQUE", 2, 4, "CRITICO_VELOZ", "Tiro certeiro (Teste d20). Se resultado >= 14, +2 de dano.", "/imagens/carta_lua.jpg"));
        lua.getBaralho().add(new Carta("c2", "Rajada de Pólvora", "ATAQUE", 3, 6, "QUEIMADURA", "Disparo explosivo que atravessa parte da defesa.", ""));
        lua.getBaralho().add(new Carta("c3", "Cortina de Fumaça", "DEFESA", 1, 3, "ESQUIVA", "Concede +3 de Escudo e +1 VLC no turno.", ""));
        lua.getBaralho().add(new Carta("c4", "Fio Invisível de Pólvora", "ARMADILHA", 2, 5, "EXPLOSAO_OCULTA", "Carta oculta: detona quando um inimigo atacar, causando 5 de dano.", ""));
        lua.getBaralho().add(new Carta("c5", "Olhar do Horizonte", "SUPORTE", 1, 2, "FOCO", "Aumenta +2 ATK neste turno e +2 em testes de d20.", ""));

        Personagem kyan = new Personagem("kyan", "Kyan", "O Lobo Branco", "Carpinteiro", "/imagens/kyan.jpg", 20, 3, 2, 3);
        kyan.getBaralho().add(new Carta("k1", "Corte Duplo dos Machados", "ATAQUE", 2, 3, "GOLPE_DUPLO", "Golpe veloz (Teste d20). Se resultado >= 14, desfere segundo corte (+3 dano).", "/imagens/carta_kyan.jpg"));
        kyan.getBaralho().add(new Carta("k2", "Martelo & Cinzel", "ATAQUE", 1, 2, "QUEBRA_DEFESA", "Impacto de construtor que abala a guarda inimiga.", ""));
        kyan.getBaralho().add(new Carta("k3", "Barricada de Carpinteiro", "DEFESA", 2, 4, "FORTIFICAR", "Ergue vigas reforçadas: ganha +4 Escudo e +1 DEF no turno.", ""));
        kyan.getBaralho().add(new Carta("k4", "Estrepes e Vigas Ocultas", "ARMADILHA", 2, 4, "CONTRA_ATAQUE", "Carta oculta: estrutura desaba no atacante, bloqueando e causando 4 de dano.", ""));
        kyan.getBaralho().add(new Carta("k5", "Pulso Rúnico Latente", "SUPORTE", 2, 3, "CURA_RUNICA", "As runas aquecem: cura 3 PV e dá +2 ATK.", ""));

        Personagem zafira = new Personagem("zafira", "Zafira", "Chama Escarlate", "Combatente", "/imagens/zafira.jpg", 22, 4, 2, 2);
        zafira.getBaralho().add(new Carta("z1", "Sopro Dracônico", "ATAQUE", 3, 6, "CHAMAS", "Chamas ancestrais (Teste d20). Se resultado >= 15, dano extra +3.", "/imagens/carta_zafira.jpg"));
        zafira.getBaralho().add(new Carta("z2", "Garra Rubra", "ATAQUE", 1, 3, "SANGRAMENTO", "Investida feroz corpo-a-corpo.", ""));
        zafira.getBaralho().add(new Carta("z3", "Escamas de Obsidiana", "DEFESA", 2, 4, "BLOQUEIO_TOTAL", "Endurece a postura e recebe +4 de Escudo.", ""));
        zafira.getBaralho().add(new Carta("z4", "Rastro de Brasas", "ARMADILHA", 2, 5, "CHAMAS_OCULTAS", "Carta oculta: incendeia o oponente ao atacar.", ""));
        zafira.getBaralho().add(new Carta("z5", "Fúria Ancestral", "SUPORTE", 2, 3, "FURIA", "Desperta o sangue dracônico: +3 ATK no turno.", ""));

        herois.put("lua", lua);
        herois.put("kyan", kyan);
        herois.put("zafira", zafira);

        adicionarInimigoInterno("Capitão Morgano — Corsário", 45, 5, 2, 4, "corsario");
        recalcularOrdemTurnos();
        registrarLog("⚓ Rodada 1 iniciada em " + cenarioTitulo + "! Turno de: " + getNomeTurnoAtual());
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
                item.put("vlc", p.getVlc());
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
                item.put("vlc", ini.get("vlc"));
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
        res.put("cenario", cenarioAtual);
        res.put("cenarioTitulo", cenarioTitulo);
        res.put("numeroRodada", numeroRodada);
        res.put("ordemTurnos", ordemTurnos);
        res.put("turnoAtual", getTurnoAtualObj());
        res.put("herois", new ArrayList(herois.values()));
        res.put("inimigos", inimigos);
        res.put("armadilhasAtivas", armadilhasAtivas);
        res.put("logs", logs);
        return res;
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

        int dadoBase = payload.containsKey("dadoFisico") && payload.get("dadoFisico") != null
                ? Integer.parseInt(payload.get("dadoFisico").toString())
                : (random.nextInt(20) + 1);
        int totalDado = dadoBase + p.getBonusImproviso();
        p.setBonusImproviso(0);

        p.setEp(p.getEp() - carta.getCustoEp());

        String resumo;
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

                int defInimigo = (int) alvoIni.get("def");
                int escudoIni = (int) alvoIni.getOrDefault("escudo", 0);
                int poder = carta.getPoderTotal() + p.getAtk() + p.getAtkTemp();

                if ("GOLPE_DUPLO".equals(carta.getEfeitoEspecial()) && totalDado >= 14) poder += 3;
                if ("CRITICO_VELOZ".equals(carta.getEfeitoEspecial()) && totalDado >= 14) poder += 2;
                if ("CHAMAS".equals(carta.getEfeitoEspecial()) && totalDado >= 15) poder += 3;
                if (dadoBase == 20) poder += 4;

                int danoTotal = Math.max(1, poder - defInimigo);
                if (escudoIni > 0) {
                    int absorvido = Math.min(escudoIni, danoTotal);
                    alvoIni.put("escudo", escudoIni - absorvido);
                    danoTotal -= absorvido;
                }
                int pvRestante = Math.max(0, (int) alvoIni.get("pv") - danoTotal);
                alvoIni.put("pv", pvRestante);

                resumo = "⚔️ " + p.getNome() + " usou [" + carta.getNome() + "] em " + alvoIni.get("nome") +
                         " (Dado: " + totalDado + ") e causou " + danoTotal + " de dano!";
                if (pvRestante == 0) {
                    resumo += " 💀 " + alvoIni.get("nome") + " foi derrotado!";
                }
            }
            case "DEFESA" -> {
                int ganhoEscudo = carta.getPoderTotal();
                p.setEscudo(p.getEscudo() + ganhoEscudo);
                p.setDefTemp(p.getDefTemp() + 1);
                resumo = "🛡️ " + p.getNome() + " ativou [" + carta.getNome() + "] (+" + ganhoEscudo + " Escudo)!";
            }
            case "ARMADILHA" -> {
                Map trap = new LinkedHashMap();
                trap.put("donoId", p.getId());
                trap.put("donoNome", p.getNome());
                trap.put("cartaNome", carta.getNome());
                trap.put("poder", carta.getPoderTotal());
                armadilhasAtivas.add(trap);
                resumo = "🪤 " + p.getNome() + " armou uma Carta Oculta na mesa!";
            }
            default -> {
                int cura = carta.getPoderTotal();
                p.setPv(Math.min(p.getPvMax(), p.getPv() + cura));
                p.setAtkTemp(p.getAtkTemp() + 2);
                resumo = "✨ " + p.getNome() + " usou [" + carta.getNome() + "], curou " + cura + " PV e ganhou +2 ATK!";
            }
        }

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

        int dadoBase = payload.containsKey("dadoFisico") && payload.get("dadoFisico") != null
                ? Integer.parseInt(payload.get("dadoFisico").toString())
                : (random.nextInt(20) + 1);

        String msg;
        if ("DEFESA".equals(carta.getTipo())) {
            int esc = (int) iniAtivo.getOrDefault("escudo", 0) + carta.getValorBase();
            iniAtivo.put("escudo", esc);
            msg = "🛡️ " + iniAtivo.get("nome") + " usou [" + carta.getNome() + "] (+" + carta.getValorBase() + " Escudo)!";
        } else if ("SUPORTE".equals(carta.getTipo())) {
            int novoPv = Math.min((int) iniAtivo.get("pvMax"), (int) iniAtivo.get("pv") + 5);
            iniAtivo.put("pv", novoPv);
            iniAtivo.put("atk", (int) iniAtivo.get("atk") + 2);
            msg = "💀 " + iniAtivo.get("nome") + " usou [" + carta.getNome() + "] (+5 PV, +2 ATK)!";
        } else {
            if (!armadilhasAtivas.isEmpty()) {
                Map trap = (Map) armadilhasAtivas.remove(0);
                int danoTrap = (int) trap.get("poder");
                iniAtivo.put("pv", Math.max(0, (int) iniAtivo.get("pv") - danoTrap));
                msg = "💥 ARMADILHA ATIVADA! [" + trap.get("cartaNome") + "] de " + trap.get("donoNome") + " causou " + danoTrap + " de dano em " + iniAtivo.get("nome") + "!";
            } else {
                int dano = Math.max(1, carta.getValorBase() + (dadoBase >= 18 ? 3 : 0) - (alvo.getDef() + alvo.getDefTemp()));
                if (alvo.getEscudo() > 0) {
                    int absorvido = Math.min(alvo.getEscudo(), dano);
                    alvo.setEscudo(alvo.getEscudo() - absorvido);
                    dano -= absorvido;
                }
                alvo.setPv(Math.max(0, alvo.getPv() - dano));
                msg = "🩸 " + iniAtivo.get("nome") + " jogou [" + carta.getNome() + "] em " + alvo.getNome() +
                      " (Dado: " + dadoBase + ") causando " + dano + " de dano!";
            }
        }

        recalcularOrdemTurnos();
        registrarLog(msg);
        return Map.of("ok", true, "mensagem", msg);
    }

    @PostMapping("/turno")
    public Map encerrarTurno(@RequestBody(required = false) Map payload) {
        recalcularOrdemTurnos();
        if (ordemTurnos.isEmpty()) return Map.of("ok", true);

        indiceTurnoAtual++;
        if (indiceTurnoAtual >= ordemTurnos.size()) {
            indiceTurnoAtual = 0;
            numeroRodada++;
            for (Object obj : herois.values()) {
                Personagem p = (Personagem) obj;
                p.setEp(p.getEpMax());
                p.setAtkTemp(0);
                p.setDefTemp(0);
            }
            registrarLog("🔄 Rodada " + numeroRodada + "! Energia (EP) restaurada. Turno de: " + getNomeTurnoAtual());
        } else {
            registrarLog("⏭️ Turno passado! Agora é a vez de: " + getNomeTurnoAtual());
        }
        return Map.of("ok", true, "turnoAtual", getTurnoAtualObj());
    }

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
        int total = dadoFisico + bonusAtributo + p.getBonusImproviso();
        p.setBonusImproviso(0);

        String sucesso = total >= 15 ? "✅ SUCESSO ÉPICO!" : (total >= 10 ? "☑️ SUCESSO!" : "❌ FALHA!");
        String msg = "🎲 Improviso de " + p.getNome() + " [" + descricao + "]: Dado " + dadoFisico +
                     " + " + atributo + " (" + bonusAtributo + ") = " + total + " → " + sucesso;
        registrarLog(msg);
        return Map.of("ok", true, "mensagem", msg, "total", total);
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
        registrarLog("💀 Novo oponente entrou na arena: " + nome + " (PV " + pv + " | VLC " + vlc + ")!");
        return Map.of("ok", true);
    }

    @PostMapping("/mestre/remover-inimigo")
    public Map removerInimigo(@RequestBody Map payload) {
        String id = String.valueOf(payload.get("inimigoId"));
        inimigos.removeIf(obj -> ((Map) obj).get("id").equals(id));
        recalcularOrdemTurnos();
        registrarLog("🗑️ Oponente removido da arena pelo Mestre.");
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
                registrarLog("🎲 " + p.getNome() + " recebeu +3 de bônus para a próxima rolagem de d20!");
            }
            case "curar" -> { p.setPv(p.getPvMax()); p.setEp(p.getEpMax()); registrarLog("✨ " + p.getNome() + " teve PV e EP restaurados!"); }
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
