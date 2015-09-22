/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.ag.view;


import com.ag.model.Menu;
import com.ag.service.MenuPerfilManager;
import com.ag.service.SpringContext;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;
import org.primefaces.component.menubar.Menubar;
//import org.primefaces.component.menuitem.MenuItem;
//import org.primefaces.component.submenu.Submenu;
//import org.primefaces.model.DefaultMenuModel;
//import org.primefaces.model.MenuModel;
import org.primefaces.model.menu.DefaultMenuItem;
import org.primefaces.model.menu.DefaultMenuModel;
import org.primefaces.model.menu.DefaultSubMenu;
import org.primefaces.model.menu.MenuModel;

/**
 *
 * @author 85154220
 * Modificado el 22/12/2014 por SISJMM
 */
@ManagedBean
@SessionScoped
public class TemplateView implements Serializable {

    @ManagedProperty("#{login}")
    private Login login;
    private SpringContext context;
    private MenuPerfilManager menuPerfilManager;
    private Menubar menuBar = new Menubar();
    private MenuModel model = new DefaultMenuModel();
    private String consecutivo;

    public TemplateView() {
    }

    @PostConstruct
    public void init() {
        context = SpringContext.getInstance();
        menuPerfilManager = (MenuPerfilManager) context.getBean("MenuPerfilManager");
        construirMenu(login.getUsuario());

    }

    private void reset() {
        HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
        session.invalidate();
    }

    public String getUsuario() {
        return getLogin().getUsuario();
    }

    /**
     * @return the login
     */
    public Login getLogin() {
        return login;
    }

    /**
     * @param login the login to set
     */
    public void setLogin(Login login) {
        this.login = login;
    }

//    public static void main(String[] args) {
//        new TemplateView().construirMenu("COMALEX");
//    }
    
     /**
     * Se creó nuevamente la opción de menú. La creación anterior no es compatible con la 
     * versión 4.0 de primefaces.
     * Construye el menú dinámicamente para la versión 4.0 de primefaces.
     * Se crearon los métodos:
     *  -construirMenu : Construye el menú principla.
     *  -buscarHijo    : Busca los hijos de los padres.
     *  -buscarHijoHijo: Busca los hijos de los hijos.
     *  -verificarHijo : Verifique que cualquier elemento (hijo, padre) tengan hijos o no.
     * @autor <b>Jose Mejia</b>
     * @see TemplateView
     * @since 22/12/2014
     * @param usuario
     * @return 
     */
    
    public void construirMenu(String usuario) {
        List<Menu> menu = menuPerfilManager.MenuPerfilManagerUsu(usuario);
        //List <Menu> men= menuPerfilManager.MenuPerfilManagerUsu("COMALEX");
        model = new DefaultMenuModel();
        DefaultSubMenu subMenu;
        for (Menu menuTemp : menu) {
            //Buscar los padres
            if (menuTemp.getCodPadre() == null) {
                //Hijos de padre
                //PAdre 0 -
                subMenu = new DefaultSubMenu();
                //Buscar los hijos de 0
                subMenu = buscarHijo(menuTemp.getCodigo(), menu);
                subMenu.setLabel(menuTemp.getNombre());
                subMenu.setIcon(menuTemp.getIcono());
                model.addElement(subMenu);
            }
        }

    }
    //PAdre padre

    public DefaultSubMenu buscarHijo(String ipPadre, List<Menu> men) {
        DefaultSubMenu submenu = new DefaultSubMenu();
        for (Menu menuTemp : men) {
            if (menuTemp.getCodPadre() != null && menuTemp.getCodPadre().equals(ipPadre)) {
                if (verificarHijo(men, menuTemp.getCodigo())) {
                    //Se busca el hijo
                    submenu.addElement(buscarHijoHijo(menuTemp.getCodigo(), men));
                } else {
                    //No tiene Hijo, es un elemento
                    DefaultMenuItem item = new DefaultMenuItem();
                    item.setValue(menuTemp.getNombre());
                    item.setUrl(menuTemp.getUrl());
                    item.setIcon(menuTemp.getIcono());
                    submenu.addElement(item);
                }
                submenu.setLabel(menuTemp.getNombre());
                submenu.setIcon(menuTemp.getIcono());
            }
        }
        return submenu;
    }

    public DefaultSubMenu buscarHijoHijo(String idHijo, List<Menu> men) {
        DefaultSubMenu submenuTempo = new DefaultSubMenu();
        for (int i = 0; i < men.size(); i++) {
            if (men.get(i).getCodigo().equals(idHijo)) {
                if (verificarHijo(men, men.get(i).getCodigo())) {
                    //Se busca el hijo
                    submenuTempo.addElement(buscarHijo(men.get(i).getCodigo(), men));
                } else {
                    //No tiene Hijo, es un elemento
                    DefaultMenuItem item = new DefaultMenuItem();
                    item.setValue(men.get(i).getNombre());
                    item.setUrl(men.get(i).getUrl());
                    item.setIcon(men.get(i).getIcono());
                    submenuTempo.addElement(item);
                }
                submenuTempo.setLabel(men.get(i).getNombre());
                submenuTempo.setIcon(men.get(i).getIcono());
            }
        }
        return submenuTempo;
    }

    public boolean verificarHijo(List<Menu> menu, String id) {
        for (int i = 0; i < menu.size(); i++) {
            if (menu.get(i).getCodPadre() != null) {
                if (menu.get(i).getCodPadre().equals(id)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * @return the menuBar
     */
    public Menubar getMenuBar() {
        return menuBar;
    }

    /**
     * @param menuBar the menuBar to set
     */
    public void setMenuBar(Menubar menuBar) {
        this.menuBar = menuBar;
    }

    /**
     * @return the model
     */
    public MenuModel getModel() {
        return model;
    }

    /**
     * @param model the model to set
     */
    public void setModel(MenuModel model) {
        this.model = model;
    }

    /**
     * @return the consecutivo
     */
    public String getConsecutivo() {
        return consecutivo;
    }

    /**
     * @param consecutivo the consecutivo to set
     */
    public void setConsecutivo(String consecutivo) {
        this.consecutivo = consecutivo;
    }
}
