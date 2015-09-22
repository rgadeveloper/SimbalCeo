/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ag.view;

/**
 *
 * @author Larry
 */
import com.ag.model.Componente;
import com.ag.model.view.Nodo;
import com.ag.model.view.SimpleImageInfo;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.primefaces.event.map.OverlaySelectEvent;
import org.primefaces.model.map.DefaultMapModel;
import org.primefaces.model.map.LatLng;
import org.primefaces.model.map.MapModel;
import org.primefaces.model.map.Marker;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

@ManagedBean
@SessionScoped
public class MapBean implements Serializable {

  private MapModel advancedModel;
  private Marker marker;
  private String cordX;
  private String cordY;
  private String nombreComponente;
  private String perdidas;
  private HashMap zoomTipoComponente;
  private String zoom;
  private String center;
  private String tipo;
  private String codigo;
  private String nombreTipo;
  private String numMacroTot;
  private String numMacrosFuncionando;
  private String localizacion;
  private String numSuministrosFact;
  private Integer numFotos;
  private StreamedContent graphicText;
  private String height;
  private String width;
  private String nivel;
  private String ruta;
  private String nombreImagen;

  public MapBean() {
    //Shared coordinates
    /*
     * LatLng coord1 = new LatLng(2.26,-76.36); LatLng coord2 = new
     * LatLng(11.24722,-74.20167);
     *
     * advancedModel.addOverlay(new Marker(coord1, "Popayan"));
     * advancedModel.addOverlay(new Marker(coord2, "Santa Marta"));
     */
  }

  public void createMapa() {
    advancedModel = new DefaultMapModel();
    LatLng coord = new LatLng(Double.parseDouble(cordY), Double.parseDouble(cordX));
    advancedModel.addOverlay(new Marker(coord, codigo + ":" + tipo + ":" + getNombreComponente() + " : " + getPerdidas() + "%"));

  }

  public MapModel getAdvancedModel() {
    return advancedModel;
  }

  public void onMarkerSelect(OverlaySelectEvent event) {
    marker = (Marker) event.getOverlay();
    try {
      String title = marker.getTitle();
      String[] aux = title.split(":");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public Marker getMarker() {
    return marker;
  }

  public void setNodo(Nodo node) {
    this.cordX = node.getCoordenadaX();
    this.cordY = node.getCoordenadaY();
    this.nombreComponente = node.getNombre();
    this.perdidas = node.getPerdidas();
    this.tipo = node.getTipo();
    this.codigo = node.getCodigo();
    this.nombreTipo = node.getNombreTipo();
    this.numMacroTot = node.getNumMacroTot();
    this.numMacrosFuncionando = node.getNumMacrosFuncionando();
    this.numSuministrosFact = node.getNumSuministrosFact();
    setZoom(node.getTipo());
    setCenter(node);
    createMapa();
  }

  public void setNodoList(Nodo nodoPadre) {
//    advancedModel = new DefaultMapModel();
//    List nodeList = nodoPadre.getHijos();
//    setZoom(nodoPadre.getTipo());
//
//    System.out.println("MapBean --> " + nodoPadre.getTipo());
//    setCenter(nodoPadre);
//    if (nodeList != null) {
//      for (int i = 0; i < nodeList.size(); i++) {
//        try {
//          Nodo nodoDataHijo = (Nodo) nodeList.get(i);
//          if (nodoDataHijo.getCoordenadaX() != null && nodoDataHijo.getCoordenadaY() != null) {
//            double x = Double.parseDouble(nodoDataHijo.getCoordenadaX());
//            LatLng coord = new LatLng(Double.parseDouble(nodoDataHijo.getCoordenadaY()), x);
//            setZoom(nodoDataHijo.getTipo());
//            nombreTipo = nodoDataHijo.getNombreTipo();
//            numMacroTot = nodoDataHijo.getNumMacroTot();
//            numMacrosFuncionando = nodoDataHijo.getNumMacrosFuncionando();
//            localizacion = nodoDataHijo.getLocalizacion();
//            nombreComponente = nodoDataHijo.getNombre();
//            perdidas = nodoDataHijo.getPerdidas();
//            numSuministrosFact = nodoDataHijo.getNumSuministrosFact();
//            if (nodoDataHijo.getTipo().equals("8")) {
//              String carpeta;
//              if (nodoDataHijo.getTipoUso().equalsIgnoreCase("COMERCIAL")) {
//                carpeta = "comercial";
//              } else if (nodoDataHijo.getTipoUso().equalsIgnoreCase("RESIDENCIAL")) {
//                carpeta = "residencial";
//              } else if (nodoDataHijo.getTipoUso().equalsIgnoreCase("INDUSTRIAL")) {
//                carpeta = "industrial";
//              } else if (nodoDataHijo.getTipoUso().equalsIgnoreCase("OFICIAL")) {
//                carpeta = "oficial";
//              } else if (nodoDataHijo.getTipoUso().equalsIgnoreCase("ALUM PUBLICO")) {
//                carpeta = "alumbrado";
//              } else {
//                carpeta = "basico";
//              }
//              //System.out.println("MAp bean nombre del color "+nodoDataHijo.getNombreColor());
//              if (nodoDataHijo.getNombreColor().equalsIgnoreCase("azul")) {
//                advancedModel.addOverlay(new Marker(coord, nodoDataHijo.getCodigo() + ":" + nodoDataHijo.getTipo() + ":" + nodoDataHijo.getNombre(), nodoDataHijo.getImagen(), "../resources/images/icon-gmap/trafo/" + carpeta + "/azul.png"));
//              } else if (nodoDataHijo.getNombreColor().equalsIgnoreCase("amarillo")) {
//                advancedModel.addOverlay(new Marker(coord, nodoDataHijo.getCodigo() + ":" + nodoDataHijo.getTipo() + ":" + nodoDataHijo.getNombre(), nodoDataHijo.getImagen(), "../resources/images/icon-gmap/trafo/" + carpeta + "/amarillo.png"));
//              } else if (nodoDataHijo.getNombreColor().equalsIgnoreCase("blanco")) {
//                advancedModel.addOverlay(new Marker(coord, nodoDataHijo.getCodigo() + ":" + nodoDataHijo.getTipo() + ":" + nodoDataHijo.getNombre(), nodoDataHijo.getImagen(), "../resources/images/icon-gmap/trafo/" + carpeta + "/blanco.png"));
//              } else if (nodoDataHijo.getNombreColor().equalsIgnoreCase("verde")) {
//                advancedModel.addOverlay(new Marker(coord, nodoDataHijo.getCodigo() + ":" + nodoDataHijo.getTipo() + ":" + nodoDataHijo.getNombre(), nodoDataHijo.getImagen(), "../resources/images/icon-gmap/trafo/" + carpeta + "/verde.png"));
//              } else if (nodoDataHijo.getNombreColor().equalsIgnoreCase("rojo")) {
//                advancedModel.addOverlay(new Marker(coord, nodoDataHijo.getCodigo() + ":" + nodoDataHijo.getTipo() + ":" + nodoDataHijo.getNombre(), nodoDataHijo.getImagen(), "../resources/images/icon-gmap/trafo/" + carpeta + "/rojo.png"));
//              } else if (nodoDataHijo.getNombreColor().equalsIgnoreCase("violeta")) {
//                advancedModel.addOverlay(new Marker(coord, nodoDataHijo.getCodigo() + ":" + nodoDataHijo.getTipo() + ":" + nodoDataHijo.getNombre(), nodoDataHijo.getImagen(), "../resources/images/icon-gmap/trafo/" + carpeta + "/violeta.png"));
//              } else if (nodoDataHijo.getNombreColor().equalsIgnoreCase("naranja")) {
//                advancedModel.addOverlay(new Marker(coord, nodoDataHijo.getCodigo() + ":" + nodoDataHijo.getTipo() + ":" + nodoDataHijo.getNombre(), nodoDataHijo.getImagen(), "../resources/images/icon-gmap/trafo/" + carpeta + "/naranja.png"));
//              } else if (nodoDataHijo.getNombreColor().equalsIgnoreCase("fucsia")) {
//                advancedModel.addOverlay(new Marker(coord, nodoDataHijo.getCodigo() + ":" + nodoDataHijo.getTipo() + ":" + nodoDataHijo.getNombre(), nodoDataHijo.getImagen(), "../resources/images/icon-gmap/trafo/" + carpeta + "/fucsia.png"));
//              } else {
//                advancedModel.addOverlay(new Marker(coord, nodoDataHijo.getCodigo() + ":" + nodoDataHijo.getTipo() + ":" + nodoDataHijo.getNombre(), nodoDataHijo.getImagen(), "../resources/images/icon-gmap/trafo/" + carpeta + "/none.png"));
//              }
//            } else {
//              if (nodoDataHijo.getTipo().equals("9")) {
//                String icono;
//                if (nodoDataHijo.getTipoUso().equalsIgnoreCase("COMERCIAL")) {
//                  icono = "comercial";
//                } else if (nodoDataHijo.getTipoUso().equalsIgnoreCase("RESIDENCIAL")) {
//                  icono = "residencial";
//                } else if (nodoDataHijo.getTipoUso().equalsIgnoreCase("INDUSTRIAL")) {
//                  icono = "industrial";
//                } else if (nodoDataHijo.getTipoUso().equalsIgnoreCase("OFICIAL")) {
//                  icono = "oficial";
//                } else if (nodoDataHijo.getTipoUso().equalsIgnoreCase("ALUM PUBLICO")) {
//                  icono = "alumbrado";
//                } else {
//                  icono = "basico";
//                }
//                advancedModel.addOverlay(new Marker(coord, nodoDataHijo.getCodigo() + ":" + nodoDataHijo.getTipo() + ":" + nodoDataHijo.getNombre(), nodoDataHijo.getImagen(), "../resources/images/icon-gmap/cliente/" + icono + ".png"));
//              } else {
//                advancedModel.addOverlay(new Marker(coord, nodoDataHijo.getCodigo() + ":" + nodoDataHijo.getTipo() + ":" + nodoDataHijo.getNombre(), nodoDataHijo.getImagen()));
//              }
//            }
//          }
//        } catch (Exception e) {
//          e.printStackTrace();
//        }
//      }
//    }

    advancedModel = new DefaultMapModel();
    List nodeList = nodoPadre.getHijos();
    setZoom(nodoPadre.getTipo());

    if (nodeList != null && nodeList.size() != 0) {
      for (int i = 0; i < nodeList.size(); i++) {
        Nodo nodoDataHijo = (Nodo) nodeList.get(i);
        if (nodoDataHijo.getCoordenadaX() != null) {
          if (i == 0) {
            if (nodoPadre.getTipo().equals("8")) {
              setCenter(nodoPadre);
            } else {
              setCenter(nodoDataHijo);
            }
          }
          double x = Double.parseDouble(nodoDataHijo.getCoordenadaX());
          LatLng coord = new LatLng(Double.parseDouble(nodoDataHijo.getCoordenadaY()), x);
          setZoom(nodoDataHijo.getTipo());
          nombreTipo = nodoDataHijo.getNombreTipo();
          numMacroTot = nodoDataHijo.getNumMacroTot();
          numMacrosFuncionando = nodoDataHijo.getNumMacrosFuncionando();
          localizacion = nodoDataHijo.getLocalizacion();
          nombreComponente = nodoDataHijo.getNombre();
          perdidas = nodoDataHijo.getPerdidas();
          numSuministrosFact = nodoDataHijo.getNumSuministrosFact();
//                    tipoUso          = nodoDataHijo.getTipoUso();
          cordX = nodoDataHijo.getCoordenadaX();
          cordY = nodoDataHijo.getCoordenadaY();
          /*if (!nodoDataHijo.getTipo().equals("9")) {
           if (nodoDataHijo.getTipo().equals("8")) {                            
           if (nodoDataHijo.getTipoTrafo().equals("COMERCIAL"))
           advancedModel.addOverlay(new Marker(coord,nodoDataHijo.getCodigo()+":"+nodoDataHijo.getTipo()+":"+ nodoDataHijo.getNombre() + " : " + nodoDataHijo.getPerdidas() + "%", "comercialMarker.png","../resources/images/comercialMarker.png"));
           else if(nodoDataHijo.getTipoTrafo().equals("RESIDENCIAL"))                                 
           advancedModel.addOverlay(new Marker(coord,nodoDataHijo.getCodigo()+":"+nodoDataHijo.getTipo()+":"+ nodoDataHijo.getNombre() + " : " + nodoDataHijo.getPerdidas() + "%", "residencialMarker.png","../resources/images/residencialMarker.png"));
           else if(nodoDataHijo.getTipoTrafo().equals("INDUSTRIAL"))
           advancedModel.addOverlay(new Marker(coord,nodoDataHijo.getCodigo()+":"+nodoDataHijo.getTipo()+":"+ nodoDataHijo.getNombre() + " : " + nodoDataHijo.getPerdidas() + "%", "industrialMarker.png","../resources/images/industrialMarker.png"));
           else
           advancedModel.addOverlay(new Marker(coord,nodoDataHijo.getCodigo()+":"+nodoDataHijo.getTipo()+":"+ nodoDataHijo.getNombre() + " : " + nodoDataHijo.getPerdidas() + "%")); 
           }else
           advancedModel.addOverlay(new Marker(coord,nodoDataHijo.getCodigo()+":"+nodoDataHijo.getTipo()+":"+ nodoDataHijo.getNombre() + " : " + nodoDataHijo.getPerdidas() + "%"));
           } else {*/
          if (nodoDataHijo.getTipo().equals("8")) {
            if (i == 0) {
              double xi = Double.parseDouble(nodoDataHijo.getCoordenadaX());
              LatLng coordC = new LatLng(Double.parseDouble(nodoDataHijo.getCoordenadaY()), xi);
              if (nodoPadre.getTipo().equals("2")) {
                advancedModel.addOverlay(new Marker(coordC, nodoPadre.getCodigo() + ":" + nodoPadre.getTipo() + ":" + nodoPadre.getNombre(), null, "../resources/images/icon-gmap/cliente/tipo-2.png"));
              } else {
                advancedModel.addOverlay(new Marker(coordC, nodoPadre.getCodigo() + ":" + nodoPadre.getTipo() + ":" + nodoPadre.getNombre(), null, "../resources/images/icon-gmap/cliente/tipo-6-3-7.png"));
              }
            }
            String carpeta = "";
            if (nodoDataHijo.getTipoUso().equalsIgnoreCase("COMERCIAL")) {
              carpeta = "comercial";
            } else if (nodoDataHijo.getTipoUso().equalsIgnoreCase("RESIDENCIAL")) {
              carpeta = "residencial";
            } else if (nodoDataHijo.getTipoUso().equalsIgnoreCase("INDUSTRIAL")) {
              carpeta = "industrial";
            } else if (nodoDataHijo.getTipoUso().equalsIgnoreCase("OFICIAL")) {
              carpeta = "oficial";
            } else if (nodoDataHijo.getTipoUso().equalsIgnoreCase("ALUM PUBLICO")) {
              carpeta = "alumbrado";
            } else {
              carpeta = "basico";
            }

            if (nodoDataHijo.getNombreColor().equalsIgnoreCase("azul")) {
              advancedModel.addOverlay(new Marker(coord, nodoDataHijo.getCodigo() + ":" + nodoDataHijo.getTipo() + ":" + nodoDataHijo.getNombre(),  nodoDataHijo.getImagen(), "../resources/images/icon-gmap/trafo/" + (carpeta.equals("") ? "azul.png" : carpeta + "/azul.png")));
            } else if (nodoDataHijo.getNombreColor().equalsIgnoreCase("amarillo")) {
              advancedModel.addOverlay(new Marker(coord, nodoDataHijo.getCodigo() + ":" + nodoDataHijo.getTipo() + ":" + nodoDataHijo.getNombre(),  nodoDataHijo.getImagen(), "../resources/images/icon-gmap/trafo/" + (carpeta.equals("") ? "amarillo.png" : carpeta + "/amarillo.png")));
            } else if (nodoDataHijo.getNombreColor().equalsIgnoreCase("blanco")) {
              advancedModel.addOverlay(new Marker(coord, nodoDataHijo.getCodigo() + ":" + nodoDataHijo.getTipo() + ":" + nodoDataHijo.getNombre(),  nodoDataHijo.getImagen(), "../resources/images/icon-gmap/trafo/" + (carpeta.equals("") ? "blanco.png" : carpeta + "/blanco.png")));
            } else if (nodoDataHijo.getNombreColor().equalsIgnoreCase("verde")) {
              advancedModel.addOverlay(new Marker(coord, nodoDataHijo.getCodigo() + ":" + nodoDataHijo.getTipo() + ":" + nodoDataHijo.getNombre(),  nodoDataHijo.getImagen(), "../resources/images/icon-gmap/trafo/" + (carpeta.equals("") ? "verde.png" : carpeta + "/verde.png")));
            } else if (nodoDataHijo.getNombreColor().equalsIgnoreCase("rojo")) {
              advancedModel.addOverlay(new Marker(coord, nodoDataHijo.getCodigo() + ":" + nodoDataHijo.getTipo() + ":" + nodoDataHijo.getNombre(),  nodoDataHijo.getImagen(), "../resources/images/icon-gmap/trafo/" + (carpeta.equals("") ? "rojo.png" : carpeta + "/rojo.png")));
            } else if (nodoDataHijo.getNombreColor().equalsIgnoreCase("violeta")) {
              advancedModel.addOverlay(new Marker(coord, nodoDataHijo.getCodigo() + ":" + nodoDataHijo.getTipo() + ":" + nodoDataHijo.getNombre(),  nodoDataHijo.getImagen(), "../resources/images/icon-gmap/trafo/" + (carpeta.equals("") ? "violeta.png" : carpeta + "/violeta.png")));
            } else if (nodoDataHijo.getNombreColor().equalsIgnoreCase("naranja")) {
              advancedModel.addOverlay(new Marker(coord, nodoDataHijo.getCodigo() + ":" + nodoDataHijo.getTipo() + ":" + nodoDataHijo.getNombre(),  nodoDataHijo.getImagen(), "../resources/images/icon-gmap/trafo/" + (carpeta.equals("") ? "naranja.png" : carpeta + "/naranja.png")));
            } else if (nodoDataHijo.getNombreColor().equalsIgnoreCase("fucsia")) {
              advancedModel.addOverlay(new Marker(coord, nodoDataHijo.getCodigo() + ":" + nodoDataHijo.getTipo() + ":" + nodoDataHijo.getNombre(),  nodoDataHijo.getImagen(), "../resources/images/icon-gmap/trafo/" + (carpeta.equals("") ? "fucsia.png" : carpeta + "/fucsia.png")));
            } else {
              advancedModel.addOverlay(new Marker(coord, nodoDataHijo.getCodigo() + ":" + nodoDataHijo.getTipo() + ":" + nodoDataHijo.getNombre(),  nodoDataHijo.getImagen(), "../resources/images/icon-gmap/trafo/" + (carpeta.equals("") ? "none.png" : carpeta + "/none.png")));
            }
          } else if (nodoDataHijo.getTipo().equals("9")) {
            if (i == 0) {
              if (nodoPadre.getTipo().equals("8")) {
                double xi = Double.parseDouble(nodoPadre.getCoordenadaX());
                LatLng coordI = new LatLng(Double.parseDouble(nodoPadre.getCoordenadaY()), xi);
                String carpeta = "";
                if (nodoPadre.getTipoUso().equalsIgnoreCase("COMERCIAL")) {
                  carpeta = "comercial";
                } else if (nodoPadre.getTipoUso().equalsIgnoreCase("RESIDENCIAL")) {
                  carpeta = "residencial";
                } else if (nodoPadre.getTipoUso().equalsIgnoreCase("INDUSTRIAL")) {
                  carpeta = "industrial";
                } else if (nodoPadre.getTipoUso().equalsIgnoreCase("OFICIAL")) {
                  carpeta = "oficial";
                } else if (nodoPadre.getTipoUso().equalsIgnoreCase("ALUM PUBLICO")) {
                  carpeta = "alumbrado";
                } else {
                  carpeta = "basico";
                }

                if (nodoPadre.getNombreColor().equalsIgnoreCase("azul")) {
                  advancedModel.addOverlay(new Marker(coordI, nodoPadre.getCodigo() + ":" + nodoPadre.getTipo() + ":" + nodoPadre.getNombre(), nodoDataHijo.getImagen(), "../resources/images/icon-gmap/trafo/" + (carpeta.equals("") ? "azul.png" : carpeta + "/azul.png")));
                } else if (nodoPadre.getNombreColor().equalsIgnoreCase("amarillo")) {
                  advancedModel.addOverlay(new Marker(coordI, nodoPadre.getCodigo() + ":" + nodoPadre.getTipo() + ":" + nodoPadre.getNombre(), nodoDataHijo.getImagen(), "../resources/images/icon-gmap/trafo/" + (carpeta.equals("") ? "amarillo.png" : carpeta + "/amarillo.png")));
                } else if (nodoPadre.getNombreColor().equalsIgnoreCase("blanco")) {
                  advancedModel.addOverlay(new Marker(coordI, nodoPadre.getCodigo() + ":" + nodoPadre.getTipo() + ":" + nodoPadre.getNombre(), nodoDataHijo.getImagen(), "../resources/images/icon-gmap/trafo/" + (carpeta.equals("") ? "blanco.png" : carpeta + "/blanco.png")));
                } else if (nodoPadre.getNombreColor().equalsIgnoreCase("verde")) {
                  advancedModel.addOverlay(new Marker(coordI, nodoPadre.getCodigo() + ":" + nodoPadre.getTipo() + ":" + nodoPadre.getNombre(), nodoDataHijo.getImagen(), "../resources/images/icon-gmap/trafo/" + (carpeta.equals("") ? "verde.png" : carpeta + "/verde.png")));
                } else if (nodoPadre.getNombreColor().equalsIgnoreCase("rojo")) {
                  advancedModel.addOverlay(new Marker(coordI, nodoPadre.getCodigo() + ":" + nodoPadre.getTipo() + ":" + nodoPadre.getNombre(), nodoDataHijo.getImagen(), "../resources/images/icon-gmap/trafo/" + (carpeta.equals("") ? "rojo.png" : carpeta + "/rojo.png")));
                } else if (nodoPadre.getNombreColor().equalsIgnoreCase("violeta")) {
                  advancedModel.addOverlay(new Marker(coordI, nodoPadre.getCodigo() + ":" + nodoPadre.getTipo() + ":" + nodoPadre.getNombre(), nodoDataHijo.getImagen(), "../resources/images/icon-gmap/trafo/" + (carpeta.equals("") ? "violeta.png" : carpeta + "/violeta.png")));
                } else if (nodoPadre.getNombreColor().equalsIgnoreCase("naranja")) {
                  advancedModel.addOverlay(new Marker(coordI, nodoPadre.getCodigo() + ":" + nodoPadre.getTipo() + ":" + nodoPadre.getNombre(), nodoDataHijo.getImagen(), "../resources/images/icon-gmap/trafo/" + (carpeta.equals("") ? "naranja.png" : carpeta + "/naranja.png")));
                } else if (nodoPadre.getNombreColor().equalsIgnoreCase("fucsia")) {
                  advancedModel.addOverlay(new Marker(coordI, nodoPadre.getCodigo() + ":" + nodoPadre.getTipo() + ":" + nodoPadre.getNombre(), nodoDataHijo.getImagen(), "../resources/images/icon-gmap/trafo/" + (carpeta.equals("") ? "fucsia.png" : carpeta + "/fucsia.png")));
                } else {
                  advancedModel.addOverlay(new Marker(coordI, nodoPadre.getCodigo() + ":" + nodoPadre.getTipo() + ":" + nodoPadre.getNombre(), nodoDataHijo.getImagen(), "../resources/images/icon-gmap/trafo/" + (carpeta.equals("") ? "none.png" : carpeta + "/none.png")));
                }
              }
            }
            String icono;
            if (nodoDataHijo.getTipoUso().equalsIgnoreCase("COMERCIAL")) {
              icono = "comercial";
            } else if (nodoDataHijo.getTipoUso().equalsIgnoreCase("RESIDENCIAL")) {
              icono = "residencial";
            } else if (nodoDataHijo.getTipoUso().equalsIgnoreCase("INDUSTRIAL")) {
              icono = "industrial";
            } else if (nodoDataHijo.getTipoUso().equalsIgnoreCase("OFICIAL")) {
              icono = "oficial";
            } else if (nodoDataHijo.getTipoUso().equalsIgnoreCase("ALUM PUBLICO")) {
              icono = "alumbrado";
            } else {
              icono = "basico";
            }
            advancedModel.addOverlay(new Marker(coord, nodoDataHijo.getCodigo() + ":" + nodoDataHijo.getTipo() + ":" + nodoDataHijo.getNombre(), nodoDataHijo.getImagen(), "../resources/images/icon-gmap/cliente/" + icono + ".png"));
          } else if (nodoDataHijo.getTipo().equals("6") || nodoDataHijo.getTipo().equals("3") || nodoDataHijo.getTipo().equals("7")) {
            advancedModel.addOverlay(new Marker(coord, nodoDataHijo.getCodigo() + ":" + nodoDataHijo.getTipo() + ":" + nodoDataHijo.getNombre(), nodoDataHijo.getImagen(), "../resources/images/icon-gmap/cliente/tipo-6-3-7.png"));
          } else if (nodoDataHijo.getTipo().equals("10") || nodoDataHijo.getTipo().equals("11")) {
            advancedModel.addOverlay(new Marker(coord, nodoDataHijo.getCodigo() + ":" + nodoDataHijo.getTipo() + ":" + nodoDataHijo.getNombre(), nodoDataHijo.getImagen(), "../resources/images/icon-gmap/cliente/tipo-10-11.png"));
          } else if (nodoDataHijo.getTipo().equals("0") || nodoDataHijo.getTipo().equals("1")) {
            advancedModel.addOverlay(new Marker(coord, nodoDataHijo.getCodigo() + ":" + nodoDataHijo.getTipo() + ":" + nodoDataHijo.getNombre(), nodoDataHijo.getImagen(), "../resources/images/icon-gmap/cliente/tipo-0-1.png"));
          } else if (nodoDataHijo.getTipo().equals("2")) {
            advancedModel.addOverlay(new Marker(coord, nodoDataHijo.getCodigo() + ":" + nodoDataHijo.getTipo() + ":" + nodoDataHijo.getNombre(), nodoDataHijo.getImagen(), "../resources/images/icon-gmap/cliente/tipo-2.png"));
          } else if (nodoDataHijo.getTipo().equals("4") || nodoDataHijo.getTipo().equals("5")) {
            advancedModel.addOverlay(new Marker(coord, nodoDataHijo.getCodigo() + ":" + nodoDataHijo.getTipo() + ":" + nodoDataHijo.getNombre(), nodoDataHijo.getImagen(), "../resources/images/icon-gmap/cliente/tipo-4-5.png"));
          }
        }
      }
    } else {
      /*En caso de que el PCI no tenga hijos, se muestra de todas formas en el mapa*/
      setCenter(nodoPadre);
      double X = Double.parseDouble(nodoPadre.getCoordenadaX());
      LatLng coordenada = new LatLng(Double.parseDouble(nodoPadre.getCoordenadaY()), X);
      advancedModel.addOverlay(new Marker(coordenada, nodoPadre.getCodigo() + ":" + nodoPadre.getTipo() + ":" + nodoPadre.getNombre(), null, "../resources/images/icon-gmap/cliente/" + "basico" + ".png"));
    }

  }

  /**
   * Con este método se pintan todos los hijo dependiendo del nivel donde se
   * ubique el usuario
   *
   * @autor <b>Jose Mejia</b>
   * @see MapBean
   * @since 29/10/2014
   * @param compoList
   */
  public void getCoordenadasCompo(List<Componente> compoList) {
    try {
      setNumFotos((Integer) 0);
      if (compoList instanceof List && compoList.size() > 0) {
        for (int i = 0; i < compoList.size(); i++) {
          if (compoList.get(i).getCoordX() != null && !compoList.get(i).getCoordX().equals("") && compoList.get(i).getCoordY() != null && !compoList.get(i).getCoordY().equals("")) {
            setNumFotos((Integer) (getNumFotos() + 1));
            double x = Double.parseDouble(compoList.get(i).getCoordX());
            LatLng coord = new LatLng(Double.parseDouble(compoList.get(i).getCoordY()), x);
            setZoom(compoList.get(i).getTipoComponente().getTbltipo().getTipo());
            nombreTipo = compoList.get(i).getTipoComponente().getTbltipo().getTipo();
            numMacroTot = String.valueOf(compoList.size());
            numMacrosFuncionando = String.valueOf(compoList.size());
            localizacion = compoList.get(i).getTbltipo1().getTipo();
            nombreComponente = compoList.get(i).getNombre();
            perdidas = "0";
            numSuministrosFact = "0";
            if (compoList.get(i).getTipoComponente().getTbltipo().getTipo().equals("8")) {
              String carpeta = "";
              if (compoList.get(i).getTbltipo1().getNombre().equalsIgnoreCase("COMERCIAL")) {
                carpeta = "amarillo";
              } else if (compoList.get(i).getTbltipo1().getNombre().equalsIgnoreCase("RESIDENCIAL")) {
                carpeta = "azul";
              } else if (compoList.get(i).getTbltipo1().getNombre().equalsIgnoreCase("INDUSTRIAL")) {
                carpeta = "blanco";
              } else if (compoList.get(i).getTbltipo1().getNombre().equalsIgnoreCase("OFICIAL")) {
                carpeta = "naranja";
              } else if (compoList.get(i).getTbltipo1().getNombre().equalsIgnoreCase("ALUM PUBLICO")) {
                carpeta = "verde";
              } else {
                carpeta = "violeta";
              }
              advancedModel.addOverlay(new Marker(coord, compoList.get(i).getIdComponente() + ":" + compoList.get(i).getTipoComponente().getTbltipo().getTipo() + ":" + compoList.get(i).getNombre(), compoList.get(i).getTbltipo().getNombre(), "../resources/images/icon-gmap/trafo/" + carpeta + ".png"));
            } else {
              if (compoList.get(i).getTipoComponente().getTbltipo().getTipo().equals("3")) {
                System.out.println("compoList.get(i).getTipoComponente().getTbltipo().getTipo().equals " + compoList.get(i).getIdComponente());
                String icono;
                icono = "basico";
                advancedModel.addOverlay(new Marker(coord, compoList.get(i).getIdComponente() + ":" + compoList.get(i).getTipoComponente().getTbltipo().getTipo() + ":" + compoList.get(i).getNombre(), compoList.get(i).getTbltipo().getNombre(), "../resources/images/icon-gmap/cliente/" + icono + ".png"));
              } else {
                advancedModel.addOverlay(new Marker(coord, compoList.get(i).getIdComponente() + ":" + compoList.get(i).getTipoComponente().getTbltipo().getTipo() + ":" + compoList.get(i).getNombre(), compoList.get(i).getTbltipo().getNombre()));
              }
            }
          }
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Con este método se pinta el padre en el mapa
   *
   * @autor <b>Jose Mejia</b>
   * @see MapBean
   * @since 29/10/2014
   * @param c
   */
  public void pintarPadre(Nodo c) {
    if (c.getCoordenadaX() != null && !c.getCoordenadaX().equals("") && c.getCoordenadaY() != null && !c.getCoordenadaY().equals("")) {
      double x = Double.parseDouble(c.getCoordenadaX());
      LatLng coord = new LatLng(Double.parseDouble(c.getCoordenadaY()), x);
      setZoom(c.getTipo());
      advancedModel.addOverlay(new Marker(coord, c.getCodigo() + ":" + c.getTipo() + ":" + c.getNombre(), c.getImagen(), "../resources/images/icon-gmap/trafo/trafo.png"));
      //advancedModel.addOverlay(new Marker(coord, c.getCodigo() + ":" + c.getTipo() + ":" + c.getNombre(), c.getCodigo() + ".png", "../resources/images/icon-gmap/trafo/trafo.png"));
    }
  }

  /**
   * Con este método se obtiene la ruta de la imagen
   *
   *
   * @autor <b>Jose Mejia</b>
   * @param imagen
   * @see MapBean
   * @return StreamedContent
   * @throws FileNotFoundException
   * @since 29/10/2014
   */
  public StreamedContent getGraphicText(String imagen) {
    //graphicText= new DefaultStreamedContent();

    height = "350";
    width = "350";
    if (imagen != null && !imagen.equals("")) {
      InputStream inputStream = null;
      try {
        System.out.println(" ----Nombre de la imagen---   " + imagen);
        System.out.println(" ----ruta de la imagen---   " + ruta);
        System.out.println(" ----ruta de la imagen---   " + ruta + imagen);
        File file = new File(ruta + imagen);
        System.out.println("ruta de l aimagen " + file.getAbsolutePath());
        inputStream = new FileInputStream(file);
        BufferedInputStream in = new BufferedInputStream(inputStream);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int val = -1;
        try {
          while ((val = in.read()) != -1) {
            out.write(val);
          }
        } catch (IOException e) {
          e.printStackTrace();
        }
        byte[] bytes = out.toByteArray();
        //graphicText = new DefaultStreamedContent(new ByteArrayInputStream(bytes), "image/png", "test.png");
        try {
          graphicText = new DefaultStreamedContent(new ByteArrayInputStream(bytes), "image/png", imagen);
          //graphicText = new DefaultStreamedContent(new ByteArrayInputStream(bytes), getMimeType("file://" + file.getAbsolutePath()), imagen);
          SimpleImageInfo image = new SimpleImageInfo(bytes);
          height = String.valueOf(image.getHeight());
          width = String.valueOf(image.getWidth());
          out.close();
          in.close();
          inputStream.close();
        } catch (IOException ex) {
          Logger.getLogger(MapBean.class.getName()).log(Level.SEVERE, null, ex);
        }
      } catch (FileNotFoundException ex) {
        Logger.getLogger(MapBean.class.getName()).log(Level.SEVERE, null, ex);
      } finally {
        try {
          inputStream.close();
        } catch (IOException ex) {
          Logger.getLogger(MapBean.class.getName()).log(Level.SEVERE, null, ex);
        }
      }

    }
    return graphicText;
  }

  /**
   * @return the zoomTipoComponente
   */
  public HashMap getZoomTipoComponente() {
    return zoomTipoComponente;
  }

  /**
   * @param zoomTipoComponente the zoomTipoComponente to set
   */
  public void setZoomTipoComponente(HashMap zoomTipoComponente) {
    this.zoomTipoComponente = zoomTipoComponente;
  }

  public void setZoom(String tipoComponente) {
    zoom = (String) zoomTipoComponente.get(tipoComponente);
  }

  public String getZoom() {
    return zoom;
  }

  /**
   * @return the center
   */
  public String getCenter() {
    return center;
  }

  /**
   * @param center the center to set
   */
  public void setCenter(Nodo node) {
    this.center = node.getCoordenadaY() + "," + node.getCoordenadaX();
  }

  /**
   * @return the nombreTipo
   */
  public String getNombreTipo() {
    return nombreTipo;
  }

  /**
   * @param nombreTipo the nombreTipo to set
   */
  public void setNombreTipo(String nombreTipo) {
    this.nombreTipo = nombreTipo;
  }

  /**
   * @return the numMacroTot
   */
  public String getNumMacroTot() {
    return numMacroTot;
  }

  /**
   * @param numMacroTot the numMacroTot to set
   */
  public void setNumMacroTot(String numMacroTot) {
    this.numMacroTot = numMacroTot;
  }

  /**
   * @return the numMacrosFuncionando
   */
  public String getNumMacrosFuncionando() {
    return numMacrosFuncionando;
  }

  /**
   * @param numMacrosFuncionando the numMacrosFuncionando to set
   */
  public void setNumMacrosFuncionando(String numMacrosFuncionando) {
    this.numMacrosFuncionando = numMacrosFuncionando;
  }

  /**
   * @return the localizacion
   */
  public String getLocalizacion() {
    return localizacion;
  }

  /**
   * @param localizacion the localizacion to set
   */
  public void setLocalizacion(String localizacion) {
    this.localizacion = localizacion;
  }

  /**
   * @return the nombreComponente
   */
  public String getNombreComponente() {
    return nombreComponente;
  }

  /**
   * @param nombreComponente the nombreComponente to set
   */
  public void setNombreComponente(String nombreComponente) {
    this.nombreComponente = nombreComponente;
  }

  /**
   * @return the perdidas
   */
  public String getPerdidas() {
    return perdidas;
  }

  /**
   * @param perdidas the perdidas to set
   */
  public void setPerdidas(String perdidas) {
    this.perdidas = perdidas;
  }

  /**
   * @return the numSuministrosFact
   */
  public String getNumSuministrosFact() {
    return numSuministrosFact;
  }

  /**
   * @param numSuministrosFact the numSuministrosFact to set
   */
  public void setNumSuministrosFact(String numSuministrosFact) {
    this.numSuministrosFact = numSuministrosFact;
  }

  public Integer getNumFotos() {
    return numFotos;
  }

  public void setNumFotos(Integer numFotos) {
    this.numFotos = numFotos;
  }

  public String getCordX() {
    return cordX;
  }

  public void setCordX(String cordX) {
    this.cordX = cordX;
  }

  public String getCordY() {
    return cordY;
  }

  public void setCordY(String cordY) {
    this.cordY = cordY;
  }

  public String getTipo() {
    return tipo;
  }

  public void setTipo(String tipo) {
    this.tipo = tipo;
  }

  public String getCodigo() {
    return codigo;
  }

  public void setCodigo(String codigo) {
    this.codigo = codigo;
  }

  public StreamedContent getGraphicText() {
    return graphicText;
  }

  public void setGraphicText(StreamedContent graphicText) {
    this.graphicText = graphicText;
  }

  public String getHeight() {
    return height;
  }

  public void setHeight(String height) {
    this.height = height;
  }

  public String getWidth() {
    return width;
  }

  public void setWidth(String width) {
    this.width = width;
  }

  public String getNivel() {
    return nivel;
  }

  public void setNivel(String nivel) {
    this.nivel = nivel;
  }

  public String getRuta() {
    return ruta;
  }

  public void setRuta(String ruta) {
    this.ruta = ruta;
  }

}
