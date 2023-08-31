package br.com.smartagro;

import java.io.Serializable;

public class Cidade implements Serializable {
    private String nome;
    private String uf;
    private String id;
    private String latitude;
    private String longitude;
    private boolean usarLocalizacao;
    private String urlLocalizacao;

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getUf() {
        return uf;
    }

    public void setUf(String uf) {
        this.uf = uf;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public boolean isUsarLocalizacao() {
        return usarLocalizacao;
    }

    public void setUsarLocalizacao(boolean usarLocalizacao) {
        this.usarLocalizacao = usarLocalizacao;
    }

    public String getUrlLocalizacao() {
        return urlLocalizacao;
    }

    public void setUrlLocalizacao(String urlLocalizacao) {
        this.urlLocalizacao = urlLocalizacao;
    }
}
