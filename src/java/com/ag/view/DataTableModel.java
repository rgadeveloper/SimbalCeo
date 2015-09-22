/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ag.view;

/**
 *
 * @author 85154220
 */

import java.util.List;
import javax.faces.model.ListDataModel;
import org.primefaces.model.SelectableDataModel;
  
public class DataTableModel extends ListDataModel implements SelectableDataModel<Object> {    
  
    public DataTableModel() {  
    }  
  
    public DataTableModel(List<Object> data) {  
        super(data);  
    }  
      
    @Override  
    public Object getRowData(String rowKey) {  
        //In a real app, a more efficient way like a query by rowKey should be implemented to deal with huge data  
          
        List<Object> objetos = (List<Object>) getWrappedData();  
          
        for(Object obj : objetos) {  
            if(objetos.toString().equals(rowKey))  
                return obj;  
        }  
          
        return null;  
    }

    @Override
    public Object getRowKey(Object obj) {
        return obj.toString();
    }

    
  
    
}
