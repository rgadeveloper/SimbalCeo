package com.ag.view;

import com.ag.service.InterfazManager;
import com.ag.service.SpringContext;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import org.primefaces.model.UploadedFile;

@ManagedBean
@SessionScoped
public class CargaTrafosSinMacro implements Serializable {

  private String mensaje;
  private String estado;
  @ManagedProperty("#{login}")
  private Login login;
  private UploadedFile file;
  private SpringContext context;
  private InterfazManager interfazManager;
  
  public CargaTrafosSinMacro() {
    context = SpringContext.getInstance();
    interfazManager = (InterfazManager) context.getBean("InterfazManager");
  }

  public String getMensaje() {
    return mensaje;
  }

  public void setMensaje(String mensaje) {
    this.mensaje = mensaje;
  }

  public String getEstado() {
    return estado;
  }

  public void setEstado(String estado) {
    this.estado = estado;
  }

  public Login getLogin() {
    return login;
  }

  public void setLogin(Login login) {
    this.login = login;
  }

  public UploadedFile getFile() {
    return file;
  }

  public void setFile(UploadedFile file) {
    this.file = file;
  }

  public void upload() throws IOException {
    if (file != null && file.getFileName() != null && file.getFileName() != "") {
      estado = "Cargado";
      mensaje = "";
      boolean sw = true;
      sw = copyFile(file.getInputstream());//eviamos al copyfile
      if (sw) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Correcto!", "El archivo fue cargado correctamente."));
      } else {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error!", "El archivo no fue cargado correctamente."));
      }
      //SpringContext context = SpringContext.getInstance();
      //ArchivoManager archivoManager = (ArchivoManager) context.getBean("ArchivoManager");
    } else {
      FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Atención!", "Debe seleccionar un archivo para poder realizar la carga."));
    }
    limpiar();
  }

  private boolean copyFile(InputStream inputStream) {
    HashMap<String, String> map = new HashMap();
    BufferedReader bufferedReader = null;
    String linea = "";
    String columnas[] = null;
    List<String[]> registros = new ArrayList();
    List<String[]> registrosProcesar = new ArrayList();

    boolean sw = true;

    try {
      bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
      columnas = bufferedReader.readLine().split(";");
      for (int j = 0; j < columnas.length; j++) {
        map.put("" + j, columnas[j]);
      }

      /*Verificación de columnas*/
      //verificando que todas las columnas tengan valor
      int i = 0;
      while ((linea = bufferedReader.readLine()) != null) {
        registros.add(linea.split(";"));
        //Iterando por cada columna
        for (int j = 0; j < registros.get(j).length; j++) {
          if (registros.get(i)[j].equals("")) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error!", "Por favor verifique el registro " + (i + 1) + " en la columna " + map.get("" + j));
            FacesContext.getCurrentInstance().addMessage(null, msg);
            sw = false;
            break;
          }
        }
        if (!sw) {
          break;
        }
        i++;
      }/*Fin verificación de columnas*/

    } catch (Exception ex) {
      ex.printStackTrace();
    }

    /*Pasando al procedure*/
    //Si todos los registros tienen valor en sus columnas, se pasan al procedure
    if (sw) {
      int contadorRegistro = 0;

      //Iterando lista de registros
      for (Iterator<String[]> iterator = registros.iterator(); iterator.hasNext();) {

        try {
          //Cada registro lo inserto en un array para validar
          String[] registro = iterator.next();
          contadorRegistro++;

          //Carga instalada
          try {
            Double cargaInstalada = Double.valueOf(registro[5].replace(",", "."));

            //Periodo
            try {
              int periodo = Integer.valueOf(registro[15]);

              //Se insertan en un arrayList los registros que pasaron las validaciones
              registrosProcesar.add(registro);

            } catch (NumberFormatException ex) {
              mensaje += "El periodo para el registro: " + contadorRegistro + " cuyo periodo ingresado es:"
                      + registro[15] + " con id de trafo es: " + registro[0] + " debe ser numérico<br>";
              ex.printStackTrace();
            }

          } catch (NumberFormatException ex) {
            mensaje += "La carga instalada para el registro: " + contadorRegistro + " cuyo tipo de carga ingresada es:"
                    + registro[5] + " con id de trafo es: " + registro[0] + " debe ser numérico<br>";
          }

        } catch (Exception ex) {
          mensaje += "Se ha producido un error desconocido en el registro: " + contadorRegistro + "<br>";
          ex.printStackTrace();
        }

      }//fin iteración lista de registros

      if (registrosProcesar instanceof List && registrosProcesar.size() > 0) {
        String error;
        String[] registro;
        for (Iterator<String[]> procesar = registrosProcesar.iterator(); procesar.hasNext();) {
          registro = procesar.next();
          error = interfazManager.cargaTrafosSinMacro(
                  registro[0], registro[1], registro[2], registro[3], 
                  registro[4], Integer.parseInt(registro[5]), registro[6], registro[7], 
                  registro[0], registro[0], registro[0], registro[0], 
                  registro[0], registro[0], registro[0], Integer.parseInt(registro[0]));
        }
        
      } else {
        System.out.println("Archivo no pasó");
      }

    }/*Fin pasando al procedure*/

    return sw;
  }

  private void limpiar() {
    file = null;
    mensaje = "";
    estado = "Carga";
  }

}
