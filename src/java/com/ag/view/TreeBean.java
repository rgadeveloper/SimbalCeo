/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ag.view;

import com.ag.model.*;
import com.ag.model.view.ComboLista;
import com.ag.model.view.DataBalanceHijo;
import com.ag.model.view.DataRangosBalance;
import com.ag.service.ArbolManager;
import com.ag.service.BalanceManager;
import com.ag.service.SpringContext;
import java.util.HashMap;
import java.util.List;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import org.primefaces.event.NodeSelectEvent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;
import com.ag.model.view.*;
import com.ag.model.view.Nodo;
import com.ag.service.ReporteManager;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.*;
import javax.faces.bean.*;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.JRXlsExporter;
import org.primefaces.event.NodeCollapseEvent;
import org.primefaces.event.NodeExpandEvent;
import org.primefaces.model.chart.CartesianChartModel;
import org.primefaces.model.chart.ChartSeries;
import org.primefaces.model.chart.PieChartModel;

@ManagedBean
@SessionScoped
public class TreeBean implements Serializable {

  private String idComponente;
  private TreeNode root;
  private TreeNode selectedNode;
  private HashMap data;
  private SpringContext context;
  private ArbolManager arbolManager;
  private BalanceManager balanceManager;
  private ReporteManager reporteManager;
  private String tipoArbol;
  private String periodo;
  private String estilo = "font-size:12px;font-weight:580;";
  private List<DataBalanceHijo> infoBalance;
  private Balances balance;
  //  private String mostrarPanelGrid;
  private DataRangosBalance dataRangosBalance;
  private Boolean mostrarTablaZona;
  private Boolean mostrarTablaTrafo;
  private MapBean mapa;
  private String url;
  private HashMap zoomTipoComponente;
  private ComboLista periodoSelected;
  private List<String> listPeriodos;
  private boolean isInicial = true;
  private Medida medida;
  private AtrComponenteMedida atrComponenteMedida;
  private AtrComponente atrComponente;
  private ComponenteMedida componenteMedida;
  private Componente componente;
  private PadreHijo relacionGeo, relacionElec;
  private List<DataValue> listCantXTipoUso;
  private String cantSumNoMedidos;
  private List<Medida> listMedida;
  private Nodo nodo;
  private String rutaComponente, claveBusqueda;

  private PieChartModel tortaTotal;
  private PieChartModel tortaFuncionan;
  private PieChartModel tortaNoFuncionan;
  private boolean mostrarTorta;

  private CartesianChartModel categoryModel;
  private boolean mostrarChartLine;
  private List<Serie> serieToJasper; //serie para el reporte

  private String grafico;
  private String graficoVE;

  private List<Componente> trafosBajo;
  private List<Componente> trafosMedio;
  private List<Componente> trafosCritico;
  private List<Componente> trafosNegativos;
  private List<Componente> trafosInconsistentes;
  private List<Componente> trafosSinBalances;

  private List<Novedades> novedades;
  private Novedades novedadSelected;
  private List<Tbltipo> tiposNovedades;
  private String idTipoNovedad;
  private boolean mostrarBtnNovedades;

  private String idClienteAbuscar;
  private String tipoComponente;
  private UbicacionMacroV ubicacionMV;
  private String idBarCir;

  @ManagedProperty("#{login}")
  private Login login;

  private HashMap<String, String> m;
  private HashMap<String, List<Nodo>> mListTrafo;

  private String idNif;
  private String idPlaca;
  private String tpComp;

  public TreeBean() {
    grafico = "PestanaGraficaTorta.xhtml";
    graficoVE = "VEpestanaGraficaTorta.xhtml";
    context = SpringContext.getInstance();
    // Obtenemos el servicio ArbolManager
    arbolManager = (ArbolManager) context.getBean("ArbolManager");
    periodo = "-";
    m = new HashMap<String, String>();
    mListTrafo = new HashMap<String, List<Nodo>>();
  }

  //@PostConstruct
  public void init() {
    try {
      balanceManager = (BalanceManager) context.getBean("BalanceManager");
      reporteManager = (ReporteManager) context.getBean("ReporteManager");

      setTiposNovedades(balanceManager.getTiposNovedades());
      setZoomTipoComponente();
      String codigoNodo = "-1";
      String tipo = "-1";
      String clave = codigoNodo + tipo;
      int tipoCompo;
      data = new HashMap();
      root = new DefaultTreeNode(codigoNodo, null);

      //Se llena el valor del periodo con el maximo de la base de datos
      if (isInicial) {
        setListPeriodos((List<String>) arbolManager.getPeriodo());
        periodo = listPeriodos.get(0);
        isInicial = false;
      }
      // Definimos el Objeto Nodo raiz
      Nodo n0 = new Nodo(codigoNodo);
        // Se obtiene el arbol inicial

      /* INI JMM 22122014 */
      n0.setNodoVisual(root);
      /* FIN JMM 22122014 */
      // Se obtiene el arbol inicial
      List hijos = arbolManager.cargaArbolInicial(getTipoArbol(), periodo);

      n0.setHijos(hijos);
      data.put(clave, n0);
      setMapa(new MapBean());
      // Los hijos de nodo raiz son agregados a la estructura data para que se pueda obtener la info a mostrar en el arbol
      if (hijos != null) {
        for (int i = 0; i < hijos.size(); i++) {
          Nodo nodoDataHijo = (Nodo) hijos.get(i);
          data.put(nodoDataHijo.getCodigo() + nodoDataHijo.getTipo(), nodoDataHijo);
          if (i == 0) {
            getMapa().setZoomTipoComponente(zoomTipoComponente);
            getMapa().setNodo(nodoDataHijo);

            infoBalance = balanceManager.cuadroMando(nodoDataHijo.getCodigo(), nodoDataHijo.getTipo(), periodo);
            balance = balanceManager.getBalances(nodoDataHijo.getCodigo(), nodoDataHijo.getTipo(), periodo);

            url = "";

            tipoCompo = Integer.parseInt(nodoDataHijo.getTipo());

            if (tipoCompo == 0 || tipoCompo == 1 || tipoCompo == 2 || tipoCompo == 4 || tipoCompo == 5) {
              dataRangosBalance = balanceManager.getRangosZonas(nodoDataHijo.getCodigo(), nodoDataHijo.getTipo(), tipoArbol, periodo);
              url = tipoArbol.equals("NIV200") ? "VistaGeograficaEmp.xhtml" : "VistaElectricaEmp.xhtml";
              trafosBajo = balanceManager.getTrafosByRango(nodoDataHijo.getCodigo(), nodoDataHijo.getTipo(), periodo, "BAJO");
              trafosMedio = balanceManager.getTrafosByRango(nodoDataHijo.getCodigo(), nodoDataHijo.getTipo(), periodo, "MEDIO");
              trafosCritico = balanceManager.getTrafosByRango(nodoDataHijo.getCodigo(), nodoDataHijo.getTipo(), periodo, "CRITICO");
              trafosNegativos = balanceManager.getTrafosByRango(nodoDataHijo.getCodigo(), nodoDataHijo.getTipo(), periodo, "NEGATIVO");
              trafosInconsistentes = balanceManager.getTrafosByRango(nodoDataHijo.getCodigo(), nodoDataHijo.getTipo(), periodo, "INCONSISTENTE");
              trafosSinBalances = balanceManager.getTrafosByRango(nodoDataHijo.getCodigo(), nodoDataHijo.getTipo(), periodo, "SIN BALANCE");

              /*dataRangosBalance.setBajo(trafosBajo.size()>0?String.valueOf(trafosBajo.size()):"");
               dataRangosBalance.setMedio(trafosMedio.size()>0?String.valueOf(trafosMedio.size()):"");
               dataRangosBalance.setCritico(trafosCritico.size()>0?String.valueOf(trafosCritico.size()):"");
               dataRangosBalance.setNegativos(trafosNegativos.size()>0?String.valueOf(trafosNegativos.size()):"");
               dataRangosBalance.setInconsistentes(trafosInconsistentes.size()>0?String.valueOf(trafosInconsistentes.size()):"");
               dataRangosBalance.setSinBalance(trafosSinBalances.size()>0?String.valueOf(trafosBajo.size()):"");*/
              visualizarChartLine(nodoDataHijo.getCodigo(), nodoDataHijo.getTipo(), periodo);
              visualizarTortas();  //error aqui Alex
              mostrarBtnNovedades = false;
            }
            if (tipoCompo == 6 || tipoCompo == 3 || tipoCompo == 7) {
              url = tipoArbol.equals("NIV200") ? "VistaGeograficaMunic.xhtml" : "VistaElectricaMunic.xhtml";
            }
            if (tipoCompo == 8) {
              url = tipoArbol.equals("NIV200") ? "VistaGeograficaTrafo.xhtml" : "VistaElectricaTrafo.xhtml";
              setMedida(balanceManager.getMedida(nodoDataHijo.getCodigo(), periodo, nodoDataHijo.getTipo()));
              setAtrComponenteMedida(balanceManager.getAtrComponenteMedida(nodoDataHijo.getCodigo(), periodo));
              setAtrComponente(balanceManager.getAtrComponente(nodoDataHijo.getCodigo()));
              setComponenteMedida(balanceManager.getComponenteMedida(nodoDataHijo.getCodigo(), periodo));
              setComponente(balanceManager.getComponente(nodoDataHijo.getCodigo()));
              relacionGeo = balanceManager.getZonaMunicipio(nodoDataHijo.getCodigo(), periodo);
              relacionElec = balanceManager.getSubestacionCircuito(nodoDataHijo.getCodigo(), periodo);
            }
            if (tipoCompo == 10 || tipoCompo == 11) {
              url = tipoArbol.equals("NIV200") ? "VistaGeograficaMacV.xhtml" : "VistaElectricaMacV.xhtml";
              setComponente(balanceManager.getComponente(nodoDataHijo.getCodigo()));
              relacionGeo = balanceManager.getZonaMunicipio(nodoDataHijo.getCodigo(), periodo);
              relacionElec = balanceManager.getSubestacionCircuito(nodoDataHijo.getCodigo(), periodo);
            }
            if (tipoCompo == 9) {
              url = tipoArbol.equals("NIV200") ? "VistaGeograficaSumin.xhtml" : "VistaElectricaSumin.xhtml";
              setListMedida(balanceManager.getListMedida(nodoDataHijo.getCodigo(), Integer.parseInt(periodo)));
              setAtrComponente(balanceManager.getAtrComponente(nodoDataHijo.getCodigo()));
              setComponente(balanceManager.getComponente(nodoDataHijo.getCodigo()));
            }

          }
        }
      }
      // Se agregan los objetos TreeNodo al arbol root
      agregarNodos(clave, root);
    } catch (Exception e) {
      FacesContext contextm = FacesContext.getCurrentInstance();
      contextm.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Verifique que las interfaces hayan sido ejecutadas.", ""));
    }
  }

  public void buscarComponente() {
    try {
      //rutaComponente=tipoComponente.equals("Trafo")? arbolManager.rutaTrafo(idClienteAbuscar, tipoArbol, periodo):arbolManager.rutaSuministro(idClienteAbuscar, tipoArbol, periodo);
      if (getIdNif() == null) {
        setIdNif("");
      }
      if (getIdPlaca() == null) {
        setIdPlaca("");
      }
      String resultado;
      /* INI AAMC 20140807 */

      if (tpComp.equals("Trafo")) {
        //resultado = arbolManager.rutaViewGeografica(tpComp, tipoArbol, periodo, idNif, idPlaca);
        resultado = arbolManager.rutaTrafo(getIdNif(), tipoArbol, periodo);
      } else {
        //resultado = arbolManager.rutaView(tpComp, tipoArbol, periodo, idNif, idPlaca);
        resultado = arbolManager.rutaSuministro(getIdNif(), tipoArbol, periodo);
      }
      rutaComponente = resultado.split(";")[0];
      claveBusqueda = resultado;
      /* FIN AAMC 20140807 */
      if (rutaComponente.length() > 25) {
        abrirArbol();
      }

    } catch (Exception e) {
      e.printStackTrace();
      limpiarBuscarComponente();
    } finally {
      idClienteAbuscar = null;
      tipoComponente = null;
    }
  }

  public void abrirArbol() {
    /* INI AAMC 20140807 */
    String[] jerarquia = claveBusqueda.split(";");
    int rebentado = 0;
    for (int i = 1; i < jerarquia.length; i++) {
      try {
        String clave = jerarquia[i];
        System.out.println("--->" + clave);
        obtenerInfo(clave);
        Nodo nodo = (Nodo) data.get(clave);
        if (nodo != null) {
          nodo.getNodoVisual().setExpanded(true);
          agregarNodos(clave, nodo.getNodoVisual());
        } else {
          rebentado++;
          break;
        }
      } catch (Exception e) {
        e.printStackTrace();
        rebentado++;
        break;
      }
    }

    try {
      System.out.println("rebentado-> " + rebentado);
      Nodo nodoData = (Nodo) data.get(jerarquia[jerarquia.length - (1 + rebentado)]);
      TreeNode nodoBuscado = nodoData.getNodoVisual();
      nodoBuscado.setSelected(true);
      onNodeSelect(nodoBuscado);
      nodoBuscado.setExpanded(true);
      onNodeExpanded(nodoBuscado);
    } catch (Exception e) {
      e.printStackTrace();
    }
    /* FIN AAMC 20140807 */
  }

  /*
   public void buscarComponente() throws SQLException{
   try {            
   rutaComponente=tipoComponente.equals("Trafo")?
   arbolManager.rutaTrafo(idClienteAbuscar, tipoArbol, periodo):
   arbolManager.rutaSuministro(idClienteAbuscar, tipoArbol, periodo);
   } catch (Exception e) {
   limpiarBuscarComponente(); 
   }finally{
   idClienteAbuscar=null;
   tipoComponente=null;
   }
   }*/
  public void limpiarBuscarComponente() {
    rutaComponente = null;
    idClienteAbuscar = null;
    tipoComponente = null;
  }

  public void mostrarNovedades() {
    String idComponenteMedida = componenteMedida.getIdComponenteMedida() != null
            ? componenteMedida.getIdComponenteMedida().toString() : "";
    setNovedades(balanceManager.getNovedadesByTrafo(idComponenteMedida));
    //llenar list de novedades               
  }

  public void mostrarNovedades2() {
    String idComponenteMedida = componenteMedida.getIdComponenteMedida() != null
            ? componenteMedida.getIdComponenteMedida().toString() : "";
    setNovedades(balanceManager.getNovedadesByTrafo(idComponenteMedida));
    //llenar list de novedades               
  }

  public void crearNovedad(String usuario, String programa) {
    balanceManager.saveNovedad(usuario, programa, Integer.valueOf(periodo), idTipoNovedad, componenteMedida);
    mostrarNovedades();
    idTipoNovedad = null;
  }

  public void eliminarNovedad() {
    if (novedadSelected != null && !novedades.isEmpty()) {
      balanceManager.deleteNovedad(novedadSelected);
      novedades.remove(novedadSelected);
    }
  }

  public void editarNovedad(String usuario, String programa) {
    boolean actualizar = false;
    // Novedades novedadActual=novedadSelected;
    if (!idTipoNovedad.equals(novedadSelected.getTbltipo().getTipo())) {
      balanceManager.deleteNovedad(novedadSelected);
      NovedadesPK llavePrimaria = novedadSelected.getNovedadesPK();
      novedadSelected.setTbltipo(balanceManager.getTipo(idTipoNovedad));
      llavePrimaria.setTipoNovedad(idTipoNovedad);
      novedadSelected.setNovedadesPK(llavePrimaria);
      actualizar = true;
    }

    int periodoVisualizado = Integer.valueOf(periodo);
    if (periodoVisualizado != novedadSelected.getPeriodo()) {
      novedadSelected.setPeriodo(periodoVisualizado);
      actualizar = true;
    }

    if (actualizar) {
      //balanceManager.deleteNovedad(novedadActual);
      novedadSelected.setUsuario(usuario);
      novedadSelected.setPrograma(programa);
      balanceManager.editNovedad(novedadSelected);
      mostrarNovedades();
    }

  }

  JasperPrint jasperPrint;

  public void initjTorta() throws JRException, URISyntaxException {
    List<GraficasReporte> graficasReporte = new ArrayList<GraficasReporte>();
    GraficasReporte gr = new GraficasReporte();
    gr.setFuncionan(balance.getTotalMacrosFunc().longValue());
    gr.setNofuncionan(balance.getCantMacrosNoFunc().longValue());
    gr.setBajo(dataRangosBalance.getBajo() != null && !dataRangosBalance.getBajo().equals("")
            ? Long.valueOf(dataRangosBalance.getBajo()) : 0);
    gr.setMedio(dataRangosBalance.getMedio() != null && !dataRangosBalance.getMedio().equals("")
            ? Long.valueOf(dataRangosBalance.getMedio()) : 0);
    gr.setCritico(dataRangosBalance.getCritico() != null && !dataRangosBalance.getCritico().equals("")
            ? Long.valueOf(dataRangosBalance.getCritico()) : 0);
    gr.setInconsistente(dataRangosBalance.getInconsistentes() != null && !dataRangosBalance.getInconsistentes().equals("")
            ? Long.valueOf(dataRangosBalance.getInconsistentes()) : 0);
    gr.setNegativo(dataRangosBalance.getNegativos() != null && !dataRangosBalance.getNegativos().equals("")
            ? Long.valueOf(dataRangosBalance.getNegativos()) : 0);
    gr.setSinbalance(dataRangosBalance.getSinBalance() != null && !dataRangosBalance.getSinBalance().equals("")
            ? Long.valueOf(dataRangosBalance.getSinBalance()) : 0);
    graficasReporte.add(gr);

    JRBeanCollectionDataSource beanCollectionDataSource = new JRBeanCollectionDataSource(graficasReporte);
    String reporte = "torta.jasper";

    Map parameters = new HashMap();
    parameters.put("logo", "com/ag/reportes/jaspers/CEOlogoReportes.jpg");

    jasperPrint = JasperFillManager.fillReport(this.getClass().getClassLoader().getResourceAsStream("com/ag/reportes/jaspers/" + reporte), parameters, beanCollectionDataSource);
  }

  public void initjLinea() throws JRException, URISyntaxException {
    JRBeanCollectionDataSource beanCollectionDataSource = new JRBeanCollectionDataSource(serieToJasper);
    String reporte = "linea.jasper";
    Map parameters = new HashMap();
    parameters.put("logo", "com/ag/reportes/jaspers/CEOlogoReportes.jpg");

    jasperPrint = JasperFillManager.fillReport(this.getClass().getClassLoader().getResourceAsStream("com/ag/reportes/jaspers/" + reporte), parameters, beanCollectionDataSource);
  }

  public void initjResumenZonas(String nivel, String nodo, String formato) throws JRException, URISyntaxException {
    List<ResumenNivelZona> resumenEmpresa = new ArrayList<ResumenNivelZona>();
    //if (balance!=null && dataRangosBalance!=null) {
    resumenEmpresa.add(new ResumenNivelZona(balance, dataRangosBalance));
        //}

    JRBeanCollectionDataSource beanCollectionDataSource = new JRBeanCollectionDataSource(resumenEmpresa);
    String reporte;
    Map parameters = new HashMap();
    parameters.put("nodo", nodo);
    if (formato.equals("pdf")) {
      parameters.put("logo", "com/ag/reportes/jaspers/CEOlogoReportes.jpg");
      reporte = "resumen" + nivel + ".jasper";
    } else {
      reporte = "resumen" + nivel + "XLS.jasper";
      parameters.put(JRParameter.IS_IGNORE_PAGINATION, true);//para excel
    }

    jasperPrint = JasperFillManager.fillReport(this.getClass().getClassLoader().getResourceAsStream("com/ag/reportes/jaspers/" + reporte), parameters, beanCollectionDataSource);
  }

  public void initjResumenTrafo(String formato) throws JRException, URISyntaxException {
    List<ResumenNivelTrafo> ResumenTrafo = new ArrayList<ResumenNivelTrafo>();
    ResumenNivelTrafo rnt = new ResumenNivelTrafo(balance,
            componenteMedida,
            componente,
            relacionGeo,
            relacionElec,
            cantSumNoMedidos,
            atrComponente,
            atrComponenteMedida,
            medida);
    ResumenTrafo.add(rnt);
    JRBeanCollectionDataSource beanCollectionDataSource = new JRBeanCollectionDataSource(ResumenTrafo);
    String reporte;
    Map parameters = new HashMap();

    if (formato.equals("pdf")) {
      parameters.put("logo", "com/ag/reportes/jaspers/CEOlogoReportes.jpg");
      reporte = "resumenTrafo.jasper";
    } else {
      reporte = "resumenTrafoXLS.jasper";
      parameters.put(JRParameter.IS_IGNORE_PAGINATION, true);//para excel
    }
    //parameters.put("nodo", nodo);                                
    jasperPrint = JasperFillManager.fillReport(this.getClass().getClassLoader().getResourceAsStream("com/ag/reportes/jaspers/" + reporte), parameters, beanCollectionDataSource);
  }

  public void initjResumenSuministro(String formato) throws JRException, URISyntaxException {
    List<ResumenNivelSuministro> ResumenSuministro = new ArrayList<ResumenNivelSuministro>();
    ResumenNivelSuministro rns = new ResumenNivelSuministro(componente, atrComponente, listMedida);
    ResumenSuministro.add(rns);
    JRBeanCollectionDataSource beanCollectionDataSource = new JRBeanCollectionDataSource(ResumenSuministro);
    String reporte;
    Map parameters = new HashMap();

    if (formato.equals("pdf")) {
      parameters.put("logo", "com/ag/reportes/jaspers/CEOlogoReportes.jpg");
      reporte = "resumenSuministro.jasper";
    } else {
      reporte = "resumenSuministroXLS.jasper";
      parameters.put(JRParameter.IS_IGNORE_PAGINATION, true);//para excel
    }
    //parameters.put("nodo", nodo);                                
    jasperPrint = JasperFillManager.fillReport(this.getClass().getClassLoader().getResourceAsStream("com/ag/reportes/jaspers/" + reporte), parameters, beanCollectionDataSource);
  }

  public void exportar(String reporte, String formato) throws JRException, IOException {
    try {
      Nodo nodoData = (Nodo) data.get(idComponente);

      if (reporte.equals("Gráficos Lineas")) {
        initjLinea();
        reporte = "Gráficos Lineas - " + nodoData.getNombre();
      } else {
        if (reporte.equals("Gráficos Tortas")) {
          initjTorta();
          reporte = "Gráficos Tortas - " + nodoData.getNombre();
        } else {
          if (reporte.equals("EmpZonSub") || reporte.equals("MunBarCir")) {
            initjResumenZonas(reporte, nodoData.getNombre(), formato);
            reporte = "Resumen - " + nodoData.getNombre();
          } else {
            if (reporte.equals("Trafo")) {
              initjResumenTrafo(formato);
              reporte = "Resumen - " + nodoData.getNombre();
            } else {
              initjResumenSuministro(formato);
              reporte = "Resumen - " + nodoData.getNombre();
            }
          }
        }
      }

      HttpServletResponse httpServletResponse = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();
      httpServletResponse.addHeader("Content-disposition", "attachment; filename=" + reporte + "." + formato);
      ServletOutputStream servletOutputStream = httpServletResponse.getOutputStream();

      if (formato.equals("pdf")) {
        JasperExportManager.exportReportToPdfStream(jasperPrint, servletOutputStream);
      } else {
        JRXlsExporter xlsxExporter = new JRXlsExporter();
        xlsxExporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
        xlsxExporter.setParameter(JRExporterParameter.OUTPUT_STREAM, servletOutputStream);
        xlsxExporter.exportReport();
      }

      servletOutputStream.flush();
      servletOutputStream.close();

    } catch (Exception e) {
      FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
              "Ha ocurrido un error al generar el reporte.", ""));
    }

  }

  public String padreTrafo(BigDecimal id, String tipo) {
    String idComercial = " : ";
    ZonaGeografica z = balanceManager.getPadreByTipo(id.toString(), tipo);
    if (z != null) {
      idComercial = (z.getIdComercial() == null) ? " : " : z.getIdComercial();
    }
    return idComercial;
  }

  public String perdidaTrafo(BigDecimal id, String tipo) {
    String perdida = " : ";
    Balances b = balanceManager.getBalances(id.toString(), "8", periodo);
    if (b != null) {
      if (tipo.equals("mes")) {
        perdida = (b.getPorcPerdidaMes() == null) ? " : " : String.valueOf(b.getPorcPerdidaMes());
      } else {
        perdida = (b.getPorcPerdidaMovil() == null) ? " : " : String.valueOf(b.getPorcPerdidaMovil());
      }
    }
    return perdida;
  }

  public void ocultarTortas() {
    tortaTotal = new PieChartModel();
    tortaNoFuncionan = new PieChartModel();
    tortaFuncionan = new PieChartModel();
    mostrarTorta = false;
  }

  public void visualizarTortas() {
    //Para la torta que muestra macros Funcionando y No_Funcionando 
    if (balance != null) {
      Number fun = balance.getTotalMacrosFunc() == null ? 0 : //error aqui Alex
              balance.getTotalMacrosFunc().intValue();
      Number noFun = balance.getCantMacrosNoFunc().intValue();
      tortaTotal = new PieChartModel();
      tortaTotal.set("Funcionan", fun);
      tortaTotal.set("No funcionan", noFun);

      /*Number total = fun.floatValue()+ noFun.floatValue();
       float porcFun = (fun.floatValue()/total.floatValue())*100;
       float porcNoFun = (noFun.floatValue()/total.floatValue())*100;        
       tortaTotal = new PieChartModel();        
       if (noFun.intValue()==0) {
       tortaTotal.set("Funcionan",100);
       }else if(fun.intValue()==0){
       tortaTotal.set("No funcionan",100);  
       }else if (porcNoFun >= 99){
       tortaTotal.set("Funcionan",2);
       tortaTotal.set("No funcionan", 98);
       }else if (porcFun >= 99){
       tortaTotal.set("Funcionan",98);
       tortaTotal.set("No funcionan", 2);
       }else{
       tortaTotal.set("Funcionan",fun);
       tortaTotal.set("No funcionan", noFun); 
       }*/
      //para torta que muestra solo macros Funcionado
      int bajo = dataRangosBalance.getBajo() == null ? 0
              : Integer.parseInt(dataRangosBalance.getBajo());
      int medio = dataRangosBalance.getMedio() == null ? 0
              : Integer.parseInt(dataRangosBalance.getMedio());
      int critico = dataRangosBalance.getCritico() == null ? 0
              : Integer.parseInt(dataRangosBalance.getCritico());
      tortaFuncionan = new PieChartModel();
      tortaFuncionan.set("Bajo", bajo);
      tortaFuncionan.set("Medio", medio);
      tortaFuncionan.set("Critico", critico);

      //para torta que muestra solo macros No Funcionado
      int negativo = dataRangosBalance.getNegativos() == null ? 0
              : Integer.parseInt(dataRangosBalance.getNegativos());
      int inconsistente = dataRangosBalance.getInconsistentes() == null ? 0
              : Integer.parseInt(dataRangosBalance.getInconsistentes());
      int sinBalance = dataRangosBalance.getSinBalance() == null ? 0
              : Integer.parseInt(dataRangosBalance.getSinBalance());
      /* int negativo =176; funciona mal
       int inconsistente =0;
       int sinBalance =2;*/
      tortaNoFuncionan = new PieChartModel();
      tortaNoFuncionan.set("Negativo", negativo);
      tortaNoFuncionan.set("Inconsistente", inconsistente);
      tortaNoFuncionan.set("Sin balance", sinBalance);
      mostrarTorta = true;
    } else {
      mostrarTorta = false;
    }

  }

  public String[] listarPeriodos(String periodoInicial) {
    String periodoObjetivo = periodoInicial;
    int tam = periodoObjetivo.length();
    String mes = periodoObjetivo.substring(tam - 2);
    String ano = periodoObjetivo.substring(0, tam - 2);
    int mesesAtras = balanceManager.getNumMesesAtras();
    String[] periodos = new String[mesesAtras];

    for (int i = 0; i < mesesAtras; i++) {
      if (mes.equals("00")) {
        mes = "12";
        ano = String.valueOf(Integer.parseInt(ano) - 1);
      }
      periodos[i] = ano + mes;
      int m = Integer.parseInt(mes) - 1;
      mes = (m > 9) ? String.valueOf(m)
              : "0" + String.valueOf(m);
    }

    return periodos;
  }

  public void visualizarChartLine(String idComponente, String tipoComponente, String periodoIni) {

    String[] periodos = listarPeriodos(periodoIni);
    categoryModel = new CartesianChartModel();
    ChartSeries mes = new ChartSeries();
    mes.setLabel("Mes");
    ChartSeries movil = new ChartSeries();
    movil.setLabel("Movil");

    serieToJasper = new ArrayList<Serie>(); //para el jasper

    for (int i = periodos.length; i > 0; i--) {
      Number porcMes = 0;
      Number porcMovil = 0;
      String per = periodos[i - 1];

      Balances b = balanceManager.getBalances(idComponente, tipoComponente, per);
      if (b != null) {
        porcMes = b.getPorcPerdidaMes() == null ? 0 : b.getPorcPerdidaMes();
        porcMovil = b.getPorcPerdidaMovil() == null ? 0 : b.getPorcPerdidaMovil();
      }
      mes.set(per, porcMes);
      movil.set(per, porcMovil);
      serieToJasper.add(new Serie(per, porcMes.longValue(), porcMovil.longValue()));
    }
    categoryModel.addSeries(mes);
    categoryModel.addSeries(movil);
    mostrarChartLine = true;
  }

  public void agregarNodos(String clave, TreeNode padre) {
    Nodo nodoData = (Nodo) data.get(clave);
    if (!nodoData.isSelected()) {
      List hijos = nodoData.getHijos();
      if (hijos != null) {
        for (int i = 0; i < hijos.size(); i++) {
          Nodo nodoHijo = (Nodo) hijos.get(i);
          TreeNode nodeView = new DefaultTreeNode(nodoHijo.getCodigo() + nodoHijo.getTipo(), padre);
          /* INI JMM 22122014 */
          nodoHijo.setNodoVisual(nodeView);
          /* FIN JMM 22122014 */
        }
      }
      nodoData.setSelected(true);
      /*padre.setExpanded(true);
                          
       if(padre.getParent()!=null){ //si tiene padre para buscar hnos
       List<TreeNode> hermanos = padre.getParent().getChildren();
       for (Iterator it = hermanos.iterator(); it.hasNext();) {
       TreeNode n = (TreeNode) it.next();
       if (!n.isSelected())
       n.setExpanded(false);
       } 
       }*/

      data.put(clave, nodoData);
    }
  }

  public void obtenerInfo(String clave) {
    Nodo nodoData = (Nodo) data.get(clave);
    List hijos = null;
    if (!nodoData.isSelected()) {
      hijos = arbolManager.cargaArbolHijos(nodoData.getCodigo(), nodoData.getTipo(), periodo, login.getGestionTrafo());
      nodoData.setHijos(hijos);
      data.put(clave, nodoData);
      if (hijos != null) {
        for (int i = 0; i < hijos.size(); i++) {
          Nodo nodoDataHijo = (Nodo) hijos.get(i);
          data.put(nodoDataHijo.getCodigo() + nodoDataHijo.getTipo(), nodoDataHijo);
        }
      }

    }
        //preguntar si se quiere mostrar solo trafos en el gmap (solo_componentes de parametros) 
    //y llenar la lista de hijo solo trafos para niveles empresa,zona,subestacion|municipio
    mapa = new MapBean();
    getMapa().setZoomTipoComponente(zoomTipoComponente);
//        if (arbolManager.soloTrafoInGmap(nodoData.getTipo())) {
//            List h = arbolManager.cargaArbolHijosTrafos(nodoData.getCodigo(), nodoData.getTipo(), periodo, login.getGestionTrafo());
//            nodoData.setHijos(h);
//            getMapa().setNodoList(nodoData);
//            nodoData.setHijos(hijos);
//        }else{
//            getMapa().setNodoList(nodoData);
//        }

    getMapa().setRuta(arbolManager.getRutaImage());
    String llave = nodoData.getCodigo() + "-" + nodoData.getTipo();

    List l2Hijos = new ArrayList();
    if (!mListTrafo.isEmpty()) {
      if (!mListTrafo.containsKey(llave)) {
        l2Hijos = arbolManager.getComponentesUbicacion(nodoData.getCodigo(), nodoData.getTipo(), periodo, tipoArbol, login.getGestionTrafo());
      } else {
        l2Hijos = mListTrafo.get(llave);
      }
    } else {
      l2Hijos = arbolManager.getComponentesUbicacion(nodoData.getCodigo(), nodoData.getTipo(), periodo, tipoArbol, login.getGestionTrafo());
    }

    //h.addAll(l2Hijos);
    nodoData.setHijos(l2Hijos);
    getMapa().setNodoList(nodoData);
    nodoData.setHijos(hijos);

    if (!mListTrafo.containsKey(llave)) {
      mListTrafo.put(llave, l2Hijos);
    }

    //Pinto el trafo
    if (nodoData.getTipo().equals("8")) {
      getMapa().pintarPadre(nodoData);
    }

  }

  public TreeNode getRoot() {
    return root;
  }

  public void setRoot(TreeNode root) {
    this.root = root;
  }

  public TreeNode getSelectedNode() {
    return selectedNode;
  }

  public void setSelectedNode(TreeNode selectedNode) {
    this.selectedNode = selectedNode;
  }

  public void displaySelectedSingle(ActionEvent event) {
    if (selectedNode != null) {
      FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Selected", selectedNode.getData().toString());

      FacesContext.getCurrentInstance().addMessage(null, message);
    }
  }

  public String getIdComponente() {
    return idComponente;
  }

  public void setIdComponente(String idComponente) {
    this.idComponente = idComponente;
  }

  /*
   public void onNodeSelect(NodeSelectEvent event) {
   try {
   selectedNode = event.getTreeNode();
   idComponente = (String) selectedNode.getData();        
   obtenerInfo(idComponente);
   agregarNodos(idComponente, selectedNode);
   setInfoBalance();//error aqui Alex
   selectedNode.setExpanded(true);
   } catch (Exception ex) {
   ex.printStackTrace();
   url=null;
   ocultarTortas();
   mostrarChartLine=false;
   }
   }*/
  public void onNodeSelect(NodeSelectEvent event) {
    onNodeSelect(event.getTreeNode());
  }

  public void onNodeSelect(TreeNode nodoSeleccionado) {
    try {
      selectedNode = nodoSeleccionado;
      idComponente = (String) selectedNode.getData();
      obtenerInfo(idComponente);
      agregarNodos(idComponente, selectedNode);
      setInfoBalance();//error aqui Alex
      selectedNode.setExpanded(true);
    } catch (Exception ex) {
      ex.printStackTrace();
      url = null;
      ocultarTortas();
      mostrarChartLine = false;
    }
  }
  /*
   public void onNodeExpanded(NodeExpandEvent event) {//cuando expande en el arbol
   TreeNode nodoSeleccionado = event.getTreeNode();
   if (nodoSeleccionado.getParent() != null) { //si tiene padre para buscar hnos
   List<TreeNode> hermanos = nodoSeleccionado.getParent().getChildren();
   for (Iterator it = hermanos.iterator(); it.hasNext();) {
   TreeNode n = (TreeNode) it.next();
   if (!n.isSelected()) {
   n.setExpanded(false);
   }
   }
   }
   }*/

  public void onNodeExpanded(NodeExpandEvent event) {//cuando expande en el arbol
    TreeNode nodoSeleccionado = event.getTreeNode();
    onNodeExpanded(nodoSeleccionado);
  }

  public void onNodeExpanded(TreeNode nodoSeleccionado) {//cuando expande en el arbol
    if (nodoSeleccionado.getParent() != null) { //si tiene padre para buscar hnos
      List<TreeNode> hermanos = nodoSeleccionado.getParent().getChildren();
      for (Iterator it = hermanos.iterator(); it.hasNext();) {
        TreeNode n = (TreeNode) it.next();
        if (!n.isSelected()) {
          n.setExpanded(false);
        }
        n.setSelected(false);
      }
    }
    nodoSeleccionado.setSelected(true);
  }

  public void onNodeCollapse(NodeCollapseEvent event) {//cuando contrae en el arbol
    TreeNode nodoSeleccionado = event.getTreeNode();
    nodoSeleccionado.setExpanded(false);
  }

  public String estiloColor(String nodo) {
    return estilo + (String) ((Nodo) data.get(nodo)).getColor();
  }

  public String nombreNodo(String nodo) {
    Nodo nodoData = (Nodo) data.get(nodo);
    String nombre = (String) nodoData.toString();
    //String codeHash = nodoData.getCodigo() + "-" + nodoData.getTipo();
    try {
      Balances bAll = balanceManager.getBalancesAll(nodoData.getCodigo(), periodo, nodoData.getTipo());
      //if (!m.containsKey(codeHash)) { //Comenté este if debido a que se estaban repitiendo la cantidad de suministros en todos los periodos.
        
        if (bAll instanceof Balances && bAll != null) {
          nombre += " - Sum " + bAll.getCantSuministrosTotal();
          //m.put(codeHash, nombre);//Comenté este if debido a que se estaban repitiendo la cantidad de suministros en todos los periodos.
        }
      //} else {//Comenté este if debido a que se estaban repitiendo la cantidad de suministros en todos los periodos.
        //nombre = m.get(codeHash);//Comenté este if debido a que se estaban repitiendo la cantidad de suministros en todos los periodos.
        //nombre += " - Sum " + bAll.getCantSuministrosTotal();//Comenté este if debido a que se estaban repitiendo la cantidad de suministros en todos los periodos.
      //}//Comenté este if debido a que se estaban repitiendo la cantidad de suministros en todos los periodos.
    } catch (Exception e) {
      e.printStackTrace();
    }
    System.out.println("--> paso x aquí - nombreNodo");
    return nombre;
  }

  /**
   * @return the infoBalance
   */
  public List<DataBalanceHijo> getInfoBalance() {
    return infoBalance;
  }

  /**
   * @param infoBalance the infoBalance to set
   */
  public void setInfoBalance() throws SQLException {
    Nodo nodoData = (Nodo) data.get(idComponente);
    infoBalance = balanceManager.cuadroMando(nodoData.getCodigo(), nodoData.getTipo(), periodo);
    balance = balanceManager.getBalances(nodoData.getCodigo(), nodoData.getTipo(), periodo);

    int tipoCompo = Integer.parseInt(nodoData.getTipo());
    url = "";

    if (tipoCompo == 0 || tipoCompo == 1 || tipoCompo == 2 || tipoCompo == 4 || tipoCompo == 5) {
      dataRangosBalance = balanceManager.getRangosZonas(nodoData.getCodigo(), nodoData.getTipo(), tipoArbol, periodo);
      url = tipoArbol.equals("NIV200") ? "VistaGeograficaEmp.xhtml" : "VistaElectricaEmp.xhtml";
      trafosBajo = balanceManager.getTrafosByRango(nodoData.getCodigo(), nodoData.getTipo(), periodo, "BAJO");
      trafosMedio = balanceManager.getTrafosByRango(nodoData.getCodigo(), nodoData.getTipo(), periodo, "MEDIO");
      trafosCritico = balanceManager.getTrafosByRango(nodoData.getCodigo(), nodoData.getTipo(), periodo, "CRITICO");
      trafosNegativos = balanceManager.getTrafosByRango(nodoData.getCodigo(), nodoData.getTipo(), periodo, "NEGATIVO");
      trafosInconsistentes = balanceManager.getTrafosByRango(nodoData.getCodigo(), nodoData.getTipo(), periodo, "INCONSISTENTE");
      trafosSinBalances = balanceManager.getTrafosByRango(nodoData.getCodigo(), nodoData.getTipo(), periodo, "SIN BALANCE");

      /*dataRangosBalance.setBajo(trafosBajo.size()>0?String.valueOf(trafosBajo.size()):"");
       dataRangosBalance.setMedio(trafosMedio.size()>0?String.valueOf(trafosMedio.size()):"");
       dataRangosBalance.setCritico(trafosCritico.size()>0?String.valueOf(trafosCritico.size()):"");
       dataRangosBalance.setNegativos(trafosNegativos.size()>0?String.valueOf(trafosNegativos.size()):"");
       dataRangosBalance.setInconsistentes(trafosInconsistentes.size()>0?String.valueOf(trafosInconsistentes.size()):"");
       dataRangosBalance.setSinBalance(trafosSinBalances.size()>0?String.valueOf(trafosBajo.size()):"");*/
      visualizarChartLine(nodoData.getCodigo(), nodoData.getTipo(), periodo);
      visualizarTortas();  //error aqui Alex
      mostrarBtnNovedades = false;
    }
    if (tipoCompo == 6 || tipoCompo == 3 || tipoCompo == 7) {
      dataRangosBalance = balanceManager.getRangosZonas(nodoData.getCodigo(), nodoData.getTipo(), tipoArbol, periodo);
      idBarCir = tipoCompo == 3 || tipoCompo == 7 ? nodoData.getCodigo() : "no";
      url = tipoArbol.equals("NIV200") ? "VistaGeograficaMunic.xhtml" : "VistaElectricaMunic.xhtml";
      trafosBajo = balanceManager.getTrafosByRango(nodoData.getCodigo(), nodoData.getTipo(), periodo, "BAJO");
      trafosMedio = balanceManager.getTrafosByRango(nodoData.getCodigo(), nodoData.getTipo(), periodo, "MEDIO");
      trafosCritico = balanceManager.getTrafosByRango(nodoData.getCodigo(), nodoData.getTipo(), periodo, "CRITICO");
      trafosNegativos = balanceManager.getTrafosByRango(nodoData.getCodigo(), nodoData.getTipo(), periodo, "NEGATIVO");
      trafosInconsistentes = balanceManager.getTrafosByRango(nodoData.getCodigo(), nodoData.getTipo(), periodo, "INCONSISTENTE");
      trafosSinBalances = balanceManager.getTrafosByRango(nodoData.getCodigo(), nodoData.getTipo(), periodo, "SIN BALANCE");
      visualizarChartLine(nodoData.getCodigo(), nodoData.getTipo(), periodo);
      visualizarTortas();
      mostrarBtnNovedades = false;
    }
    if (tipoCompo == 8) {
      dataRangosBalance = balanceManager.getRangosZonas(nodoData.getCodigo(), nodoData.getTipo(), tipoArbol, periodo);
      url = tipoArbol.equals("NIV200") ? "VistaGeograficaTrafo.xhtml" : "VistaElectricaTrafo.xhtml";
      setMedida(balanceManager.getMedida(nodoData.getCodigo(), periodo, nodoData.getTipo()));
      setAtrComponenteMedida(balanceManager.getAtrComponenteMedida(nodoData.getCodigo(), periodo));
      setAtrComponente(balanceManager.getAtrComponente(nodoData.getCodigo()));
      setComponenteMedida(balanceManager.getComponenteMedida(nodoData.getCodigo(), periodo));
      setComponente(balanceManager.getComponente(nodoData.getCodigo()));
      relacionGeo = balanceManager.getZonaMunicipio(nodoData.getCodigo(), periodo);
      relacionElec = balanceManager.getSubestacionCircuito(nodoData.getCodigo(), periodo);
      listCantXTipoUso = balanceManager.getCantSumXTipoUso(nodoData.getCodigo(), periodo);
      cantSumNoMedidos = balanceManager.getCantSumNoMedidos(nodoData.getCodigo(), periodo);
      visualizarChartLine(nodoData.getCodigo(), nodoData.getTipo(), periodo);
      ocultarTortas();
      mostrarBtnNovedades = true;
    }
    if (tipoCompo == 10 || tipoCompo == 11) {
      url = tipoArbol.equals("NIV200") ? "VistaGeograficaMacV.xhtml" : "VistaElectricaMacV.xhtml";
      setComponente(balanceManager.getComponente(nodoData.getCodigo()));
      setUbicacionMV(arbolManager.getUbicacionMacroV(nodoData.getCodigo(), periodo));
      ocultarTortas();
      mostrarChartLine = false;
      mostrarBtnNovedades = false;
    }
    if (tipoCompo == 9) {
      url = tipoArbol.equals("NIV200") ? "VistaGeograficaSumin.xhtml" : "VistaElectricaSumin.xhtml";

      setListMedida(balanceManager.getListMedida(nodoData.getCodigo(), Integer.parseInt(periodo)));
      setAtrComponente(balanceManager.getAtrComponente(nodoData.getCodigo()));
      setComponente(balanceManager.getComponente(nodoData.getCodigo()));
      ocultarTortas();
      mostrarChartLine = false;
      mostrarBtnNovedades = false;
    }

  }

  /**
   * @return the tipoArbol
   */
  public String getTipoArbol() {
    return tipoArbol;
  }

  /**
   * @param tipoArbol the tipoArbol to set
   */
  public void setTipoArbol(String tipoArbol) {
    this.tipoArbol = tipoArbol;
    mostrarBtnNovedades = false;
    novedades = new ArrayList<Novedades>();
    init();
  }

  /**
   * @return the balance
   */
  public Balances getBalance() {
    return balance;
  }

  /**
   * @param balance the balance to set
   */
  public void setBalance(Balances balance) {
    this.balance = balance;
  }

  public DataRangosBalance getDataRangosBalance() {
    return dataRangosBalance;
  }

  /**
   * @param dataRangosBalance the dataRangosBalance to set
   */
  public void setDataRangosBalance(DataRangosBalance dataRangosBalance) {
    this.dataRangosBalance = dataRangosBalance;
  }

  public Boolean getMostrarTablaTrafo() {
    return mostrarTablaTrafo;
  }

  public void setMostrarTablaTrafo(Boolean estado) {
    this.mostrarTablaTrafo = estado;
  }

  public Boolean getMostrarTablaZona() {
    return mostrarTablaZona;
  }

  public void setMostrarTablaZona(Boolean estado) {
    this.mostrarTablaZona = estado;
  }

  /**
   * @return the mapa
   */
  public MapBean getMapa() {
    return mapa;
  }

  /**
   * @param mapa the mapa to set
   */
  public void setMapa(MapBean mapa) {
    this.mapa = mapa;
  }

  public String getUrl() {
    return url;
  }

  /**
   * @param url the url to set
   */
  public void setUrl(String url) {
    this.url = url;
  }

  public void setZoomTipoComponente() {
    List tipos = arbolManager.getZoomMapa();
    zoomTipoComponente = new HashMap();
    for (int i = 0; i < tipos.size(); i++) {
      TipoComponente tipo = (TipoComponente) tipos.get(i);
      zoomTipoComponente.put(String.valueOf(tipo.getIdTipoComponente()), tipo.getZoom());
    }
  }

  public String actualizarPeriodo() {
    System.out.println(periodo);
    init();
    url = null;
    ocultarTortas();
    mostrarChartLine = false;
    return null;
  }

  /**
   * @return the listPeriodos
   */
  public List<String> getListPeriodos() {
    return listPeriodos;
  }

  /**
   * @param listPeriodos the listPeriodos to set
   */
  public void setListPeriodos(List<String> listPeriodos) {
    this.listPeriodos = listPeriodos;
  }

  /**
   * @return the periodoSelected
   */
  public ComboLista getPeriodoSelected() {
    //  actualizarPeriodo();
    return periodoSelected;
  }

  /**
   * @param periodoSelected the periodoSelected to set
   */
  public void setPeriodoSelected(ComboLista periodoSelected) {
    this.periodoSelected = periodoSelected;
  }

  /**
   * @return the medida
   */
  public Medida getMedida() {
    return medida;
  }

  /**
   * @param medida the medida to set
   */
  public void setMedida(Medida medida) {
    this.medida = medida;
  }

  /**
   * @return the atrComponenteMedida
   */
  public AtrComponenteMedida getAtrComponenteMedida() {
    return atrComponenteMedida;
  }

  /**
   * @param atrComponenteMedida the atrComponenteMedida to set
   */
  public void setAtrComponenteMedida(AtrComponenteMedida atrComponenteMedida) {
    this.atrComponenteMedida = atrComponenteMedida;
  }

  /**
   * @return the atrComponente
   */
  public AtrComponente getAtrComponente() {
    return atrComponente;
  }

  /**
   * @param atrComponente the atrComponente to set
   */
  public void setAtrComponente(AtrComponente atrComponente) {
    this.atrComponente = atrComponente;
  }

  /**
   * @return the componenteMedida
   */
  public ComponenteMedida getComponenteMedida() {
    return componenteMedida;
  }

  /**
   * @param componenteMedida the componenteMedida to set
   */
  public void setComponenteMedida(ComponenteMedida componenteMedida) {
    this.componenteMedida = componenteMedida;
  }

  /**
   * @return the componente
   */
  public Componente getComponente() {
    return componente;
  }

  /**
   * @param componente the componente to set
   */
  public void setComponente(Componente componente) {
    this.componente = componente;
  }

  /**
   * @return the relacionGeo
   */
  public PadreHijo getRelacionGeo() {
    return relacionGeo;
  }

  /**
   * @param relacionGeo the relacionGeo to set
   */
  public void setRelacionGeo(PadreHijo relacionGeo) {
    this.relacionGeo = relacionGeo;
  }

  /**
   * @return the relacionElec
   */
  public PadreHijo getRelacionElec() {
    return relacionElec;
  }

  /**
   * @param relacionElec the relacionElec to set
   */
  public void setRelacionElec(PadreHijo relacionElec) {
    this.relacionElec = relacionElec;
  }

  /**
   * @return the relacionElec
   */
  public String getPeriodo() {
    return periodo;
  }

  /**
   * @param relacionElec the relacionElec to set
   */
  public void setPeriodo(String periodo) {
    this.periodo = periodo;
  }

  /**
   * @return the listCantXTipoUso
   */
  public List<DataValue> getListCantXTipoUso() {
    return listCantXTipoUso;
  }

  /**
   * @param listCantXTipoUso the listCantXTipoUso to set
   */
  public void setListCantXTipoUso(List<DataValue> listCantXTipoUso) {
    this.listCantXTipoUso = listCantXTipoUso;
  }

  /**
   * @return the cantSumNoMedidos
   */
  public String getCantSumNoMedidos() {
    return cantSumNoMedidos;
  }

  /**
   * @param cantSumNoMedidos the cantSumNoMedidos to set
   */
  public void setCantSumNoMedidos(String cantSumNoMedidos) {
    this.cantSumNoMedidos = cantSumNoMedidos;
  }

  /**
   * @return the listMedida
   */
  public List<Medida> getListMedida() {
    return listMedida;
  }

  /**
   * @param listMedida the listMedida to set
   */
  public void setListMedida(List<Medida> listMedida) {
    this.listMedida = listMedida;
  }

  public String obtenerNivel(String titulo) {
    String[] aux = titulo.split(":");
    Nodo n1 = (Nodo) data.get(aux[0] + aux[1]);
    System.out.println("nivel------> " + aux[0] + "-" + aux[1]);
    //setNodo((Nodo) data.get(aux[0]+aux[1]));
    if (n1 == null && !mListTrafo.isEmpty()) {
      Collection c = mListTrafo.values();
      List<Nodo> l = new ArrayList<Nodo>();
      l.addAll(c);
      for (int i = 0; i < l.size(); i++) {
        List<Nodo> ll = (List<Nodo>) l.get(i);
        //***
        if(ll != null){
          for (Nodo n : ll) {
            String codigo = n.getCodigo() + "-" + n.getTipo();
            if (codigo.equals(aux[0] + "-" + aux[1])) {
              setNodo((Nodo) n);
              break;
            }
          }
        }else{
          continue;
        }
      }

    } else {
      setNodo(n1);
    }
    return getNodo().getNombreTipo();
  }

  /**
   * @return the nodo
   */
  public Nodo getNodo() {
    return nodo;
  }

  /**
   * @param nodo the nodo to set
   */
  public void setNodo(Nodo nodo) {
    this.nodo = nodo;
  }

  public PieChartModel getTortaTotal() {
    return tortaTotal;
  }

  public void setTortaTotal(PieChartModel tortaTotal) {
    this.tortaTotal = tortaTotal;
  }

  public PieChartModel getTortaFuncionan() {
    return tortaFuncionan;
  }

  public void setTortaFuncionan(PieChartModel tortaFuncionan) {
    this.tortaFuncionan = tortaFuncionan;
  }

  public PieChartModel getTortaNoFuncionan() {
    return tortaNoFuncionan;
  }

  public void setTortaNoFuncionan(PieChartModel tortaNoFuncionan) {
    this.tortaNoFuncionan = tortaNoFuncionan;
  }

  public boolean isMostrarTorta() {
    return mostrarTorta;
  }

  public void setMostrarTorta(boolean mostrarTorta) {
    this.mostrarTorta = mostrarTorta;
  }

  public CartesianChartModel getCategoryModel() {
    return categoryModel;
  }

  public void setCategoryModel(CartesianChartModel categoryModel) {
    this.categoryModel = categoryModel;
  }

  public boolean isMostrarChartLine() {
    return mostrarChartLine;
  }

  public void setMostrarChartLine(boolean mostrarChartLine) {
    this.mostrarChartLine = mostrarChartLine;
  }

  public String getGrafico() {
    return grafico;
  }

  public void setGrafico(String grafico) {
    this.grafico = grafico;
  }

  public String getGraficoVE() {
    return graficoVE;
  }

  public void setGraficoVE(String graficoVE) {
    this.graficoVE = graficoVE;
  }

  public List<Componente> getTrafosBajo() {
    return trafosBajo;
  }

  public void setTrafosBajo(List<Componente> trafosBajo) {
    this.trafosBajo = trafosBajo;
  }

  public List<Componente> getTrafosCritico() {
    return trafosCritico;
  }

  public void setTrafosCritico(List<Componente> trafosCritico) {
    this.trafosCritico = trafosCritico;
  }

  public List<Componente> getTrafosInconsistentes() {
    return trafosInconsistentes;
  }

  public void setTrafosInconsistentes(List<Componente> trafosInconsistentes) {
    this.trafosInconsistentes = trafosInconsistentes;
  }

  public List<Componente> getTrafosMedio() {
    return trafosMedio;
  }

  public void setTrafosMedio(List<Componente> trafosMedio) {
    this.trafosMedio = trafosMedio;
  }

  public List<Componente> getTrafosNegativos() {
    return trafosNegativos;
  }

  public void setTrafosNegativos(List<Componente> trafosNegativos) {
    this.trafosNegativos = trafosNegativos;
  }

  public List<Componente> getTrafosSinBalances() {
    return trafosSinBalances;
  }

  public void setTrafosSinBalances(List<Componente> trafosSinBalances) {
    this.trafosSinBalances = trafosSinBalances;
  }

  public boolean isMostrarBtnNovedades() {
    return mostrarBtnNovedades;
  }

  public void setMostrarBtnNovedades(boolean mostrarBtnNovedades) {
    this.mostrarBtnNovedades = mostrarBtnNovedades;
  }

  public List<Novedades> getNovedades() {
    return novedades;
  }

  public void setNovedades(List<Novedades> novedades) {
    this.novedades = novedades;
  }

  public String getIdTipoNovedad() {
    return idTipoNovedad;
  }

  public void setIdTipoNovedad(String idTipoNovedad) {
    this.idTipoNovedad = idTipoNovedad;
  }

  public List<Tbltipo> getTiposNovedades() {
    return tiposNovedades;
  }

  public void setTiposNovedades(List<Tbltipo> tiposNovedades) {
    this.tiposNovedades = tiposNovedades;
  }

  public Novedades getNovedadSelected() {
    return novedadSelected;
  }

  public void setNovedadSelected(Novedades novedadSelected) {
    this.novedadSelected = novedadSelected;
  }

  public String getRutaComponente() {
    return rutaComponente;
  }

  public void setRutaComponente(String rutaComponente) {
    this.rutaComponente = rutaComponente;
  }

  public String getIdClienteAbuscar() {
    return idClienteAbuscar;
  }

  public void setIdClienteAbuscar(String idClienteAbuscar) {
    this.idClienteAbuscar = idClienteAbuscar;
  }

  public String getTipoComponente() {
    return tipoComponente;
  }

  public void setTipoComponente(String tipoComponente) {
    this.tipoComponente = tipoComponente;
  }

  public UbicacionMacroV getUbicacionMV() {
    return ubicacionMV;
  }

  public void setUbicacionMV(UbicacionMacroV ubicacionMV) {
    this.ubicacionMV = ubicacionMV;
  }

  public String getIdBarCir() {
    return idBarCir;
  }

  public void setIdBarCir(String idBarCir) {
    this.idBarCir = idBarCir;
  }

  public Login getLogin() {
    return login;
  }

  public void setLogin(Login login) {
    this.login = login;
  }

  public String getIdNif() {
    return idNif;
  }

  public void setIdNif(String idNif) {
    this.idNif = idNif;
  }

  public String getIdPlaca() {
    return idPlaca;
  }

  public void setIdPlaca(String idPlaca) {
    this.idPlaca = idPlaca;
  }

  public String getTpComp() {
    return tpComp;
  }

  public void setTpComp(String tpComp) {
    this.tpComp = tpComp;
  }

}
