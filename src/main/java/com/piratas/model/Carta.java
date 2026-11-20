package com.piratas.model;

public class Carta {
    private String nome;
    private String tipo; // ATAQUE, TECNICA, ARMADILHA, ITEM, FRUTA
    private int custoEP;
    private int valorBase; // Dano base, escudo ou cura
    private String descricao;

    public Carta(String nome, String tipo, int custoEP, int valorBase, String descricao) {
        this.nome = nome;
        this.tipo = tipo;
        this.custoEP = custoEP;
        this.valorBase = valorBase;
        this.descricao = descricao;
    }

    public String getNome() { return nome; }
    public String getTipo() { return tipo; }
    public int getCustoEP() { return custoEP; }
    public int getValorBase() { return valorBase; }
    public String getDescricao() { return descricao; }
}