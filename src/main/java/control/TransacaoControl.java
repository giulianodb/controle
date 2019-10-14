package control;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.inject.Named;

import service.ClienteService;
import service.TransacaoService;
import service.UsuarioService;
import entity.Cliente;
import entity.TipoTransacaoEnum;
import entity.Transacao;
import entity.Usuario;
import exception.ControleException;

@Named
@SessionScoped
public class TransacaoControl implements Serializable 	{
	
	@EJB
	private TransacaoService transacaoService;
	
	@EJB
	private ClienteService clienteService;
	
	@EJB
	private UsuarioService usuarioService;
	
	private List<Transacao> listaTransacao;
	
	private Date dataInicialPesquisa;
	
	private Date dataFinalPesquisa;
	
	private List<Cliente> listaClienteCombo = new ArrayList<Cliente>();
	
	private List<Usuario> listaAdvogadoCombo = new ArrayList<Usuario>();
	
	private List<TipoTransacaoEnum> listaTipoTransacaoCombo = new ArrayList<TipoTransacaoEnum>();
	
	private Integer idClientePesquisa;
	
	private Integer idAdvogadoPesquisa;
	
	private TipoTransacaoEnum tipoTransacaoPesquisa;
	
	//utilziado para realziar a exclusão de transacao
	private Integer idTransacao;
	
	private Transacao transacao;
	
	
	// Atributos para controlar visualização na pesquisa de movimentações
	
	private Boolean mostrarClientes = false;
	
	private Boolean mostrarAdvogados = false;
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -7578649290436644774L;
	
	
	
	public String listarTransacao(){
		//Iniciando os combos.
		listaClienteCombo = clienteService.pesquisarCliente(new Cliente());
		listaAdvogadoCombo = usuarioService.pesquisarUsuario(new Usuario());
		listaTipoTransacaoCombo = transacaoService.listaTipoTransacao();

		
		listaTransacao = new ArrayList<Transacao>();
		
				
		return "/pages/transacao/listar_transacao";
	}
	
	public String pesquisarTransacao(){
		
			listaTransacao = transacaoService.pesquisarTransacao(dataInicialPesquisa, dataFinalPesquisa, idClientePesquisa, idAdvogadoPesquisa, tipoTransacaoPesquisa);
			

		
		return "/pages/transacao/listar_transacao";
	}
	
	
	public String deletarTransacao(){
		
		try {
			transacaoService.deletarTransacao(idTransacao);
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso ao deletar movimentação.", ""));
			
		}catch (ControleException e) {
			e.printStackTrace();

			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getMessage(), ""));
		}
		
		catch (Exception e) {
			e.printStackTrace();

			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro ao excluir movimentação.", ""));
		}
		
		return listarTransacao();
	}
	
	
	public String deletarTodasTransacao(){
		
		try {
			
			List<Transacao> lista = transacaoService.pesquisarTransacao(null, null, null, null, TipoTransacaoEnum.PAGAMENTO);
			List<Transacao> lista2 = transacaoService.pesquisarTransacao(null, null, null, null, TipoTransacaoEnum.ADIANTAMENTO);
			List<Transacao> lista3 = transacaoService.pesquisarTransacao(null, null, null, null, TipoTransacaoEnum.ENTRADA);
			List<Transacao> lista4 = transacaoService.pesquisarTransacao(null, null, null, null, TipoTransacaoEnum.SAIDA);
			List<Transacao> lista5 = transacaoService.pesquisarTransacao(null, null, null, null, TipoTransacaoEnum.IMPOSTO);
			List<Transacao> lista6 = transacaoService.pesquisarTransacao(null, null, null, null, TipoTransacaoEnum.DISPONIBILIZADO);
			
			transacaoService.deletarTodasTransacao(lista);
			transacaoService.deletarTodasTransacao(lista2);
			transacaoService.deletarTodasTransacao(lista3);
			transacaoService.deletarTodasTransacao(lista4);
			transacaoService.deletarTodasTransacao(lista5);
			transacaoService.deletarTodasTransacao(lista6);
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso ao deletar movimentação.", ""));
			
		}catch (ControleException e) {
			e.printStackTrace();

			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getMessage(), ""));
		}
		
		catch (Exception e) {
			e.printStackTrace();

			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro ao excluir movimentação.", ""));
		}
		
		return listarTransacao();
	}
	
	
	
	public String iniciarSalvarTransacaoManual(){
		try{
				listaTipoTransacaoCombo = transacaoService.listaTipoTransacaoManual();
			if(transacao == null){
				transacao = new Transacao();
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			
		}
		
		return "/pages/transacao/editar_transacao";
	}
	
	
	public String salvarTransacaoManual(){
		try {
			transacaoService.inserirTransacaoManual(transacao);
			
			transacao = new Transacao();
			
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso ao salvar movimentação.", ""));
			
		} catch (Exception e) {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro ao salvar movimentação.", ""));
			e.printStackTrace();
		}
		
		return listarTransacao();
	}
	
	
	public String listarTransacaoCliente(){
		
		listaClienteCombo = clienteService.pesquisarCliente(new Cliente());
		
		listaTransacao = new ArrayList<Transacao>();
		
		return "/pages/transacao/listar_transacao_cliente";
	}
	
	public String pesquisarTransacaoCliente(){
		
		listaTransacao = transacaoService.pesquisarTransacaoCliente(dataInicialPesquisa, dataFinalPesquisa, idClientePesquisa);
			

		
		return "/pages/transacao/listar_transacao_cliente";
	}
	
	
	
	public String listarTransacaoAdvogado(){
		
		listaAdvogadoCombo = usuarioService.pesquisarUsuario(new Usuario());
		listaTransacao = new ArrayList<Transacao>();
		
		return "/pages/transacao/listar_transacao_advogado";
	}
	
	public String pesquisarTransacaoAdvogado(){
		
		try {
			listaTransacao = transacaoService.pesquisarTransacaoAdvogado(dataInicialPesquisa, dataFinalPesquisa, idAdvogadoPesquisa);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return "/pages/transacao/listar_transacao_advogado";
	}
	
	
	public void tipoTransacaoEvent(ActionEvent actionEvent){
		if (tipoTransacaoPesquisa != null && tipoTransacaoPesquisa.equals(TipoTransacaoEnum.PAGAMENTO)){
			mostrarAdvogados = true;
			mostrarClientes = false;
		}
		else if(tipoTransacaoPesquisa != null && tipoTransacaoPesquisa.equals(TipoTransacaoEnum.DISPONIBILIZADO)) {
			mostrarAdvogados = false;
			mostrarClientes = true;
		}
		else {
			mostrarAdvogados = false;
			mostrarClientes = false;
		}
		
	}
	
	
	public List<Transacao> getListaTransacao() {
		return listaTransacao;
	}

	public void setListaTransacao(List<Transacao> listaTransacao) {
		this.listaTransacao = listaTransacao;
	}

	public Date getDataInicialPesquisa() {
		return dataInicialPesquisa;
	}

	public void setDataInicialPesquisa(Date dataInicialPesquisa) {
		this.dataInicialPesquisa = dataInicialPesquisa;
	}

	public Date getDataFinalPesquisa() {
		return dataFinalPesquisa;
	}

	public void setDataFinalPesquisa(Date dataFinalPesquisa) {
		this.dataFinalPesquisa = dataFinalPesquisa;
	}

	public List<Cliente> getListaClienteCombo() {
		return listaClienteCombo;
	}

	public void setListaClienteCombo(List<Cliente> listaClienteCombo) {
		this.listaClienteCombo = listaClienteCombo;
	}

	public Integer getIdClientePesquisa() {
		return idClientePesquisa;
	}

	public void setIdClientePesquisa(Integer idClientePesquisa) {
		this.idClientePesquisa = idClientePesquisa;
	}

	public Integer getIdTransacao() {
		return idTransacao;
	}

	public void setIdTransacao(Integer idTransacao) {
		this.idTransacao = idTransacao;
	}

	public Integer getIdAdvogadoPesquisa() {
		return idAdvogadoPesquisa;
	}

	public void setIdAdvogadoPesquisa(Integer idAdvogadoPesquisa) {
		this.idAdvogadoPesquisa = idAdvogadoPesquisa;
	}

	public List<Usuario> getListaAdvogadoCombo() {
		return listaAdvogadoCombo;
	}

	public void setListaAdvogadoCombo(List<Usuario> listaAdvogadoCombo) {
		this.listaAdvogadoCombo = listaAdvogadoCombo;
	}

	public Transacao getTransacao() {
		return transacao;
	}

	public void setTransacao(Transacao transacao) {
		this.transacao = transacao;
	}

	public List<TipoTransacaoEnum> getListaTipoTransacaoCombo() {
		return listaTipoTransacaoCombo;
	}

	public void setListaTipoTransacaoCombo(
			List<TipoTransacaoEnum> listaTipoTransacaoCombo) {
		this.listaTipoTransacaoCombo = listaTipoTransacaoCombo;
	}

	public TipoTransacaoEnum getTipoTransacaoPesquisa() {
		return tipoTransacaoPesquisa;
	}

	public void setTipoTransacaoPesquisa(TipoTransacaoEnum tipoTransacaoPesquisa) {
		this.tipoTransacaoPesquisa = tipoTransacaoPesquisa;
	}

	public Boolean getMostrarClientes() {
		return mostrarClientes;
	}

	public void setMostrarClientes(Boolean mostrarClientes) {
		this.mostrarClientes = mostrarClientes;
	}

	public Boolean getMostrarAdvogados() {
		return mostrarAdvogados;
	}

	public void setMostrarAdvogados(Boolean mostrarAdvogados) {
		this.mostrarAdvogados = mostrarAdvogados;
	}

	
	
}
