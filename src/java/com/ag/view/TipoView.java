/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ag.view;

import com.ag.model.Grupo;
import com.ag.model.Tbltipo;
import com.ag.service.SpringContext;
import com.ag.service.TipoManager;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

/**
 *
 * @author arodriguezr
 */
@ManagedBean
@SessionScoped
public class TipoView implements Serializable {

    private SpringContext context;
    private TipoManager tipoManager;
    private List<Grupo> grupos;
    private List<Tbltipo> tipos;
    private List<Tbltipo> tiposDescripcion;
    private String codgrupo;
    private String codtipo;
    private String nombre;

    public TipoView() {
        context = SpringContext.getInstance();
        tipoManager = (TipoManager) context.getBean("TipoManager");
        setGrupos(tipoManager.getGruposAll());
        setTipos(tipoManager.getTiposAll());
        setTiposDescripcion(tipoManager.getTiposDescripciones());
        /*
         * setColores(rangoManager.getColoresAll());
        setTipoComponentes(rangoManager.getTiposComponentesAll());
         */
        codgrupo = "todo";
        codtipo = null;
        nombre = null;
    }

    public void filtrar() {
        if (codgrupo != null && !codgrupo.equals("")) {
            tipos = new ArrayList<Tbltipo>();
            tipos = codgrupo.equals("todo")
                    ? tipoManager.getTiposAll()
                    : tipoManager.getTiposByGrupo(codgrupo);
        }
        codtipo = null;
        nombre = null;
    }

    public void saveTipo(String usuario, String programa) {
        try {

            if (!tipoManager.existeTipo(codtipo)) {
                Tbltipo t = tipoManager.save(usuario, programa, codtipo.toUpperCase(), nombre, codgrupo);
                tipos.add(t);
                if (codgrupo.equals("TRC000")) tiposDescripcion.add(t);                     
                
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO,
                        "Se ha creado el Tipo con exito", ""));
            } else {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_WARN,
                        "El codigo del Tipo ya existe", ""));
            }           
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_FATAL,
                    "Ha ocurrido un error en el sistema al crear el Tipo", ""));
        } finally {
            codtipo = null;
            nombre = null;
        }
    }
    
    public List<Tbltipo> tiposByGrupo (String codgrup){
        return tipoManager.getTiposByGrupo(codgrup);
    }

    /*
     * private void ClearCampos(){ codgrupo=null; codtipo=null; nombre=null;
    }
     */
    public List<Grupo> getGrupos() {
        return grupos;
    }

    public void setGrupos(List<Grupo> grupos) {
        this.grupos = grupos;
    }

    public List<Tbltipo> getTipos() {
        return tipos;
    }

    public void setTipos(List<Tbltipo> tipos) {
        this.tipos = tipos;
    }

    public String getCodgrupo() {
        return codgrupo;
    }

    public void setCodgrupo(String codgrupo) {
        this.codgrupo = codgrupo;
    }

    public String getCodtipo() {
        return codtipo;
    }

    public void setCodtipo(String codtipo) {
        this.codtipo = codtipo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public List<Tbltipo> getTiposDescripcion() {
        return tiposDescripcion;
    }

    public void setTiposDescripcion(List<Tbltipo> tiposDescripcion) {
        this.tiposDescripcion = tiposDescripcion;
    }    
}
