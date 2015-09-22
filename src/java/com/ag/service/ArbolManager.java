/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ag.service;

import com.ag.model.Componente;
import com.ag.model.view.UbicacionMacroV;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author 85154220
 */
public interface ArbolManager {
    
    public List cargaArbolInicial(String nivelGrupo,String periodo);
    
    public List cargaArbolHijos(String codigoPadre,String tipoPadre,String periodo, HashMap gestionFiltros);
    
    public List cargaArbolHijosTrafos(String codigoPadre,String tipoPadre,String periodo, HashMap gestionFiltros);
    
    public boolean soloTrafoInGmap(String tipo);
            
    public List getZoomMapa();

    public List getPeriodo();
    
    public String rutaTrafo(String idCliente, String tipoArbol, String periodo) throws SQLException;
    
    public String rutaSuministro(String idCliente, String tipoArbol, String periodo) throws SQLException;
    
    public UbicacionMacroV getUbicacionMacroV(String idComponente, String periodo) throws SQLException;
    
    /**
     * Lista de componente que se van a mostar en el mapa
     * @autor <b>Jose Mejia</b>
     * @see MapBean
     * @since 29/10/2014
     * @param codigo
     * @param tipo
     * @param periodo
     * @param arbol
     * @return 
     */
    public List<Componente> getComponentesUbicacion(String codigo,String tipo,String periodo, String arbol
            ,HashMap gestionFiltros);
    
    /**
     * Ruta donde estan almacendas las imagenes
     * @autor <b>Jose Mejia</b>
     * @see MapBean
     * @since 29/10/2014
     * @return 
     */
    public String getRutaImage();
}
