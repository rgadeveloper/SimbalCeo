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
public interface SArchivoManager {
    
    public String[] cargaAmarreMasivo(String usuario, String programa, InputStream inputStream);

    public String[] cargaConsumoMasivo(String usuario, String programa, InputStream inputStream);    
    
}
