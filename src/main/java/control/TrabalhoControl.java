package control;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.inject.Inject;
import javax.inject.Named;

import service.CasoService;
import service.ClienteService;
import service.TrabalhoService;
import service.UsuarioService;
import util.DateUtil;
import entity.Caso;
import entity.Cliente;
import entity.PapelEnum;
import entity.StatusTrabalho;
import entity.Trabalho;
import entity.Usuario;
import exception.ControleException;

@Named
@SessionScoped
public class TrabalhoControl implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1182838017143918623L;

	@EJB
	private TrabalhoService trabalhoService;
	
	@EJB
	private UsuarioService usuarioService;
	
	@EJB
	private ClienteService clienteService;
	
	@EJB
	private CasoService casoService;
	
	@Inject
	private Trabalho trabalho;
	
	@Inject
	private Trabalho trabalhoPesquisa;
	
	@Inject
	private UsuarioLogadoControl usuarioLogadoControl;
	
	private List<String> listaHorarios = new ArrayList<String>();
	
	private List<Trabalho> listaTrabalho;
	
	private String operacao;
	
	private String nome;
	
	private List<StatusTrabalho> listaStatusTrabalhoPesquisa;
	
	private String clientePesquisa;
	
	private Date dataInicioPesquisa;
	
	private Date dataFimPesquisa;
	
	//Atributos para alteração de trabalho
	
	private List<Usuario> listaAdvogadosCombo;
	
	private List<Cliente> listaClienteCombo;
	
	private List<Caso> listaCasoCombo = new ArrayList<Caso>();
	
	
	private Integer idAdvogadoCombo;
	
	private Integer idClienteCombo;
	
	private Integer idCasoCombo;
	
	private Boolean statusCheckBox;
	
	 //atributos para controlar a questão de compartilhamento de trabalho
	private List<Usuario> listUsuarioNaoVinculadosCompartilhamento;
	
	private List<Usuario> listUsuarioVinculadosCompartilhamento;
	
	//atributos para mudar seleção
	
	//Para controlar selecao cobravel para todos trabalhos selecionados
	private Boolean mudarSelecaoCobravel = false;
	
	private StatusTrabalho mudarSelecaoStatusTrabalho;
	
	@PostConstruct
	public void init(){
		Calendar c = Calendar.getInstance();
		c.add(Calendar.DAY_OF_YEAR,-30 ); //removento dois dias;
		
		dataInicioPesquisa = c.getTime();
		dataFimPesquisa = new Date();
	}
	
	public String listarTrabalho(){
		
		try {
			popularHoras();
			popularListaStatus();
			
			//TODO retirar quando estiver em request scoped
			
			if (trabalhoPesquisa == null || trabalhoPesquisa.getCaso() == null || trabalhoPesquisa.getAdvogado() == null){
				trabalhoPesquisa = new Trabalho();
				Caso caso = new Caso();
				caso.setCliente(new Cliente());
				trabalhoPesquisa.setCaso(caso);
				trabalhoPesquisa.setAdvogado(new Usuario());
				trabalhoPesquisa.setStatusTrabalho(StatusTrabalho.CRIADO);
				clientePesquisa = null;
				listaTrabalho = null;
			}
			
			return pesquisarTrabalho();
			
		} catch (Exception e) {
			e.printStackTrace();
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_FATAL, "Problemas ao carregar lista de trabalhos", ""));
			
			return "/pages/inicial";
		}
	}
	
	public String pesquisarTrabalho(){
		try {
			popularHoras();
			popularListaStatus();
			//trabalho.getCaso().setNomeCaso(null);
			//trabalho.setStatusTrabalho(null);
			
			dataInicioPesquisa = DateUtil.adicionarHoraInicio(dataInicioPesquisa);
			dataFimPesquisa = DateUtil.adicionarHoraFim(dataFimPesquisa);
			listaTrabalho = trabalhoService.pesquisarTrabalho(trabalhoPesquisa,dataInicioPesquisa,dataFimPesquisa,clientePesquisa);
			
			return "/pages/trabalho/listar_trabalho";
			
		} catch (Exception e) {
			e.printStackTrace();
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_FATAL, "Problemas ao carregar lista de trabalhos", ""));
			return "/pages/inicial";
		}
		
		
	}
	


	public String iniciarCadastrarTrabalho(){
		try {
			//TODO Para session scope tirar quando for request
			
			//trabalho = new Trabalho();
			
			//TODO veririfcar se possui um caso para inserir.. caso contrário lançar excpetion
			Caso casoTemp = trabalho.getCaso();
			trabalho = new Trabalho();
			trabalho.setCaso(casoTemp);
			popularHoras();
			
			
			trabalho.setDataTrabalho(new Date());
			operacao = "Incluir";
			
			listarUsuariosNaoVinculadosCompartilhamento();
			
			listUsuarioVinculadosCompartilhamento = new ArrayList<Usuario>();
			
			
			return "/pages/trabalho/editar_trabalho";
			
		} catch (Exception e) {
			e.printStackTrace();
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_FATAL, "Problemas ao iniciar cadastro de trabalho", ""));
			
		}
		
		return listarTrabalho();
	}

	public String iniciarAlterarTrabalho(){
		try {
			operacao = "Alterar";
			trabalho = trabalhoService.obterTrabalho(trabalho.getIdTrabalho());
			
			if (usuarioLogadoControl.getUsuario().getPapel().getNomePapel().equals(PapelEnum.ADMINISTRADOR.getDescricao())){
				//popular combos..
				idAdvogadoCombo = trabalho.getAdvogado().getIdUsuario();
				idCasoCombo = trabalho.getCaso().getIdCaso();
				idClienteCombo = trabalho.getCaso().getCliente().getIdCliente();
				
				listaAdvogadosCombo = usuarioService.pesquisarUsuario(null);
				listaClienteCombo = clienteService.pesquisarCliente(new Cliente());
				listaCasoCombo = casoService.listarCasoPorCliente(idClienteCombo);				
				
			}
			
			listarUsuariosNaoVinculadosCompartilhamento();
			
			for (Usuario usuarioVinculado : trabalho.getUsuarios()) {
				if (listUsuarioNaoVinculadosCompartilhamento.contains(usuarioVinculado)){
					listUsuarioNaoVinculadosCompartilhamento.remove(usuarioVinculado);
				}
			}
			listUsuarioVinculadosCompartilhamento = trabalho.getUsuarios();
			
			
		} catch (Exception e) {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_FATAL, "Problemas ao alterar trabalho", ""));
			
			return listarTrabalho();
		}
		return "/pages/trabalho/editar_trabalho";
	}
	
	public String iniciarExcluirTrabalho(){
		operacao = "Excluir";
		return "/pages/trabalho/editar_trabalho";
	}
	
	public String cadastrarTrabalho(){
		try {
			trabalho.setAdvogado(usuarioLogadoControl.getUsuario());
			trabalhoService.cadastrarTrabalho(trabalho);
			
			//Para session scope...
			trabalho = new Trabalho();
			trabalho.setCaso(new Caso());
			trabalho.setAdvogado(new Usuario());
			
			FacesContext.getCurrentInstance().addMessage(null, 
					new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso ao cadastrar trabalho.", ""));
			
		} catch (Exception e) {
			e.printStackTrace();
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_FATAL, "Problemas ao cadastrar trabalho", ""));
		}
		
		return listarTrabalho();
	}
	
	public String alterarTrabalho(){
		try {
			
			if (usuarioLogadoControl.getUsuario().getPapel().getNomePapel().equals(PapelEnum.ADMINISTRADOR.getDescricao())){
				
			
				if (!trabalho.getCaso().getIdCaso().equals(idCasoCombo)){
					trabalho.setCaso(casoService.obterCaso(idCasoCombo));
				}
				//Se o for alterado o advogado, pesquisa o advogado selecionado e altera o valor
				//da hora.
				if (!trabalho.getAdvogado().getIdUsuario().equals(idAdvogadoCombo)){
					
					if (trabalho.getAdvogado().getPapel().getNomePapel().equals(PapelEnum.ADVOGADO_JUNIOR.getDescricao())){
						trabalho.setValorHora(trabalho.getCaso().getCliente().getValorHoraJunior());
					}
					else if(trabalho.getAdvogado().getPapel().getNomePapel().equals(PapelEnum.ADVOGADO_PLENO.getDescricao())){
						trabalho.setValorHora(trabalho.getCaso().getCliente().getValorHoraPleno());
					}
					else if(trabalho.getAdvogado().getPapel().getNomePapel().equals(PapelEnum.ESTAGIARIO.getDescricao())){
						trabalho.setValorHora(trabalho.getCaso().getCliente().getValorHoraEstagiario());
					}
					else{
						trabalho.setValorHora(trabalho.getCaso().getCliente().getValorHoraSenior());
					}
					
					
					trabalho.setAdvogado(usuarioService.obterUsuarioPorId(idAdvogadoCombo));
				}
				
				
				trabalho.setAdvogado(usuarioService.obterUsuarioPorId(idAdvogadoCombo));
				trabalho.setCaso(casoService.obterCaso(idCasoCombo));
			}
			
			trabalhoService.alterarTrabalho(trabalho);
			FacesContext.getCurrentInstance().addMessage(null, 
					new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso ao alterar trabalho.", ""));
		} 
		catch(ControleException e){
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_FATAL, e.getMessage(), ""));
		}
		
		catch (Exception e) {
			e.printStackTrace();
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_FATAL, "Problemas ao alterar trabalho", ""));
		}
		return listarTrabalho();
	}
	

	public String excluirTrabalho(){
		
		try {
			trabalhoService.exlcuirTrabalho(trabalho);
			FacesContext.getCurrentInstance().addMessage(null, 
					new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso ao excluir trabalho.", ""));
			
		} catch (Exception e) {
			e.printStackTrace();
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_FATAL, "Problemas ao excluir trabalho", ""));
		
		}
		
		return listarTrabalho();
	}
	
	public String enviarFaturamento(){
		
		try {
			trabalhoService.alterarListaTrabalho(listaTrabalho,true,null);
			FacesContext.getCurrentInstance().addMessage(null, 
					new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso enviar trabalhos selecionados para faturamento.", ""));
		} catch (Exception e) {
			e.printStackTrace();
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_FATAL, "Problemas ao enviar trabalhos para faturamento", ""));
		
		}
		
		return null;
	}
	
	public String salvarAlteradosAdm(){
		
		try {
			trabalhoService.alterarListaTrabalho(listaTrabalho,false,null);
			FacesContext.getCurrentInstance().addMessage(null, 
					new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso ao alterar trabalhos selecionados.", ""));
		} 
		catch (ControleException e) {
			e.printStackTrace();
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_FATAL, e.getMessage(), ""));
		
		}
		catch (Exception e) {
			e.printStackTrace();
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_FATAL, "Problemas alterar trabalhos", ""));
		
		}
		
		return pesquisarTrabalho();
	}
	
	public void popularHoras(){
		
		if (listaHorarios.size() == 0){
			for (int i = 0; i < 12; i++) {
				for (int j = 0; j < 4; j++) {
					if (j == 0){
						listaHorarios.add(((Integer)i).toString()+":00");
					}
					else if(j == 1){
						listaHorarios.add(((Integer)i).toString()+":15");
					}
					else if(j == 2){
						listaHorarios.add(((Integer)i).toString()+":30");
					}
					else {
						listaHorarios.add(((Integer)i).toString()+":45");
					}
					
				}
			}
		}
	}
	
	private void popularListaStatus() {
		if (listaStatusTrabalhoPesquisa == null || listaStatusTrabalhoPesquisa.size() == 0){
			listaStatusTrabalhoPesquisa = new ArrayList<StatusTrabalho>();
			listaStatusTrabalhoPesquisa.add(StatusTrabalho.CRIADO);
			listaStatusTrabalhoPesquisa.add(StatusTrabalho.ENVIADO_FATURAR);
			listaStatusTrabalhoPesquisa.add(StatusTrabalho.FATURADO);
//			listaStatusTrabalhoPesquisa.add(StatusTrabalho.PARCIAL_PAGO);
			listaStatusTrabalhoPesquisa.add(StatusTrabalho.PAGO);
		}
	}
	
	public void listarCasosDoCliente(ActionEvent actionEvent){
		
		listaCasoCombo = casoService.listarCasoPorCliente(idClienteCombo);
		
	}
	
	public void selecionarListaTrabalho (ActionEvent actionEvent){
		
		for (Trabalho trabalho : listaTrabalho) {
			trabalho.setAlterarViaAdm(statusCheckBox);
		}
		
	}
	
	//Ajax na lsita de trabalho para alterar todos os trabalhos para a opção escolhida para cobrável
	public void mudarListaTrabalhoCobravel (ActionEvent actionEvent){
		if (mudarSelecaoCobravel != null){
			for (Trabalho trabalho : listaTrabalho) {
				if(trabalho.getAlterarViaAdm()){
					trabalho.setCobravel(mudarSelecaoCobravel);
				}
			}
		}
		
	}
	
	//Ajax na lsita de trabalho para alterar todos os trabalhos para a opção escolhida de faturamento
	public void mudarListaTrabalhoFaturamento (ActionEvent actionEvent){
		if (mudarSelecaoStatusTrabalho != null){
			for (Trabalho trabalho : listaTrabalho) {
				if(trabalho.getAlterarViaAdm()){
					trabalho.setStatusTrabalho(mudarSelecaoStatusTrabalho);
				}
			}
		}
		
	}




	
	
	/**
	 * Utilizado para listar todos os advogados que podem ser vinculados ao caso no papel de compartilhando trabalho
	 * @return
	 */
	//TODO rever!!!! esses retornos malandros
	public String listarUsuariosNaoVinculadosCompartilhamento(){
		try {
			listUsuarioNaoVinculadosCompartilhamento = usuarioService.pesquisarUsuario(null);
			
		} catch (Exception e) {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro ao carregar lista de usuário", ""));
			return "/pages/inicial";
		}
		
		return "/pages/usuario/listar_usuario"; 
	}
	
	
	/**
	 * Responsável em adicionar os advogados no trabalho como compartilhados
	 * @return
	 */
	
	public void adicionarUsuarioCompartilhamento(Usuario user){
		listUsuarioNaoVinculadosCompartilhamento.remove(user);
		if (listUsuarioVinculadosCompartilhamento != null){
			listUsuarioVinculadosCompartilhamento.add(user);
		}
		else {
			listUsuarioVinculadosCompartilhamento = new ArrayList<Usuario>();
			listUsuarioVinculadosCompartilhamento.add(user);
		}
		
	}
	
	public void removerUsuarioCompartilhamento(Usuario user){
		listUsuarioVinculadosCompartilhamento.remove(user);
		if (listUsuarioNaoVinculadosCompartilhamento != null){
			listUsuarioNaoVinculadosCompartilhamento.add(user);
		}
		else {
			listUsuarioNaoVinculadosCompartilhamento = new ArrayList<Usuario>();
			listUsuarioNaoVinculadosCompartilhamento.add(user);
		}
		
	}
	
	
	
	
	public Trabalho getTrabalho() {
		return trabalho;
	}

	public void setTrabalho(Trabalho trabalho) {
		this.trabalho = trabalho;
	}

	public List<Trabalho> getListaTrabalho() {
		return listaTrabalho;
	}

	public void setListaTrabalho(List<Trabalho> listaTrabalho) {
		this.listaTrabalho = listaTrabalho;
	}

	public String getOperacao() {
		return operacao;
	}

	public void setOperacao(String operacao) {
		this.operacao = operacao;
	}

	public String getNome() {
		if (trabalho !=null && trabalho.getHorasTrabalho() != null && trabalho.getValorHora() != null) {
			Float te = trabalho.getHorasTrabalho() * trabalho.getValorHora();
			return nome+te.toString();
		}
		else {
			return nome;
		}
	}

	public void setNome(String nome) {
		if (trabalho !=null && trabalho.getHorasTrabalho() != null && trabalho.getValorHora() != null) {
			Float te = trabalho.getHorasTrabalho() * trabalho.getValorHora();
			this.nome = nome+te.toString();
		}
		else {
			this.nome = nome;
		}
	}

	public List<String> getListaHorarios() {
		return listaHorarios;
	}

	public void setListaHorarios(List<String> listaHorarios) {
		this.listaHorarios = listaHorarios;
	}


	public String getClientePesquisa() {
		return clientePesquisa;
	}

	public void setClientePesquisa(String clientePesquisa) {
		this.clientePesquisa = clientePesquisa;
	}



	public List<StatusTrabalho> getListaStatusTrabalhoPesquisa() {
		return listaStatusTrabalhoPesquisa;
	}



	public void setListaStatusTrabalhoPesquisa(
			List<StatusTrabalho> listaStatusTrabalhoPesquisa) {
		this.listaStatusTrabalhoPesquisa = listaStatusTrabalhoPesquisa;
	}



	public Date getDataInicioPesquisa() {
		return dataInicioPesquisa;
	}



	public void setDataInicioPesquisa(Date dataInicioPesquisa) {
		this.dataInicioPesquisa = dataInicioPesquisa;
	}



	public Date getDataFimPesquisa() {
		return dataFimPesquisa;
	}



	public void setDataFimPesquisa(Date dataFimPesquisa) {
		this.dataFimPesquisa = dataFimPesquisa;
	}

	public List<Usuario> getListaAdvogadosCombo() {
		return listaAdvogadosCombo;
	}

	public void setListaAdvogadosCombo(List<Usuario> listaAdvogadosCombo) {
		this.listaAdvogadosCombo = listaAdvogadosCombo;
	}

	public List<Cliente> getListaClienteCombo() {
		return listaClienteCombo;
	}

	public void setListaClienteCombo(List<Cliente> listaClienteCombo) {
		this.listaClienteCombo = listaClienteCombo;
	}

	public List<Caso> getListaCasoCombo() {
		return listaCasoCombo;
	}

	public void setListaCasoCombo(List<Caso> listaCasoCombo) {
		this.listaCasoCombo = listaCasoCombo;
	}

	public Integer getIdAdvogadoCombo() {
		return idAdvogadoCombo;
	}

	public void setIdAdvogadoCombo(Integer idAdvogadoCombo) {
		this.idAdvogadoCombo = idAdvogadoCombo;
	}

	public Integer getIdClienteCombo() {
		return idClienteCombo;
	}

	public void setIdClienteCombo(Integer idClienteCombo) {
		this.idClienteCombo = idClienteCombo;
	}

	public Integer getIdCasoCombo() {
		return idCasoCombo;
	}

	public void setIdCasoCombo(Integer idCasoCombo) {
		this.idCasoCombo = idCasoCombo;
	}

	public Boolean getStatusCheckBox() {
		return statusCheckBox;
	}

	public void setStatusCheckBox(Boolean statusCheckBox) {
		this.statusCheckBox = statusCheckBox;
	}

	public List<Usuario> getListUsuarioNaoVinculadosCompartilhamento() {
		return listUsuarioNaoVinculadosCompartilhamento;
	}

	public void setListUsuarioNaoVinculadosCompartilhamento(
			List<Usuario> listUsuarioNaoVinculadosCompartilhamento) {
		this.listUsuarioNaoVinculadosCompartilhamento = listUsuarioNaoVinculadosCompartilhamento;
	}

	public List<Usuario> getListUsuarioVinculadosCompartilhamento() {
		return listUsuarioVinculadosCompartilhamento;
	}

	public void setListUsuarioVinculadosCompartilhamento(
			List<Usuario> listUsuarioVinculadosCompartilhamento) {
		this.listUsuarioVinculadosCompartilhamento = listUsuarioVinculadosCompartilhamento;
	}

	public Trabalho getTrabalhoPesquisa() {
		return trabalhoPesquisa;
	}

	public void setTrabalhoPesquisa(Trabalho trabalhoPesquisa) {
		this.trabalhoPesquisa = trabalhoPesquisa;
	}

	public boolean isMudarSelecaoCobravel() {
		return mudarSelecaoCobravel;
	}

	public void setMudarSelecaoCobravel(boolean mudarSelecaoCobravel) {
		this.mudarSelecaoCobravel = mudarSelecaoCobravel;
	}

	public StatusTrabalho getMudarSelecaoStatusTrabalho() {
		return mudarSelecaoStatusTrabalho;
	}

	public void setMudarSelecaoStatusTrabalho(
			StatusTrabalho mudarSelecaoStatusTrabalho) {
		this.mudarSelecaoStatusTrabalho = mudarSelecaoStatusTrabalho;
	}



}
