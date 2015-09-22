package com.ag.view;


import com.ag.service.ArbolManager;
import com.ag.service.ConsecutivoManager;
import com.ag.service.MenuPerfilManager;
import com.ag.service.SpringContext;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import org.primefaces.event.NodeCollapseEvent;
import org.primefaces.event.NodeExpandEvent;
import org.primefaces.event.NodeSelectEvent;
import org.primefaces.event.NodeUnselectEvent;
import org.primefaces.model.TreeNode;
import org.primefaces.model.DefaultTreeNode;

@ManagedBean
@SessionScoped

public class BalanceView implements Serializable {
     private List infoBalance;

    /**
     * @return the infoBalance
     */
    public List getInfoBalance() {
        return infoBalance;
    }

    /**
     * @param infoBalance the infoBalance to set
     */
    public void setInfoBalance(List infoBalance) {
        this.infoBalance = infoBalance;
    }

}
