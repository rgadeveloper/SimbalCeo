/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ag.Interfaces;

import com.ag.service.InterfazManager;
import com.ag.service.SpringContext;
import javax.faces.bean.ManagedBean;  
import javax.faces.bean.SessionScoped;
import java.io.Serializable;

import javax.faces.application.FacesMessage;  
import javax.faces.context.FacesContext; 

/**
 *
 * @author arodriguezr
 */
@ManagedBean 
@SessionScoped
public class EjecutarInterfaz implements Serializable {

    private SpringContext context;
    private InterfazManager interfazManager;
    private String periodo;
    
    private String interfaz;
    //private Map<String,String> opciones = new HashMap<String, String>();   
   
    public EjecutarInterfaz() {
        context = SpringContext.getInstance();        
        interfazManager =   (InterfazManager) context.getBean("InterfazManager");  
    }      
   
    
    public void calcularBalance() {
      try {        
        int parametro=!periodo.equals("") && periodo!=null?
                Integer.valueOf(periodo):0;
        String error = interfazManager.calcularBalance(parametro);        
        if (error == null || error == "" || error.equals(""))
            FacesContext.getCurrentInstance().addMessage
                   (null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                   "El Calculo de Balances finalizo correctamente.", ""));
        else
            FacesContext.getCurrentInstance().addMessage
                   (null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                   "El Calculo de Balances no se ejecuto con exito.", "Detalle:"+error)); 
            
      } catch (Exception e) {
          FacesContext.getCurrentInstance().addMessage
                   (null, new FacesMessage(FacesMessage.SEVERITY_FATAL,
                   "Upss ha ocurrido un error en el sistema.", "Detalle:"+e.getMessage())); 
      }finally{
          periodo=null;
      }
        
    }
     
    public void ejecutarInterfaz() {
      try { 
        if (interfaz!=null && !interfaz.equals("") && periodo!=null && !periodo.equals("")) {
              
            String error;
            if (interfaz.equals("P_INT_CEO_SIMBAL"))
              error = interfazManager.ceoAsimbal(Integer.valueOf(periodo));
            else{
              if (interfaz.equals("P_INT_LECTURAS")){
                int borrar= interfazManager.getBorrarLecturas();
                error = interfazManager.lecturas(Integer.valueOf(periodo), borrar);
              }else // sino es P_INT_EJECUTAR
                error = interfazManager.ejecutarTodo(Integer.valueOf(periodo));
            }
                     
            if (error == null || error.equals(""))
                FacesContext.getCurrentInstance().addMessage
                    (null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "La interfaz finalizo correctamente.", ""));
            else
                FacesContext.getCurrentInstance().addMessage
                    (null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "La interfaz no se ejecuto con exito.", "Detalle:"+error)); 
        }else{
            FacesContext.getCurrentInstance().addMessage
                    (null, new FacesMessage(FacesMessage.SEVERITY_WARN,
                    "Debe seleccionar el periodo y escoger la interfaz a ejecutar", ""));
        }   
      } catch (Exception e) {
          FacesContext.getCurrentInstance().addMessage
                   (null, new FacesMessage(FacesMessage.SEVERITY_FATAL,
                   "Upss ha ocurrido un error en el sistema.", "Detalle:"+e.getMessage())); 
      }/*finally{
          periodo=null;
          interfaz=null;
      }*/
        
    }

    public String getPeriodo() {
        return periodo;
    }

    public void setPeriodo(String periodo) {
        this.periodo = periodo;
    }

    public String getInterfaz() {
        return interfaz;
    }

    public void setInterfaz(String interfaz) {
        this.interfaz = interfaz;
    }    
    
}
