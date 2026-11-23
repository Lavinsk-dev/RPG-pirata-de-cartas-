package com.piratas.model;

public class Carta {
    private String id;
    private String nome;
    private String tipo; // ATAQUE, DEFESA, ARMADILHA, SUPORTE
    private int custoEp;
    private int valorBase;
    private int nivel;
    private String efeitoEspecial;
    private String descricao;
    private String imagemUrl;

    public Carta() {}

    public Carta(String id, String nome, String tipo, int custoEp, int valorBase, String efeitoEspecial, String descricao, String imagemUrl) {
        this.id = id;
        this.nome = nome;
        this.tipo = tipo;
        this.custoEp = custoEp;
        this.valorBase = valorBase;
        this.nivel = 1;
        this.efeitoEspecial = efeitoEspecial;
        this.descricao = descricao;
        this.imagemUrl = imagemUrl;
    }

    public int getPoderTotal() {
        return valorBase + ((nivel - 1) * 2);
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public int getCustoEp() { return custoEp; }
    public void setCustoEp(int custoEp) { this.custoEp = custoEp; }
    public int getValorBase() { return valorBase; }
    public void setValorBase(int valorBase) { this.valorBase = valorBase; }
    public int getNivel() { return nivel; }
    public void setNivel(int nivel) { this.nivel = nivel; }
    public String getEfeitoEspecial() { return efeitoEspecial; }
    public void setEfeitoEspecial(String efeitoEspecial) { this.efeitoEspecial = efeitoEspecial; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public String getImagemUrl() { return imagemUrl; }
    public void setImagemUrl(String imagemUrl) { this.imagemUrl = imagemUrl; }
}
