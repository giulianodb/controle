package control;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Named;

import service.ClienteService;
import service.ValorHonorarioService;
import entity.Cliente;
import entity.ClienteFisica;
import entity.ClienteJuridica;

@Named
@SessionScoped
public class ClienteControl implements Serializable {
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -5422990602258436088L;

	@EJB
	private ClienteService clienteService;
	
	@EJB
	private ValorHonorarioService honorarioService;
	
	private Cliente cliente;
	
	private List<Cliente> listaCliente;
	
	private String operacao;  
	
	public String listarCliente(){
		try {
			
			cliente = new Cliente();
			
			listaCliente = clienteService.pesquisarCliente(cliente);
			
			return "/pages/cliente/listar_cliente";
			
		} catch (Exception e) {
			 e.printStackTrace();	
			 FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_FATAL, "Problemas ao listar clientes", ""));
			 return "/pages/inicial";
		}
		

	}
	
	public String pesquisarCliente(){
		try {
			if (cliente == null){
				cliente = new Cliente();
			}
			listaCliente = clienteService.pesquisarCliente(cliente);
			
			return "/pages/cliente/listar_cliente";
			
		} catch (Exception e) {
			 e.printStackTrace();
			 FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_FATAL, "Problemas ao listar clientes", ""));
			 return "/pages/inicial";
		}
		
	}
	
	public String iniciarCadastrarClienteFisica(){
		try {
			operacao = "Incluir";
			
			cliente = new ClienteFisica();
			
			cliente.setValorHoraEstagiario(honorarioService.obterValorHoraPorPapel("Estagiario"));
			cliente.setValorHoraJunior(honorarioService.obterValorHoraPorPapel("Advogado_Junior"));
			cliente.setValorHoraPleno(honorarioService.obterValorHoraPorPapel("Advogado_Pleno"));
			cliente.setValorHoraSenior(honorarioService.obterValorHoraPorPapel("Advogado_Senior"));
			
			return "/pages/cliente/editar_cliente_fisica";
			
		} catch (Exception e) {
			e.printStackTrace();
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_FATAL, "Problemas ao iniciar cadastro de cliente", ""));
			return listarCliente();
					
		}
	}
	
	public String iniciarCadastrarClienteJuridica(){
		
		try {
			operacao = "Incluir";
			
			cliente = new ClienteJuridica();
			
			cliente.setValorHoraEstagiario(honorarioService.obterValorHoraPorPapel("Estagiario"));
			cliente.setValorHoraJunior(honorarioService.obterValorHoraPorPapel("Advogado_Junior"));
			cliente.setValorHoraPleno(honorarioService.obterValorHoraPorPapel("Advogado_Pleno"));
			cliente.setValorHoraSenior(honorarioService.obterValorHoraPorPapel("Advogado_Senior"));
			
			return "/pages/cliente/editar_cliente_juridica";
		} catch (Exception e) {
			e.printStackTrace();
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_FATAL, "Problemas ao iniciar cadastro de cliente", ""));
			return listarCliente();
		}
	
	}
	
	public String iniciarAlterarCliente(){
		try {
			operacao = "Alterar";
			
			if (cliente instanceof ClienteFisica){
				return "/pages/cliente/editar_cliente_fisica";
			}
			else {
				return "/pages/cliente/editar_cliente_juridica";
				
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_FATAL, "Problemas ao iniciar alteração de cliente", ""));
			return listarCliente();
		}

	}
	
	public String iniciarExcluirCliente(){
		try {
			operacao = "Excluir";
			
			if (cliente instanceof ClienteFisica){
				return "/pages/cliente/editar_cliente_fisica";
			}
			else {
				return "/pages/cliente/editar_cliente_juridica";
				
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_FATAL, "Problemas ao iniciar exclusão de cliente", ""));
			return listarCliente();
		}
	}
	
	public String cadastrarCliente(){
		try {
			cliente.setDataCadastro(new Date());
			clienteService.cadastrarCliente(cliente);
			
			FacesContext.getCurrentInstance().addMessage(null, 
					new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso ao incluir cliente.", ""));
			
		} catch (Exception e) {
			e.printStackTrace();
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_FATAL, "Problemas ao cadastrar cliente", ""));
		}
		return listarCliente();
	}
	
	public String alterarCliente(){
		try {
			clienteService.alterarCliente(cliente);
			
			FacesContext.getCurrentInstance().addMessage(null, 
					new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso ao alterar cliente.", ""));
			
		} catch (Exception e) {
			e.printStackTrace();
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_FATAL, "Problemas ao realizar a alteração de cliente", ""));
		}
		return listarCliente();
	}

	public String excluirCliente(){
		
		try {
			clienteService.exlcuirCliente(cliente);
			
			FacesContext.getCurrentInstance().addMessage(null, 
					new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso ao excluir cliente.", ""));
		} catch (Exception e) {
			e.printStackTrace();
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_FATAL, "Problemas ao realizar a exclusão de cliente", ""));
		}
		
		return listarCliente();
	}
	
	public Cliente getCliente() {
		return cliente;
	}

	public void setCliente(Cliente cliente) {
		this.cliente = cliente;
	}

	public String getOperacao() {
		return operacao;
	}

	public void setOperacao(String operacao) {
		this.operacao = operacao;
	}

	public List<Cliente> getListaCliente() {
		return listaCliente;
	}

	public void setListaCliente(List<Cliente> listaCliente) {
		this.listaCliente = listaCliente;
	}

}
