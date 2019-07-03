package com.jminovasi.zakat.model;

public class ModelRekapZakat {

    private String id, tipe, jumlah, tanggal, muzakki, amil, alamat;

    public ModelRekapZakat() {
    }

    public ModelRekapZakat(String id, String tipe, String jumlah, String tanggal, String muzakki, String amil, String alamat) {
        this.id = id;
        this.tipe = tipe;
        this.jumlah = jumlah;
        this.tanggal = tanggal;
        this.muzakki = muzakki;
        this.amil = amil;
        this.alamat = alamat;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTipe() {
        return tipe;
    }

    public void setTipe(String tipe) {
        this.tipe = tipe;
    }

    public String getJumlah() {
        return jumlah;
    }

    public void setJumlah(String jumlah) {
        this.jumlah = jumlah;
    }

    public String getTanggal() {
        return tanggal;
    }

    public void setTanggal(String tanggal) {
        this.tanggal = tanggal;
    }

    public String getMuzakki() {
        return muzakki;
    }

    public void setMuzakki(String muzakki) {
        this.muzakki = muzakki;
    }

    public String getAmil() {
        return amil;
    }

    public void setAmil(String amil) {
        this.amil = amil;
    }

    public String getAlamat() {
        return alamat;
    }

    public void setAlamat(String alamat) {
        this.alamat = alamat;
    }
}
