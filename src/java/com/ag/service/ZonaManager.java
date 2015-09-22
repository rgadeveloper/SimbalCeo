/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.ag.service;
import com.ag.model.TipoComponente;
import com.ag.model.ZonaGeografica;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.List;

/**
 *
 * @author 85154220
 */
public interface ZonaManager { 
    
    public void save(String usuario, String programa, BigDecimal idZona, String nombre, BigInteger idPadre, TipoComponente tipoComponente, String coordX, String coordY, String idComercial);
            
    public List<ZonaGeografica> getZonasAll(); 
    
    public List<TipoComponente> getTiposComponentes(); 
       
    public ZonaGeografica getZonaGeografica(String id);
    
    public TipoComponente getTipoComponente(String id);
    
    public void update(ZonaGeografica zona);

}
