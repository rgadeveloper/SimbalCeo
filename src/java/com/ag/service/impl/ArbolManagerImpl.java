package com.ag.service.impl;

import com.ag.dao.Dao;
import com.ag.model.Balances;
import com.ag.model.Componente;

import com.ag.model.RangoClasificacion;
import com.ag.model.view.Nodo;
import com.ag.model.view.UbicacionMacroV;
import com.ag.service.ArbolManager;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 *
 * @author 85154220
 */
@Service("ArbolManager")
public class ArbolManagerImpl implements ArbolManager {

  @Autowired
  @Qualifier("DaoHibernate")
  private Dao dao;

  @Override
  public List cargaArbolInicial(String nivelGrupo, String periodo) {
    String parametro = "";
    if (nivelGrupo != null) {
      if (nivelGrupo.equals("NIV100")) {
        parametro = "NIVEL_ENERGETICO";
      } else {
        parametro = "NIVEL_GEOGRAFICO";
      }
    }

    String hql = "SELECT t.idZona, t.nombre,t.tipoComponente.idTipoComponente,t.coordX,t.coordY,t.tipoComponente.descripcion "
            + " FROM ZonaGeografica t ,  Parametro p"
            + " WHERE t.tipoComponente.idTipoComponente = p.valor "
            + " AND  t.tipoComponente.tbltipo.tipo = '" + nivelGrupo + "' "
            + " AND t.estado = 'AC001' "
            + " and p.idParametro ='" + parametro + "'";
    return obtenerArbol(hql, periodo, null);

  }

  @Override
  public List cargaArbolHijos(String codigoPadre, String tipoPadre, String periodo, HashMap gestionFiltros) {
    int contZona, contUbicacion, contComponente;

    String hql = "SELECT COUNT(*) "
            + " FROM ZonaGeografica p, ZonaGeografica h "
            + " WHERE h.idPadre = " + codigoPadre
            + " AND p.idZona = h.idPadre"
            + " AND p.tipoComponente.idTipoComponente = " + tipoPadre;
    contZona = Integer.parseInt(String.valueOf(dao.findObject(hql)));
    hql = "SELECT COUNT(*) "
            + " FROM RelComponenteUbicacion r "
            + " WHERE  r.zonaGeografica.idZona =  " + codigoPadre
            + " AND   r.zonaGeografica.tipoComponente.idTipoComponente = " + tipoPadre
            + " AND   r.relComponenteUbicacionPK.periodoIni <=" + periodo
            + " AND   r.periodoFin       > " + periodo;
    contUbicacion = Integer.parseInt(String.valueOf(dao.findObject(hql)));

    hql = "SELECT COUNT(*) "
            + "FROM RelComponente r "
            + " WHERE r.relComponentePK.idComponente =" + codigoPadre
            + " AND r.componente1.tipoComponente.idTipoComponente = " + tipoPadre
            + " AND  r.relComponentePK.periodoIni <= " + periodo
            + " AND r.periodoFin> " + periodo;
    contComponente = Integer.parseInt(String.valueOf(dao.findObject(hql)));
    String hqlArbol = null;
    if (contZona > 0) {
      hqlArbol = "SELECT  z.idZona, z.nombre,  z.tipoComponente.idTipoComponente,z.coordX,z.coordY,z.tipoComponente.descripcion "
              + " FROM ZonaGeografica z "
              + " WHERE z.idPadre =" + codigoPadre;
    } else if (contUbicacion > 0) {
      hqlArbol = "SELECT ru.componente.idComponente, ru.componente.nombre,"
              + " ru.componente.tipoComponente.idTipoComponente, "
              + " ru.componente.coordX,ru.componente.coordY,"
              + " ru.componente.tipoComponente.descripcion,"
              + " ru.componente.tbltipo1.nombre,"
              + " (SELECT a.tbltipo1.nombre FROM AtrComponente a WHERE a.componente.idComponente=ru.componente.idComponente) as tipoUso,"
              + " (SELECT a.tbltipo5.tipo FROM AtrComponente a WHERE a.componente.idComponente=ru.componente.idComponente) as tipoRed "
              + " FROM RelComponenteUbicacion ru "
              + " WHERE  ru.zonaGeografica.idZona = " + codigoPadre
              + "  AND  ru.zonaGeografica.tipoComponente.idTipoComponente = " + tipoPadre
              + "  AND  ru.relComponenteUbicacionPK.periodoIni <=" + periodo
              + "  AND  ru.periodoFin                          > " + periodo
              + " order by ru.componente.nombre";

    } else if (contComponente > 0) {
      hqlArbol = "SELECT r.relComponentePK.idComponenteHijo, r.componente.idCliente,"
              + " r.componente.tipoComponente.idTipoComponente,"
              + " nvl(r.componente.coordX, 0), nvl(r.componente.coordY, 0),"
              + " r.componente.tipoComponente.descripcion,"
              + " r.componente.tbltipo1.nombre,"
              + " (SELECT a.tbltipo1.nombre FROM AtrComponente a WHERE a.componente.idComponente=r.componente.idComponente) as tipoUso,"
              + " (SELECT a.tbltipo5.tipo FROM AtrComponente a WHERE a.componente.idComponente=r.componente.idComponente) as tipoRed "
              + " FROM RelComponente r "
              + " WHERE r.relComponentePK.idComponente =" + codigoPadre
              + "  AND  r.relComponentePK.periodoIni <= " + periodo
              + " AND r.periodoFin> " + periodo
              + " AND r.componente1.tipoComponente.idTipoComponente = " + tipoPadre
              + " order by r.componente.idCliente";
    }
    if (hqlArbol == null) {
      return null;
    }
    return obtenerArbol(hqlArbol, periodo, gestionFiltros);
  }

  @Override
  public List cargaArbolHijosTrafos(String codigoPadre, String tipoPadre, String periodo, HashMap gestionFiltros) {
    int contTrafosByEmpresa = 0, contTrafosByZona = 0, contTrafosBySubMun = 0;

    String hql;
    if (tipoPadre.equals("4") || tipoPadre.equals("0")) { //empresa
      hql = "SELECT COUNT(*) FROM RelComponenteUbicacion r "
              + "WHERE r.relComponenteUbicacionPK.periodoIni <=" + periodo + " and "
              + "r.periodoFin>" + periodo + " and "
              + "r.relComponenteUbicacionPK.idZona in (SELECT z.idZona"
              + " FROM ZonaGeografica z"
              + " WHERE z.idPadre in (SELECT z.idZona"
              + " FROM ZonaGeografica z"
              + " WHERE z.idPadre in (SELECT zh.idZona"
              + " FROM ZonaGeografica zp, ZonaGeografica zh"
              + " WHERE zp.idZona=zh.idPadre and zp.idZona=" + codigoPadre + ")))";
      contTrafosByEmpresa = Integer.parseInt(String.valueOf(dao.findObject(hql)));
    } else if (tipoPadre.equals("5") || tipoPadre.equals("1")) { //zona
      hql = "SELECT COUNT(*) FROM RelComponenteUbicacion r "
              + "WHERE r.relComponenteUbicacionPK.periodoIni <=" + periodo + " and "
              + "r.periodoFin>" + periodo + " and "
              + "r.relComponenteUbicacionPK.idZona in (SELECT z.idZona"
              + " FROM ZonaGeografica z"
              + " WHERE z.idPadre in (SELECT zh.idZona"
              + " FROM ZonaGeografica zp, ZonaGeografica zh"
              + " WHERE zp.idZona=zh.idPadre and zp.idZona=" + codigoPadre + "))";
      contTrafosByZona = Integer.parseInt(String.valueOf(dao.findObject(hql)));
    } else if (tipoPadre.equals("6") || tipoPadre.equals("2")) { //municipio o subestacion
      hql = "SELECT COUNT(*) FROM RelComponenteUbicacion r "
              + "WHERE r.relComponenteUbicacionPK.periodoIni <=" + periodo + " and "
              + "r.periodoFin>" + periodo + " and "
              + "r.relComponenteUbicacionPK.idZona in (SELECT z.idZona"
              + " FROM ZonaGeografica z"
              + " WHERE z.idZona in (SELECT zh.idZona"
              + " FROM ZonaGeografica zp, ZonaGeografica zh"
              + " WHERE zp.idZona=zh.idPadre and zp.idZona=" + codigoPadre + "))";
      contTrafosBySubMun = Integer.parseInt(String.valueOf(dao.findObject(hql)));
    }

    String hqlArbol = null;
    if (contTrafosByEmpresa > 0) {
      /*
       hqlArbol ="SELECT r.componente.idComponente, r.componente.nombre,"
       + " r.componente.tipoComponente.idTipoComponente, "
       + " r.componente.coordX,r.componente.coordY,"
       + " r.componente.tipoComponente.descripcion,"
       + " r.componente.tbltipo1.nombre,"
       + " (SELECT a.tbltipo1.nombre FROM AtrComponente a WHERE a.componente.idComponente=r.componente.idComponente) as tipoUso," 
       + " (SELECT a.tbltipo5.tipo FROM AtrComponente a WHERE a.componente.idComponente=r.componente.idComponente) as tipoRed "+
       " FROM RelComponenteUbicacion r "
       + "WHERE r.relComponenteUbicacionPK.periodoIni <=" + periodo +" and "
       + "r.periodoFin>"+periodo+" and "
       + "r.relComponenteUbicacionPK.idZona in (SELECT z.idZona"
       + " FROM ZonaGeografica z"
       + " WHERE z.idPadre in (SELECT z.idZona"
       + " FROM ZonaGeografica z"
       + " WHERE z.idPadre in (SELECT zh.idZona"
       + " FROM ZonaGeografica zp, ZonaGeografica zh"
       + " WHERE zp.idZona=zh.idPadre and zp.idZona="+codigoPadre+")))";
       */
      hqlArbol = "SELECT r.componente.idComponente, r.componente.nombre,"
              + " r.componente.tipoComponente.idTipoComponente, "
              + " r.componente.coordX,r.componente.coordY,"
              + " r.componente.tipoComponente.descripcion,"
              + " r.componente.tbltipo1.nombre,"
              + " (SELECT a.tbltipo1.nombre FROM AtrComponente a WHERE a.componente.idComponente=r.componente.idComponente) as tipoUso,"
              + " (SELECT a.tbltipo5.tipo FROM AtrComponente a WHERE a.componente.idComponente=r.componente.idComponente) as tipoRed "
              + " FROM RelComponenteUbicacion r , ZonaGeografica z1,ZonaGeografica z2,ZonaGeografica z3,ZonaGeografica z4 "
              + " WHERE r.relComponenteUbicacionPK.periodoIni <=" + periodo + " and "
              + " r.periodoFin>" + periodo
              + " and z1.idZona = " + codigoPadre
              + " and z2.idPadre = z1.idZona"
              + " and z3.idPadre = z2.idZona"
              + " and z4.idPadre = z3.idZona"
              + " and r.relComponenteUbicacionPK.idZona = z4.idZona ";
    } else if (contTrafosByZona > 0) {
      hqlArbol = "SELECT r.componente.idComponente, r.componente.nombre,"
              + " r.componente.tipoComponente.idTipoComponente, "
              + " r.componente.coordX,r.componente.coordY,"
              + " r.componente.tipoComponente.descripcion,"
              + " r.componente.tbltipo1.nombre,"
              + " (SELECT a.tbltipo1.nombre FROM AtrComponente a WHERE a.componente.idComponente=r.componente.idComponente) as tipoUso,"
              + " (SELECT a.tbltipo5.tipo FROM AtrComponente a WHERE a.componente.idComponente=r.componente.idComponente) as tipoRed "
              + " FROM RelComponenteUbicacion r "
              + "WHERE r.relComponenteUbicacionPK.periodoIni <=" + periodo + " and "
              + "r.periodoFin>" + periodo + " and "
              + "r.relComponenteUbicacionPK.idZona in (SELECT z.idZona"
              + " FROM ZonaGeografica z"
              + " WHERE z.idPadre in (SELECT zh.idZona"
              + " FROM ZonaGeografica zp, ZonaGeografica zh"
              + " WHERE zp.idZona=zh.idPadre and zp.idZona=" + codigoPadre + "))";
    } else if (contTrafosBySubMun > 0) {
      hqlArbol = "SELECT r.componente.idComponente, r.componente.nombre,"
              + " r.componente.tipoComponente.idTipoComponente, "
              + " r.componente.coordX,r.componente.coordY,"
              + " r.componente.tipoComponente.descripcion,"
              + " r.componente.tbltipo1.nombre,"
              + " (SELECT a.tbltipo1.nombre FROM AtrComponente a WHERE a.componente.idComponente=r.componente.idComponente) as tipoUso,"
              + " (SELECT a.tbltipo5.tipo FROM AtrComponente a WHERE a.componente.idComponente=r.componente.idComponente) as tipoRed "
              + " FROM RelComponenteUbicacion r "
              + "WHERE r.relComponenteUbicacionPK.periodoIni <=" + periodo + " and "
              + "r.periodoFin>" + periodo + " and "
              + "r.relComponenteUbicacionPK.idZona in (SELECT z.idZona"
              + " FROM ZonaGeografica z"
              + " WHERE z.idZona in (SELECT zh.idZona"
              + " FROM ZonaGeografica zp, ZonaGeografica zh"
              + " WHERE zp.idZona=zh.idPadre and zp.idZona=" + codigoPadre + "))";
    }

    if (hqlArbol == null) {
      return null;
    }
    return obtenerArbol(hqlArbol, periodo, gestionFiltros);

    //MOLLEJA
  }

  @Override
  public boolean soloTrafoInGmap(String tipo) {
    if (tipo.equals("4") || tipo.equals("0")
            || tipo.equals("5") || tipo.equals("1")
//            || tipo.equals("12") || tipo.equals("11")//Prueba
            || tipo.equals("6") || tipo.equals("2")) {
      String hql = "SELECT p.valor FROM Parametro p WHERE p.idParametro ='SOLO_COMPONENTES'";
      String valor = dao.findObject(hql).toString();
      return valor.equalsIgnoreCase("si") ? true : false;
    }
    return false;
  }

  public List obtenerArbol(String hql, String periodo, HashMap gestionFiltros) {
//        try{
//        List elemento = dao.find(hql);
//        List hijos = new ArrayList();
//        boolean sw_nodo=getMostrarNodo().equalsIgnoreCase("si")?
//                        false:true;
//        if(elemento!= null)
//            for(Iterator it = elemento.iterator();it.hasNext();){
//                Object [] valores = (Object[]) it.next();
//                String codigo = String.valueOf(valores[0]);                
//                String tipo = ((Short) valores[2]).toString();
//                Balances b = getBalances(codigo,tipo,periodo);
//                String perdidas = getPerdidas(b);
//                String color = getColor(tipo,perdidas);                
//                if (sw_nodo && !tipo.equals("9") && !tipo.equals("10")) {
//                  if (perdidas.equals("null")) continue;
//                  Double porcperd = Double.parseDouble(perdidas);
//                  if (porcperd>70 || porcperd<-5) continue; 
//                }               
//                String nombre = (String) valores[1];
//                String coordX = (String) valores[3];
//                String coordY = (String) valores[4];
//                //PTORRES.20140514.INI
//                if (!perdidas.equals("null")){
//                    Nodo hijo = new Nodo(codigo,nombre,tipo,color,perdidas,coordX,coordY);
//                    if(b != null){
//                        hijo.setNumMacroTot(String.valueOf(b.getTotalMacros()));
//                        hijo.setNumMacrosFuncionando(String.valueOf(b.getTotalMacrosFunc()));
//                        hijo.setNumSuministrosFact(String.valueOf(b.getCantSumFact()));
//                    }
//                    hijo.setNombreTipo((String) valores[5]);
//                    if (tipo.equals("8")) {
//                        //Gestionar filtros para Trafos
//                        String usoTrafo=(String) valores[7];
//                        String redTrafo=(String) valores[8];
//                        String rangoTrafo=getRango(b);
//                        if (gestionFiltros!=null && !gestionFiltros.isEmpty()) {
//                            String tipo_uso=(String)gestionFiltros.get("TIPO_USO");
//                            String tipo_red=(String)gestionFiltros.get("TIPO_RED");
//                            String rango=(String)gestionFiltros.get("RANGO");                        
//                            if(!tipo_uso.equals("NO") && !usoTrafo.equals(tipo_uso))
//                                continue;
//                            if(!tipo_red.equals("NO") && !redTrafo.equals(tipo_red))
//                                continue;
//                            if(!rango.equals("NO") && !rangoTrafo.equals(rango))
//                                continue;
//                        }
//                        hijo.setLocalizacion((String) valores[6]);                    
//                        hijo.setTipoUso(usoTrafo);
//                        hijo.setNombreColor(getNombreColor(perdidas));
//                    }        
//                    if (tipo.equals("9")) 
//                        hijo.setTipoUso((String) valores[7]);
//                    hijos.add(hijo);
//                }
//                //PTORRES.20140514.FIN
//            }
//        return hijos;
//        }catch(Exception e){
//            e.printStackTrace();
//            return null;
//        }

    try {
      List elemento = dao.find(hql);
      List hijos = new ArrayList();
      boolean sw_nodo = getMostrarNodo().equalsIgnoreCase("si")
              ? false : true;
      if (elemento != null) {
        for (Iterator it = elemento.iterator(); it.hasNext();) {
          Object[] valores = (Object[]) it.next();
          String codigo = String.valueOf(valores[0]);
          String tipo = ((Short) valores[2]).toString();
          Balances b = getBalances(codigo, tipo, periodo);
          String perdidas = getPerdidas(b);
          String color = getColor(tipo, perdidas);
          if (sw_nodo && !tipo.equals("9") && !tipo.equals("10")) {
            if (perdidas.equals("null")) {
              continue;
            }
            Double porcperd = Double.parseDouble(perdidas);
            if (porcperd > 70 || porcperd < -5) {
              continue;
            }
          }

          //LOB.20140802.INI
          //String nombre = (String) valores[1] ;
          String nombre;
          if (tipo.equals("8")) {
            //nombre = (String) valores[1] + " - " + (String) valores[9];
            nombre = (String) valores[1] + " - " + (String) valores[8];
          } else {
            nombre = (String) valores[1];
          }
          //LOB.20140803.FIN  

          String coordX = (String) valores[3];
          String coordY = (String) valores[4];
          //PTORRES.20140514.INI

          if (perdidas.equals("null") && tipo.equals("9")) {
            if (perdidas.equals("null")) {
              perdidas = "";
            }
            Nodo hijo = new Nodo(codigo, nombre, tipo, color, perdidas, coordX, coordY);
            if (b != null) {
              hijo.setNumMacroTot(String.valueOf(b.getTotalMacros()));
              hijo.setNumMacrosFuncionando(String.valueOf(b.getTotalMacrosFunc()));
              hijo.setNumSuministrosFact(String.valueOf(b.getCantSumFact()));
            }
            hijo.setNombreTipo((String) valores[5]);
            hijo.setTipoUso((String) valores[7]);
            hijos.add(hijo);
          } else if (!perdidas.equals("null")) {
            Nodo hijo = new Nodo(codigo, nombre, tipo, color, perdidas, coordX, coordY);
            if (b != null) {
              hijo.setNumMacroTot(String.valueOf(b.getTotalMacros()));
              hijo.setNumMacrosFuncionando(String.valueOf(b.getTotalMacrosFunc()));
              hijo.setNumSuministrosFact(String.valueOf(b.getCantSumFact()));
            }
            hijo.setNombreTipo((String) valores[5]);
            if (tipo.equals("8")) {
              //Gestionar filtros para Trafos
              String usoTrafo = (String) valores[7];
              String redTrafo = (String) valores[8];
              String rangoTrafo = getRango(b);
              if (gestionFiltros != null && !gestionFiltros.isEmpty()) {
                String tipo_uso = (String) gestionFiltros.get("TIPO_USO");
                String tipo_red = (String) gestionFiltros.get("TIPO_RED");
                String rango = (String) gestionFiltros.get("RANGO");
                if (!tipo_uso.equals("NO") && !usoTrafo.equals(tipo_uso)) {
                  continue;
                }
                if (!tipo_red.equals("NO") && !redTrafo.equals(tipo_red)) {
                  continue;
                }
                if (!rango.equals("NO") && !rangoTrafo.equals(rango)) {
                  continue;
                }
              }
              hijo.setLocalizacion((String) valores[6]);
              hijo.setTipoUso(usoTrafo);
              hijo.setNombreColor(getNombreColor(perdidas));
            }
            hijos.add(hijo);
          }
          //PTORRES.20140514.FIN
        }
      }
      return hijos;
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }

  }

  public String getColor(String tipoComponente, String perdidas) {

    String hql = !perdidas.equals("null") ? "select  r "
            + " from RangoClasificacion r  "
            + " where r.tipoComponente.idTipoComponente = " + tipoComponente
            + " and porcMinimo <= " + perdidas
            + " and porcMaximo > " + perdidas
            : //si no tiene balances
            "select  r "
            + " from RangoClasificacion r  "
            + " where r.tipoComponente.idTipoComponente = " + tipoComponente
            + " and porcMinimo is null "
            + " and porcMaximo is null ";
    RangoClasificacion objColor = (RangoClasificacion) dao.findObject(hql);
    String color = "color:rgb(212,208,200)"; //no ha sido asignado ningun rango
    if (objColor != null) {
      color = "color:rgb(" + objColor.getColor().getRojo() + "," + objColor.getColor().getVerde() + "," + objColor.getColor().getAzul() + ")";
    }
    return color;
  }

  public String getNombreColor(String perdidas) {
    String hql = !perdidas.equals("null") ? "select  r "
            + " from RangoClasificacion r  "
            + " where r.tipoComponente.idTipoComponente = 8"
            + " and porcMinimo <= " + perdidas
            + " and porcMaximo > " + perdidas
            : //si no tiene balances
            "select  r "
            + " from RangoClasificacion r  "
            + " where r.tipoComponente.idTipoComponente = 8"
            + " and porcMinimo is null "
            + " and porcMaximo is null ";
    RangoClasificacion objColor = (RangoClasificacion) dao.findObject(hql);
    String colorName = "none"; //no ha sido asignado ningun rango
    if (objColor != null) {
      colorName = objColor.getColor().getDescripcion();
    }
    return colorName;
  }

  public Balances getBalances(String codigo, String tipo, String periodo) {
    String hql = " SELECT b FROM Balances b "
            + " WHERE b.balancesPK.idComponente =  " + codigo
            + " and  b.balancesPK.periodo = " + periodo
            + " and  b.balancesPK.idTipoComponente=" + tipo;
    Balances bal = (Balances) dao.findObject(hql);
    return bal;
  }

  public String getPerdidas(Balances bal) {
    String perdidas = "null";
    if (bal != null && bal.getPorcPerdidaMes() != null) {
      perdidas = String.valueOf(bal.getPorcPerdidaMes());
    }
    return perdidas;
  }

  public String getRango(Balances bal) {
    String rango = "null";
    if (bal != null && bal.getRangoClasificacion() != null) {
      rango = bal.getRangoClasificacion().getDescripcion();
    }
    return rango;
  }

  @Override
  public List getZoomMapa() {
    String hql = " SELECT t FROM TipoComponente t ";
    List tip = dao.find(hql);
    return tip;
  }

  @Override
  public List getPeriodo() {
    String hql = " SELECT distinct  b.balancesPK.periodo  from Balances  b "
            + " order by  b.balancesPK.periodo desc";
    List periodo = dao.find(hql);
    List listCombo = new ArrayList();
    for (int i = 0; i < periodo.size(); i++) {
      String per = String.valueOf(periodo.get(i));
      listCombo.add(per);
    }
    return listCombo;
  }

  public static void main(String[] agr) {
    try {
      ArbolManagerImpl app = new ArbolManagerImpl();

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public String rutaTrafo(String idCliente, String tipoArbol, String periodo) throws SQLException {
    String ruta = idCliente + " no encontrado.";
    String sql = "select emp.nombre as n1, zona.nombre as n2, mun_sub.nombre as n3, bar_cir.nombre as n4, trafo.NOMBRE as n5, "
            + " emp.id_zona||emp.id_tipo_componente||';'||zona.id_zona||zona.id_tipo_componente||';'||mun_sub.id_zona||mun_sub.id_tipo_componente||';'||bar_cir.id_zona||bar_cir.id_tipo_componente||';'||trafo.id_componente||trafo.id_tipo_componente as claves "
            + "from componente trafo, rel_componente_ubicacion rcu, tipo_componente tipo, "
            + "zona_geografica bar_cir, zona_geografica mun_sub, zona_geografica zona, "
            + "zona_geografica emp "
            + "where trafo.id_cliente='" + idCliente + "' and "
            + "rcu.periodo_fin>" + periodo + " and "
            + "rcu.periodo_ini <=" + periodo + " and "
            + "rcu.id_componente=trafo.id_componente and "
            + "tipo.id_tipo_componente=bar_cir.id_tipo_componente and "
            + "tipo.tipo_arbol='" + tipoArbol + "' and "
            + "bar_cir.id_zona=rcu.id_zona and "
            + "mun_sub.id_zona=bar_cir.id_padre and "
            + "zona.id_zona=mun_sub.id_padre and "
            + "emp.id_zona=zona.id_padre";

    //boolean sw_nodo = getMostrarNodo().equalsIgnoreCase("si") ? false : true;
    try {
          //  int c=0;
      //Modo lectura, optimiza la base de datos
      System.out.println("Lanza el querie");
      List l = dao.executeQuerie(sql, true);
      System.out.println("Lanzó el querie");
      if (l.size() > 0) {
        System.out.println("Lanzó el querie + size " + l.size());
        for (int i = 0; i < l.size(); i++) {
          Object[] row = (Object[]) l.get(i);
          try {
            ruta = getString(row[0]) + "/" + getString(row[1]) + "/" + getString(row[2]) + "/" + getString(row[3]) + "/" + getString(row[4]);
            String claves = getString(row[5]);
            ruta = ruta + ";" + claves;
            break;
          } catch (Exception ex) {
            Logger.getLogger(ArbolManagerImpl.class.getName()).log(Level.SEVERE, null, ex);
          }

        }
        System.out.println("Se termino correctamente sin errores");
      }

    } catch (Exception ex) {
      Logger.getLogger(ArbolManagerImpl.class.getName()).log(Level.SEVERE, null, ex);
    }
    return ruta;
  }

  @Override
  public String rutaSuministro(String idCliente, String tipoArbol, String periodo) throws SQLException {
    String ruta = idCliente + " no encontrado.";
    String sql = "select emp.nombre as n1, zona.nombre as n2, mun_sub.nombre as n3, bar_cir.nombre as n4, trafo.NOMBRE as n5, sumi.nombre as n6, "
            + " emp.id_Zona||emp.id_tipo_componente||';'||zona.id_Zona||zona.id_tipo_componente||';'||mun_sub.id_Zona||mun_sub.id_tipo_componente||';'||bar_cir.id_Zona||bar_cir.id_tipo_componente||';'||trafo.id_componente||trafo.id_tipo_componente as claves"
            + " from rel_componente r, "
            + "componente trafo, "
            + "componente sumi, "
            + "rel_componente_ubicacion rcu, "
            + "tipo_componente tipo, "
            + "zona_geografica bar_cir, "
            + "zona_geografica mun_sub, "
            + "zona_geografica zona, "
            + "zona_geografica emp "
            + "where r.id_componente_hijo=sumi.id_componente and "
            + "sumi.id_cliente='" + idCliente + "' and "
            + "r.periodo_fin>" + periodo + " and "
            + "r.periodo_ini <=" + periodo + " and "
            + "r.id_componente=trafo.id_componente and "
            + "sumi.id_componente=r.id_componente_hijo and "
            + "rcu.id_componente=trafo.id_componente and "
            + "tipo.id_tipo_componente=bar_cir.id_tipo_componente and "
            + "tipo.tipo_arbol='" + tipoArbol + "' and "
            + "bar_cir.id_zona=rcu.id_zona and "
            + "mun_sub.id_zona=bar_cir.id_padre and "
            + "zona.id_zona=mun_sub.id_padre and "
            + "emp.id_zona=zona.id_padre";

    try {
          //  int c=0;
      //Modo lectura, optimiza la base de datos
      System.out.println("Lanza el querie");
      List l = dao.executeQuerie(sql, true);
      System.out.println("Lanzó el querie");
      if (l.size() > 0) {
        System.out.println("Lanzó el querie + size " + l.size());
        for (int i = 0; i < l.size(); i++) {
          Object[] row = (Object[]) l.get(i);
          try {
            ruta = getString(row[0]) + "/" + getString(row[1]) + "/" + getString(row[2]) + "/" + getString(row[3]) + "/" + getString(row[4]) + "/" + getString(row[5]);
            String claves = getString(row[6]);
            ruta = ruta + ";" + claves;
            break;
          } catch (Exception ex) {
            Logger.getLogger(ArbolManagerImpl.class.getName()).log(Level.SEVERE, null, ex);
          }

        }
        System.out.println("Se termino correctamente sin errores");
      }

    } catch (Exception ex) {
      Logger.getLogger(ArbolManagerImpl.class.getName()).log(Level.SEVERE, null, ex);
    }

    return ruta;
  }

  @Override
  public UbicacionMacroV getUbicacionMacroV(String idComponente, String periodo) throws SQLException {
    UbicacionMacroV result = new UbicacionMacroV();
    String cantTrafos = "(select count(*) "
            + "from rel_componente rc "
            + "where rc.id_componente=" + idComponente + " and "
            + "rc.periodo_ini<=" + periodo + " and "
            + "rc.periodo_fin>" + periodo + ")";

    String sql = "select zona.nombre as n0,munsub.nombre as n1,barcir.nombre as n2," + cantTrafos + " as n3 "
            + "from rel_componente_ubicacion rcu,"
            + "     zona_geografica barcir,"
            + "     zona_geografica munsub,"
            + "     zona_geografica zona "
            + "where rcu.id_componente=" + idComponente + " and"
            + "      rcu.periodo_ini<=" + periodo + " and "
            + "      rcu.periodo_fin>" + periodo + " and"
            + "      rcu.id_zona=barcir.id_zona and"
            + "      barcir.id_padre=munsub.id_zona and"
            + "      munsub.id_padre=zona.id_zona";

    try {
      //Modo lectura, optimiza la base de datos
      List l = dao.executeQuerie(sql, true);
      if (l != null && l.size() > 0) {
        for (int i = 0; i < l.size(); i++) {
          Object[] row = (Object[]) l.get(i);
          try {
            result.setZona(getString(row[0]));
            result.setMunSub(getString(row[1]));
            result.setBarCir(getString(row[2]));
            result.setCantTrafosAso(getString(row[3]));
            break;
          } catch (Exception ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
          }
        }
      }
    } catch (Exception ex) {
      Logger.getLogger(ArbolManagerImpl.class.getName()).log(Level.SEVERE, null, ex);
    }

//         ResultSet rs = dao.getConnection().createStatement().executeQuery(sql);
//         if(rs.next()){
//            result.setZona(rs.getString(1));
//            result.setMunSub(rs.getString(2));
//            result.setBarCir(rs.getString(3));
//            result.setCantTrafosAso(rs.getString(4));
//         }
    return result;
  }

  private String getMostrarNodo() {
    String hql = "SELECT p.valor FROM Parametro p WHERE p.idParametro ='MOSTRAR_INCONSISTENTES'";
    return dao.findObject(hql).toString();
  }

  /**
   * @autor <b>Jose Mejia</b>
   * @see MapBean
   * @since 29/10/2014
   * @param codigo
   * @param tipo
   * @param periodo
   * @param arbol
   * @return
   */
  @Override
  public List<Componente> getComponentesUbicacion(String codigo, String tipo, String periodo, String arbol, HashMap gestionFiltros) {
    String querie = getArmarQueries(codigo, tipo, periodo, arbol);
    System.out.println("-------- getComponentesUbicacion " + querie);
    if (querie != null && !querie.equals("")) {
      List<Componente> result = getCoordenadasCompo(querie, periodo, gestionFiltros);
      if (result instanceof List && result.size() > 0) {
        return result;
      }
    }
    return null;
  }

  /**
   * Arma el querie que va a ejecutar
   *
   * @autor <b>Jose Mejia</b>
   * @see MapBean
   * @since 29/10/2014
   * @param codigo
   * @param tipoV
   * @param periodo
   * @param arbol
   * @return
   */
  public String getArmarQueries(String codigo, String tipoV, String periodo, String arbol) {
    String codPpal = codigo;
    String tipo = tipoV;
    String sql = "";
    if (tipo.equals("0"))//Empresa
    {
      sql = "select trafo.id_componente as idComponente, trafo.nombre as nombre, trafo.id_tipo_componente as idTipocCompo, trafo.coord_x as coordx, trafo.coord_y as coordy, tbl.NOMBRE as tipoUso, tbl1.NOMBRE as redTrafo, tbl2.NOMBRE as localizacion, atr.IMAGEN as imagen,b.PORC_PERDIDA_MES as perMes, b.TOTAL_MACROS as totalMacr,b.TOTAL_MACROS_FUNC as macroFuncio,b.CANT_SUM_FACT as cantSumFac,rc.DESCRIPCION as rango, 'color:rgb(' || c.ROJO||','|| c.VERDE||','|| c.AZUL||')' as color,c.descripcion as nombrecolor \n"
              + "        from componente trafo, rel_componente_ubicacion rcu,  tipo_componente tipo, \n"
              + "             zona_geografica bar_cir, zona_geografica mun_sub, zona_geografica zona, \n"
              + "             zona_geografica emp , Atr_Componente atr,tbltipo tbl,tbltipo tbl1,tbltipo tbl2,Balances b,Rango_Clasificacion rc, Color c \n"
              + "        where rcu.id_componente=trafo.id_componente \n"
              + "              and  tipo.id_tipo_componente=bar_cir.id_tipo_componente  \n"
              + "              and  tipo.tipo_arbol='" + arbol + "' \n"
              + "              and  bar_cir.id_zona=rcu.id_zona \n"
              + "              and  mun_sub.id_zona=bar_cir.id_padre \n"
              + "              and  zona.id_zona=mun_sub.id_padre \n"
              + "              and  emp.id_zona=zona.id_padre \n"
              + "              and  emp.ID_ZONA=" + codPpal + "\n"
              + "              and   atr.ID_COMPONENTE=trafo.id_componente\n"
              + "              and   atr.TIPO_USO=tbl.TIPO\n"
              + "              and   atr.TIPO_RED=tbl1.TIPO\n"
              + "              and   trafo.LOCALIZACION= tbl2.TIPO\n"
              + "              and   b.Id_Componente=trafo.id_componente\n"
              + "              and   b.ID_TIPO_COMPONENTE=trafo.id_tipo_componente\n"
              + "              and   b.RANGO_BALANCE=rc.ID_RANGO  \n"
              + "              and   rc.ID_COLOR=c.ID_COLOR\n"
              + "              and   b.PERIODO =" + periodo + "\n"
              + "              and  rcu.periodo_ini <=  " + periodo + "\n"
              + "              and  rcu.periodo_fin >   " + periodo + "";
    } else if (tipo.equals("1") || tipo.equals("5"))//Zona
    {
      sql = "select trafo.id_componente as idComponente, trafo.nombre as nombre, trafo.id_tipo_componente as idTipocCompo, trafo.coord_x as coordx, trafo.coord_y as coordy, tbl.NOMBRE as tipoUso, tbl1.NOMBRE as redTrafo, tbl2.NOMBRE as localizacion, atr.IMAGEN as imagen,b.PORC_PERDIDA_MES as perMes, b.TOTAL_MACROS as totalMacr,b.TOTAL_MACROS_FUNC as macroFuncio,b.CANT_SUM_FACT as cantSumFac,rc.DESCRIPCION as rango, 'color:rgb(' || c.ROJO||','|| c.VERDE||','|| c.AZUL||')' as color,c.descripcion as nombrecolor \n"
              + "        from componente trafo, rel_componente_ubicacion rcu, tipo_componente tipo, \n"
              + "             zona_geografica bar_cir, zona_geografica mun_sub, zona_geografica zona , Atr_Componente atr,tbltipo tbl,tbltipo tbl1,tbltipo tbl2,Balances b,Rango_Clasificacion rc, Color c\n"
              + "        where rcu.id_componente=trafo.id_componente \n"
              + "        and   tipo.id_tipo_componente=bar_cir.id_tipo_componente \n"
              + "        and   tipo.tipo_arbol='" + arbol + "' \n"
              + "        and   bar_cir.id_zona=rcu.id_zona  \n"
              + "        and   mun_sub.id_zona=bar_cir.id_padre  \n"
              + "        and   zona.id_zona=mun_sub.id_padre\n"
              + "        and  zona.id_zona=" + codPpal + "\n"
              + "        and   atr.ID_COMPONENTE=trafo.id_componente\n"
              + "        and   atr.TIPO_USO=tbl.TIPO\n"
              + "        and   atr.TIPO_RED=tbl1.TIPO\n"
              + "        and   trafo.LOCALIZACION= tbl2.TIPO\n"
              + "        and   b.Id_Componente=trafo.id_componente\n"
              + "        and   b.ID_TIPO_COMPONENTE=trafo.id_tipo_componente\n"
              + "        and   b.RANGO_BALANCE=rc.ID_RANGO  \n"
              + "        and   rc.ID_COLOR=c.ID_COLOR\n"
              + "        and   b.PERIODO =" + periodo + "\n"
              + "        and  rcu.periodo_ini <= " + periodo + "\n"
              + "        and  rcu.periodo_fin >  " + periodo + "";
    } else if (tipo.equals("2") || tipo.equals("6"))//municipio subestacion
    {
      sql = "select trafo.id_componente as idComponente, trafo.nombre as nombre, trafo.id_tipo_componente as idTipocCompo, trafo.coord_x as coordx, trafo.coord_y as coordy, tbl.NOMBRE as tipoUso, tbl1.NOMBRE as redTrafo, tbl2.NOMBRE as localizacion, atr.IMAGEN as imagen,b.PORC_PERDIDA_MES as perMes, b.TOTAL_MACROS as totalMacr,b.TOTAL_MACROS_FUNC as macroFuncio,b.CANT_SUM_FACT as cantSumFac,rc.DESCRIPCION as rango, 'color:rgb(' || c.ROJO||','|| c.VERDE||','|| c.AZUL||')' as color,c.descripcion as nombrecolor \n"
              + "        from componente trafo, rel_componente_ubicacion rcu, tipo_componente tipo, \n"
              + "             zona_geografica bar_cir, zona_geografica mun_sub, Atr_Componente atr,tbltipo tbl,tbltipo tbl1,tbltipo tbl2,Balances b,Rango_Clasificacion rc, Color c\n"
              + "        where rcu.id_componente=trafo.id_componente \n"
              + "        and   tipo.id_tipo_componente=bar_cir.id_tipo_componente \n"
              + "        and   tipo.tipo_arbol='" + arbol + "' \n"
              + "        and   bar_cir.id_zona=rcu.id_zona  \n"
              + "        and   mun_sub.id_zona=bar_cir.id_padre  \n"
              + "        and   mun_sub.id_zona=" + codPpal + "\n"
              + "        and   atr.ID_COMPONENTE=trafo.id_componente\n"
              + "        and   atr.TIPO_USO=tbl.TIPO\n"
              + "        and   atr.TIPO_RED=tbl1.TIPO\n"
              + "        and   trafo.LOCALIZACION= tbl2.TIPO\n"
              + "        and   b.Id_Componente=trafo.id_componente\n"
              + "        and   b.ID_TIPO_COMPONENTE=trafo.id_tipo_componente\n"
              + "        and   b.RANGO_BALANCE=rc.ID_RANGO  \n"
              + "        and   rc.ID_COLOR=c.ID_COLOR\n"
              + "        and   b.PERIODO =" + periodo + "\n"
              + "        and  rcu.periodo_ini <= " + periodo + "\n"
              + "        and  rcu.periodo_fin >  " + periodo + "";

    } else if (tipo.equals("3") || tipo.equals("7"))//Circuito
    {
      sql = "select trafo.id_componente as idComponente, trafo.nombre as nombre, trafo.id_tipo_componente as idTipocCompo, trafo.coord_x as coordx, trafo.coord_y as coordy, tbl.NOMBRE as tipoUso, tbl1.NOMBRE as redTrafo, tbl2.NOMBRE as localizacion, atr.IMAGEN as imagen,b.PORC_PERDIDA_MES as perMes, b.TOTAL_MACROS as totalMacr,b.TOTAL_MACROS_FUNC as macroFuncio,b.CANT_SUM_FACT as cantSumFac,rc.DESCRIPCION as rango, 'color:rgb(' || c.ROJO||','|| c.VERDE||','|| c.AZUL||')' as color,c.descripcion as nombrecolor \n"
              + "        from componente trafo, rel_componente_ubicacion rcu, tipo_componente tipo, \n"
              + "             zona_geografica bar_cir, Atr_Componente atr,tbltipo tbl,tbltipo tbl1,tbltipo tbl2,Balances b,Rango_Clasificacion rc, Color c\n"
              + "        where rcu.id_componente=trafo.id_componente \n"
              + "        and   tipo.id_tipo_componente=bar_cir.id_tipo_componente \n"
              + "        and   tipo.tipo_arbol='" + arbol + "' \n"
              + "        and   bar_cir.id_zona=" + codPpal + "\n"
              + "        and   bar_cir.id_zona=rcu.id_zona\n"
              + "        and   atr.ID_COMPONENTE=trafo.id_componente\n"
              + "        and   atr.TIPO_USO=tbl.TIPO\n"
              + "        and   atr.TIPO_RED=tbl1.TIPO\n"
              + "        and   trafo.LOCALIZACION= tbl2.TIPO\n"
              + "        and   b.Id_Componente=trafo.id_componente\n"
              + "        and   b.ID_TIPO_COMPONENTE=trafo.id_tipo_componente\n"
              + "        and   b.RANGO_BALANCE=rc.ID_RANGO  \n"
              + "        and   rc.ID_COLOR=c.ID_COLOR\n"
              + "        and   b.PERIODO =" + periodo + "\n"
              + "        and  rcu.periodo_ini <= " + periodo + "\n"
              + "        and  rcu.periodo_fin >  " + periodo + "";
    }
    return sql;
  }

  /**
   * Lista de componente
   *
   * @autor <b>Jose Mejia</b>
   * @see MapBean
   * @since 29/10/2014
   * @param sql
   * @return
   */
  public List getCoordenadasCompo(String sql, String periodo, HashMap gestionFiltros) {
    List hijos = new ArrayList();
    //boolean sw_nodo = getMostrarNodo().equalsIgnoreCase("si") ? false : true;
    try {
          //  int c=0;
      //Modo lectura, optimiza la base de datos
      System.out.println("Lanza el querie");
      List l = dao.executeQuerie(sql, false);
      System.out.println("Lanzó el querie");
      if (l.size() > 0) {
        System.out.println("Lanzó el querie + size " + l.size());
        for (int i = 0; i < l.size(); i++) {
          Object[] row = (Object[]) l.get(i);
          try {
                        //Balances b = getBalances(rs.getString(1),rs.getString(3),periodo);
            // c++;
            String idCompo = getString(row[0] != null ? row[0] : "0");
            String nombreComp = getString(row[1]);
            String idTipoComp = getString(row[2] != null ? row[2] : "0");
            String coordX = getString(row[3]);
            String coordY = getString(row[4]);
            String usoTrafo = getString(row[5]);
            String redTrafo = getString(row[6]);
            String localizacion = getString(row[7]);
            String imagen = getString(row[8]);
            String porPerdMes = getString(row[9] != null ? row[9] : "0");
            String totalMacros = getString(row[10] != null ? row[10] : "0");
            String totalMacFun = getString(row[11] != null ? row[11] : "0");
            String cantSumFact = getString(row[12] != null ? row[12] : "0");
            String rangoTrafo = getString(row[13]);
            String color = getString(row[14]);
            String nombreColor = getString(row[15]);

//                        System.out.println("id: "+idCompo);
//                        System.out.println("usoTrafo: "+usoTrafo);
//                        System.out.println("Color: "+color);
//                        System.out.println("Nombre Color:"+nombreColor);
            if (porPerdMes != null && !porPerdMes.equals("null")) {

              Nodo hijo = new Nodo();
              hijo.setCodigo(idCompo);
              hijo.setColor(color);
              hijo.setCoordenadaX(coordX);
              hijo.setCoordenadaY(coordY);
              hijo.setImagen(imagen);
              hijo.setLocalizacion(localizacion);
              hijo.setNombre(nombreComp);
              hijo.setNombreColor(nombreColor);
              hijo.setNombreTipo(redTrafo);
              hijo.setNumMacroTot(totalMacros);
              hijo.setNumMacrosFuncionando(totalMacFun);
              hijo.setNumSuministrosFact(cantSumFact);
              hijo.setPerdidas(porPerdMes);
              hijo.setTipo(idTipoComp);
              hijo.setTipoUso(usoTrafo);

              hijos.add(hijo);
            }

          } catch (Exception ex) {
            Logger.getLogger(ArbolManagerImpl.class.getName()).log(Level.SEVERE, null, ex);
          }

        }
        System.out.println("Se termino correctamente sin errores");
      }

      System.out.println("mañamamama " + hijos.size());
    } catch (Exception ex) {
      Logger.getLogger(ArbolManagerImpl.class.getName()).log(Level.SEVERE, null, ex);
    }

    return hijos;
  }

  public static BigDecimal getBigDecimal(Object value) {
    BigDecimal ret = null;
    if (value != null) {
      if (value instanceof BigDecimal) {
        ret = (BigDecimal) value;
      } else if (value instanceof String) {
        ret = new BigDecimal((String) value);
      } else if (value instanceof BigInteger) {
        ret = new BigDecimal((BigInteger) value);
      } else if (value instanceof Number) {
        ret = new BigDecimal(((Number) value).doubleValue());
      } else {
        throw new ClassCastException("Not possible to coerce [" + value + "] from class " + value.getClass() + " into a BigDecimal.");
      }
    }
    return ret;
  }

  /**
   * Castea el objeto a string
   *
   * @autor <b>Jose Mejia</b>
   * @see ArbolManagerImpl
   * @since 27/11/2014
   * @param value
   * @return
   */
  public String getString(Object value) {
    String ret = "";
    if (value != null) {
      if (value instanceof BigDecimal) {
        ret = ((BigDecimal) value).toString();
      } else if (value instanceof String) {
        ret = (String) value;
      } else if (value instanceof BigInteger) {
        ret = ((BigInteger) value).toString();
      } else if (value instanceof Number) {
        ret = new BigDecimal(((Number) value).doubleValue()).toString();
      } else if (value instanceof Long) {
        ret = ((Long) value).toString();
      } else if (value instanceof Integer) {
        ret = ((Integer) value).toString();
      } else if (value instanceof Character) {
        ret = ((Character) value).toString();
      } else {
        throw new ClassCastException("No es posible castear el object [" + value + "] a String, porque es de tipo " + value.getClass());
      }
    }
    return ret;
  }

  /**
   * Este método lo modifique porque generaba una excepción
   *
   * @autor <b>Jose Mejia</b>
   * @see MapBean
   * @since 29/10/2014
   * @return
   */
  @Override
  public String getRutaImage() {
    String ruta = "";
    String sql = "Select p.VALOR From Parametro p Where p.ID_PARAMETRO='RUTAIMAGEN'";
    try {
          //  int c=0;
      //Modo lectura, optimiza la base de datos
      System.out.println("Lanza el querie");
      List l = dao.executeQuerie(sql, true);
      System.out.println("Lanzó el querie");
      if (l.size() > 0) {
        System.out.println("Lanzó el querie + size " + l.size());
        for (int i = 0; i < l.size(); i++) {

          try {

            Object row = l.get(i);
            System.out.println("" + l.get(i).toString());
            ruta = getString(row);
            break;
          } catch (Exception ex) {
            ruta = "";
            Logger.getLogger(ArbolManagerImpl.class.getName()).log(Level.SEVERE, null, ex);
          }
        }
        System.out.println("Se termino correctamente sin errores");
      }

    } catch (Exception ex) {
      Logger.getLogger(ArbolManagerImpl.class.getName()).log(Level.SEVERE, null, ex);
    }
    return ruta;
  }

}



/**************************************/
///*
// * To change this template, choose Tools | Templates
// * and open the template in the editor.
// */
//package com.ag.service.impl;
//
//import com.ag.dao.Dao;
//import com.ag.model.Balances;
//import com.ag.model.Componente;
//
//import com.ag.model.RangoClasificacion;
//import com.ag.model.view.Nodo;
//import com.ag.model.view.UbicacionMacroV;
//import com.ag.service.ArbolManager;
//import java.math.BigDecimal;
//import java.math.BigInteger;
//import java.sql.*;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.Iterator;
//import java.util.List;
//import java.util.logging.Level;
//import java.util.logging.Logger;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.stereotype.Service;
//
///**
// *
// * @author 85154220
// */
//@Service("ArbolManager")
//public class ArbolManagerImpl implements ArbolManager {
//
//  @Autowired
//  @Qualifier("DaoHibernate")
//  private Dao dao;
//
//  @Override
//  public List cargaArbolInicial(String nivelGrupo, String periodo) {
//    String parametro = "";
//    if (nivelGrupo != null) {
//      if (nivelGrupo.equals("NIV100")) {
//        parametro = "NIVEL_ENERGETICO";
//      } else {
//        parametro = "NIVEL_GEOGRAFICO";
//      }
//    }
//
//    String hql = "SELECT t.idZona, t.nombre,t.tipoComponente.idTipoComponente,t.coordX,t.coordY,t.tipoComponente.descripcion "
//            + " FROM ZonaGeografica t ,  Parametro p"
//            + " WHERE t.tipoComponente.idTipoComponente = p.valor "
//            + " AND  t.tipoComponente.tbltipo.tipo = '" + nivelGrupo + "' "
//            + " AND t.estado = 'AC001' "
//            + " and p.idParametro ='" + parametro + "'";
//    return obtenerArbol(hql, periodo, null);
//
//  }
//
//  @Override
//  public List cargaArbolHijos(String codigoPadre, String tipoPadre, String periodo, HashMap gestionFiltros) {
//    int contZona, contUbicacion, contComponente;
//
//    String hql = "SELECT COUNT(*) "
//            + " FROM ZonaGeografica p, ZonaGeografica h "
//            + " WHERE h.idPadre = " + codigoPadre
//            + " AND p.idZona = h.idPadre"
//            + " AND p.tipoComponente.idTipoComponente = " + tipoPadre;
//    contZona = Integer.parseInt(String.valueOf(dao.findObject(hql)));
//    hql = "SELECT COUNT(*) "
//            + " FROM RelComponenteUbicacion r "
//            + " WHERE  r.zonaGeografica.idZona =  " + codigoPadre
//            + " AND   r.zonaGeografica.tipoComponente.idTipoComponente = " + tipoPadre
//            + " AND   r.relComponenteUbicacionPK.periodoIni <=" + periodo
//            + " AND   r.periodoFin       > " + periodo;
//    contUbicacion = Integer.parseInt(String.valueOf(dao.findObject(hql)));
//
//    hql = "SELECT COUNT(*) "
//            + "FROM RelComponente r "
//            + " WHERE r.relComponentePK.idComponente =" + codigoPadre
//            + " AND r.componente1.tipoComponente.idTipoComponente = " + tipoPadre
//            + " AND  r.relComponentePK.periodoIni <= " + periodo
//            + " AND r.periodoFin> " + periodo;
//    contComponente = Integer.parseInt(String.valueOf(dao.findObject(hql)));
//    String hqlArbol = null;
//    if (contZona > 0) {
//      hqlArbol = "SELECT  z.idZona, z.nombre,  z.tipoComponente.idTipoComponente,z.coordX,z.coordY,z.tipoComponente.descripcion "
//              + " FROM ZonaGeografica z "
//              + " WHERE z.idPadre =" + codigoPadre;
//    } else if (contUbicacion > 0) {
//      hqlArbol = "SELECT ru.componente.idComponente, ru.componente.nombre,"
//              + " ru.componente.tipoComponente.idTipoComponente, "
//              + " ru.componente.coordX,ru.componente.coordY,"
//              + " ru.componente.tipoComponente.descripcion,"
//              + " ru.componente.tbltipo1.nombre,"
//              + " (SELECT a.tbltipo1.nombre FROM AtrComponente a WHERE a.componente.idComponente=ru.componente.idComponente) as tipoUso,"
//              + " (SELECT a.tbltipo5.tipo FROM AtrComponente a WHERE a.componente.idComponente=ru.componente.idComponente) as tipoRed "
//              + " FROM RelComponenteUbicacion ru "
//              + " WHERE  ru.zonaGeografica.idZona = " + codigoPadre
//              + "  AND  ru.zonaGeografica.tipoComponente.idTipoComponente = " + tipoPadre
//              + "  AND  ru.relComponenteUbicacionPK.periodoIni <=" + periodo
//              + "  AND  ru.periodoFin                          > " + periodo
//              + " order by ru.componente.nombre";
//
//    } else if (contComponente > 0) {
//      hqlArbol = "SELECT r.relComponentePK.idComponenteHijo, r.componente.idCliente,"
//              + " r.componente.tipoComponente.idTipoComponente,"
//              + " nvl(r.componente.coordX, 0), nvl(r.componente.coordY, 0),"
//              + " r.componente.tipoComponente.descripcion,"
//              + " r.componente.tbltipo1.nombre,"
//              + " (SELECT a.tbltipo1.nombre FROM AtrComponente a WHERE a.componente.idComponente=r.componente.idComponente) as tipoUso,"
//              + " (SELECT a.tbltipo5.tipo FROM AtrComponente a WHERE a.componente.idComponente=r.componente.idComponente) as tipoRed "
//              + " FROM RelComponente r "
//              + " WHERE r.relComponentePK.idComponente =" + codigoPadre
//              + "  AND  r.relComponentePK.periodoIni <= " + periodo
//              + " AND r.periodoFin> " + periodo
//              + " AND r.componente1.tipoComponente.idTipoComponente = " + tipoPadre
//              + " order by r.componente.idCliente";
//    }
//    if (hqlArbol == null) {
//      return null;
//    }
//    return obtenerArbol(hqlArbol, periodo, gestionFiltros);
//  }
//
//  @Override
//  public List cargaArbolHijosTrafos(String codigoPadre, String tipoPadre, String periodo, HashMap gestionFiltros) {
//    int contTrafosByEmpresa = 0, contTrafosByZona = 0, contTrafosBySubMun = 0;
//
//    String hql;
//    if (tipoPadre.equals("4") || tipoPadre.equals("0")) { //empresa
//      hql = "SELECT COUNT(*) FROM RelComponenteUbicacion r "
//              + "WHERE r.relComponenteUbicacionPK.periodoIni <=" + periodo + " and "
//              + "r.periodoFin>" + periodo + " and "
//              + "r.relComponenteUbicacionPK.idZona in (SELECT z.idZona"
//              + " FROM ZonaGeografica z"
//              + " WHERE z.idPadre in (SELECT z.idZona"
//              + " FROM ZonaGeografica z"
//              + " WHERE z.idPadre in (SELECT zh.idZona"
//              + " FROM ZonaGeografica zp, ZonaGeografica zh"
//              + " WHERE zp.idZona=zh.idPadre and zp.idZona=" + codigoPadre + ")))";
//      contTrafosByEmpresa = Integer.parseInt(String.valueOf(dao.findObject(hql)));
//    } else if (tipoPadre.equals("5") || tipoPadre.equals("1")) { //zona
//      hql = "SELECT COUNT(*) FROM RelComponenteUbicacion r "
//              + "WHERE r.relComponenteUbicacionPK.periodoIni <=" + periodo + " and "
//              + "r.periodoFin>" + periodo + " and "
//              + "r.relComponenteUbicacionPK.idZona in (SELECT z.idZona"
//              + " FROM ZonaGeografica z"
//              + " WHERE z.idPadre in (SELECT zh.idZona"
//              + " FROM ZonaGeografica zp, ZonaGeografica zh"
//              + " WHERE zp.idZona=zh.idPadre and zp.idZona=" + codigoPadre + "))";
//      contTrafosByZona = Integer.parseInt(String.valueOf(dao.findObject(hql)));
//    } else if (tipoPadre.equals("6") || tipoPadre.equals("2")) { //municipio o subestacion
//      hql = "SELECT COUNT(*) FROM RelComponenteUbicacion r "
//              + "WHERE r.relComponenteUbicacionPK.periodoIni <=" + periodo + " and "
//              + "r.periodoFin>" + periodo + " and "
//              + "r.relComponenteUbicacionPK.idZona in (SELECT z.idZona"
//              + " FROM ZonaGeografica z"
//              + " WHERE z.idZona in (SELECT zh.idZona"
//              + " FROM ZonaGeografica zp, ZonaGeografica zh"
//              + " WHERE zp.idZona=zh.idPadre and zp.idZona=" + codigoPadre + "))";
//      contTrafosBySubMun = Integer.parseInt(String.valueOf(dao.findObject(hql)));
//    }
//
//    String hqlArbol = null;
//    if (contTrafosByEmpresa > 0) {
//      /*
//       hqlArbol ="SELECT r.componente.idComponente, r.componente.nombre,"
//       + " r.componente.tipoComponente.idTipoComponente, "
//       + " r.componente.coordX,r.componente.coordY,"
//       + " r.componente.tipoComponente.descripcion,"
//       + " r.componente.tbltipo1.nombre,"
//       + " (SELECT a.tbltipo1.nombre FROM AtrComponente a WHERE a.componente.idComponente=r.componente.idComponente) as tipoUso," 
//       + " (SELECT a.tbltipo5.tipo FROM AtrComponente a WHERE a.componente.idComponente=r.componente.idComponente) as tipoRed "+
//       " FROM RelComponenteUbicacion r "
//       + "WHERE r.relComponenteUbicacionPK.periodoIni <=" + periodo +" and "
//       + "r.periodoFin>"+periodo+" and "
//       + "r.relComponenteUbicacionPK.idZona in (SELECT z.idZona"
//       + " FROM ZonaGeografica z"
//       + " WHERE z.idPadre in (SELECT z.idZona"
//       + " FROM ZonaGeografica z"
//       + " WHERE z.idPadre in (SELECT zh.idZona"
//       + " FROM ZonaGeografica zp, ZonaGeografica zh"
//       + " WHERE zp.idZona=zh.idPadre and zp.idZona="+codigoPadre+")))";
//       */
//      hqlArbol = "SELECT r.componente.idComponente, r.componente.nombre,"
//              + " r.componente.tipoComponente.idTipoComponente, "
//              + " r.componente.coordX,r.componente.coordY,"
//              + " r.componente.tipoComponente.descripcion,"
//              + " r.componente.tbltipo1.nombre,"
//              + " (SELECT a.tbltipo1.nombre FROM AtrComponente a WHERE a.componente.idComponente=r.componente.idComponente) as tipoUso,"
//              + " (SELECT a.tbltipo5.tipo FROM AtrComponente a WHERE a.componente.idComponente=r.componente.idComponente) as tipoRed "
//              + " FROM RelComponenteUbicacion r , ZonaGeografica z1,ZonaGeografica z2,ZonaGeografica z3,ZonaGeografica z4 "
//              + " WHERE r.relComponenteUbicacionPK.periodoIni <=" + periodo + " and "
//              + " r.periodoFin>" + periodo
//              + " and z1.idZona = " + codigoPadre
//              + " and z2.idPadre = z1.idZona"
//              + " and z3.idPadre = z2.idZona"
//              + " and z4.idPadre = z3.idZona"
//              + " and r.relComponenteUbicacionPK.idZona = z4.idZona ";
//    } else if (contTrafosByZona > 0) {
//      hqlArbol = "SELECT r.componente.idComponente, r.componente.nombre,"
//              + " r.componente.tipoComponente.idTipoComponente, "
//              + " r.componente.coordX,r.componente.coordY,"
//              + " r.componente.tipoComponente.descripcion,"
//              + " r.componente.tbltipo1.nombre,"
//              + " (SELECT a.tbltipo1.nombre FROM AtrComponente a WHERE a.componente.idComponente=r.componente.idComponente) as tipoUso,"
//              + " (SELECT a.tbltipo5.tipo FROM AtrComponente a WHERE a.componente.idComponente=r.componente.idComponente) as tipoRed "
//              + " FROM RelComponenteUbicacion r "
//              + "WHERE r.relComponenteUbicacionPK.periodoIni <=" + periodo + " and "
//              + "r.periodoFin>" + periodo + " and "
//              + "r.relComponenteUbicacionPK.idZona in (SELECT z.idZona"
//              + " FROM ZonaGeografica z"
//              + " WHERE z.idPadre in (SELECT zh.idZona"
//              + " FROM ZonaGeografica zp, ZonaGeografica zh"
//              + " WHERE zp.idZona=zh.idPadre and zp.idZona=" + codigoPadre + "))";
//    } else if (contTrafosBySubMun > 0) {
//      hqlArbol = "SELECT r.componente.idComponente, r.componente.nombre,"
//              + " r.componente.tipoComponente.idTipoComponente, "
//              + " r.componente.coordX,r.componente.coordY,"
//              + " r.componente.tipoComponente.descripcion,"
//              + " r.componente.tbltipo1.nombre,"
//              + " (SELECT a.tbltipo1.nombre FROM AtrComponente a WHERE a.componente.idComponente=r.componente.idComponente) as tipoUso,"
//              + " (SELECT a.tbltipo5.tipo FROM AtrComponente a WHERE a.componente.idComponente=r.componente.idComponente) as tipoRed "
//              + " FROM RelComponenteUbicacion r "
//              + "WHERE r.relComponenteUbicacionPK.periodoIni <=" + periodo + " and "
//              + "r.periodoFin>" + periodo + " and "
//              + "r.relComponenteUbicacionPK.idZona in (SELECT z.idZona"
//              + " FROM ZonaGeografica z"
//              + " WHERE z.idZona in (SELECT zh.idZona"
//              + " FROM ZonaGeografica zp, ZonaGeografica zh"
//              + " WHERE zp.idZona=zh.idPadre and zp.idZona=" + codigoPadre + "))";
//    }
//
//    if (hqlArbol == null) {
//      return null;
//    }
//    return obtenerArbol(hqlArbol, periodo, gestionFiltros);
//
//    //MOLLEJA
//  }
//
//  @Override
//  public boolean soloTrafoInGmap(String tipo) {
//    if (tipo.equals("4") || tipo.equals("0")
//            || tipo.equals("5") || tipo.equals("1")
////            || tipo.equals("12") || tipo.equals("11")//Prueba
//            || tipo.equals("6") || tipo.equals("2")) {
//      String hql = "SELECT p.valor FROM Parametro p WHERE p.idParametro ='SOLO_COMPONENTES'";
//      String valor = dao.findObject(hql).toString();
//      return valor.equalsIgnoreCase("si") ? true : false;
//    }
//    return false;
//  }
//
//  public List obtenerArbol(String hql, String periodo, HashMap gestionFiltros) {
////        try{
////        List elemento = dao.find(hql);
////        List hijos = new ArrayList();
////        boolean sw_nodo=getMostrarNodo().equalsIgnoreCase("si")?
////                        false:true;
////        if(elemento!= null)
////            for(Iterator it = elemento.iterator();it.hasNext();){
////                Object [] valores = (Object[]) it.next();
////                String codigo = String.valueOf(valores[0]);                
////                String tipo = ((Short) valores[2]).toString();
////                Balances b = getBalances(codigo,tipo,periodo);
////                String perdidas = getPerdidas(b);
////                String color = getColor(tipo,perdidas);                
////                if (sw_nodo && !tipo.equals("9") && !tipo.equals("10")) {
////                  if (perdidas.equals("null")) continue;
////                  Double porcperd = Double.parseDouble(perdidas);
////                  if (porcperd>70 || porcperd<-5) continue; 
////                }               
////                String nombre = (String) valores[1];
////                String coordX = (String) valores[3];
////                String coordY = (String) valores[4];
////                //PTORRES.20140514.INI
////                if (!perdidas.equals("null")){
////                    Nodo hijo = new Nodo(codigo,nombre,tipo,color,perdidas,coordX,coordY);
////                    if(b != null){
////                        hijo.setNumMacroTot(String.valueOf(b.getTotalMacros()));
////                        hijo.setNumMacrosFuncionando(String.valueOf(b.getTotalMacrosFunc()));
////                        hijo.setNumSuministrosFact(String.valueOf(b.getCantSumFact()));
////                    }
////                    hijo.setNombreTipo((String) valores[5]);
////                    if (tipo.equals("8")) {
////                        //Gestionar filtros para Trafos
////                        String usoTrafo=(String) valores[7];
////                        String redTrafo=(String) valores[8];
////                        String rangoTrafo=getRango(b);
////                        if (gestionFiltros!=null && !gestionFiltros.isEmpty()) {
////                            String tipo_uso=(String)gestionFiltros.get("TIPO_USO");
////                            String tipo_red=(String)gestionFiltros.get("TIPO_RED");
////                            String rango=(String)gestionFiltros.get("RANGO");                        
////                            if(!tipo_uso.equals("NO") && !usoTrafo.equals(tipo_uso))
////                                continue;
////                            if(!tipo_red.equals("NO") && !redTrafo.equals(tipo_red))
////                                continue;
////                            if(!rango.equals("NO") && !rangoTrafo.equals(rango))
////                                continue;
////                        }
////                        hijo.setLocalizacion((String) valores[6]);                    
////                        hijo.setTipoUso(usoTrafo);
////                        hijo.setNombreColor(getNombreColor(perdidas));
////                    }        
////                    if (tipo.equals("9")) 
////                        hijo.setTipoUso((String) valores[7]);
////                    hijos.add(hijo);
////                }
////                //PTORRES.20140514.FIN
////            }
////        return hijos;
////        }catch(Exception e){
////            e.printStackTrace();
////            return null;
////        }
//
//    try {
//      List elemento = dao.find(hql);
//      List hijos = new ArrayList();
//      boolean sw_nodo = getMostrarNodo().equalsIgnoreCase("si")
//              ? false : true;
//      if (elemento != null) {
//        for (Iterator it = elemento.iterator(); it.hasNext();) {
//          Object[] valores = (Object[]) it.next();
//          String codigo = String.valueOf(valores[0]);
//          String tipo = ((Short) valores[2]).toString();
//          Balances b = getBalances(codigo, tipo, periodo);
//          String perdidas = getPerdidas(b);
//          String color = getColor(tipo, perdidas);
//          if (sw_nodo && !tipo.equals("9") && !tipo.equals("10")) {
//            if (perdidas.equals("null")) {
//              continue;
//            }
//            Double porcperd = Double.parseDouble(perdidas);
//            if (porcperd > 70 || porcperd < -5) {
//              continue;
//            }
//          }
//
//          //LOB.20140802.INI
//          //String nombre = (String) valores[1] ;
//          String nombre;
//          if (tipo.equals("8")) {
//            //nombre = (String) valores[1] + " - " + (String) valores[9];
//            nombre = (String) valores[1] + " - " + (String) valores[8];
//          } else {
//            nombre = (String) valores[1];
//          }
//          //LOB.20140803.FIN  
//
//          String coordX = (String) valores[3];
//          String coordY = (String) valores[4];
//          //PTORRES.20140514.INI
//
//          if (perdidas.equals("null") && tipo.equals("9")) {
//            if (perdidas.equals("null")) {
//              perdidas = "";
//            }
//            Nodo hijo = new Nodo(codigo, nombre, tipo, color, perdidas, coordX, coordY);
//            if (b != null) {
//              hijo.setNumMacroTot(String.valueOf(b.getTotalMacros()));
//              hijo.setNumMacrosFuncionando(String.valueOf(b.getTotalMacrosFunc()));
//              hijo.setNumSuministrosFact(String.valueOf(b.getCantSumFact()));
//            }
//            hijo.setNombreTipo((String) valores[5]);
//            hijo.setTipoUso((String) valores[7]);
//            hijos.add(hijo);
//          } else if (!perdidas.equals("null")) {
//            Nodo hijo = new Nodo(codigo, nombre, tipo, color, perdidas, coordX, coordY);
//            if (b != null) {
//              hijo.setNumMacroTot(String.valueOf(b.getTotalMacros()));
//              hijo.setNumMacrosFuncionando(String.valueOf(b.getTotalMacrosFunc()));
//              hijo.setNumSuministrosFact(String.valueOf(b.getCantSumFact()));
//            }
//            hijo.setNombreTipo((String) valores[5]);
//            if (tipo.equals("8")) {
//              //Gestionar filtros para Trafos
//              String usoTrafo = (String) valores[7];
//              String redTrafo = (String) valores[8];
//              String rangoTrafo = getRango(b);
//              if (gestionFiltros != null && !gestionFiltros.isEmpty()) {
//                String tipo_uso = (String) gestionFiltros.get("TIPO_USO");
//                String tipo_red = (String) gestionFiltros.get("TIPO_RED");
//                String rango = (String) gestionFiltros.get("RANGO");
//                if (!tipo_uso.equals("NO") && !usoTrafo.equals(tipo_uso)) {
//                  continue;
//                }
//                if (!tipo_red.equals("NO") && !redTrafo.equals(tipo_red)) {
//                  continue;
//                }
//                if (!rango.equals("NO") && !rangoTrafo.equals(rango)) {
//                  continue;
//                }
//              }
//              hijo.setLocalizacion((String) valores[6]);
//              hijo.setTipoUso(usoTrafo);
//              hijo.setNombreColor(getNombreColor(perdidas));
//            }
//            hijos.add(hijo);
//          }
//          //PTORRES.20140514.FIN
//        }
//      }
//      return hijos;
//    } catch (Exception e) {
//      e.printStackTrace();
//      return null;
//    }
//
//  }
//
//  public String getColor(String tipoComponente, String perdidas) {
//
//    String hql = !perdidas.equals("null") ? "select  r "
//            + " from RangoClasificacion r  "
//            + " where r.tipoComponente.idTipoComponente = " + tipoComponente
//            + " and porcMinimo <= " + perdidas
//            + " and porcMaximo > " + perdidas
//            : //si no tiene balances
//            "select  r "
//            + " from RangoClasificacion r  "
//            + " where r.tipoComponente.idTipoComponente = " + tipoComponente
//            + " and porcMinimo is null "
//            + " and porcMaximo is null ";
//    RangoClasificacion objColor = (RangoClasificacion) dao.findObject(hql);
//    String color = "color:rgb(212,208,200)"; //no ha sido asignado ningun rango
//    if (objColor != null) {
//      color = "color:rgb(" + objColor.getColor().getRojo() + "," + objColor.getColor().getVerde() + "," + objColor.getColor().getAzul() + ")";
//    }
//    return color;
//  }
//
//  public String getNombreColor(String perdidas) {
//    String hql = !perdidas.equals("null") ? "select  r "
//            + " from RangoClasificacion r  "
//            + " where r.tipoComponente.idTipoComponente = 8"
//            + " and porcMinimo <= " + perdidas
//            + " and porcMaximo > " + perdidas
//            : //si no tiene balances
//            "select  r "
//            + " from RangoClasificacion r  "
//            + " where r.tipoComponente.idTipoComponente = 8"
//            + " and porcMinimo is null "
//            + " and porcMaximo is null ";
//    RangoClasificacion objColor = (RangoClasificacion) dao.findObject(hql);
//    String colorName = "none"; //no ha sido asignado ningun rango
//    if (objColor != null) {
//      colorName = objColor.getColor().getDescripcion();
//    }
//    return colorName;
//  }
//
//  public Balances getBalances(String codigo, String tipo, String periodo) {
//    String hql = " SELECT b FROM Balances b "
//            + " WHERE b.balancesPK.idComponente =  " + codigo
//            + " and  b.balancesPK.periodo = " + periodo
//            + " and  b.balancesPK.idTipoComponente=" + tipo;
//    Balances bal = (Balances) dao.findObject(hql);
//    return bal;
//  }
//
//  public String getPerdidas(Balances bal) {
//    String perdidas = "null";
//    if (bal != null && bal.getPorcPerdidaMes() != null) {
//      perdidas = String.valueOf(bal.getPorcPerdidaMes());
//    }
//    return perdidas;
//  }
//
//  public String getRango(Balances bal) {
//    String rango = "null";
//    if (bal != null && bal.getRangoClasificacion() != null) {
//      rango = bal.getRangoClasificacion().getDescripcion();
//    }
//    return rango;
//  }
//
//  @Override
//  public List getZoomMapa() {
//    String hql = " SELECT t FROM TipoComponente t ";
//    List tip = dao.find(hql);
//    return tip;
//  }
//
//  @Override
//  public List getPeriodo() {
//    String hql = " SELECT distinct  b.balancesPK.periodo  from Balances  b "
//            + " order by  b.balancesPK.periodo desc";
//    List periodo = dao.find(hql);
//    List listCombo = new ArrayList();
//    for (int i = 0; i < periodo.size(); i++) {
//      String per = String.valueOf(periodo.get(i));
//      listCombo.add(per);
//    }
//    return listCombo;
//  }
//
//  public static void main(String[] agr) {
//    try {
//      ArbolManagerImpl app = new ArbolManagerImpl();
//
//    } catch (Exception e) {
//      e.printStackTrace();
//    }
//  }
//
//  @Override
//  public String rutaTrafo(String idCliente, String tipoArbol, String periodo) throws SQLException {
//    String ruta = idCliente + " no encontrado.";
//    String sql = "select emp.nombre as n1, zona.nombre as n2, mun_sub.nombre as n3, bar_cir.nombre as n4, trafo.NOMBRE as n5, "
//            + " emp.id_zona||emp.id_tipo_componente||';'||zona.id_zona||zona.id_tipo_componente||';'||mun_sub.id_zona||mun_sub.id_tipo_componente||';'||bar_cir.id_zona||bar_cir.id_tipo_componente||';'||trafo.id_componente||trafo.id_tipo_componente as claves "
//            + "from componente trafo, rel_componente_ubicacion rcu, tipo_componente tipo, "
//            + "zona_geografica bar_cir, zona_geografica mun_sub, zona_geografica zona, "
//            + "zona_geografica emp "
//            + "where trafo.id_cliente='" + idCliente + "' and "
//            + "rcu.periodo_fin>" + periodo + " and "
//            + "rcu.periodo_ini <=" + periodo + " and "
//            + "rcu.id_componente=trafo.id_componente and "
//            + "tipo.id_tipo_componente=bar_cir.id_tipo_componente and "
//            + "tipo.tipo_arbol='" + tipoArbol + "' and "
//            + "bar_cir.id_zona=rcu.id_zona and "
//            + "mun_sub.id_zona=bar_cir.id_padre and "
//            + "zona.id_zona=mun_sub.id_padre and "
//            + "emp.id_zona=zona.id_padre";
//
//    //boolean sw_nodo = getMostrarNodo().equalsIgnoreCase("si") ? false : true;
//    try {
//      //  int c=0;
//      //Modo lectura, optimiza la base de datos
//      System.out.println("Lanza el querie");
//      List l = dao.executeQuerie(sql, true);
//      System.out.println("Lanzó el querie");
//      if (l.size() > 0) {
//        System.out.println("Lanzó el querie + size " + l.size());
//        for (int i = 0; i < l.size(); i++) {
//          Object[] row = (Object[]) l.get(i);
//          try {
//            ruta = getString(row[0]) + "/" + getString(row[1]) + "/" + getString(row[2]) + "/" + getString(row[3]) + "/" + getString(row[4]);
//            String claves = getString(row[5]);
//            ruta = ruta + ";" + claves;
//            break;
//          } catch (Exception ex) {
//            Logger.getLogger(ArbolManagerImpl.class.getName()).log(Level.SEVERE, null, ex);
//          }
//
//        }
//        System.out.println("Se termino correctamente sin errores");
//      }
//
//    } catch (Exception ex) {
//      Logger.getLogger(ArbolManagerImpl.class.getName()).log(Level.SEVERE, null, ex);
//    }
//    return ruta;
//  }
//
//  @Override
//  public String rutaSuministro(String idCliente, String tipoArbol, String periodo) throws SQLException {
//    String ruta = idCliente + " no encontrado.";
//    String sql = "select emp.nombre as n1, zona.nombre as n2, mun_sub.nombre as n3, bar_cir.nombre as n4, trafo.NOMBRE as n5, sumi.nombre as n6, "
//            + " emp.id_Zona||emp.id_tipo_componente||';'||zona.id_Zona||zona.id_tipo_componente||';'||mun_sub.id_Zona||mun_sub.id_tipo_componente||';'||bar_cir.id_Zona||bar_cir.id_tipo_componente||';'||trafo.id_componente||trafo.id_tipo_componente as claves"
//            + " from rel_componente r, "
//            + "componente trafo, "
//            + "componente sumi, "
//            + "rel_componente_ubicacion rcu, "
//            + "tipo_componente tipo, "
//            + "zona_geografica bar_cir, "
//            + "zona_geografica mun_sub, "
//            + "zona_geografica zona, "
//            + "zona_geografica emp "
//            + "where r.id_componente_hijo=sumi.id_componente and "
//            + "sumi.id_cliente='" + idCliente + "' and "
//            + "r.periodo_fin>" + periodo + " and "
//            + "r.periodo_ini <=" + periodo + " and "
//            + "r.id_componente=trafo.id_componente and "
//            + "sumi.id_componente=r.id_componente_hijo and "
//            + "rcu.id_componente=trafo.id_componente and "
//            + "tipo.id_tipo_componente=bar_cir.id_tipo_componente and "
//            + "tipo.tipo_arbol='" + tipoArbol + "' and "
//            + "bar_cir.id_zona=rcu.id_zona and "
//            + "mun_sub.id_zona=bar_cir.id_padre and "
//            + "zona.id_zona=mun_sub.id_padre and "
//            + "emp.id_zona=zona.id_padre";
//
//    try {
//      //  int c=0;
//      //Modo lectura, optimiza la base de datos
//      System.out.println("Lanza el querie");
//      List l = dao.executeQuerie(sql, true);
//      System.out.println("Lanzó el querie");
//      if (l.size() > 0) {
//        System.out.println("Lanzó el querie + size " + l.size());
//        for (int i = 0; i < l.size(); i++) {
//          Object[] row = (Object[]) l.get(i);
//          try {
//            ruta = getString(row[0]) + "/" + getString(row[1]) + "/" + getString(row[2]) + "/" + getString(row[3]) + "/" + getString(row[4]) + "/" + getString(row[5]);
//            String claves = getString(row[6]);
//            ruta = ruta + ";" + claves;
//            break;
//          } catch (Exception ex) {
//            Logger.getLogger(ArbolManagerImpl.class.getName()).log(Level.SEVERE, null, ex);
//          }
//
//        }
//        System.out.println("Se termino correctamente sin errores");
//      }
//
//    } catch (Exception ex) {
//      Logger.getLogger(ArbolManagerImpl.class.getName()).log(Level.SEVERE, null, ex);
//    }
//
//    return ruta;
//  }
//
//  @Override
//  public UbicacionMacroV getUbicacionMacroV(String idComponente, String periodo) throws SQLException {
//    UbicacionMacroV result = new UbicacionMacroV();
//    String cantTrafos = "(select count(*) "
//            + "from rel_componente rc "
//            + "where rc.id_componente=" + idComponente + " and "
//            + "rc.periodo_ini<=" + periodo + " and "
//            + "rc.periodo_fin>" + periodo + ")";
//
//    String sql = "select zona.nombre as n0,munsub.nombre as n1,barcir.nombre as n2," + cantTrafos + " as n3 "
//            + "from rel_componente_ubicacion rcu,"
//            + "     zona_geografica barcir,"
//            + "     zona_geografica munsub,"
//            + "     zona_geografica zona "
//            + "where rcu.id_componente=" + idComponente + " and"
//            + "      rcu.periodo_ini<=" + periodo + " and "
//            + "      rcu.periodo_fin>" + periodo + " and"
//            + "      rcu.id_zona=barcir.id_zona and"
//            + "      barcir.id_padre=munsub.id_zona and"
//            + "      munsub.id_padre=zona.id_zona";
//
//    try {
//      //Modo lectura, optimiza la base de datos
//      List l = dao.executeQuerie(sql, true);
//      if (l != null && l.size() > 0) {
//        for (int i = 0; i < l.size(); i++) {
//          Object[] row = (Object[]) l.get(i);
//          try {
//            result.setZona(getString(row[0]));
//            result.setMunSub(getString(row[1]));
//            result.setBarCir(getString(row[2]));
//            result.setCantTrafosAso(getString(row[3]));
//            break;
//          } catch (Exception ex) {
//            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
//          }
//        }
//      }
//    } catch (Exception ex) {
//      Logger.getLogger(ArbolManagerImpl.class.getName()).log(Level.SEVERE, null, ex);
//    }
//
////         ResultSet rs = dao.getConnection().createStatement().executeQuery(sql);
////         if(rs.next()){
////            result.setZona(rs.getString(1));
////            result.setMunSub(rs.getString(2));
////            result.setBarCir(rs.getString(3));
////            result.setCantTrafosAso(rs.getString(4));
////         }
//    return result;
//  }
//
//  private String getMostrarNodo() {
//    String hql = "SELECT p.valor FROM Parametro p WHERE p.idParametro ='MOSTRAR_INCONSISTENTES'";
//    return dao.findObject(hql).toString();
//  }
//
//  /**
//   * @autor <b>Jose Mejia</b>
//   * @see MapBean
//   * @since 29/10/2014
//   * @param codigo
//   * @param tipo
//   * @param periodo
//   * @param arbol
//   * @return
//   */
//  @Override
//  public List<Componente> getComponentesUbicacion(String codigo, String tipo, String periodo, String arbol, HashMap gestionFiltros) {
//    String querie = getArmarQueries(codigo, tipo, periodo, arbol);
//    System.out.println("-------- getComponentesUbicacion " + querie);
//    if (querie != null && !querie.equals("")) {
//      List<Componente> result = getCoordenadasCompo(querie, periodo, gestionFiltros);
//      if (result instanceof List && result.size() > 0) {
//        return result;
//      }
//    }
//    return null;
//  }
//
//  /**
//   * Arma el querie que va a ejecutar
//   *
//   * @autor <b>Jose Mejia</b>
//   * @see MapBean
//   * @since 29/10/2014
//   * @param codigo
//   * @param tipoV
//   * @param periodo
//   * @param arbol
//   * @return
//   */
//  public String getArmarQueries(String codigo, String tipoV, String periodo, String arbol) {
//    String codPpal = codigo;
//    String tipo = tipoV;
//    String sql = "";
//    if (tipo.equals("0"))//Empresa
//    {
//      sql = "select trafo.id_componente as idComponente, trafo.nombre as nombre, trafo.id_tipo_componente as idTipocCompo, trafo.coord_x as coordx, trafo.coord_y as coordy, tbl.NOMBRE as tipoUso, tbl1.NOMBRE as redTrafo, tbl2.NOMBRE as localizacion, atr.IMAGEN as imagen,b.PORC_PERDIDA_MES as perMes, b.TOTAL_MACROS as totalMacr,b.TOTAL_MACROS_FUNC as macroFuncio,b.CANT_SUM_FACT as cantSumFac,rc.DESCRIPCION as rango, 'color:rgb(' || c.ROJO||','|| c.VERDE||','|| c.AZUL||')' as color,c.descripcion as nombrecolor \n"
//              + "        from componente trafo, rel_componente_ubicacion rcu,  tipo_componente tipo, \n"
//              + "             zona_geografica bar_cir, zona_geografica mun_sub, zona_geografica zona, \n"
//              + "             zona_geografica emp , Atr_Componente atr,tbltipo tbl,tbltipo tbl1,tbltipo tbl2,Balances b,Rango_Clasificacion rc, Color c \n"
//              + "        where rcu.id_componente=trafo.id_componente \n"
//              + "              and  tipo.id_tipo_componente=bar_cir.id_tipo_componente  \n"
//              + "              and  tipo.tipo_arbol='" + arbol + "' \n"
//              + "              and  bar_cir.id_zona=rcu.id_zona \n"
//              + "              and  mun_sub.id_zona=bar_cir.id_padre \n"
//              + "              and  zona.id_zona=mun_sub.id_padre \n"
//              + "              and  emp.id_zona=zona.id_padre \n"
//              + "              and  emp.ID_ZONA=" + codPpal + "\n"
//              + "              and   atr.ID_COMPONENTE=trafo.id_componente\n"
//              + "              and   atr.TIPO_USO=tbl.TIPO\n"
//              + "              and   atr.TIPO_RED=tbl1.TIPO\n"
//              + "              and   trafo.LOCALIZACION= tbl2.TIPO\n"
//              + "              and   b.Id_Componente=trafo.id_componente\n"
//              + "              and   b.ID_TIPO_COMPONENTE=trafo.id_tipo_componente\n"
//              + "              and   b.RANGO_BALANCE=rc.ID_RANGO  \n"
//              + "              and   rc.ID_COLOR=c.ID_COLOR\n"
//              + "              and   b.PERIODO =" + periodo + "\n"
//              + "              and  rcu.periodo_ini <=  " + periodo + "\n"
//              + "              and  rcu.periodo_fin >   " + periodo + "";
//    } else if (tipo.equals("1") || tipo.equals("5"))//Zona
//    {
//      sql = "select trafo.id_componente as idComponente, trafo.nombre as nombre, trafo.id_tipo_componente as idTipocCompo, trafo.coord_x as coordx, trafo.coord_y as coordy, tbl.NOMBRE as tipoUso, tbl1.NOMBRE as redTrafo, tbl2.NOMBRE as localizacion, atr.IMAGEN as imagen,b.PORC_PERDIDA_MES as perMes, b.TOTAL_MACROS as totalMacr,b.TOTAL_MACROS_FUNC as macroFuncio,b.CANT_SUM_FACT as cantSumFac,rc.DESCRIPCION as rango, 'color:rgb(' || c.ROJO||','|| c.VERDE||','|| c.AZUL||')' as color,c.descripcion as nombrecolor \n"
//              + "        from componente trafo, rel_componente_ubicacion rcu, tipo_componente tipo, \n"
//              + "             zona_geografica bar_cir, zona_geografica mun_sub, zona_geografica zona , Atr_Componente atr,tbltipo tbl,tbltipo tbl1,tbltipo tbl2,Balances b,Rango_Clasificacion rc, Color c\n"
//              + "        where rcu.id_componente=trafo.id_componente \n"
//              + "        and   tipo.id_tipo_componente=bar_cir.id_tipo_componente \n"
//              + "        and   tipo.tipo_arbol='" + arbol + "' \n"
//              + "        and   bar_cir.id_zona=rcu.id_zona  \n"
//              + "        and   mun_sub.id_zona=bar_cir.id_padre  \n"
//              + "        and   zona.id_zona=mun_sub.id_padre\n"
//              + "        and  zona.id_zona=" + codPpal + "\n"
//              + "        and   atr.ID_COMPONENTE=trafo.id_componente\n"
//              + "        and   atr.TIPO_USO=tbl.TIPO\n"
//              + "        and   atr.TIPO_RED=tbl1.TIPO\n"
//              + "        and   trafo.LOCALIZACION= tbl2.TIPO\n"
//              + "        and   b.Id_Componente=trafo.id_componente\n"
//              + "        and   b.ID_TIPO_COMPONENTE=trafo.id_tipo_componente\n"
//              + "        and   b.RANGO_BALANCE=rc.ID_RANGO  \n"
//              + "        and   rc.ID_COLOR=c.ID_COLOR\n"
//              + "        and   b.PERIODO =" + periodo + "\n"
//              + "        and  rcu.periodo_ini <= " + periodo + "\n"
//              + "        and  rcu.periodo_fin >  " + periodo + "";
//    } else if (tipo.equals("2") || tipo.equals("6"))//municipio subestacion
//    {
//      sql = "select trafo.id_componente as idComponente, trafo.nombre as nombre, trafo.id_tipo_componente as idTipocCompo, trafo.coord_x as coordx, trafo.coord_y as coordy, tbl.NOMBRE as tipoUso, tbl1.NOMBRE as redTrafo, tbl2.NOMBRE as localizacion, atr.IMAGEN as imagen,b.PORC_PERDIDA_MES as perMes, b.TOTAL_MACROS as totalMacr,b.TOTAL_MACROS_FUNC as macroFuncio,b.CANT_SUM_FACT as cantSumFac,rc.DESCRIPCION as rango, 'color:rgb(' || c.ROJO||','|| c.VERDE||','|| c.AZUL||')' as color,c.descripcion as nombrecolor \n"
//              + "        from componente trafo, rel_componente_ubicacion rcu, tipo_componente tipo, \n"
//              + "             zona_geografica bar_cir, zona_geografica mun_sub, Atr_Componente atr,tbltipo tbl,tbltipo tbl1,tbltipo tbl2,Balances b,Rango_Clasificacion rc, Color c\n"
//              + "        where rcu.id_componente=trafo.id_componente \n"
//              + "        and   tipo.id_tipo_componente=bar_cir.id_tipo_componente \n"
//              + "        and   tipo.tipo_arbol='" + arbol + "' \n"
//              + "        and   bar_cir.id_zona=rcu.id_zona  \n"
//              + "        and   mun_sub.id_zona=bar_cir.id_padre  \n"
//              + "        and   mun_sub.id_zona=" + codPpal + "\n"
//              + "        and   atr.ID_COMPONENTE=trafo.id_componente\n"
//              + "        and   atr.TIPO_USO=tbl.TIPO\n"
//              + "        and   atr.TIPO_RED=tbl1.TIPO\n"
//              + "        and   trafo.LOCALIZACION= tbl2.TIPO\n"
//              + "        and   b.Id_Componente=trafo.id_componente\n"
//              + "        and   b.ID_TIPO_COMPONENTE=trafo.id_tipo_componente\n"
//              + "        and   b.RANGO_BALANCE=rc.ID_RANGO  \n"
//              + "        and   rc.ID_COLOR=c.ID_COLOR\n"
//              + "        and   b.PERIODO =" + periodo + "\n"
//              + "        and  rcu.periodo_ini <= " + periodo + "\n"
//              + "        and  rcu.periodo_fin >  " + periodo + "";
//
//    } else if (tipo.equals("3") || tipo.equals("7"))//Circuito
//    {
//      sql = "select trafo.id_componente as idComponente, trafo.nombre as nombre, trafo.id_tipo_componente as idTipocCompo, trafo.coord_x as coordx, trafo.coord_y as coordy, tbl.NOMBRE as tipoUso, tbl1.NOMBRE as redTrafo, tbl2.NOMBRE as localizacion, atr.IMAGEN as imagen,b.PORC_PERDIDA_MES as perMes, b.TOTAL_MACROS as totalMacr,b.TOTAL_MACROS_FUNC as macroFuncio,b.CANT_SUM_FACT as cantSumFac,rc.DESCRIPCION as rango, 'color:rgb(' || c.ROJO||','|| c.VERDE||','|| c.AZUL||')' as color,c.descripcion as nombrecolor \n"
//              + "        from componente trafo, rel_componente_ubicacion rcu, tipo_componente tipo, \n"
//              + "             zona_geografica bar_cir, Atr_Componente atr,tbltipo tbl,tbltipo tbl1,tbltipo tbl2,Balances b,Rango_Clasificacion rc, Color c\n"
//              + "        where rcu.id_componente=trafo.id_componente \n"
//              + "        and   tipo.id_tipo_componente=bar_cir.id_tipo_componente \n"
//              + "        and   tipo.tipo_arbol='" + arbol + "' \n"
//              + "        and   bar_cir.id_zona=" + codPpal + "\n"
//              + "        and   bar_cir.id_zona=rcu.id_zona\n"
//              + "        and   atr.ID_COMPONENTE=trafo.id_componente\n"
//              + "        and   atr.TIPO_USO=tbl.TIPO\n"
//              + "        and   atr.TIPO_RED=tbl1.TIPO\n"
//              + "        and   trafo.LOCALIZACION= tbl2.TIPO\n"
//              + "        and   b.Id_Componente=trafo.id_componente\n"
//              + "        and   b.ID_TIPO_COMPONENTE=trafo.id_tipo_componente\n"
//              + "        and   b.RANGO_BALANCE=rc.ID_RANGO  \n"
//              + "        and   rc.ID_COLOR=c.ID_COLOR\n"
//              + "        and   b.PERIODO =" + periodo + "\n"
//              + "        and  rcu.periodo_ini <= " + periodo + "\n"
//              + "        and  rcu.periodo_fin >  " + periodo + "";
//    }
//    return sql;
//  }
//
//  /**
//   * Lista de componente
//   *
//   * @autor <b>Jose Mejia</b>
//   * @see MapBean
//   * @since 29/10/2014
//   * @param sql
//   * @return
//   */
//  public List getCoordenadasCompo(String sql, String periodo, HashMap gestionFiltros) {
//    List hijos = new ArrayList();
//    //boolean sw_nodo = getMostrarNodo().equalsIgnoreCase("si") ? false : true;
//    try {
//      //  int c=0;
//      //Modo lectura, optimiza la base de datos
//      System.out.println("Lanza el querie");
//      List l = dao.executeQuerie(sql, false);
//      System.out.println("Lanzó el querie");
//      if (l.size() > 0) {
//        System.out.println("Lanzó el querie + size " + l.size());
//        for (int i = 0; i < l.size(); i++) {
//          Object[] row = (Object[]) l.get(i);
//          try {
//            //Balances b = getBalances(rs.getString(1),rs.getString(3),periodo);
//            // c++;
//            String idCompo = getString(row[0] != null ? row[0] : "0");
//            String nombreComp = getString(row[1]);
//            String idTipoComp = getString(row[2] != null ? row[2] : "0");
//            String coordX = getString(row[3]);
//            String coordY = getString(row[4]);
//            String usoTrafo = getString(row[5]);
//            String redTrafo = getString(row[6]);
//            String localizacion = getString(row[7]);
//            String imagen = getString(row[8]);
//            String porPerdMes = getString(row[9] != null ? row[9] : "0");
//            String totalMacros = getString(row[10] != null ? row[10] : "0");
//            String totalMacFun = getString(row[11] != null ? row[11] : "0");
//            String cantSumFact = getString(row[12] != null ? row[12] : "0");
//            String rangoTrafo = getString(row[13]);
//            String color = getString(row[14]);
//            String nombreColor = getString(row[15]);
//
////                        System.out.println("id: "+idCompo);
////                        System.out.println("usoTrafo: "+usoTrafo);
////                        System.out.println("Color: "+color);
////                        System.out.println("Nombre Color:"+nombreColor);
//            if (porPerdMes != null && !porPerdMes.equals("null")) {
//
//              Nodo hijo = new Nodo();
//              hijo.setCodigo(idCompo);
//              hijo.setColor(color);
//              hijo.setCoordenadaX(coordX);
//              hijo.setCoordenadaY(coordY);
//              hijo.setImagen(imagen);
//              hijo.setLocalizacion(localizacion);
//              hijo.setNombre(nombreComp);
//              hijo.setNombreColor(nombreColor);
//              hijo.setNombreTipo(redTrafo);
//              hijo.setNumMacroTot(totalMacros);
//              hijo.setNumMacrosFuncionando(totalMacFun);
//              hijo.setNumSuministrosFact(cantSumFact);
//              hijo.setPerdidas(porPerdMes);
//              hijo.setTipo(idTipoComp);
//              hijo.setTipoUso(usoTrafo);
//
//              hijos.add(hijo);
//            }
//
//          } catch (Exception ex) {
//            Logger.getLogger(ArbolManagerImpl.class.getName()).log(Level.SEVERE, null, ex);
//          }
//
//        }
//        System.out.println("Se termino correctamente sin errores");
//      }
//
//      System.out.println("mañamamama " + hijos.size());
//    } catch (Exception ex) {
//      Logger.getLogger(ArbolManagerImpl.class.getName()).log(Level.SEVERE, null, ex);
//    }
//
//    return hijos;
//  }
//
//  public static BigDecimal getBigDecimal(Object value) {
//    BigDecimal ret = null;
//    if (value != null) {
//      if (value instanceof BigDecimal) {
//        ret = (BigDecimal) value;
//      } else if (value instanceof String) {
//        ret = new BigDecimal((String) value);
//      } else if (value instanceof BigInteger) {
//        ret = new BigDecimal((BigInteger) value);
//      } else if (value instanceof Number) {
//        ret = new BigDecimal(((Number) value).doubleValue());
//      } else {
//        throw new ClassCastException("Not possible to coerce [" + value + "] from class " + value.getClass() + " into a BigDecimal.");
//      }
//    }
//    return ret;
//  }
//
//  /**
//   * Castea el objeto a string
//   *
//   * @autor <b>Jose Mejia</b>
//   * @see ArbolManagerImpl
//   * @since 27/11/2014
//   * @param value
//   * @return
//   */
//  public String getString(Object value) {
//    String ret = "";
//    if (value != null) {
//      if (value instanceof BigDecimal) {
//        ret = ((BigDecimal) value).toString();
//      } else if (value instanceof String) {
//        ret = (String) value;
//      } else if (value instanceof BigInteger) {
//        ret = ((BigInteger) value).toString();
//      } else if (value instanceof Number) {
//        ret = new BigDecimal(((Number) value).doubleValue()).toString();
//      } else if (value instanceof Long) {
//        ret = ((Long) value).toString();
//      } else if (value instanceof Integer) {
//        ret = ((Integer) value).toString();
//      } else if (value instanceof Character) {
//        ret = ((Character) value).toString();
//      } else {
//        throw new ClassCastException("No es posible castear el object [" + value + "] a String, porque es de tipo " + value.getClass());
//      }
//    }
//    return ret;
//  }
//
//  /**
//   * Este método lo modifique porque generaba una excepción
//   *
//   * @autor <b>Jose Mejia</b>
//   * @see MapBean
//   * @since 29/10/2014
//   * @return
//   */
//  @Override
//  public String getRutaImage() {
//    String ruta = "";
//    String sql = "Select p.VALOR From Parametro p Where p.ID_PARAMETRO='RUTAIMAGEN'";
//    try {
//      //  int c=0;
//      //Modo lectura, optimiza la base de datos
//      System.out.println("Lanza el querie");
//      List l = dao.executeQuerie(sql, true);
//      System.out.println("Lanzó el querie");
//      if (l.size() > 0) {
//        System.out.println("Lanzó el querie + size " + l.size());
//        for (int i = 0; i < l.size(); i++) {
//
//          try {
//
//            Object row = l.get(i);
//            System.out.println("" + l.get(i).toString());
//            ruta = getString(row);
//            break;
//          } catch (Exception ex) {
//            ruta = "";
//            Logger.getLogger(ArbolManagerImpl.class.getName()).log(Level.SEVERE, null, ex);
//          }
//        }
//        System.out.println("Se termino correctamente sin errores");
//      }
//
//    } catch (Exception ex) {
//      Logger.getLogger(ArbolManagerImpl.class.getName()).log(Level.SEVERE, null, ex);
//    }
//    return ruta;
//  }
//
//}
