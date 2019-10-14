package control;

import java.io.Serializable;
import java.util.List;

import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Named;

import service.ParticipacaoDefaultService;
import service.ParticipacaoService;
import entity.ParticipacaoDefault;
import entity.ValorHonorario;
import exception.ControleException;

@Named
@SessionScoped
public class ParticipacaoDefaultControl implements Serializable {
	
	@EJB
	private ParticipacaoDefaultService participacaoDefaultService;
	/**
	 * 
	 */
	private static final long serialVersionUID = -4053982830306971790L;
	
	private List<ParticipacaoDefault> listaParticipacaoDefault;
	
	private ParticipacaoDefault participacaoDefault;
	
	public String listarParticipacaoDefault() {
		listaParticipacaoDefault = participacaoDefaultService.listarParticipacaoDefault();
		return "/pages/participacao/listar_participacao_default";
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

	public String alterarParticipacaoDefault() {
		
		try {
			participacaoDefaultService.alterarListaParticipacaoDefault(listaParticipacaoDefault);
			
			
			FacesContext.getCurrentInstance().addMessage(null, 
					new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso ao alterar valores de participação.", ""));
			
			
		} 
		
	 catch (ControleException e) {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_FATAL, e.getMessage(), ""));
			e.printStackTrace();
			
		}
		catch (Exception e) {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_FATAL, "Problemas ao realizar a alteração de valores default de participção", ""));
			e.printStackTrace();
			
		}
		return listarParticipacaoDefault();

	}

	public String iniciarExcluirValorHonorario() {

		return "";
	}

	public String excluirValorHonorario() {

		return "";
	}


	public List<ParticipacaoDefault> getListaParticipacaoDefault() {
		return listaParticipacaoDefault;
	}

	public void setListaParticipacaoDefault(
			List<ParticipacaoDefault> listaParticipacaoDefault) {
		this.listaParticipacaoDefault = listaParticipacaoDefault;
	}

	public ParticipacaoDefault getParticipacaoDefault() {
		return participacaoDefault;
	}

	public void setParticipacaoDefault(ParticipacaoDefault participacaoDefault) {
		this.participacaoDefault = participacaoDefault;
	}
}
