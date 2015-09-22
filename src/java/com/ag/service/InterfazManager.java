package com.ag.service;

public interface InterfazManager {
    
    public String calcularBalance(int periodo);
    
    public String lecturas(int periodo, int borrar);
    
    public int getBorrarLecturas();
    
    public String ejecutarTodo(int periodo);
    
    public String ceoAsimbal(int periodo);
    
    public String cargaTrafosSinMacro(
            String transformador, String estadoCorte, String direccion, String coordX, 
            String coordY, int carga, String tipoMedidor, String fechaInstalacion, 
            String uso, String subcategoria, String cicloConsumo, String medidor, 
            String ruta, String circuito, String barrio, int periodo);
    
}
