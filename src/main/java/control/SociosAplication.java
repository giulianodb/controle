package control;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.enterprise.context.ApplicationScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Named;

import service.AdmService;
import service.UsuarioService;
import entity.Usuario;
import exception.ControleException;

@Named
@ApplicationScoped
public class SociosAplication {

	private List<Usuario> listaSocios;

	@EJB
	private UsuarioService usuarioService;

	@EJB
	private AdmService admService;

	public List<Usuario> getListaSocios() {
		return listaSocios;
	}

	public void setListaSocios(List<Usuario> listaSocios) {
		this.listaSocios = listaSocios;
	}

	@PostConstruct
	public void init() {
		listaSocios = usuarioService.listarSocios();
	}

	public void popularBase() throws Exception {
		try {
			admService.popularBase();
			
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso ao popular base.", ""));
		} catch (ControleException e) {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, e.getMessage(), ""));
		} catch (Exception e) {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro ao popular base.", ""));
		}
	}

}
