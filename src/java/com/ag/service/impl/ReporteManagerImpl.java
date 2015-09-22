/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ag.service.impl;

import com.ag.dao.Dao;
import com.ag.model.*;
import com.ag.model.view.*;
import com.ag.service.ReporteManager;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 *
 * @author Larry
 */
@Service("ReporteManager")
public class ReporteManagerImpl implements ReporteManager {

    @Autowired
    @Qualifier("DaoHibernate")
    private Dao dao;
    
    @Override
    public List<ZonaGeografica> getZonasByTipo(String tipo) {

        String hql ="Select z from ZonaGeografica z "
                  + "where z.estado='AC001' and "
                  + "z.tipoComponente.idTipoComponente=" + tipo
                  + " order by z.idComercial";
        return dao.find(hql);
    }

    @Override
    public List<Componente> getTrafosByEmpresa(String id) {
        String hql ="SELECT r.componente FROM RelComponenteUbicacion r "
                  + "WHERE r.periodoFin=999912 and "
                  + "r.relComponenteUbicacionPK.idZona in (SELECT z.idZona"
                                                             + " FROM ZonaGeografica z"
                                                             + " WHERE z.idPadre in (SELECT z.idZona"
                                                                                 + " FROM ZonaGeografica z"
                                                                                 + " WHERE z.idPadre in (SELECT zh.idZona"
                                                                                                     + " FROM ZonaGeografica zp, ZonaGeografica zh"
                                                                                                     + " WHERE zp.idZona=zh.idPadre and zp.idZona="+id+")))";
        return dao.find(hql);
    }

    @Override
    public List<Componente> getTrafosByZona(String id) {
        String hql ="SELECT r.componente FROM RelComponenteUbicacion r "
                  + "WHERE r.periodoFin=999912 and "
                  + "r.relComponenteUbicacionPK.idZona in (SELECT z.idZona"
                                                             + " FROM ZonaGeografica z"
                                                             + " WHERE z.idPadre in (SELECT zh.idZona"
                                                                                 + " FROM ZonaGeografica zp, ZonaGeografica zh"
                                                                                 + " WHERE zp.idZona=zh.idPadre and zp.idZona="+id+"))";
        return dao.find(hql);
    }

    @Override
    public List<Componente> getTrafosByCircuitoOrBarrio(String id) {
        String hql ="SELECT r.componente FROM RelComponenteUbicacion r "
                   +"WHERE r.periodoFin=999912 and r.relComponenteUbicacionPK.idZona="+id;
        return dao.find(hql);
    }

    @Override
    public List<Componente> getTrafosBySubestacion(String id) {
        String hql ="SELECT r.componente FROM RelComponenteUbicacion r "
                  + "WHERE r.periodoFin=999912 and "
                  + "r.relComponenteUbicacionPK.idZona in (SELECT z.idZona"
                                                             + " FROM ZonaGeografica z"
                                                             + " WHERE z.idZona in (SELECT zh.idZona"
                                                                                + " FROM ZonaGeografica zp, ZonaGeografica zh"
                                                                                + " WHERE zp.idZona=zh.idPadre and zp.idZona="+id+"))";
        return dao.find(hql);
    }    
    
    @Override
    public List<Componente> getTrafosByVista(String vista) {
        String hql ="SELECT rcu.componente "
                + "FROM RelComponenteUbicacion rcu "
                + "WHERE rcu.periodoFin=999912 and "
                + "rcu.componente.tipoComponente.idTipoComponente=8 and "
                + "rcu.zonaGeografica.tipoComponente.tbltipo.tipo='"+vista+"'";
        return dao.find(hql);
    }   
    
    @Override
    public List<Componente> getTrafo(String id) {
        String hql ="SELECT c FROM Componente c WHERE c.idComponente="+id;
        return dao.find(hql);
    }   
    
    @Override
    public Balances getBalanceByPeriodo(String idComponente, int periodo) {
        String hql = "select max(b.balancesPK.periodo)"
                                              + " from Balances b"
                                              + " where b.balancesPK.idTipoComponente=8 and"
                                                    + " b.balancesPK.idComponente="+idComponente;
        String periodoObjetivo = dao.findObject(hql).toString();
        int tam = periodoObjetivo.length();        
        String mes = periodoObjetivo.substring(tam-2);
        String ano = periodoObjetivo.substring(0, tam-2);
        String[] periodos = new String[12];
        for (int i = 0; i < 12; i++) {            
            if (mes.equals("00")) {
                mes="12";
                ano=String.valueOf(Integer.parseInt(ano)-1);
            }
            periodos[i]=ano+mes;
            int m=Integer.parseInt(mes)-1;
            mes=(m>9)?String.valueOf(m):
                      "0"+String.valueOf(m);
        }
               
        
        hql = "SELECT b FROM Balances b "
                   + "WHERE b.balancesPK.idTipoComponente=8 and b.balancesPK.idComponente="+idComponente
                   + " and b.balancesPK.periodo ="+periodos[periodo];
        
        return dao.findObject(hql);
    }
    
    @Override
    public Balances getBalanceByPeriodo(String idComponente, String periodo, String tipo) {        
        String hql = "SELECT b FROM Balances b "
                   + "WHERE b.balancesPK.idTipoComponente="+tipo+" and b.balancesPK.idComponente="+idComponente
                   + " and b.balancesPK.periodo ="+periodo;
        
        return dao.findObject(hql);
    }

    @Override
    public String getRutaReportes() {
        String hql ="SELECT p.valor FROM Parametro p WHERE p.idParametro ='RUTA_REPORTE'";
        return dao.findObject(hql).toString();
    }
    
    @Override
    public String getRutaLogoForReportes() {
        String hql ="SELECT p.valor FROM Parametro p WHERE p.idParametro ='RUTA_LOGO_REPORTES'";
        return dao.findObject(hql).toString();
    }

    /*@Override
    public String[] getPeriodosByTrafos(List<Componente> trafos) {
        String periodoObjetivo="000000";
        Iterator iter = trafos.iterator();        
        while (iter.hasNext()){
            Componente c = (Componente) iter.next();
            String hql = "select max(b.balancesPK.periodo)"
                   +" from Balances b"
                   +" where b.balancesPK.idTipoComponente=8 and"
                        + " b.balancesPK.idComponente = "+c.getIdComponente();
            periodoObjetivo=dao.findObject(hql).toString();
            /*if (periodoAct.compareTo(periodoObjetivo)>0)
                periodoObjetivo=periodoAct;
        } 
        
        int tam = periodoObjetivo.length();        
        String mes = periodoObjetivo.substring(tam-2);
        String ano = periodoObjetivo.substring(0, tam-2);
        String[] periodos = new String[12];
        for (int i = 0; i < 12; i++) {            
            if (mes.equals("00")) {
                mes="12";
                ano=String.valueOf(Integer.parseInt(ano)-1);
            }
            periodos[i]=ano+mes;
            int m=Integer.parseInt(mes)-1;
            mes=(m>9)?String.valueOf(m):
                      "0"+String.valueOf(m);
        }
        
        return periodos;
    }*/
    
    @Override
    public String[] getPeriodoMax(){        
        String hql = "select max(b.balancesPK.periodo)"
                   + " from Balances b"
                   + " where b.balancesPK.idTipoComponente=8";
        
        String periodoObjetivo = dao.findObject(hql).toString();        
        int tam = periodoObjetivo.length();        
        String mes = periodoObjetivo.substring(tam-2);
        String ano = periodoObjetivo.substring(0, tam-2);
        String[] periodos = new String[12];
        for (int i = 0; i < 12; i++) {            
            if (mes.equals("00")) {
                mes="12";
                ano=String.valueOf(Integer.parseInt(ano)-1);
            }
            periodos[i]=ano+mes;
            int m=Integer.parseInt(mes)-1;
            mes=(m>9)?String.valueOf(m):
                      "0"+String.valueOf(m);
        }
        
        return periodos;
    }
    
    //OPERATIVO
    
    @Override
    public List<Tbltipo> getActividades() {
        String hql = "select t "
                + " from Tbltipo t"
                + " where t.grupo.codigo='TAA000'";
        return dao.find(hql);        
    }

    @Override
    public List<Campania> getCampanias(String fechaIni, String fechaFin, String tipoActividad) {
        String hql="SELECT c FROM Campania c "
                + "where trunc(c.fechaInicio)>='"+fechaIni+"' "
                 + "and trunc(c.fechaInicio)<='"+fechaFin+"' "
                 + "and c.tbltipo.tipo="+tipoActividad;
        return dao.find(hql);
    }

    @Override
    public List<ResultCampania> getResultCampanias(List<Campania> campanias) {
        List<ResultCampania> resultCampanias = new ArrayList<ResultCampania>();       
        if (!campanias.isEmpty()){
          for (Iterator it = campanias.iterator(); it.hasNext();) {
                Campania c = (Campania) it.next();
                String idCampania = c.getIdCampania().toString();
                String nombre = c.getDescripcion();
                String periodo = String.valueOf(c.getPeriodo());
                Tbltipo tipo = c.getTbltipo();                
                ResultCampania rc = new ResultCampania(idCampania, nombre, periodo, tipo);
                 //demas valores de los query de Larry 
                if (tipo.getTipo().equals("7526")) {//Revisión/Normalización Macro
                   rc.setCantMacrosTotal(cantidadDeMacros("Total", idCampania));
                   rc.setCantMacrosNormales(cantidadDeMacros("Normales", idCampania));
                   rc.setCantMacrosRevisados(cantidadDeMacros("Revisados", idCampania));
                   rc.setCantMacrosNormalizados(cantidadDeMacros("Normalizados", idCampania));
                   rc.setCantMacrosIrregularidad(cantidadDeMacros("Irregularidad", idCampania));
                   rc.setPorcEfectividadMacros( rc.getCantMacrosTotal()!=0?
                                               (rc.getCantMacrosRevisados()/rc.getCantMacrosTotal())*100:
                                                0); 
                }else{//Revisión del usuario               
                   rc.setCantClientes(cantidadDeClientes("Total", idCampania));
                   rc.setCantClientesNormales(cantidadDeClientes("Normales", idCampania));
                   rc.setCantClientesRevisados(cantidadDeClientes("Revisados", idCampania));
                   rc.setCantClientesNormalizados(cantidadDeClientes("Normalizados", idCampania));
                   rc.setCantClientesIrregularidad(cantidadDeClientes("Irregularidad", idCampania));
                    /*double porcMacro=rc.getCantMacrosTotal()!=0?
                                    (rc.getCantMacrosRevisados()/rc.getCantMacrosTotal())*100:
                                    0;*/
                   rc.setPorcEfectividadClientes( rc.getCantMacrosTotal()!=0?
                                                (rc.getCantMacrosRevisados()/rc.getCantMacrosTotal())*100:
                                                 0);
                }
                resultCampanias.add(rc);
          }
        }
        return resultCampanias;
    }

    @Override
    public List<ResultCampania> getResultCampanias(String idCampania) {
        List<ResultCampania> resultCampanias = new ArrayList<ResultCampania>();
        String hql="SELECT c FROM Campania c WHERE c.idCampania = "+idCampania;
        Campania c = dao.findObject(hql);
        if (c!=null) {
                String nombre = c.getDescripcion();
                String periodo = String.valueOf(c.getPeriodo());
                Tbltipo tipo = c.getTbltipo();                
                ResultCampania rc = new ResultCampania(idCampania, nombre, periodo, tipo);
                 //demas valores de los query de Larry 
                if (tipo.getTipo().equals("7526")) {//Revisión/Normalización Macro
                   rc.setCantMacrosTotal(cantidadDeMacros("Total", idCampania));
                   rc.setCantMacrosNormales(cantidadDeMacros("Normales", idCampania));
                   rc.setCantMacrosRevisados(cantidadDeMacros("Revisados", idCampania));
                   rc.setCantMacrosNormalizados(cantidadDeMacros("Normalizados", idCampania));
                   rc.setCantMacrosIrregularidad(cantidadDeMacros("Irregularidad", idCampania));
                   rc.setPorcEfectividadMacros( rc.getCantMacrosTotal()!=0?
                                               (rc.getCantMacrosRevisados()/rc.getCantMacrosTotal())*100:
                                                0);   
                   rc.setMostrarValoresMacros(true);
                }else{//Revisión del usuario               
                   rc.setCantClientes(cantidadDeClientes("Total", idCampania));
                   rc.setCantClientesNormales(cantidadDeClientes("Normales", idCampania));
                   rc.setCantClientesRevisados(cantidadDeClientes("Revisados", idCampania));
                   rc.setCantClientesNormalizados(cantidadDeClientes("Normalizados", idCampania));
                   rc.setCantClientesIrregularidad(cantidadDeClientes("Irregularidad", idCampania));
                   rc.setPorcEfectividadClientes( rc.getCantMacrosTotal()!=0?
                                                (rc.getCantMacrosRevisados()/rc.getCantMacrosTotal())*100:
                                                 0);
                   rc.setMostrarValoresClientes(true);
                }
                resultCampanias.add(rc); 
        }
        return resultCampanias;
    }

    @Override
    public List<ResultOrdenTrabajo> getResultOrdenesTrabajo(List<Campania> campanias, String filtro, List<ordenesDeTrabajo> resultOrdenesTrabajoForJasper) {
        List<ResultOrdenTrabajo> resultOrdenTrabajo = new ArrayList<ResultOrdenTrabajo>();       
        if (!campanias.isEmpty()){
          for (Iterator it = campanias.iterator(); it.hasNext();) {
                Campania c = (Campania) it.next();
                String idCampania = c.getIdCampania().toString();
                String nombre = c.getDescripcion();
                String periodo = String.valueOf(c.getPeriodo());
                Tbltipo tipo = c.getTbltipo(); 
                //buscamos todos las ordenes de trabajos y creamos los objetos
                List<OrdenTrabajo> ordenesTrabajo = ordenesTrabajoByCampania(idCampania, filtro);
                if (!ordenesTrabajo.isEmpty()){
                    resultOrdenesTrabajoForJasper.add(new ordenesDeTrabajo(idCampania, nombre, periodo, tipo, ordenesTrabajo)); 
                    for (Iterator itO = ordenesTrabajo.iterator(); itO.hasNext();) {
                        OrdenTrabajo o = (OrdenTrabajo) itO.next();
                        ResultOrdenTrabajo rot = new ResultOrdenTrabajo(idCampania, nombre, periodo, tipo, o);
                        resultOrdenTrabajo.add(rot);
                    }
                }               
          }
        }
        return resultOrdenTrabajo;
    }

    @Override
    public List<ResultOrdenTrabajo> getResultOrdenesTrabajo(String idCampania, String filtro, List<ordenesDeTrabajo> resultOrdenesTrabajoForJasper) {
        List<ResultOrdenTrabajo> resultOrdenTrabajo = new ArrayList<ResultOrdenTrabajo>();       
        String hql="SELECT c FROM Campania c WHERE c.idCampania = "+idCampania;
        Campania c = dao.findObject(hql);        
        if (c!=null) {
            String nombre = c.getDescripcion();
            String periodo = String.valueOf(c.getPeriodo());
            Tbltipo tipo = c.getTbltipo();                
            List<OrdenTrabajo> ordenesTrabajo = ordenesTrabajoByCampania(idCampania, filtro);
            if (!ordenesTrabajo.isEmpty()){
                resultOrdenesTrabajoForJasper.add(new ordenesDeTrabajo(idCampania, nombre, periodo, tipo, ordenesTrabajo)); 
                for (Iterator itO = ordenesTrabajo.iterator(); itO.hasNext();) {
                    OrdenTrabajo o = (OrdenTrabajo) itO.next();
                    ResultOrdenTrabajo rot = new ResultOrdenTrabajo(idCampania, nombre, periodo, tipo, o);
                    resultOrdenTrabajo.add(rot);
                }
            }      
        }
        return resultOrdenTrabajo;
    }
    
        public double cantidadDeMacros(String con, String idCampania){
        double result;
        String hql;
        
        if (con.equals("Total")){
            hql="SELECT COUNT(*) FROM  OrdenTrabajo o WHERE o.campania.idCampania = "+idCampania
                    + " and o.tipoComponente=8";
            result = Double.valueOf(dao.findObject(hql).toString());
        }else{
            if (con.equals("Irregularidad")){
               hql="SELECT COUNT(*) FROM  OrdenTrabajo o WHERE o.campania.idCampania = "+idCampania
                    + " AND o.irregularidad='S' and o.tipoComponente=8"; 
               result = Double.valueOf(dao.findObject(hql).toString());
            }else{
               if (con.equals("Normales")){
                   hql="SELECT COUNT(*) FROM  OrdenTrabajo o WHERE o.campania.idCampania = "+idCampania
                    + " AND o.irregularidad='N' and o.tipoComponente=8";
                   result = Double.valueOf(dao.findObject(hql).toString());
               }else{
                    if (con.equals("Normalizados")){
                       hql="SELECT COUNT(*) FROM  OrdenTrabajo o WHERE o.campania.idCampania = "+idCampania
                        + " AND o.normalizado='S' and o.tipoComponente=8"; 
                       result = Double.valueOf(dao.findObject(hql).toString());
                    }else{
                       result=0;
                    }                     
               }  
            }        
        }   
        
        return result;
    }
        
    public List<OrdenTrabajo> ordenesTrabajoByCampania(String idCampania, String filtro){
        String hql="SELECT o FROM OrdenTrabajo o "
                 + "WHERE o.campania.idCampania = " +idCampania;
        
        if (filtro.equals("Irregularidad")) {
            hql=hql+" And o.irregularidad='S'";
        } 
        
        if(filtro.equals("SinResolver")){
            hql+=" And o.estado='ESO001'"; 
        }
        
        return dao.find(hql);        
    }
    
    public double cantidadDeClientes(String con, String idCampania){
        double result;
        String hql;
        
        if (con.equals("Total")){
            hql="SELECT COUNT(*) FROM  OrdenTrabajo o WHERE o.campania.idCampania = "+idCampania
                    + " and o.tipoComponente=9";
            result = Double.valueOf(dao.findObject(hql).toString());
        }else{
            if (con.equals("Irregularidad")){
               hql="SELECT COUNT(*) FROM  OrdenTrabajo o WHERE o.campania.idCampania = "+idCampania
                    + " AND o.irregularidad='S' and o.tipoComponente=9"; 
               result = Double.valueOf(dao.findObject(hql).toString());
            }else{
               if (con.equals("Normales")){
                   hql="SELECT COUNT(*) FROM  OrdenTrabajo o WHERE o.campania.idCampania = "+idCampania
                    + " AND o.irregularidad='N' and o.tipoComponente=9";
                   result = Double.valueOf(dao.findObject(hql).toString());
               }else{
                    if (con.equals("Normalizados")){
                       hql="SELECT COUNT(*) FROM  OrdenTrabajo o WHERE o.campania.idCampania = "+idCampania
                        + " AND o.normalizado='S' and o.tipoComponente=9"; 
                       result = Double.valueOf(dao.findObject(hql).toString());
                    }else{
                       result=0;
                    }                     
               }  
            }        
        }   
        
        return result;
    }
    
    @Override
    public List<Trafo> getTrafosByPeriodo(String periodo) {
        List<Trafo> trafosInconsistentes = new ArrayList<Trafo>();

        String hql = "SELECT c, b "
                + "FROM Componente c, Balances b "
                + "WHERE c.idComponente=b.balancesPK.idComponente and"
                + "      b.balancesPK.idTipoComponente=8 and"                
                + "      b.balancesPK.periodo=" + periodo;

        List data = dao.find(hql);
        if (data != null) {
            for (Iterator it = data.iterator(); it.hasNext();) {
                Object[] valores = (Object[]) it.next();
                Componente c = (Componente) valores[0];
                Balances b = (Balances) valores[1];
                trafosInconsistentes.add(new Trafo(c, b));
            }
        }

        return trafosInconsistentes;
    }

    @Override
    public List<Trafo> getTrafosInconsistentes(String periodo) {
       List<Trafo> trafosInconsistentes = new ArrayList<Trafo>();       
        
       /*String hql = "select c from Componente c "
            + "where c.idComponente in (select b.balancesPK.idComponente "
                     + "from Balances b, RangoClasificacion r "
                     + "where b.balancesPK.idTipoComponente=8 and b.rangoClasificacion.idRango=r.idRango and "
                      + "(r.descripcion='NEGATIVO' or r.descripcion='SIN BALANCE' or r.descripcion='INCONSISTENTE') "
                      + "and b.balancesPK.periodo=(select max(b.balancesPK.periodo) "
                                                + "from Balances b "
                                                  + "where b.balancesPK.idTipoComponente=8) ))";*/
       String hql="SELECT c, b "
                + "FROM Componente c, Balances b "
                + "WHERE c.idComponente=b.balancesPK.idComponente and"
                + "      b.balancesPK.idTipoComponente=8 and"
                + "      b.rangoClasificacion.descripcion in ('NEGATIVO','SIN BALANCE', 'INCONSISTENTE') and"
                + "      b.balancesPK.periodo="+periodo;
       
       List data = dao.find(hql);
       if (data != null) {
           for (Iterator it = data.iterator(); it.hasNext();) {
               Object[] valores = (Object[]) it.next();
               Componente c = (Componente) valores[0];
               Balances b = (Balances)valores[1];
               trafosInconsistentes.add(new Trafo(c, b));
           }  
       }                
      
      /*if (!dao.find(hql).isEmpty()){
         List<Componente> trafos = dao.find(hql);
         for (Iterator it = trafos.iterator(); it.hasNext();) {
             Componente t = (Componente) it.next();  
             List<Componente> suministros = getSuministros(t.getIdComponente().toString());
             Trafo i = new Trafo(t, suministros);
             trafosInconsistentes.add(i);
         }
      }*/
          
      return trafosInconsistentes;
    }
    
    public List<Componente> getSuministros(String idComponente){
        String hql="select rc.componente from RelComponente rc "
                 + "where rc.periodoFin=999912 and "
                 + "rc.componente1.idComponente= " +idComponente;
        return dao.find(hql);
    }

    @Override
    public Componente getComponente(String idComponente) {
        String hql = "SELECT c FROM Componente c WHERE c.idComponente ="+idComponente;
        return dao.findObject(hql);
    }

    @Override
    public ZonaGeografica getZona(String idZona, String tipo) {
        String hql="SELECT z FROM ZonaGeografica z WHERE z.idZona = "+idZona
                  +" and z.tipoComponente.idTipoComponente = "+tipo ;
        return dao.findObject(hql);
    }
    
    @Override
    public ZonaGeografica getZona(String idZona) {
        String hql="SELECT z FROM ZonaGeografica z WHERE z.idZona = "+idZona;
        return dao.findObject(hql);
    }

    @Override
    public List<String> getPeriodos() {
        String hql="SELECT distinct(b.balancesPK.periodo) FROM Balances b"
                 + " order by b.balancesPK.periodo desc";
        return dao.find(hql);
    }

    @Override
    public List<AtrComponente> getTrafosAtrByEmpresa(String id) {       
        String hql ="SELECT atr FROM RelComponenteUbicacion r, AtrComponente atr "
                  + "WHERE r.periodoFin=999912 and "
                  + "atr.componente.idComponente=r.relComponenteUbicacionPK.idComponente and "
                  + "r.relComponenteUbicacionPK.idZona in (SELECT z.idZona"
                                                             + " FROM ZonaGeografica z"
                                                             + " WHERE z.idPadre in (SELECT z.idZona"
                                                                                 + " FROM ZonaGeografica z"
                                                                                 + " WHERE z.idPadre in (SELECT zh.idZona"
                                                                                                     + " FROM ZonaGeografica zp, ZonaGeografica zh"
                                                                                                     + " WHERE zp.idZona=zh.idPadre and zp.idZona="+id+")))";
        return dao.find(hql);
    }

    @Override
    public List<AtrComponente> getTrafosAtrByZona(String id) {
        String hql ="SELECT atr FROM RelComponenteUbicacion r, AtrComponente atr "
                  + "WHERE r.periodoFin=999912 and "
                  + "atr.componente.idComponente=r.relComponenteUbicacionPK.idComponente and "                  
                  + "r.relComponenteUbicacionPK.idZona in (SELECT z.idZona"
                                                             + " FROM ZonaGeografica z"
                                                             + " WHERE z.idPadre in (SELECT zh.idZona"
                                                                                 + " FROM ZonaGeografica zp, ZonaGeografica zh"
                                                                                 + " WHERE zp.idZona=zh.idPadre and zp.idZona="+id+"))";
        return dao.find(hql);
    }

    @Override
    public List<AtrComponente> getTrafosAtrByCircuitoOrBarrio(String id) {
        String hql ="SELECT atr FROM RelComponenteUbicacion r, AtrComponente atr "
                   +"WHERE r.periodoFin=999912 and "
                   +"atr.componente.idComponente=r.relComponenteUbicacionPK.idComponente and " 
                   +"r.relComponenteUbicacionPK.idZona="+id;
        return dao.find(hql);
    }

    @Override
    public List<AtrComponente> getTrafosAtrBySubestacion(String id) {
          String hql ="SELECT atr FROM RelComponenteUbicacion r, AtrComponente atr "
                  + "WHERE r.periodoFin=999912 and "
                  + "atr.componente.idComponente=r.relComponenteUbicacionPK.idComponente and "
                  + "r.relComponenteUbicacionPK.idZona in (SELECT z.idZona"
                                                             + " FROM ZonaGeografica z"
                                                             + " WHERE z.idZona in (SELECT zh.idZona"
                                                                                + " FROM ZonaGeografica zp, ZonaGeografica zh"
                                                                                + " WHERE zp.idZona=zh.idPadre and zp.idZona="+id+"))";
        return dao.find(hql);
    }

    @Override
    public List<AtrComponente> getTrafosAtrByVista(String vista) {
        String hql ="SELECT atr "
                + "FROM RelComponenteUbicacion rcu, AtrComponente atr  "
                + "WHERE rcu.periodoFin=999912 and "
                + "atr.componente.idComponente=rcu.relComponenteUbicacionPK.idComponente and "
                + "rcu.componente.tipoComponente.idTipoComponente=8 and "
                + "rcu.zonaGeografica.tipoComponente.tbltipo.tipo='"+vista+"'";
        return dao.find(hql);
    }

    @Override
    public List<AtrComponente> getTrafoAtr(String id) {
        String hql ="SELECT atr FROM AtrComponente atr WHERE atr.componente.idComponente="+id;
        return dao.find(hql);
    }

    @Override
    public List<AtrComponente> getClientesAtrByEmpresa(String id) {
         String subhql ="(SELECT r.componente.idComponente FROM RelComponenteUbicacion r "
                        + "WHERE r.periodoFin=999912 and "
                        + "r.relComponenteUbicacionPK.idZona in (SELECT z.idZona"
                                                                    + " FROM ZonaGeografica z"
                                                                    + " WHERE z.idPadre in (SELECT z.idZona"
                                                                                        + " FROM ZonaGeografica z"
                                                                                        + " WHERE z.idPadre in (SELECT zh.idZona"
                                                                                                            + " FROM ZonaGeografica zp, ZonaGeografica zh"
                                                                                                     + " WHERE zp.idZona=zh.idPadre and zp.idZona="+id+"))))";
         String hql ="SELECT atr FROM RelComponente r, AtrComponente atr "
                   + "WHERE atr.componente.idComponente=r.relComponentePK.idComponenteHijo and "
                   + "r.relComponentePK.idComponente in "+subhql;
         
        return dao.find(hql);
    }

    @Override
    public List<AtrComponente> getClientesAtrByZona(String id) {
        String subhql ="(SELECT r.componente.idComponente FROM RelComponenteUbicacion r "
                  + "WHERE r.periodoFin=999912 and "
                  + "r.relComponenteUbicacionPK.idZona in (SELECT z.idZona"
                                                             + " FROM ZonaGeografica z"
                                                             + " WHERE z.idPadre in (SELECT zh.idZona"
                                                                                 + " FROM ZonaGeografica zp, ZonaGeografica zh"
                                                                                 + " WHERE zp.idZona=zh.idPadre and zp.idZona="+id+")))";
         String hql ="SELECT atr FROM RelComponente r, AtrComponente atr "
                   + "WHERE atr.componente.idComponente=r.relComponentePK.idComponenteHijo and "
                   + "r.relComponentePK.idComponente in "+subhql;
         
        return dao.find(hql);
    }

    @Override
    public List<AtrComponente> getClientesAtrByCircuitoOrBarrio(String id) {
        String subhql ="(SELECT r.componente.idComponente FROM RelComponenteUbicacion r "
                   +"WHERE r.periodoFin=999912 and r.relComponenteUbicacionPK.idZona="+id+")";
        
        String hql ="SELECT atr FROM RelComponente r, AtrComponente atr "
                   + "WHERE atr.componente.idComponente=r.relComponentePK.idComponenteHijo and "
                   + "r.relComponentePK.idComponente in "+subhql;
        
        return dao.find(hql);
    }

    @Override
    public List<AtrComponente> getClientesAtrBySubestacion(String id) {
        String subhql ="(SELECT r.componente.idComponente FROM RelComponenteUbicacion r "
                  + "WHERE r.periodoFin=999912 and "
                  + "r.relComponenteUbicacionPK.idZona in (SELECT z.idZona"
                                                             + " FROM ZonaGeografica z"
                                                             + " WHERE z.idZona in (SELECT zh.idZona"
                                                                                + " FROM ZonaGeografica zp, ZonaGeografica zh"
                                                                                + " WHERE zp.idZona=zh.idPadre and zp.idZona="+id+")))";
        String hql ="SELECT atr FROM RelComponente r, AtrComponente atr "
                   + "WHERE atr.componente.idComponente=r.relComponentePK.idComponenteHijo and "
                   + "r.relComponentePK.idComponente in "+subhql;
        
        return dao.find(hql);
    }

    @Override
    public List<AtrComponente> getClientesAtrByVista(String vista) {
        String subhql ="(SELECT rcu.componente.idComponente "
                + "FROM RelComponenteUbicacion rcu "
                + "WHERE rcu.periodoFin=999912 and "
                + "rcu.componente.tipoComponente.idTipoComponente=8 and "
                + "rcu.zonaGeografica.tipoComponente.tbltipo.tipo='"+vista+"')";
        
        String hql ="SELECT atr FROM RelComponente r, AtrComponente atr "
                   + "WHERE atr.componente.idComponente=r.relComponentePK.idComponenteHijo and "
                   + "r.relComponentePK.idComponente in "+subhql;
        
        return dao.find(hql);
    }

    @Override
    public List<AtrComponente> getClienteAtrByTrafo(String id) {
        String hql ="SELECT atr FROM RelComponente r, AtrComponente atr "
                   + "WHERE atr.componente.idComponente=r.relComponentePK.idComponenteHijo and "
                   + "r.relComponentePK.idComponente = "+id;
        return dao.find(hql);
    }
}
