package com.ag.service.impl;

import com.ag.dao.Dao;
import com.ag.service.InterfazManager;
import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 *
 * @author 85154220
 */
@Service("InterfazManager")
public class InterfazManagerImpl implements InterfazManager {

  @Autowired
  @Qualifier("DaoHibernate")
  private Dao dao;

  @Override
  public String calcularBalance(int periodo) {
    try {
      Connection con = dao.getConnection();

      String sql = "{call P_CALCULO_BALANCES(?,?)}";
      CallableStatement statement = con.prepareCall(sql);

      statement.setInt(1, periodo);

      statement.registerOutParameter(2, Types.VARCHAR);

      statement.executeQuery();

      String error = statement.getString(2);
      con.close();
      return error;

    } catch (SQLException ex) {
      ex.printStackTrace();
      Logger.getLogger(InterfazManagerImpl.class.getName()).log(Level.SEVERE, null, ex);
      return "Error al ejecutar en la BD";
    }
  }

  @Override
  public String lecturas(int periodo, int borrar) {
    try {
      Connection con = dao.getConnection();

      String sql = "{call P_INT_LECTURAS(?,?,?)}";
      CallableStatement statement = con.prepareCall(sql);

      statement.setInt(1, periodo);

      statement.setInt(2, borrar); //preguntar a javier

      statement.registerOutParameter(3, Types.VARCHAR);

      statement.executeQuery();

      String error = statement.getString(3);
      con.close();
      return error;

    } catch (SQLException ex) {
      ex.printStackTrace();
      Logger.getLogger(InterfazManagerImpl.class.getName()).log(Level.SEVERE, null, ex);
      return "Error al ejecutar en la BD";
    }
  }

  @Override
  public String ejecutarTodo(int periodo) {
    try {
      Connection con = dao.getConnection();

      String sql = "{call P_INT_EJECUTAR(?,?)}";
      CallableStatement statement = con.prepareCall(sql);

      statement.setInt(1, periodo);

      statement.registerOutParameter(2, Types.VARCHAR);

      statement.executeQuery();

      String error = statement.getString(2);
      con.close();
      return error;

    } catch (SQLException ex) {
      ex.printStackTrace();
      Logger.getLogger(InterfazManagerImpl.class.getName()).log(Level.SEVERE, null, ex);
      return "Error al ejecutar P_INT_EJECUTAR en la BD";
    }
  }

  @Override
  public String ceoAsimbal(int periodo) {
    try {
      Connection con = dao.getConnection();

      String sql = "{call P_INT_CEO_SIMBAL(?,?)}";
      CallableStatement statement = con.prepareCall(sql);

      statement.setInt(1, periodo);

      statement.registerOutParameter(2, Types.VARCHAR);

      statement.executeQuery();

      String error = statement.getString(2);
      con.close();
      return error;

    } catch (SQLException ex) {
      ex.printStackTrace();
      Logger.getLogger(InterfazManagerImpl.class.getName()).log(Level.SEVERE, null, ex);
      return "Error al ejecutar en P_INT_CEO_SIMBAL la BD";
    }
  }

  @Override
  public int getBorrarLecturas() {
    String Hql = "SELECT p.valor FROM Parametro p WHERE p.idParametro = 'BORRAR'";
    int valor = Integer.valueOf(dao.findObject(Hql).toString());
    return valor;
  }

  @Override
  public String cargaTrafosSinMacro(
          String transformador, String estadoCorte, String direccion, String coordX,
          String coordY, int carga, String tipoMedidor, String fechaInstalacion,
          String uso, String subcategoria, String cicloConsumo, String medidor,
          String ruta, String circuito, String barrio, int periodo) {
    try {
      Connection con = dao.getConnection();

      String sql = "{call P_TRAFO_SIN_MACRO(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}";
      CallableStatement statement = con.prepareCall(sql);

      statement.setString(1, transformador);
      statement.setString(2, estadoCorte);
      statement.setString(3, direccion);
      statement.setString(4, coordX);
      statement.setString(5, coordY);
      statement.setInt(6, carga);
      statement.setString(7, tipoMedidor);
      statement.setString(8, fechaInstalacion);
      statement.setString(9, uso);
      statement.setString(10, subcategoria);
      statement.setString(11, cicloConsumo);
      statement.setString(12, medidor);
      statement.setString(13, ruta);
      statement.setString(14, circuito);
      statement.setString(15, barrio);
      statement.setInt(16, periodo);

      statement.registerOutParameter(17, Types.VARCHAR);

      statement.executeQuery();

      String error = statement.getString(2);
      con.close();
      return error;

    } catch (SQLException ex) {
      ex.printStackTrace();
      Logger.getLogger(InterfazManagerImpl.class.getName()).log(Level.SEVERE, null, ex);
      return "Error al ejecutar en la BD";
    }
  }

}
