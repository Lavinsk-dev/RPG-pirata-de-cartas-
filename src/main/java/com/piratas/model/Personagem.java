package com.piratas.model;

import java.util.ArrayList;
import java.util.List;

public class Personagem {
    private String nome;
    private String epiteto;
    private String profissao;
    private int pvAtual;
    private int pvMax;
    private int atk;
    private int def;
    private int vlc;
    private int epAtual;
    private int epMax;

    private int escudoTemporario = 0;
    private int bonusImproviso = 0;
    private List<Carta> mao = new ArrayList<>();
    private List<Carta> armadilhasSetadas = new ArrayList<>();

    public Personagem(String nome, String epiteto, String profissao, int pvMax, int atk, int def, int vlc) {
        this.nome = nome;
        this.epiteto = epiteto;
        this.profissao = profissao;
        this.pvMax = pvMax;
        this.pvAtual = pvMax;
        this.atk = atk;
        this.def = def;
        this.vlc = vlc;
        this.epMax = 5;
        this.epAtual = 5;
    }

    // Cria a Lua exatamente como na sua carta e com o baralho inicial dela
    public static Personagem criarLua() {
        Personagem lua = new Personagem("Lua", "A Flor de Sangue", "Arqueóloga", 20, 4, 2, 2);
        lua.mao.add(new Carta("Disparo de Pederneira", "ATAQUE", 1, 2, "Causa ATK + 2 de dano (bônus extra se o alvo já estiver ferido)."));
        lua.mao.add(new Carta("Defesa Protetora", "TECNICA", 2, 5, "+5 DEF; Lua pode escolher ser o alvo de um ataque destinado a um aliado."));
        lua.mao.add(new Carta("Memória da Civilização", "TECNICA", 1, 3, "Próximo Improviso Narrativo de qualquer aliado recebe +3 no dado."));
        lua.mao.add(new Carta("Mecanismo Antigo", "ARMADILHA", 2, 4, "(Setada) Ativa no ataque inimigo: inimigo rola dado com -4 e sofre 4 de dano."));
        return lua;
    }

    // Getters para o site conseguir ler os dados e desenhar na tela
    public String getNome() { return nome; }
    public String getEpiteto() { return epiteto; }
    public String getProfissao() { return profissao; }
    public int getPvAtual() { return pvAtual; }
    public int getPvMax() { return pvMax; }
    public int getAtk() { return atk; }
    public int getDef() { return def; }
    public int getVlc() { return vlc; }
    public int getEpAtual() { return epAtual; }
    public int getEpMax() { return epMax; }
    public int getEscudoTemporario() { return escudoTemporario; }
    public int getBonusImproviso() { return bonusImproviso; }
    public List<Carta> getMao() { return mao; }
    public List<Carta> getArmadilhasSetadas() { return armadilhasSetadas; }
}