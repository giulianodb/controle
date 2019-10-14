package control;

import java.io.Serializable;
import java.util.List;

import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Named;

import service.ValorHonorarioService;

import entity.ValorHonorario;

@Named
@SessionScoped
public class ValorHonorarioControl implements Serializable {
	
	@EJB
	private ValorHonorarioService honorarioService;
	/**
	 * 
	 */
	private static final long serialVersionUID = -4053982830306971790L;
	
	private List<ValorHonorario> listaValorHonorarios;
	
	private ValorHonorario valorHonorario;
	
	public List<ValorHonorario> getListaValorHonorarios() {
		return listaValorHonorarios;
	}

	public void setListaValorHonorarios(List<ValorHonorario> listaValorHonorarios) {
		this.listaValorHonorarios = listaValorHonorarios;
	}

	public String listarValorHonorario() {
		listaValorHonorarios = honorarioService.listarHonorarios();
		return "/pages/honorarios/listar_honorario";
	}

	public String iniciarCadastrarValorHonorario() {

		return "";
	}

	public String cadastrarValorHonorario() {

		return "";
	}

	public String iniciarAlterarValorHonorario() {

		return "";
	}

	public String alterarValorHonorario() {
		
		try {
			honorarioService.alterarListaHonorarios(listaValorHonorarios);
			
			
			FacesContext.getCurrentInstance().addMessage(null, 
					new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso ao alterar honorários. Todos os clientes foram alterados.", ""));
			
			
		} catch (Exception e) {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_FATAL, "Problemas ao realizar a alteração de honorários", ""));
			e.printStackTrace();
			
		}
		return "/pages/honorarios/listar_honorario";

	}

	public String iniciarExcluirValorHonorario() {

		return "";
	}

	public String excluirValorHonorario() {

		return "";
	}

	public ValorHonorario getValorHonorario() {
		return valorHonorario;
	}

	public void setValorHonorario(ValorHonorario valorHonorario) {
		this.valorHonorario = valorHonorario;
	}
}
