package control;

import java.io.Serializable;

import javax.ejb.DuplicateKeyException;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import service.UsuarioService;

import entity.Usuario;

@Named
@SessionScoped
public class UsuarioLogadoControl implements Serializable {
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -5422990602258436088L;
	
	private Usuario usuario;
	
	private String confirmaSenha;
	
	private String senhaNova;
	
	@EJB
	private UsuarioService usuarioService;
	
	public String getConfirmaSenha() {
		return confirmaSenha;
	}

	public void setConfirmaSenha(String confirmaSenha) {
		this.confirmaSenha = confirmaSenha;
	}

	public String getSenhaNova() {
		return senhaNova;
	}

	public void setSenhaNova(String senhaNova) {
		this.senhaNova = senhaNova;
	}

	public String sair(){

		try{
			HttpServletRequest request = (HttpServletRequest)FacesContext.getCurrentInstance().getExternalContext().getRequest();
			HttpServletResponse response = (HttpServletResponse)FacesContext.getCurrentInstance().getExternalContext().getResponse();
			request.logout();
			request.getSession().invalidate();
			//
		//	request.getRequestDispatcher("./pages/inicial.jsf"). forward(request, response);
			
			//FacesContext.getCurrentInstance().responseComplete();
			
			
			
		}catch (Exception e) {
			e.printStackTrace();	

		}
		
		return "/pages/inicial.jsf?faces-redirect=true";
	}
	
	public String alterarSenha(){
		
		return "/pages/usuario/alterar_senha_usuario";
	}
	
	//muda senha
	
	 
    public String storeNewPassword() {
    	if (senhaNova.equals(confirmaSenha)){
    		usuario.setSenha(senhaNova);
    		
    		try {
				usuarioService.alterarUsuario(usuario);
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Senha Alterada com sucesso.", ""));
			} catch (DuplicateKeyException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Ocorreu um erro.", ""));
			}
    		
    		
    		return "/pages/inicial";
    	}
    	else {
    		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Senha não confere.", ""));
    		
    		return "";
    	}
    
    
    }
	
	public Usuario getUsuario() {
		return usuario;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}

	
	}
