package mx.edu.itoaxaca.sigarp.control;

import java.io.Serializable;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

/**
 * Control de explorar.xhtml (HU2). Por ahora solo guarda los criterios;
 * cuando existan las entidades de proyecto, buscar() llamará a un
 * BusquedaService del módulo EJB y llenará la lista de resultados.
 */
@Named
@ViewScoped
public class BusquedaBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private String texto;
    private String anio;
    private String area;
    private String tecnologia;
    private String empresa;

    public void buscar() {
        // TODO HU2: resultados = busquedaService.buscar(texto, anio, area, tecnologia, empresa);
    }

    public String getTexto() {
        return texto;
    }

    public void setTexto(String texto) {
        this.texto = texto;
    }

    public String getAnio() {
        return anio;
    }

    public void setAnio(String anio) {
        this.anio = anio;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getTecnologia() {
        return tecnologia;
    }

    public void setTecnologia(String tecnologia) {
        this.tecnologia = tecnologia;
    }

    public String getEmpresa() {
        return empresa;
    }

    public void setEmpresa(String empresa) {
        this.empresa = empresa;
    }
}
