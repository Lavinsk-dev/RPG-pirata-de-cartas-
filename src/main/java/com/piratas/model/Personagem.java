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

    public static Personagem criarLua() {
        Personagem lua = new Personagem("Lua", "A Flor de Sangue", "Arqueóloga", 20, 4, 2, 2);
        lua.mao.add(new Carta("Disparo de Pederneira", "ATAQUE", 1, 2, "Causa ATK + 2 de dano (+bônus do d20)."));
        lua.mao.add(new Carta("Estocada na Brecha", "ATAQUE", 2, 3, "Causa ATK + 3 de dano e ignora até 3 de DEF."));
        lua.mao.add(new Carta("Defesa Protetora", "TECNICA", 2, 5, "+5 DEF neste turno; protege um aliado."));
        lua.mao.add(new Carta("Memória da Civilização", "TECNICA", 1, 3, "Próximo Improviso Narrativo ganha +3 no dado."));
        lua.mao.add(new Carta("Mecanismo Antigo", "ARMADILHA", 2, 4, "(Setada) Causa 4 de dano e atrapalha o ataque inimigo."));
        return lua;
    }

    public static Personagem criarInimigoTeste() {
        return new Personagem("Capitão Morgano", "Mão de Ferro", "Marinha", 25, 5, 2, 1);
    }

    public boolean gastarEP(int custo) {
        if (this.epAtual >= custo) {
            this.epAtual -= custo;
            return true;
        }
        return false;
    }

    public void iniciarNovoTurno() {
        this.epAtual = Math.min(this.epMax, this.epAtual + 3);
        this.escudoTemporario = 0;
    }

    public void receberDano(int danoBruto, int ignorarDefesa) {
        int defesaEfetiva = Math.max(0, (this.def + this.escudoTemporario) - ignorarDefesa);
        int danoFinal = Math.max(1, danoBruto - defesaEfetiva);
        this.pvAtual = Math.max(0, this.pvAtual - danoFinal);
    }

    public void adicionarEscudo(int valor) { this.escudoTemporario += valor; }
    public void adicionarBonusImproviso(int valor) { this.bonusImproviso += valor; }
    public int consumirBonusImproviso() {
        int b = this.bonusImproviso;
        this.bonusImproviso = 0;
        return b;
    }

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