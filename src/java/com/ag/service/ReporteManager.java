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
public interface ReporteManager {
        
    public List<ZonaGeografica> getZonasByTipo (String tipo);
    
    public List<Componente> getTrafosByEmpresa (String id);
    
    public List<Componente> getTrafosByZona (String id);
    
    public List<Componente> getTrafosByCircuitoOrBarrio (String id);
    
    public List<Componente> getTrafosBySubestacion (String id);
    
    public List<Componente> getTrafosByVista (String vista);
    
    public List<Componente> getTrafo (String id);   
      
    public Balances getBalanceByPeriodo(String idComponente, int periodo);
    
    public Balances getBalanceByPeriodo(String idComponente, String periodo, String tipo);
    
    public String getRutaReportes();
    
    public String getRutaLogoForReportes();    
    //public String[] getPeriodosByTrafos(List<Componente> trafos);
    
    public String[] getPeriodoMax();
    
    //OPERATIVO
    
    public List<Tbltipo> getActividades();  
    
    public List<Campania> getCampanias(String fechaIni, String fechaFin, String tipoActividad);  
    
    public List<ResultCampania> getResultCampanias(List<Campania> campanias);
    
    public List<ResultCampania> getResultCampanias(String idCampania);
    
    public List<ResultOrdenTrabajo> getResultOrdenesTrabajo(List<Campania> campanias, String filtro, List<ordenesDeTrabajo> resultOrdenesTrabajoForJasper);
    
    public List<ResultOrdenTrabajo> getResultOrdenesTrabajo(String idCampania, String filtro, List<ordenesDeTrabajo> resultOrdenesTrabajoForJasper);
    
    public List<Trafo> getTrafosByPeriodo(String periodo); 
     
    public List<Trafo> getTrafosInconsistentes(String periodo);
    
    public List<String> getPeriodos();
    
    public Componente getComponente(String idComponente);
    
    public ZonaGeografica getZona(String idZona, String tipo);
    
    public ZonaGeografica getZona(String idZona);    
    
    //Para Trafos y Sus Atributos TrafoAtr
    public List<AtrComponente> getTrafosAtrByEmpresa (String id);
    
    public List<AtrComponente> getTrafosAtrByZona (String id);
    
    public List<AtrComponente> getTrafosAtrByCircuitoOrBarrio (String id);
    
    public List<AtrComponente> getTrafosAtrBySubestacion (String id);
    
    public List<AtrComponente> getTrafosAtrByVista (String vista);
    
    public List<AtrComponente> getTrafoAtr (String id);
    
     //Para Clientes y Sus Atributos ClienteAtr
    public List<AtrComponente> getClientesAtrByEmpresa (String id);
    
    public List<AtrComponente> getClientesAtrByZona (String id);
    
    public List<AtrComponente> getClientesAtrByCircuitoOrBarrio (String id);
    
    public List<AtrComponente> getClientesAtrBySubestacion (String id);
    
    public List<AtrComponente> getClientesAtrByVista (String vista);
    
    public List<AtrComponente> getClienteAtrByTrafo (String id);
    
}
