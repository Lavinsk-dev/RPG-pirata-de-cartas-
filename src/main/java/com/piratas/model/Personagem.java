package com.piratas.model;

import java.util.ArrayList;
import java.util.List;

public class Personagem {
    private String id;
    private String nome;
    private String titulo;
    private String profissao;
    private String imagemUrl;
    private int nivel;
    private int xp;
    private int ouro;
    private int pv;
    private int pvMax;
    private int ep;
    private int epMax;
    private int atk;
    private int def;
    private int vlc;
    private int escudo;
    private int bonusImproviso;
    private int atkTemp;
    private int defTemp;
    private String frutaNome;
    private String frutaDescricao;
    private List<Carta> baralho = new ArrayList<>();

    public Personagem() {}

    public Personagem(String id, String nome, String titulo, String profissao, String imagemUrl, int pvMax, int atk, int def, int vlc) {
        this.id = id;
        this.nome = nome;
        this.titulo = titulo;
        this.profissao = profissao;
        this.imagemUrl = imagemUrl;
        this.nivel = 1;
        this.xp = 0;
        this.ouro = 20;
        this.pvMax = pvMax;
        this.pv = pvMax;
        this.epMax = 5;
        this.ep = 5;
        this.atk = atk;
        this.def = def;
        this.vlc = vlc;
        this.escudo = 0;
        this.bonusImproviso = 0;
        this.atkTemp = 0;
        this.defTemp = 0;
        this.frutaNome = "";
        this.frutaDescricao = "";
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getProfissao() { return profissao; }
    public void setProfissao(String profissao) { this.profissao = profissao; }
    public String getImagemUrl() { return imagemUrl; }
    public void setImagemUrl(String imagemUrl) { this.imagemUrl = imagemUrl; }
    public int getNivel() { return nivel; }
    public void setNivel(int nivel) { this.nivel = nivel; }
    public int getXp() { return xp; }
    public void setXp(int xp) { this.xp = xp; }
    public int getOuro() { return ouro; }
    public void setOuro(int ouro) { this.ouro = ouro; }
    public int getPv() { return pv; }
    public void setPv(int pv) { this.pv = pv; }
    public int getPvMax() { return pvMax; }
    public void setPvMax(int pvMax) { this.pvMax = pvMax; }
    public int getEp() { return ep; }
    public void setEp(int ep) { this.ep = ep; }
    public int getEpMax() { return epMax; }
    public void setEpMax(int epMax) { this.epMax = epMax; }
    public int getAtk() { return atk; }
    public void setAtk(int atk) { this.atk = atk; }
    public int getDef() { return def; }
    public void setDef(int def) { this.def = def; }
    public int getVlc() { return vlc; }
    public void setVlc(int vlc) { this.vlc = vlc; }
    public int getEscudo() { return escudo; }
    public void setEscudo(int escudo) { this.escudo = escudo; }
    public int getBonusImproviso() { return bonusImproviso; }
    public void setBonusImproviso(int bonusImproviso) { this.bonusImproviso = bonusImproviso; }
    public int getAtkTemp() { return atkTemp; }
    public void setAtkTemp(int atkTemp) { this.atkTemp = atkTemp; }
    public int getDefTemp() { return defTemp; }
    public void setDefTemp(int defTemp) { this.defTemp = defTemp; }
    public String getFrutaNome() { return frutaNome; }
    public void setFrutaNome(String frutaNome) { this.frutaNome = frutaNome; }
    public String getFrutaDescricao() { return frutaDescricao; }
    public void setFrutaDescricao(String frutaDescricao) { this.frutaDescricao = frutaDescricao; }
    public List<Carta> getBaralho() { return baralho; }
    public void setBaralho(List<Carta> baralho) { this.baralho = baralho; }
}
