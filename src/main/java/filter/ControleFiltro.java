package filter;

import java.io.IOException;
import java.net.HttpRetryException;
import java.security.Principal;

import javax.ejb.EJB;
import javax.inject.Inject;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;

import service.UsuarioService;
import control.UsuarioLogadoControl;
import entity.Usuario;

@WebFilter(filterName = "ControleFiltro",urlPatterns = {"/*"})
public class ControleFiltro implements Filter {
	
	@Inject
	private UsuarioLogadoControl usuarioLogadoControl;
	
	@Inject
	private Principal userPrincipal;
	
//	@Inject
//	private MenuControl menuControl; 
//	
	@EJB
	private UsuarioService usuarioService;
//	
//	@EJB
//	private ProjetoService projetoService;
	
	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void doFilter(ServletRequest arg0, ServletResponse arg1,
			FilterChain arg2) throws IOException, ServletException {
		
		if (usuarioLogadoControl.getUsuario() == null){
			if (userPrincipal != null && !"anonymous".equals(userPrincipal.getName())){
				System.out.println("USUARIO ESTÁ LOGADO.... " + userPrincipal.getName());
				
				Usuario usuario = usuarioService.obterUsuarioPorLogin(userPrincipal.getName());
				
				usuarioLogadoControl.setUsuario(usuario);
				
			}
			
		}
		
		arg2.doFilter(arg0, arg1);
	}

	@Override
	public void init(FilterConfig arg0) throws ServletException {
		// TODO Auto-generated method stub
		
	}

}
