package control;

import java.io.Serializable;

import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Named;

import service.ContaService;
import dto.ResumoFinanceiroDTO;


@Named
@SessionScoped
public class ContaControl implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1364575060387122436L;
	/**
	 * serializable
	 */
	

	@EJB
	private ContaService contaService;
	
	private ResumoFinanceiroDTO resumoFinanceiroDTO;

	
	/**
	 * Lista todos os casos cadastrados no sistema
	 * @return
	 */
	public String visualizarResumoFinanceiro(){
		try{	
			
			resumoFinanceiroDTO = contaService.obterResumoFinanceiro();
			
		} catch (Exception e) {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro ao carregar resumo.", ""));
			e.printStackTrace();
			return "/pages/inicial";
		}
					
			return "/pages/conta/resumo";
		}


	public ResumoFinanceiroDTO getResumoFinanceiroDTO() {
		return resumoFinanceiroDTO;
	}


	public void setResumoFinanceiroDTO(ResumoFinanceiroDTO resumoFinanceiroDTO) {
		this.resumoFinanceiroDTO = resumoFinanceiroDTO;
	}



}
