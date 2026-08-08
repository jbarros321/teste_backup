package br.com.petkids.neogrid.model.dto;

import br.com.petkids.neogrid.model.enums.StatusVendedor;
import java.sql.Timestamp;

public class VendedorDTO {
    private String nomeVendedor;
    private String codigoVendedor;
    private String nomeSupervisor;
    private String codigoSupervisor;
    private String nomeGerente;
    private String codigoGerente;
    private StatusVendedor status;
    private Timestamp dataDesligamento;

    public String getNomeVendedor() { return nomeVendedor; }
    public void setNomeVendedor(String nomeVendedor) { this.nomeVendedor = nomeVendedor; }
    public String getCodigoVendedor() { return codigoVendedor; }
    public void setCodigoVendedor(String codigoVendedor) { this.codigoVendedor = codigoVendedor; }
    public String getNomeSupervisor() { return nomeSupervisor; }
    public void setNomeSupervisor(String nomeSupervisor) { this.nomeSupervisor = nomeSupervisor; }
    public String getCodigoSupervisor() { return codigoSupervisor; }
    public void setCodigoSupervisor(String codigoSupervisor) { this.codigoSupervisor = codigoSupervisor; }
    public String getNomeGerente() { return nomeGerente; }
    public void setNomeGerente(String nomeGerente) { this.nomeGerente = nomeGerente; }
    public String getCodigoGerente() { return codigoGerente; }
    public void setCodigoGerente(String codigoGerente) { this.codigoGerente = codigoGerente; }
    public StatusVendedor getStatus() { return status; }
    public void setStatus(StatusVendedor status) { this.status = status; }
    public Timestamp getDataDesligamento() { return dataDesligamento; }
    public void setDataDesligamento(Timestamp dataDesligamento) { this.dataDesligamento = dataDesligamento; }
}
