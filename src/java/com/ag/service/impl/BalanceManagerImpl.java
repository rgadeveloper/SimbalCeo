/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ag.service.impl;

import com.ag.dao.Dao;
import com.ag.model.*;
import com.ag.model.view.*;
import com.ag.service.BalanceManager;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 *
 * @author Larry
 */
@Service("BalanceManager")
public class BalanceManagerImpl implements BalanceManager {

    @Autowired
    @Qualifier("DaoHibernate")
    private Dao dao;

    
    @Override
    public List<DataBalanceHijo> cuadroMando(String idComponente, String tipoComponente, String periodo) {
        String hql = "select h.idZona,h.nombre,h.tipoComponente.descripcion,b.porcPerdidaMes, ' ' "
                + " from Balances b, ZonaGeografica p, ZonaGeografica h "
                + " where b.balancesPK.periodo= " + periodo
                + "  and b.balancesPK.idComponente = " + idComponente
                + "  and b.balancesPK.idTipoComponente = " + tipoComponente
                + "  and h.idPadre = b.balancesPK.idComponente "
                + "  and p.idZona = h.idPadre "
                + "  and p.tipoComponente.idTipoComponente =b.balancesPK.idTipoComponente";

        List data = dao.find(hql);
        if (data.isEmpty()) {
            hql = "SELECT r.componente.idComponente,  r.componente.nombre, r.componente.tipoComponente.descripcion,b.porcPerdidaMes,r.componente.idCliente "
                    + " FROM Balances b , RelComponenteUbicacion r "
                    + " WHERE b.balancesPK.periodo= " + periodo
                    + " AND r.zonaGeografica.idZona = " + idComponente
                    + " AND r.zonaGeografica.tipoComponente.idTipoComponente = " + tipoComponente
                    + " AND r.relComponenteUbicacionPK.periodoIni <= " + periodo
                    + " AND r.periodoFin       >" + periodo
                    + " AND b.balancesPK.idComponente =  r.componente.idComponente "
                    + " AND b.balancesPK.idTipoComponente =  r.componente.tipoComponente.idTipoComponente";
            data = dao.find(hql);
        } else if (data.isEmpty()) {
            hql = "SELECT r.componente1.idComponente,  r.componente1.nombre, r.componente1.tipoComponente.descripcion,b.porcPerdidaMes,r.componente1.idCliente "
                    + "  FROM Balances b , RelComponenteUbicacion r "
                    + " WHERE r.relComponentePK.idComponente = " + idComponente
                    + "   AND r.componente.tipoComponente.idTipoComponente = " + tipoComponente
                    + "   AND  r.relComponentePK.periodoIni <= " + periodo
                    + "   AND r.periodoFin> " + periodo
                    + "   AND b.balancesPK.idComponente =  r.componente1.idComponente "
                    + "   AND b.balancesPK.idTipoComponente =  r.componente1.tipoComponente.idTipoComponente";
            data = dao.find(hql);
        }

        List<DataBalanceHijo> hijos = new ArrayList<DataBalanceHijo>();
        if (data != null) {
            for (Iterator it = data.iterator(); it.hasNext();) {
                Object[] valores = (Object[]) it.next();
                String idComponenteHijo = String.valueOf(valores[0]);
                String nombre = String.valueOf(valores[1]);
                String tipo = String.valueOf(valores[2]);
                String perdidas = String.valueOf(valores[3]);
                String idCliente = String.valueOf(valores[4]);
                DataBalanceHijo hijo = new DataBalanceHijo(idComponenteHijo, nombre, tipo, perdidas, idCliente);
                hijos.add(hijo);
            }
        }
        return hijos;

    }

    /**
     * Devuelve un objeto de tipo Balances para el IdComponente,tipoComponente y periodo pasado por parametro.
     * @param idComponente
     * @param tipoComponente
     * @param periodo
     * @return 
     */
    @Override
    public Balances getBalances(String idComponente, String tipoComponente, String periodo) {
        String hql = "select b "
                + " from Balances b "
                + " where b.balancesPK.idComponente= " + idComponente
                + " and   b.balancesPK.idTipoComponente= " + tipoComponente
                + " and    b.balancesPK.periodo= " + periodo;
        return dao.findObject(hql);
    }

    /**
     * Este metodo devuelve un objeto de tipo DataRangosBalance, que contiene las cantidades de transformadores por cada rango
     * tomando como referencia la zona.
     * @param idZona
     * @param tipoComponente
     * @param tipoArbol
     * @param periodo
     * @return 
     */
    @Override
    public DataRangosBalance getRangosZonas(String idZona, String tipoComponente, String tipoArbol, String periodo) {
        DataRangosBalance rangoBalance = new DataRangosBalance();
        String hql = null;
        if (tipoComponente.equals("4") || tipoComponente.equals("0")) {
            hql = " select  b.rangoClasificacion.descripcion, count(*) "
                    + "  from Balances b,"
                    + " RelComponenteUbicacion ru,"
                    + " ZonaGeografica z2,"
                    + " ZonaGeografica z3,"
                    + " ZonaGeografica z4 "
                    + " where b.balancesPK.idComponente = ru.componente.idComponente "
                    + " and b.balancesPK.periodo = " + periodo
                    + " and b.balancesPK.idTipoComponente = 8" 
                    + " and b.balancesPK.idTipoComponente = ru.componente.tipoComponente.idTipoComponente"
                    + " and ru.zonaGeografica.idPadre = z2.idZona "
                    + " and ru.relComponenteUbicacionPK.periodoIni <="+periodo
                    + " and ru.periodoFin > "+periodo
                    + " and z2.idPadre = z3.idZona "
                    + " and z3.idPadre = z4.idZona "
                    + " and z4.idZona = " + idZona
                    + " and z4.tipoComponente.idTipoComponente = " + tipoComponente
                    + " and z4.tipoComponente.tbltipo.tipo =  '" + tipoArbol + "' "
                    //+ " and ru.periodoFin=999912"
                    + " group by b.rangoClasificacion.idRango, b.rangoClasificacion.descripcion";
        } else if (tipoComponente.equals("5") || tipoComponente.equals("1")) {
            hql = " select  b.rangoClasificacion.descripcion, count(*) "
                    + "  from Balances b,"
                    + " RelComponenteUbicacion ru,"
                    + " ZonaGeografica z2,"
                    + " ZonaGeografica z3"
                    + " where b.balancesPK.idComponente = ru.componente.idComponente "
                    + " and b.balancesPK.idTipoComponente = ru.componente.tipoComponente.idTipoComponente"
                    + " and b.balancesPK.periodo = " + periodo
                    + " and b.balancesPK.idTipoComponente = 8"                     
                    + " and ru.zonaGeografica.idPadre = z2.idZona "
                    + " and ru.relComponenteUbicacionPK.periodoIni <="+periodo
                    + " and ru.periodoFin > "+periodo
                    + " and z2.idPadre = z3.idZona "
                    + " and z3.idZona = " + idZona
                    + " and z3.tipoComponente.idTipoComponente = " + tipoComponente
                    + " and z3.tipoComponente.tbltipo.tipo =  '" + tipoArbol + "' "
                    //+ " and ru.periodoFin=999912"
                    + " group by b.rangoClasificacion.idRango, b.rangoClasificacion.descripcion";
        } else if (tipoComponente.equals("6") || tipoComponente.equals("2")) {
            hql = " select  b.rangoClasificacion.descripcion, count(*) "
                    + "  from Balances b,"
                    + " RelComponenteUbicacion ru,"
                    + " ZonaGeografica z2"
                    + " where b.balancesPK.idComponente = ru.componente.idComponente "
                    + " and b.balancesPK.periodo = " + periodo
                    + " and ru.relComponenteUbicacionPK.periodoIni <="+periodo
                    + " and ru.periodoFin > "+periodo
                    + " and b.balancesPK.idTipoComponente = 8" 
                    + " and b.balancesPK.idTipoComponente = ru.componente.tipoComponente.idTipoComponente"
                    + " and ru.zonaGeografica.idPadre = z2.idZona "
                    + " and z2.tipoComponente.idTipoComponente = " + tipoComponente
                    + " and z2.idZona = " + idZona
                    + " and z2.tipoComponente.tbltipo.tipo =  '" + tipoArbol + "' "
                    //+ " and ru.periodoFin=999912"
                    + " group by b.rangoClasificacion.idRango, b.rangoClasificacion.descripcion";
        } else if (tipoComponente.equals("7") || tipoComponente.equals("3")) {
            hql = " select  b.rangoClasificacion.descripcion, count(*) "
                    + "  from Balances b,"
                    + " RelComponenteUbicacion ru"
                    + " where b.balancesPK.idComponente = ru.componente.idComponente "
                    + " and b.balancesPK.idTipoComponente = 8" 
                    + " and b.balancesPK.idTipoComponente = ru.componente.tipoComponente.idTipoComponente"
                    + " and b.balancesPK.periodo = " + periodo
                    + " and ru.relComponenteUbicacionPK.periodoIni <="+periodo
                    + " and ru.periodoFin > "+periodo
                    + " and ru.zonaGeografica.tipoComponente.idTipoComponente = " + tipoComponente
                    + " and ru.zonaGeografica.tipoComponente.tbltipo.tipo =  '" + tipoArbol + "' "
                    + " and ru.zonaGeografica.idZona = " + idZona
                    //+ " and ru.periodoFin=999912"
                    + " group by b.rangoClasificacion.idRango, b.rangoClasificacion.descripcion";
        }
        if (hql != null) {
            List data = dao.find(hql);
            if (data != null) {
                for (Iterator it = data.iterator(); it.hasNext();) {
                    Object[] valores = (Object[]) it.next();
                    if (String.valueOf(valores[0]).equalsIgnoreCase("CRITICO")) {
                        rangoBalance.setCritico(String.valueOf(valores[1]));
                    } else if (String.valueOf(valores[0]).equalsIgnoreCase("MEDIO")) {
                        rangoBalance.setMedio(String.valueOf(valores[1]));
                    } else if (String.valueOf(valores[0]).equalsIgnoreCase("BAJO")) {
                        rangoBalance.setBajo(String.valueOf(valores[1]));
                    } else if (String.valueOf(valores[0]).equalsIgnoreCase("NEGATIVO")) {
                        rangoBalance.setNegativos(String.valueOf(valores[1]));
                    } else if (String.valueOf(valores[0]).equalsIgnoreCase("INCONSISTENTE")) {
                        rangoBalance.setInconsistentes(String.valueOf(valores[1]));
                    }else if (String.valueOf(valores[0]).equalsIgnoreCase("SIN BALANCE")) {
                        rangoBalance.setSinBalance(String.valueOf(valores[1]));
                    }
                }
            }
        }
        return rangoBalance;

    }
    
    @Override
    public Medida getMedida(String idComponente, String periodo, String idTipoCompo){
        String hql = "select m from Medida m"
                   + " where m.medidaPK.idComponente = " + idComponente 
                   + " and m.medidaPK.periodo = " + periodo
                   + " and m.medidaPK.idTipoComponente = "+idTipoCompo;
        return dao.findObject(hql);
    }
    
    @Override
    public AtrComponente getAtrComponente(String idComponente){
        String hql = "select a from AtrComponente a"
                   + " where a.componente.idComponente = " + idComponente;
        return dao.findObject(hql);
    }
    
    @Override
    public RelComponenteMedida getRelComponenteMedida(String idComponente,String periodo){
        String hql = "select r from RelComponenteMedida r "
                   + " where r.componente.idComponente = " + idComponente
                   + " and r.relComponenteMedidaPK.periodoIni <="+periodo
                   + " and r.periodoFin > " + periodo;
        return dao.findObject(hql);
    }
    
    @Override
    public AtrComponenteMedida getAtrComponenteMedida(String idComponenteMedida,String periodo){
        String hql = "select a from AtrComponenteMedida a"
                   + " where a.componenteMedida.idComponenteMedida = " + idComponenteMedida
                   + " and a.atrComponenteMedidaPK.periodoIni <="+periodo
                   + " and a.periodoFin > " + periodo;
        return dao.findObject(hql);
    }
    
	@Override
    public ComponenteMedida getComponenteMedida(String idComponente,String periodo){
        String hql = " select c from RelComponenteMedida r, ComponenteMedida c "
                    + " where r.componente.idComponente = " + idComponente
                    + " and r.relComponenteMedidaPK.periodoIni <="+periodo
                    + " and r.periodoFin > " + periodo
                    + " and c.idComponenteMedida = r.relComponenteMedidaPK.idComponenteMedida ";
        return dao.findObject(hql);
    }


	@Override
    public Componente getComponente(String idComponente){
        String hql =" select c from Componente c "
                     + " where c.idComponente = " +idComponente;
        return dao.findObject(hql);
    }

    @Override
    public PadreHijo getSubestacionCircuito(String idComponente,String periodo){
        String hql = "select  rcu.zonaGeografica.idZona, rcu.zonaGeografica.nombre, z.idZona, z.nombre"
                   + " from RelComponenteUbicacion rcu, ZonaGeografica z "
                   + " where rcu.relComponenteUbicacionPK.idComponente =  " + idComponente
                   + " and  rcu.relComponenteUbicacionPK.periodoIni <= "+periodo
                   + " and  rcu.periodoFin> " + periodo
                   + " and  rcu.zonaGeografica.idPadre = z.idZona " 
                   + " and  rcu.zonaGeografica.tipoComponente.tbltipo.tipo = 'NIV100'";
        Object [] data = dao.findObject(hql);
        PadreHijo ph;
        if(data != null)
            ph = new PadreHijo(String.valueOf(data[0]),String.valueOf(data[1]),String.valueOf(data[2]),String.valueOf(data[3]));
        else 
            ph = new PadreHijo("","","","");
        return ph;
    }

    @Override
    public PadreHijo getZonaMunicipio(String idComponente,String periodo){
        String hql = "select z.idZona, z.nombre, z1.idZona, z1.nombre"
                   + " from RelComponenteUbicacion rcu, ZonaGeografica z, ZonaGeografica z1 "
                   + " where rcu.relComponenteUbicacionPK.idComponente =  " + idComponente
                   + " and  rcu.relComponenteUbicacionPK.periodoIni <= "+periodo
                   + " and  rcu.periodoFin> " + periodo
                   + " and  rcu.zonaGeografica.idPadre = z.idZona " 
                   + " and  rcu.zonaGeografica.tipoComponente.tbltipo.tipo = 'NIV200'"
                   + " and  z.idPadre = z1.idZona";
        Object [] data = dao.findObject(hql);
        PadreHijo ph;
        if(data != null)
            ph = new PadreHijo(String.valueOf(data[0]),String.valueOf(data[1]),String.valueOf(data[2]),String.valueOf(data[3]));
        else 
            ph = new PadreHijo("","","","");
        return ph;
    }
    
    @Override
    public List<DataValue> getCantSumXTipoUso(String idComponente,String periodo){
        String hql = " select atr.tbltipo4.tipo,atr.tbltipo4.nombre,count(*) "
                     + " from RelComponente r, AtrComponente atr "
                     + " where r.componente.idComponente = " +idComponente
                     + " and atr.componente.idComponente =  r.componente1.idComponente "
                     + " and r.relComponentePK.periodoIni <= " + periodo
                     + " and r.periodoFin > " +periodo
                     + " and atr.periodoIni <= " + periodo
                     + " and atr.periodoFin > " + periodo
                     + " group by atr.tbltipo4.tipo,atr.tbltipo4.nombre" ;
        List data =  dao.find(hql);
        List<DataValue> listCombo = new ArrayList();
        if(data!= null)
        for (int i = 0; i < data.size(); i++) {
            Object [] dTu = (Object[]) data.get(i);
            DataValue tu = new DataValue(String.valueOf(dTu[0]),String.valueOf(dTu[1]),String.valueOf(dTu[2]));
            listCombo.add(tu);
        }
        return listCombo;
    }

    
    @Override
    public String getCantSumNoMedidos(String idComponente,String periodo){
        String hql = " select count(*) "
                     + " from RelComponente r, AtrComponente atr " 
                     + " where r.componente.idComponente = "  + idComponente
                     + " and atr.componente.idComponente =  r.componente1.idComponente " 
                     + " and r.relComponentePK.periodoIni <= " + periodo
                     + " and r.periodoFin > " +periodo 
                     + " and atr.periodoIni <= " + periodo
                     + " and atr.periodoFin > " + periodo
                     + " and atr.tbltipo2.tipo = 'TSU002' "
                     + " group by r.componente.idComponente" ;
        String cantidad =   String.valueOf(dao.findObject(hql)); 
        if(cantidad.equals("null"))
            cantidad = "0";
        return cantidad;
    }


    @Override
     public List<Medida> getListMedida(String idComponente, int periodo){

         String hql = "select valor from Parametro where idParametro='HISTORICO_MEDIDA'";
         int periodoParam =  Integer.parseInt(String.valueOf(dao.findObject(hql)));
         int periodos = periodo-periodoParam;
         hql ="";
         
         hql = "select m from Medida m" +
                  " where medidaPK.idComponente = " + idComponente
                 + " and  medidaPK.periodo      >= " + periodos;


          return dao.find(hql);

    }

    @Override
    public int getNumMesesAtras() {       
        String hql ="SELECT p.valor FROM Parametro p WHERE p.idParametro ='MESES_ATRAS'";
        return Integer.valueOf(dao.findObject(hql).toString());   
    }
        
    @Override
    public List<Componente> getTrafosByRango(String id, String tipo, String periodo, String descripcion) {        
        String hql="";        
        if (tipo.equals("4") || tipo.equals("0")) { //empresa
             hql ="SELECT r.componente FROM RelComponenteUbicacion r "
                  + "WHERE r.periodoFin>"+periodo+" and "
                     + "r.relComponenteUbicacionPK.idZona in (SELECT z.idZona"
                                                             + " FROM ZonaGeografica z"
                                                             + " WHERE z.idPadre in (SELECT z.idZona"
                                                                                 + " FROM ZonaGeografica z"
                                                                                 + " WHERE z.idPadre in (SELECT zh.idZona"
                                                                                                     + " FROM ZonaGeografica zp, ZonaGeografica zh"
                                                                                                     + " WHERE zp.idZona=zh.idPadre and zp.idZona="+id+")))";
        } else if (tipo.equals("5") || tipo.equals("1")) { //zona
              hql ="SELECT r.componente FROM RelComponenteUbicacion r "
                  + "WHERE r.periodoFin>"+periodo+" and "
                      + "r.relComponenteUbicacionPK.idZona in (SELECT z.idZona"
                                                             + " FROM ZonaGeografica z"
                                                             + " WHERE z.idPadre in (SELECT zh.idZona"
                                                                                 + " FROM ZonaGeografica zp, ZonaGeografica zh"
                                                                                + " WHERE zp.idZona=zh.idPadre and zp.idZona="+id+"))";
           } else if (tipo.equals("6") || tipo.equals("2")) { //municipio o subestacion
                 hql ="SELECT r.componente FROM RelComponenteUbicacion r "
                  + "WHERE r.periodoFin>"+periodo+" and "
                         + "r.relComponenteUbicacionPK.idZona in (SELECT z.idZona"
                                                             + " FROM ZonaGeografica z"
                                                             + " WHERE z.idZona in (SELECT zh.idZona"
                                                                                + " FROM ZonaGeografica zp, ZonaGeografica zh"
                                                                                + " WHERE zp.idZona=zh.idPadre and zp.idZona="+id+"))";
           } else if (tipo.equals("7") || tipo.equals("3")) { // Barrio o Circuito             
                  hql ="SELECT r.componente FROM RelComponenteUbicacion r "
                   +"WHERE r.periodoFin>"+periodo+" and "
                        + "r.relComponenteUbicacionPK.idZona="+id;
           }
        
         String hql2 ="select c from Componente c "
                    + "where c.tipoComponente.idTipoComponente=8 and "
                          + "c.idComponente in (select b.balancesPK.idComponente "
                                             + "from Balances b, RangoClasificacion r "
                                             + "where b.balancesPK.idTipoComponente=8 and b.rangoClasificacion.idRango=r.idRango "
                                             + "and r.descripcion='"+descripcion+"' "
                                             + "and b.balancesPK.periodo="+periodo+" "
                                             + "and b.balancesPK.idComponente in ("+hql+"))";
         return dao.find(hql2);
        
    }

    @Override
    public ZonaGeografica getPadreByTipo(String idComponente, String tipo) {
         String hql="SELECT z FROM RelComponenteUbicacion r, ZonaGeografica z "
                  + "WHERE r.relComponenteUbicacionPK.idZona=z.idZona "
                        + "and r.periodoFin=999912 and z.tipoComponente.idTipoComponente="+tipo
                        + " and r.relComponenteUbicacionPK.idComponente="+idComponente;
         return dao.findObject(hql);
    }

    @Override
    public List<Novedades> getNovedadesByTrafo(String idComponenteMedida) {
        String hql="SELECT n FROM Novedades n WHERE n.novedadesPK.idComponenteMedida ="+idComponenteMedida;
        return dao.find(hql);
    }

    @Override
    public void saveNovedad(String usuario, String programa, int periodo, String idTipoNovedad, ComponenteMedida componenteMedida) {
        Novedades n = new Novedades();
        n.setUsuario(usuario);
        n.setPrograma(programa);
        Fecha fecha = new Fecha();
        n.setFechaModif(fecha.getFecha());
        n.setFecha(fecha.getFechaSistema());
        n.setPeriodo(periodo);
        n.setComponenteMedida(componenteMedida);
        n.setTbltipo(getTipo(idTipoNovedad));
        
        NovedadesPK novedadesPK = new NovedadesPK(componenteMedida.getIdComponenteMedida().toBigInteger(), idTipoNovedad);
        n.setNovedadesPK(novedadesPK);
        dao.persist(n);
    }

    @Override
    public void editNovedad(Novedades n) {
        Fecha fecha = new Fecha();
        n.setFechaModif(fecha.getFecha());
        dao.persist(n);
    }
    
    @Override
    public List<Tbltipo> getTiposNovedades() {
        String hql = "select t "
                + " from Tbltipo t"
                + " where t.grupo.codigo='TIN000'";
        return dao.find(hql);
        
    }
    
    @Override
    public Tbltipo getTipo(String tipo) {
        String hql = "Select t from Tbltipo t where t.tipo ='" + tipo + "'";
        return dao.findObject(hql);
    }

    @Override
    public void deleteNovedad(Novedades n) {
        dao.delete(n);
    }
    
    
     /**
     * @autor <b>Jose Mejia</b>
     * @see MapBean
     * @since 29/10/2014
     * @param codComp
     * @param periodo
     * @param tipoC
     * @return 
     */
    @Override
    public Balances getBalancesAll(String codComp, String periodo, String tipoC) {
        return dao.findObject("From Balances b where b.balancesPK.idComponente="+codComp+" and b.balancesPK.periodo="+periodo+" and b.balancesPK.idTipoComponente="+tipoC);
    }
    
    
    /**
     * @autor <b>Jose Mejia</b>
     * @see MapBean
     * @since 29/10/2014
     * @param idComponente
     * @param tipoComponente
     * @return 
     */    
     @Override
    public Componente getComponente(String idComponente, String tipoComponente) {
        String hql = "select c "
                + " from Componente c "
                + " where c.idComponente= " + idComponente
                + " and   c.tipoComponente.idTipoComponente= " + tipoComponente;
        return dao.findObject(hql);
    }

}
