/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ag.service;

import com.ag.model.Tbltipo;
import java.io.InputStream;
import java.util.List;

/**
 *
 * @author 85154220
 */
public interface ArchivoManager {
    
    public String[] cargaMacroMasivo(String usuario, String programa, InputStream inputStream);

    public List <Tbltipo> getTipoMacromedidores();

    public String[] cargaMacroIndividual(String trafo, String macro, String tipoMacro, String usuario,String programa);
    
    public void crearMacroVirtual(String usuario, String programa, String nombre, String direccion, String idComercial);

    public boolean existeMacroVirtual (String idComercial);
    
    public String[] cargaAsociarMacroCirBar(String usuario, String programa, InputStream inputStream);
    
    public String[] cargaAsociarTrafoMacro(String usuario, String programa, InputStream inputStream);

    public String balanceVirtual(String periodo, String tipo);
}
