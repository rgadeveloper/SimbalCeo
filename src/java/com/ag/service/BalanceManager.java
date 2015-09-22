/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ag.service;

import com.ag.model.*;
import com.ag.model.view.*;
import java.util.List;

/**
 *
 * @author 85154220
 */
public interface BalanceManager {

    public List<DataBalanceHijo> cuadroMando(String idComponente,String tipoComponente, String periodo);

    public Balances getBalances(String idComponente,String tipoComponente, String periodo);
    
    public DataRangosBalance getRangosZonas(String idZona, String tipoComponente,String tipoArbol, String periodo);
    
    public Medida getMedida(String idComponente,String periodo, String idTipoCompo);
    
    public AtrComponente getAtrComponente(String idComponente);
    
    public RelComponenteMedida getRelComponenteMedida(String idComponente,String periodo);
    
    public AtrComponenteMedida getAtrComponenteMedida(String idComponenteMedida,String periodo);
    
    public PadreHijo getSubestacionCircuito(String idComponente,String periodo);

    public PadreHijo getZonaMunicipio(String idComponente,String periodo);
	
    public ComponenteMedida getComponenteMedida(String idComponenteMedida,String periodo);

    public Componente getComponente(String idComponente);
    
    public List<DataValue> getCantSumXTipoUso(String idComponente,String periodo);
    
    public String getCantSumNoMedidos(String idComponente,String periodo);

    public List<Medida> getListMedida(String idComponente, int periodo);    
    
    public int getNumMesesAtras();       
       
    public List<Componente> getTrafosByRango (String id, String tipo, String periodo, String descripcion);

    public ZonaGeografica getPadreByTipo (String idComponente, String tipo);
    
    public List<Novedades> getNovedadesByTrafo (String idComponenteMedida);
    
    public void saveNovedad(String usuario, String programa, int periodo, String idTipoNovedad, ComponenteMedida componenteMedida);
    
    public void editNovedad(Novedades n);
    
    public void deleteNovedad(Novedades n);
    
    public List<Tbltipo> getTiposNovedades();
    
    public Tbltipo getTipo(String tipo);
    
     /**
     * Obtiene un balance especifico
     * @autor <b>Jose Mejia</b>
     * @see MapBean
     * @since 29/10/2014
     * @param codComp
     * @param periodo
     * @param tipoC
     * @return 
     */
    public Balances getBalancesAll(String codComp, String periodo, String tipoC);
    
    /**
     * Obtiene un componente especifico
     * @autor <b>Jose Mejia</b>
     * @see MapBean
     * @since 29/10/2014
     * @param idComponente
     * @param tipoComponente
     * @return 
     */
    public Componente getComponente(String idComponente,String tipoComponente);
}
