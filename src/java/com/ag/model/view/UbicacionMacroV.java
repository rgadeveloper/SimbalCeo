/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ag.model.view;

/**
 *
 * @author arodriguezr
 */
public class UbicacionMacroV {
    private String zona;
    private String munSub;
    private String barCir;
    private String cantTrafosAso;

    public UbicacionMacroV() {
    }

    public UbicacionMacroV(String zona, String munSub, String barCir, String cantTrafosAso) {
        this.zona = zona;
        this.munSub = munSub;
        this.barCir = barCir;
        this.cantTrafosAso = cantTrafosAso;
    }
    
    public String getBarCir() {
        return barCir;
    }

    public void setBarCir(String barCir) {
        this.barCir = barCir;
    }

    public String getMunSub() {
        return munSub;
    }

    public void setMunSub(String munSub) {
        this.munSub = munSub;
    }

    public String getZona() {
        return zona;
    }

    public void setZona(String zona) {
        this.zona = zona;
    }

    public String getCantTrafosAso() {
        return cantTrafosAso;
    }

    public void setCantTrafosAso(String cantTrafosAso) {
        this.cantTrafosAso = cantTrafosAso;
    }   
}
