package control;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.inject.Inject;
import javax.inject.Named;

import service.CasoService;
import service.ClienteService;
import service.FaturaService;
import service.ParticipacaoDefaultService;
import service.ParticipacaoService;
import service.TipoCasoService;
import service.UsuarioService;
import util.CarregarCombos;
import util.DateUtil;
import util.NumeroUtil;
import dto.AdvogadoValorExito;
import entity.Caso;
import entity.Cliente;
import entity.Fatura;
import entity.ParcelaExito;
import entity.PreParticipacaoExito;
import entity.Prolabore;
import entity.ProlaboreEnum;
import entity.StatusCaso;
import entity.StatusTrabalho;
import entity.TipoCaso;
import entity.TipoFaturaCasoEnum;
import entity.TipoParticipacaoEnum;
import entity.Usuario;
import entity.ValorParticipacaoEnum;
import exception.AlmiranteException;
import exception.ControleException;


@Named
@SessionScoped
public class CasoControl implements Serializable {

	/**
	 * serializable
	 */
	private static final long serialVersionUID = 4146187359925003799L;

	@EJB
	private CasoService casoService;
	
	@EJB
	private ParticipacaoService participacaoService;
	
	@EJB
	private TipoCasoService tipoCasoService;
	
	@EJB
	private ClienteService clienteService;
	
	@EJB
	private UsuarioService usuarioService;
	
	@EJB
	private FaturaService faturaService;
	
	@EJB
	private ParticipacaoDefaultService participacaoDefaultService;
	
	@Inject
	private Caso caso;
	
	@Inject
	private UsuarioLogadoControl usuarioLogadoControl;
	
	@Inject
	private FaturaExitoControl faturaExitoControl;
	
	@Inject
	private FaturaControl faturaControl;

	private List<Usuario> listUsuarioNaoVinculadosIndicacao;
	
	private List<Usuario> listUsuarioVinculadosIndicacao;
	
	private List<Caso> listaCaso;
	
	private List<TipoCaso> listaTipoCaso;
	
	private List<TipoFaturaCasoEnum> listaTipoFaturamento;
	
	private List<Cliente> listaClienteCombo;
	
	private String operacao;
	
	private Date dataInicioPesquisa;
	
	private Date dataFimPesquisa;
	
	private Usuario usuarioIndicacao;
	
	private Boolean casoFixo;
	
	//usado para inserir indicação via listagem de casos
	private Integer casoTempIndicacao;
	
	//usado para inserir indicação via listagem de casos
	private List<Usuario> listUsuarioTemp;
	
	//usado para inserir indicacao via listagem, retirar
	private Boolean statusCheckBox;
	
	private List<ProlaboreEnum> listaProlaboleEnum;
	
	private Integer contadorProlabole = 0;
	
	//saber qual pralabole está sendo executado para alterar tipo de vencimento
	private Prolabore prolaboleManutencao;
	
	private List<ValorParticipacaoEnum> listaTipoExito;
	
	//atributo usado para listar advogados para finalziar caso - exito
	
	private List<AdvogadoValorExito> listaAdvogadoNaoVinculadoExito;
	
	private List<AdvogadoValorExito> listaAdvogadoVinculadoExito;
	
	
	private Float porcentagemDistribuicaoExitoSocio;
	
	private Float porcentagemDistribuicaoExitoTrabalho;
	
	private Float porcentagemDistribuicaoExitoIndicacao;
	
	private Float porcentagemDistribuicaoExitoImposto;
	
	private Float porcentagemDistribuicaoExitoFundo;
	
	private Boolean previsaoExito;
	
	private Boolean mostrarPorcetagemExito;
	
	/**
	 * Utilizado para listar todos os advogados que podem ser vinculados ao caso no papel de participantes para poderem ganhar uma participação
	 * a mais no processo de caso
	 * @return
	 */
	public String listarUsuariosNaoVinculadosParticipacao(){
		try {
			listUsuarioNaoVinculadosIndicacao = usuarioService.pesquisarUsuario(null);
			
		} catch (Exception e) {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro ao carregar lista de usuário", ""));
			return "/pages/inicial";
		}
		
		return "/pages/usuario/listar_usuario"; 
	}
	
	
	public void listarAdvogadoNaoVinculadosExito(){
		try {
			List<Usuario> usuarios = usuarioService.listarAdvogados();
			
			listaAdvogadoNaoVinculadoExito = new ArrayList<AdvogadoValorExito>();
			
			for (Usuario usuario : usuarios) {
				AdvogadoValorExito ave = new AdvogadoValorExito();
				ave.setUsuario(usuario);
				listaAdvogadoNaoVinculadoExito.add(ave);
			}
			
		} catch (Exception e) {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro ao carregar lista de usuário", ""));
		}
		
	}
	

	/**
	 * Responsável em adicionar os advogados no caso como participantes
	 * @return
	 */
	
	public void adicionarUsuarioIndicacao(Usuario user){
		listUsuarioNaoVinculadosIndicacao.remove(user);
		if (listUsuarioVinculadosIndicacao != null){
			listUsuarioVinculadosIndicacao.add(user);
		}
		else {
			listUsuarioVinculadosIndicacao = new ArrayList<Usuario>();
			listUsuarioVinculadosIndicacao.add(user);
		}
		
	}
	
	//TODO adiciona indicaçção pela listagem de casos, remover
	public void adicionarUsuarioIndicacaoTemp(Usuario user){
		
		for (Caso caso : listaCaso) {
			if(caso.getIdCaso().equals(casoTempIndicacao)){
				if (caso.getListaUsuarioIndicacao() != null){
					if(!caso.getListaUsuarioIndicacao().contains(user)){
						caso.getListaUsuarioIndicacao().add(user);
						break;
					}
				}
				else {
					caso.setListaUsuarioIndicacao((new ArrayList<Usuario>()));
					caso.getListaUsuarioIndicacao().add(user);
				}
			}
		}
		
	}
	
	
	//TODO temporario para alteração de indicação na lista de casos
	public void selecionarListaCaso(ActionEvent actionEvent){
		
		for (Caso caso : listaCaso) {
			caso.setCasoSelecionado(statusCheckBox);
		}
		
	}
	
	
	public String alterarListaCasoTemp(){
		try {
			for (Caso caso : listaCaso) {
				if (caso.getCasoSelecionado()){
					casoService.alterarCaso(caso,true);
				}
			}
			
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso ao alterar casos.", ""));
			
		} catch (Exception e) {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro ao alterar casos.", ""));
		}
		
		return listarCaso();
	}
	
	/**
	 * Responsável em remover os advogados no caso como participantes
	 * @return
	 */
	
	public void removerUsuarioIndicacao(Usuario user){
		listUsuarioVinculadosIndicacao.remove(user);
		if (listUsuarioNaoVinculadosIndicacao != null){
			listUsuarioNaoVinculadosIndicacao.add(user);
		}
		else {
			listUsuarioNaoVinculadosIndicacao = new ArrayList<Usuario>();
			listUsuarioNaoVinculadosIndicacao.add(user);
		}
		
	}
	
	/**
	 * Lista todos os casos cadastrados no sistema
	 * @return
	 */
	public String listarCaso(){
	try{	
		caso = new Caso();
		caso.setCliente(new Cliente());
		caso.setStatusCaso(StatusCaso.CRIADO);
		carregarListaTipoFaturamentoCaso();
		
		Calendar c = Calendar.getInstance();
		c.add(Calendar.MONTH,-3 ); //removento dois dias;
		
		dataInicioPesquisa = c.getTime();
		dataFimPesquisa = new Date();
		
		listaCaso = casoService.pesquisarCaso(caso,dataInicioPesquisa, dataFimPesquisa);
		
		//temp TODO remover
		listUsuarioTemp  = usuarioService.pesquisarUsuario(null);
		
	} catch (Exception e) {
		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro ao carregar lista de casos.", ""));
		return "/pages/inicial";
	}
				
		return "/pages/caso/listar_caso";
	}
	
	/**
	 * Realiza a pesquisa do caso de acordo com os parametros informados na tela
	 * @return
	 */
	public String pesquisarCaso(){
		
		try {
			carregarListaTipoFaturamentoCaso();
			listaCaso = casoService.pesquisarCaso(caso,dataInicioPesquisa, dataFimPesquisa);
			
			//TODO retirar quando sair do session scoped
			if (caso.getCliente() != null){
				caso.getCliente().setNome(null);
				caso.getCliente().setIdCliente(null);
			}else{
				caso.setCliente(new Cliente());
			}
			return "/pages/caso/listar_caso";
			
		} catch (Exception e) {
			e.printStackTrace();
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_FATAL, "Problemas ao listar caso", ""));
			return "/pages/inicial";
		}
		
	}
	
	/**
	 * Realiza a preparação para o cadastro de caso
	 * @return
	 */
	public String iniciarCadastrarCaso(){
		try {
			//TODO Para session scope tirar quando for request
			
			listaClienteCombo = clienteService.pesquisarCliente(new Cliente());
			listaTipoExito = CarregarCombos.carregarComboTipoExito();
			this.mostrarPorcetagemExito = false;
			
			//caso = new Caso();
			Cliente cliente = caso.getCliente();
			if (cliente != null && cliente.getIdCliente() != null){
				if (!listaClienteCombo.contains(cliente)){
					listaClienteCombo.add(cliente);
				}
			} else{
				if (listaCaso != null && listaCaso.size() > 0){
					cliente = listaCaso.get(0).getCliente();
				}
				
				
			}
			
			
			caso = new Caso();
			caso.setCliente(cliente);
			
			operacao = "Incluir";
			
			casoFixo = false;
			
			listaTipoCaso = tipoCasoService.pesquisarTipoCaso(new TipoCaso());
			
			carregarListaTipoFaturamentoCaso();
			
			carregarListaProlabole();
			
			caso.setTipoCaso(new TipoCaso());
			
			caso.setData(new Date());
			
			listarUsuariosNaoVinculadosParticipacao();
			
			listUsuarioVinculadosIndicacao = new ArrayList<Usuario>();
			
			return "/pages/caso/editar_caso";
			
		} catch (Exception e) {
			e.printStackTrace();
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_FATAL, "Problemas carregar informações de caso para cadastro", ""));
			return listarCaso();
		}
		
		
	}
	
	/**
	 * Realiza as instruções basicas para poder iniciar a alteração de caso
	 * @return
	 */
	public String iniciarAlterarCaso(){
		try {
			
			if (listaClienteCombo == null){
				listaClienteCombo = clienteService.pesquisarCliente(new Cliente());
			}
			
			operacao = "Alterar";
			
			caso = casoService.obterCaso(caso.getIdCaso());
			
			//mostrar area fixo
			
			if (caso.getTipoFaturaCaso().equals(TipoFaturaCasoEnum.FIXO)){
				casoFixo = true;
			}
			
			for (Prolabore pro : caso.getListaProlabole()) {
				if (pro.getEvento() != null && !pro.getEvento().equals("")){
					pro.setRenderEvento(true);
					pro.setRenderDataVencimento(false);
				}
				else {
					pro.setRenderEvento(false);
					pro.setRenderDataVencimento(true);
				}
			}
			
			carregarListaTipoFaturamentoCaso();
			carregarListaProlabole();
			listaTipoExito = CarregarCombos.carregarComboTipoExito();
			
			listaTipoCaso = tipoCasoService.pesquisarTipoCaso(new TipoCaso());
			
			listarUsuariosNaoVinculadosParticipacao();
			
			for (Usuario usuarioVinculado : caso.getListaUsuarioIndicacao()) {
				if (listUsuarioNaoVinculadosIndicacao.contains(usuarioVinculado)){
					listUsuarioNaoVinculadosIndicacao.remove(usuarioVinculado);
				}
			}
			listUsuarioVinculadosIndicacao = caso.getListaUsuarioIndicacao();
			
			return "/pages/caso/editar_caso";
			
		} catch (Exception e) {
			e.printStackTrace();
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_FATAL, "Problemas ao carregar caso para alteração", ""));
			return listarCaso();
		}
	}
	
	public String iniciarExcluirCaso(){
		try {
			operacao = "Excluir";
			
			listaTipoCaso = tipoCasoService.pesquisarTipoCaso(new TipoCaso());
			
			return "/pages/caso/editar_caso";
			
		} catch (Exception e) {
			e.printStackTrace();
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_FATAL, "Problemas carregar dados de caso para exclusão", ""));
			return listarCaso();
		}
	}
	
	public String cadastrarCaso(){
		try {
			caso.setAdvogado(usuarioLogadoControl.getUsuario());
			
			caso.setListaUsuarioIndicacao(listUsuarioVinculadosIndicacao);
			
			//Caso não for selecionado se tem exito, deixar null as informações
			if (previsaoExito != null && !previsaoExito){
				caso.setTipoExito(null);
				caso.setValorExito(null);
				caso.setBasePorcetegemExito(null);
			}
			
			casoService.cadastrarCaso(caso);
			
			//Para session scope...
			caso = new Caso();
			
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso ao cadastrar caso", ""));
			
			
		} catch (Exception e) {
			e.printStackTrace();
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_FATAL, "Problemas ao cadastrar caso", ""));
			
			// TODO: handle exception
		}
		
		return listarCaso();
	}
	
	public String alterarCaso(){
		try {
			caso.setListaUsuarioIndicacao(listUsuarioVinculadosIndicacao);
			casoService.alterarCaso(caso,false);
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso ao alterar caso", ""));
		}
		
		catch (ControleException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_FATAL, e.getMessage(), ""));
		}
		
		catch (Exception e) {
			e.printStackTrace();
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_FATAL, "Problemas ao alterar caso", ""));
		}
		
		return listarCaso();
	}

	public String excluirCaso(){
		
		try {
			casoService.exlcuirCaso(caso);
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso ao excluir caso", ""));
		} 
		
		catch (AlmiranteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_FATAL, e.getMessage(), ""));
		}
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_FATAL, "Problemas ao excluir caso", ""));
		}
		
		return listarCaso();
	}
	
	public String fecharCaso(){
		
		try {
			//gerar fatura somente se existir êxito
			Fatura fatura = casoService.finalizarCaso(caso);
			
			if (caso.getValorExito() != null && caso.getValorExito() > 0 && fatura != null){
				faturaExitoControl.gerarFaturaExito(fatura);
			}
			
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso ao fechar caso.", ""));
			
		} catch (AlmiranteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_FATAL, e.getMessage(), ""));
		}
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_FATAL, "Problemas ao fechar caso.", ""));
		}
		
		
		return listarCaso();
	}
	
	
	public String iniciarFecharCaso(){
		
		if (caso.getStatusCaso().equals(StatusCaso.FINALIZADO)){
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_FATAL, "Caso já encerrado.", ""));
			return null;
		}
		
		if(caso.getTipoExito() != null){
			this.previsaoExito = true;
			if (caso.getTipoExito().equals(ValorParticipacaoEnum.PORCENTAGEM)){
				this.mostrarPorcetagemExito = true;
			}
			else {
				this.mostrarPorcetagemExito = false;
			}
		}
		else {
			this.previsaoExito = false;
		}
		
		listaTipoExito = CarregarCombos.carregarComboTipoExito();
		
		listarAdvogadoNaoVinculadosExito();
		
		listaAdvogadoVinculadoExito = new ArrayList<AdvogadoValorExito>();
		
		caso = casoService.obterCaso(caso.getIdCaso());
		caso.setParcelaExito(new ParcelaExito());
		
		porcentagemDistribuicaoExitoFundo = participacaoDefaultService.obterValorPorNome(TipoParticipacaoEnum.FUNDO_EXITO);
		porcentagemDistribuicaoExitoImposto = participacaoDefaultService.obterValorPorNome(TipoParticipacaoEnum.IMPOSTO_EXITO);
		porcentagemDistribuicaoExitoIndicacao = participacaoDefaultService.obterValorPorNome(TipoParticipacaoEnum.INDICACAO_EXITO);
		porcentagemDistribuicaoExitoSocio = participacaoDefaultService.obterValorPorNome(TipoParticipacaoEnum.PARTICIPACAO_SOCIO_EXITO);
		porcentagemDistribuicaoExitoTrabalho = participacaoDefaultService.obterValorPorNome(TipoParticipacaoEnum.PARTICIPACAO_TRABALHO_EXITO);
		
		atualizarValoresDistribuicaoExito();
		
		return "/pages/caso/finalizar_caso";
	}
	
	//Inicia a tela para definir distruibuicao de fatura de exito
	public String definirDistribuicaoExito(Caso caso) {
		this.caso = casoService.obterCaso(caso.getIdCaso());
		listarAdvogadoNaoVinculadosExito();
		
		listaAdvogadoVinculadoExito = new ArrayList<AdvogadoValorExito>();
		
		caso = casoService.obterCaso(caso.getIdCaso());
		
		porcentagemDistribuicaoExitoFundo = participacaoDefaultService.obterValorPorNome(TipoParticipacaoEnum.FUNDO_EXITO);
		porcentagemDistribuicaoExitoImposto = participacaoDefaultService.obterValorPorNome(TipoParticipacaoEnum.IMPOSTO_EXITO);
		porcentagemDistribuicaoExitoIndicacao = participacaoDefaultService.obterValorPorNome(TipoParticipacaoEnum.INDICACAO_EXITO);
		porcentagemDistribuicaoExitoSocio = participacaoDefaultService.obterValorPorNome(TipoParticipacaoEnum.PARTICIPACAO_SOCIO_EXITO);
		porcentagemDistribuicaoExitoTrabalho = participacaoDefaultService.obterValorPorNome(TipoParticipacaoEnum.PARTICIPACAO_TRABALHO_EXITO);
		
		atualizarValoresDistribuicaoExito();
		
		return "/pages/caso/distribuicao_exito";
	}
	
	
	public String realizarDistribuicaoExito() {
		try {
			//TODO
			/*
			 * Deve ser usado para futuro 
			*/
			List<PreParticipacaoExito> listaPreParticipacaoExito = new ArrayList<PreParticipacaoExito>();
			for (AdvogadoValorExito dto : listaAdvogadoVinculadoExito) {
				PreParticipacaoExito p = new PreParticipacaoExito();
				p.setAdvogado(dto.getUsuario());
				p.setValorParticipacao(dto.getValorExito());
				listaPreParticipacaoExito.add(p);
				p.setCaso(caso);
//				participacaoService.cadastrarPreParticipacaoExito(p);
			}
			
			caso.setListaPreParticipacaoExito(listaPreParticipacaoExito);
			
			Fatura fatura = faturaService.obterFaturaPorParcelaExito(caso.getParcelaExito());
			
			//Setando as porcentagens...
			fatura.setPorcentagemFundo(porcentagemDistribuicaoExitoFundo);
			fatura.setPorcentagemImposto(porcentagemDistribuicaoExitoImposto);
			fatura.setPorcentagemIndicacao(porcentagemDistribuicaoExitoIndicacao);
			fatura.setPorcentagemParticipacaoSocio(porcentagemDistribuicaoExitoSocio);
			fatura.setPorcentagemParticipacaoTrabalho(porcentagemDistribuicaoExitoTrabalho);
			
			
			
			caso.setValorDistribuicaoExitoImposto(NumeroUtil.valorDaPorcentagem(caso.getValorExito(), porcentagemDistribuicaoExitoImposto));
			
			float valorSemImposto = NumeroUtil.diminuirDinheiro(caso.getValorExito(), caso.getValorDistribuicaoExitoImposto(), 3);
			
			caso.setValorDistribuicaoExitoFundo(NumeroUtil.valorDaPorcentagem(valorSemImposto, porcentagemDistribuicaoExitoFundo));
			caso.setValorDistribuicaoExitoIndicacao(NumeroUtil.valorDaPorcentagem(valorSemImposto, porcentagemDistribuicaoExitoIndicacao));
			caso.setValorDistribuicaoExitoSocio(NumeroUtil.valorDaPorcentagem(valorSemImposto, porcentagemDistribuicaoExitoSocio));
			caso.setValorDistribuicaoExitoTrabalho(NumeroUtil.valorDaPorcentagem(valorSemImposto, porcentagemDistribuicaoExitoTrabalho));
			
			
			participacaoService.cadastrarParticipacaoExito(caso,fatura);
			
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso ao realizarDistribuição", ""));
			return 	faturaControl.listarFaturas();
		} catch (Exception e) {
			e.printStackTrace();
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_FATAL, e.getMessage(), ""));
			
			return "";
		}

     }
	
	public void atualizarValoresDistribuicaoExito(){
		caso.setValorDistribuicaoExitoImposto(NumeroUtil.valorDaPorcentagem(caso.getValorExito(), porcentagemDistribuicaoExitoImposto));
		
		float valorSemImposto = NumeroUtil.diminuirDinheiro(caso.getValorExito(), caso.getValorDistribuicaoExitoImposto(), 3);
		
		caso.setValorDistribuicaoExitoFundo(NumeroUtil.valorDaPorcentagem(valorSemImposto, porcentagemDistribuicaoExitoFundo));
		caso.setValorDistribuicaoExitoIndicacao(NumeroUtil.valorDaPorcentagem(valorSemImposto, porcentagemDistribuicaoExitoIndicacao));
		caso.setValorDistribuicaoExitoSocio(NumeroUtil.valorDaPorcentagem(valorSemImposto, porcentagemDistribuicaoExitoSocio));
		caso.setValorDistribuicaoExitoTrabalho(NumeroUtil.valorDaPorcentagem(valorSemImposto, porcentagemDistribuicaoExitoTrabalho));
	}
	
	/**
	 * Responsável em adicionar os advogados para exito
	 * @return
	 */
	
	public void adicionarUsuarioExito(AdvogadoValorExito user){
		listaAdvogadoNaoVinculadoExito.remove(user);
		if (listaAdvogadoVinculadoExito != null){
			listaAdvogadoVinculadoExito.add(user);
		}
		else {
			listaAdvogadoVinculadoExito = new ArrayList<AdvogadoValorExito>();
			listaAdvogadoVinculadoExito.add(user);
		}
		
	}
	
	/**
	 * Responsável em remover os advogados no caso do êxito
	 * @return
	 */
	
	public void removerUsuarioExito(AdvogadoValorExito user){
		listaAdvogadoVinculadoExito.remove(user);
		if (listaAdvogadoNaoVinculadoExito != null){
			listaAdvogadoNaoVinculadoExito.add(user);
		}
		else {
			listaAdvogadoNaoVinculadoExito = new ArrayList<AdvogadoValorExito>();
			listaAdvogadoNaoVinculadoExito.add(user);
		}
		
	}
	
	
	
	private void carregarListaTipoFaturamentoCaso(){
		this.listaTipoFaturamento = new ArrayList<TipoFaturaCasoEnum>();
		this.listaTipoFaturamento.add(TipoFaturaCasoEnum.VARIAVEL);
		this.listaTipoFaturamento.add(TipoFaturaCasoEnum.FIXO);
		
	}
	
	private void carregarListaProlabole(){
		this.listaProlaboleEnum = new ArrayList<ProlaboreEnum>();
		this.listaProlaboleEnum.add(ProlaboreEnum.EVENTO);
		this.listaProlaboleEnum.add(ProlaboreEnum.VENCIMENTO);
		
	}
	
	
	public void selecionarTipoFaturamento(ActionEvent actionEvent){
		System.out.println("AQUIII");
		if(caso.getTipoFaturaCaso().equals(TipoFaturaCasoEnum.FIXO)){
			casoFixo = true;
			if (caso.getListaProlabole() == null){
				caso.setListaProlabole(new ArrayList<Prolabore>());
				addProalbole(null);
			}
		}
		else {
			casoFixo = false;
		}
		
	}
	
	
	public void addProalbole(ActionEvent actionEvent){
		
		System.out.println("Adicionando prolabole na lista...");
		int novaOrdem = this.caso.getListaProlabole().size();
		int ultimaOrdem = this.caso.getListaProlabole().size();
		
		novaOrdem++;
		
		Prolabore p = new Prolabore(novaOrdem);
		
		//verificar se existem outros prolabores na lista, se existir seguir o mesmo padrão
		
		if (ultimaOrdem > 0){
			int ultimoIndex = ultimaOrdem - 1;
				
			//se tiver data de vencimento, ultimo prolabore tem data seguir o mesmo padrão
			if (caso.getListaProlabole().get(ultimoIndex).getDataVencimento() != null){
				
				Date dataUltimaOrdem = caso.getListaProlabole().get(ultimoIndex).getDataVencimento();
				
				p.setCaso(caso);
				
				p.setRenderEvento(false);
				p.setRenderDataVencimento(true);
				
				p.setDataVencimento(DateUtil.somaMeses(dataUltimaOrdem, 1));
				p.setStatusProlaboreEnum(StatusTrabalho.CRIADO);
				p.setProlaboreEnum(ProlaboreEnum.VENCIMENTO);
			} else {
				p.setCaso(caso);
				p.setRenderEvento(true);
				p.setStatusProlaboreEnum(StatusTrabalho.CRIADO);
				p.setProlaboreEnum(ProlaboreEnum.EVENTO);
			}
		}
		else {
			p.setCaso(caso);
			p.setRenderEvento(true);
			p.setStatusProlaboreEnum(StatusTrabalho.CRIADO);
			p.setProlaboreEnum(ProlaboreEnum.EVENTO);
		}
		
		this.caso.getListaProlabole().add(p);
		
	}
	
	//TODO retirar método não está sendo usado mais
	//pois não existe mais conceito de parcelas de exito
	
//	public void addParcelaExito(ActionEvent actionEvent){
//		
//		System.out.println("Adicionando Parcela exito na lista...");
//		int ordem = this.caso.getListaParcelaExito().size();
//		ordem++;
//		
//		ParcelaExito p = new ParcelaExito(ordem);
//		p.setCaso(caso);
//		p.setStatusParcela(StatusTrabalho.CRIADO);
//		
//		this.caso.getListaParcelaExito().add(p);
//		
//		
//	}
//	
	
	public void alterarTipoVencimento(Prolabore pr){
		
		System.out.println("ALTERANDO TIPO VENCIMENTO.....");
		
		for (Prolabore p : this.caso.getListaProlabole()) {
			if (p.equals(pr)){
				p.setProlaboreEnum(pr.getProlaboreEnum());
				if (pr.getProlaboreEnum().equals(ProlaboreEnum.EVENTO)){
					p.setRenderEvento(true);
					p.setRenderDataVencimento(false);
					p.setDataVencimento(null);
				}
				else {
					p.setRenderEvento(false);
					p.setEvento(null);
					p.setRenderDataVencimento(true);
				}
			}
		}
		
		
	}
	
	public void tipoExitoListener(){
		mostrarPorcetagemExito = false;
		if (caso.getTipoExito()!=null && caso.getTipoExito().equals(ValorParticipacaoEnum.PORCENTAGEM)){
			mostrarPorcetagemExito = true;
		}
	}
	
	public void porcetagemExitoListener() {
		if (caso.getBasePorcetegemExito() != null && !caso.getBasePorcetegemExito().equals("") && caso.getValorPorcetagemExito() != null && !caso.getValorPorcetagemExito().equals("") ){
			caso.setValorExito(NumeroUtil.valorDaPorcentagem(caso.getBasePorcetegemExito(), caso.getValorPorcetagemExito()));
		}
		
	}
	
	public Caso getCaso() {
		return caso;
	}

	public void setCaso(Caso caso) {
		this.caso = caso;
	}

	public List<Caso> getListaCaso() {
		return listaCaso;
	}

	public void setListaCaso(List<Caso> listaCaso) {
		this.listaCaso = listaCaso;
	}

	public String getOperacao() {
		return operacao;
	}

	public void setOperacao(String operacao) {
		this.operacao = operacao;
	}

	public List<TipoCaso> getListaTipoCaso() {
		return listaTipoCaso;
	}

	public void setListaTipoCaso(List<TipoCaso> listaTipoCaso) {
		this.listaTipoCaso = listaTipoCaso;
	}

	public String getName() {
		if (caso.getTempo() != null && caso.getValorHora()!= null){
			Float t = caso.getTempo() * caso.getValorHora();
			return t.toString();
		}
	
		else {
			return "0";
		}
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



	public List<Usuario> getListUsuarioNaoVinculadosIndicacao() {
		return listUsuarioNaoVinculadosIndicacao;
	}



	public void setListUsuarioNaoVinculadosIndicacao(
			List<Usuario> listUsuarioNaoVinculadosIndicacao) {
		this.listUsuarioNaoVinculadosIndicacao = listUsuarioNaoVinculadosIndicacao;
	}


	public List<TipoFaturaCasoEnum> getListaTipoFaturamento() {
		return listaTipoFaturamento;
	}


	public void setListaTipoFaturamento(
			List<TipoFaturaCasoEnum> listaTipoFaturamento) {
		this.listaTipoFaturamento = listaTipoFaturamento;
	}


	public List<Usuario> getListUsuarioVinculadosIndicacao() {
		return listUsuarioVinculadosIndicacao;
	}


	public void setListUsuarioVinculadosIndicacao(
			List<Usuario> listUsuarioVinculadosIndicacao) {
		this.listUsuarioVinculadosIndicacao = listUsuarioVinculadosIndicacao;
	}


	public Usuario getUsuarioIndicacao() {
		return usuarioIndicacao;
	}


	public void setUsuarioIndicacao(Usuario usuarioIndicacao) {
		this.usuarioIndicacao = usuarioIndicacao;
	}


	public Boolean getCasoFixo() {
		return casoFixo;
	}


	public void setCasoFixo(Boolean casoFixo) {
		this.casoFixo = casoFixo;
	}



	public List<Usuario> getListUsuarioTemp() {
		return listUsuarioTemp;
	}


	public void setListUsuarioTemp(List<Usuario> listUsuarioTemp) {
		this.listUsuarioTemp = listUsuarioTemp;
	}


	public Integer getCasoTempIndicacao() {
		return casoTempIndicacao;
	}


	public void setCasoTempIndicacao(Integer casoTempIndicacao) {
		this.casoTempIndicacao = casoTempIndicacao;
	}


	public Boolean getStatusCheckBox() {
		return statusCheckBox;
	}


	public void setStatusCheckBox(Boolean statusCheckBox) {
		this.statusCheckBox = statusCheckBox;
	}


	public List<ProlaboreEnum> getListaProlaboleEnum() {
		return listaProlaboleEnum;
	}


	public void setListaProlaboleEnum(List<ProlaboreEnum> listaProlaboleEnum) {
		this.listaProlaboleEnum = listaProlaboleEnum;
	}


	public Prolabore getProlaboleManutencao() {
		return prolaboleManutencao;
	}


	public void setProlaboleManutencao(Prolabore prolaboleManutencao) {
		this.prolaboleManutencao = prolaboleManutencao;
	}


	public Integer getContadorProlabole() {
		return contadorProlabole;
	}


	public void setContadorProlabole(Integer contadorProlabole) {
		this.contadorProlabole = contadorProlabole;
	}


	public List<Cliente> getListaClienteCombo() {
		return listaClienteCombo;
	}


	public void setListaClienteCombo(List<Cliente> listaClienteCombo) {
		this.listaClienteCombo = listaClienteCombo;
	}


	public List<ValorParticipacaoEnum> getListaTipoExito() {
		return listaTipoExito;
	}


	public void setListaTipoExito(List<ValorParticipacaoEnum> listaTipoExito) {
		this.listaTipoExito = listaTipoExito;
	}


	public List<AdvogadoValorExito> getListaAdvogadoNaoVinculadoExito() {
		return listaAdvogadoNaoVinculadoExito;
	}


	public void setListaAdvogadoNaoVinculadoExito(
			List<AdvogadoValorExito> listaAdvogadoNaoVinculadoExito) {
		this.listaAdvogadoNaoVinculadoExito = listaAdvogadoNaoVinculadoExito;
	}


	public List<AdvogadoValorExito> getListaAdvogadoVinculadoExito() {
		return listaAdvogadoVinculadoExito;
	}


	public void setListaAdvogadoVinculadoExito(
			List<AdvogadoValorExito> listaAdvogadoVinculadoExito) {
		this.listaAdvogadoVinculadoExito = listaAdvogadoVinculadoExito;
	}


	public Float getPorcentagemDistribuicaoExitoSocio() {
		return porcentagemDistribuicaoExitoSocio;
	}


	public void setPorcentagemDistribuicaoExitoSocio(
			Float porcentagemDistribuicaoExitoSocio) {
		this.porcentagemDistribuicaoExitoSocio = porcentagemDistribuicaoExitoSocio;
	}


	public Float getPorcentagemDistribuicaoExitoTrabalho() {
		return porcentagemDistribuicaoExitoTrabalho;
	}


	public void setPorcentagemDistribuicaoExitoTrabalho(
			Float porcentagemDistribuicaoExitoTrabalho) {
		this.porcentagemDistribuicaoExitoTrabalho = porcentagemDistribuicaoExitoTrabalho;
	}


	public Float getPorcentagemDistribuicaoExitoIndicacao() {
		return porcentagemDistribuicaoExitoIndicacao;
	}


	public void setPorcentagemDistribuicaoExitoIndicacao(
			Float porcentagemDistribuicaoExitoIndicacao) {
		this.porcentagemDistribuicaoExitoIndicacao = porcentagemDistribuicaoExitoIndicacao;
	}


	public Float getPorcentagemDistribuicaoExitoImposto() {
		return porcentagemDistribuicaoExitoImposto;
	}


	public void setPorcentagemDistribuicaoExitoImposto(
			Float porcentagemDistribuicaoExitoImposto) {
		this.porcentagemDistribuicaoExitoImposto = porcentagemDistribuicaoExitoImposto;
	}


	public Float getPorcentagemDistribuicaoExitoFundo() {
		return porcentagemDistribuicaoExitoFundo;
	}


	public void setPorcentagemDistribuicaoExitoFundo(
			Float porcentagemDistribuicaoExitoFundo) {
		this.porcentagemDistribuicaoExitoFundo = porcentagemDistribuicaoExitoFundo;
	}


	public Boolean getPrevisaoExito() {
		return previsaoExito;
	}


	public void setPrevisaoExito(Boolean previsaoExito) {
		this.previsaoExito = previsaoExito;
	}


	public Boolean getMostrarPorcetagemExito() {
		return mostrarPorcetagemExito;
	}


	public void setMostrarPorcetagemExito(Boolean mostrarPorcetagemExito) {
		this.mostrarPorcetagemExito = mostrarPorcetagemExito;
	}


}
