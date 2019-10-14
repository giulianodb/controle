package control;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletResponse;

import service.ClienteService;
import service.FaturaService;
import service.ParticipacaoDefaultService;
import service.ParticipacaoService;
import service.ProlaboreService;
import service.UsuarioService;
import util.NumeroUtil;
import entity.Cliente;
import entity.Fatura;
import entity.ParticipacaoDefault;
import entity.PreParticipacaoFixo;
import entity.Prolabore;
import entity.StatusTrabalho;
import entity.TipoParticipacaoEnum;
import entity.Usuario;
import entity.ValorParticipacaoEnum;
import exception.ControleException;

@Named
@SessionScoped
public class FaturaFixoControl implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 441317418357476411L;

	/**
	 * 
	 */
	
	private List<Prolabore> listProlabore;
	
	private Date dataInicioPesquisa;
	
	private Date dataFimPesquisa;
	
	private List<Cliente> listaClienteCombo;
	
	private Prolabore prolaborePesquisa;
	
	private Prolabore prolaboreGerarFatura;
	
	private Boolean eventoPesquisa;
	
	private Integer idClienteComboPesquisa;
	@EJB
	private FaturaService faturaService;
	@EJB
	private ProlaboreService prolaboreService;
	
	@EJB
	private ClienteService clienteService;
	
	@EJB
	private UsuarioService usuarioService;
	
	@EJB
	private ParticipacaoDefaultService participacaoDefaultService;
	
	@EJB
	private ParticipacaoService participacaoService;
	
	private Boolean statusCheckBox;
	
	//Atributos para controlar pre participaçãoE
	
	@Inject
	private Prolabore prolaborePreParticipacao;
	
	private List<ValorParticipacaoEnum> listValorParticipacaoEnum;
	
	//usado para mostrar valor total de participação no cadastro da pre participação
	private float valorParticipacaoTrabalho;
	
	private float saldoParticipacao;
	
	private Date dataVencimentoEvento;
	
	@Inject
	private FaturaControl faturaControl;
	
	/**
	 * Pesquisa dos clientes com faturas variaveis para serem faturados
	 * @return
	 */
	
	public String pesquisarEmissaoFaturaFixo(){
		carregarComboCliente();
		listProlabore = prolaboreService.pesquisarProlabore(idClienteComboPesquisa, dataInicioPesquisa, dataFimPesquisa, eventoPesquisa);
		
		
		
		return "/pages/faturamento/listar_faturar_fixo";
	}
	
	public void carregarComboCliente(){
		if (listaClienteCombo == null || listaClienteCombo.size() == 0){
			listaClienteCombo = clienteService.pesquisarCliente(new Cliente());
		}
		
	}
	
	public Boolean mostrarListaFaturamentoFixo(){
		
		if(listProlabore != null && listProlabore.size() > 0){
			return true;
		}
		else {
			return false;
		}
		
	}
	
	//usado para setar prolabore selecionado para realizar as operações de pre participacao
	public void carregarEditarPreParticipacao(Prolabore p){
		try {
			this.prolaborePreParticipacao = p;
			
			carregarPreParticiapacao(p);
			
			carregarComboPreParticipacao();
			
//			carregarPorcentagemDistribuicao();
			
			carregarValoresPreParticipacao();
			
			if (this.prolaborePreParticipacao.getValorParticipacaoEnum() == null){
				this.prolaborePreParticipacao.setValorParticipacaoEnum(ValorParticipacaoEnum.PORCENTAGEM);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro ao carregar participações.", ""));// TODO: handle exception
		}
		
	}
	
	//Responsavel em popular os campos de 	private DecimalFormat valorParticipacaoTrabalho; private DecimalFormat saldoParticipacao;
	private void carregarValoresPreParticipacao() {
		// TODO Auto-generated method stub
		Float valorPreParticipacaoSemImposto = participacaoService.obterValorTotalTrabalhoSemImposto(prolaborePreParticipacao.getValor().floatValue(),prolaborePreParticipacao.getPorcentagemImposto());
		valorParticipacaoTrabalho = participacaoService.obterValorParticipacaoTrabalho
				(prolaborePreParticipacao.getPorcentagemParticipacaoTrabalho(), valorPreParticipacaoSemImposto, 0f, false, 0);
		
		if (prolaborePreParticipacao.getListaPreParticipacaoFixos() == null){
			saldoParticipacao = prolaborePreParticipacao.getValor().floatValue();
		}
		else {
			Float soma = 0f;
			for (PreParticipacaoFixo p : prolaborePreParticipacao.getListaPreParticipacaoFixos() ) {
				
				if (p.getValorParticipacao() != null){
					soma = NumeroUtil.somarDinheiro(soma, p.getValorParticipacao(), 2);
				}
			}
			
			if (prolaborePreParticipacao.getValorParticipacaoEnum() != null ){
				if (prolaborePreParticipacao.getValorParticipacaoEnum().equals(ValorParticipacaoEnum.PORCENTAGEM)) {
					saldoParticipacao = NumeroUtil.diminuirDinheiro(100F, soma, 2);
				}
			}
			else {
				saldoParticipacao = NumeroUtil.diminuirDinheiro(valorParticipacaoTrabalho, soma, 2);
			}
			
					
		}
		
	}
	
	
	/**
	 * Adiciona criterio de trabalho na parte das pendencias
	 * @return
	 */
	public String adicionarCriterioTrabalho(Integer idFatura){
		prolaborePreParticipacao = prolaboreService.obterProlaborePorFatura(idFatura);
		
		carregarPreParticiapacao(prolaborePreParticipacao);
		
		carregarComboPreParticipacao();
		
//		carregarPorcentagemDistribuicao();
		
		carregarValoresPreParticipacao();
		
		if (this.prolaborePreParticipacao.getValorParticipacaoEnum() == null){
			this.prolaborePreParticipacao.setValorParticipacaoEnum(ValorParticipacaoEnum.PORCENTAGEM);
		}
		
		return "/pages/faturamento/incluir_criterio_trabalho_fixo";
	}
	
	private void carregarPorcentagemDistribuicao() {
		// TODO Auto-generated method stub
		List<ParticipacaoDefault> listarParticipacaoDefault = participacaoDefaultService.listarParticipacaoDefault();
		for (ParticipacaoDefault p : listarParticipacaoDefault) {
			if (p.getNome().equals(TipoParticipacaoEnum.FUNDO)){
				prolaborePreParticipacao.setPorcentagemFundo(p.getValor());
			}
			else if(p.getNome().equals(TipoParticipacaoEnum.IMPOSTO)){
				prolaborePreParticipacao.setPorcentagemImposto(p.getValor());
			}
			else if(p.getNome().equals(TipoParticipacaoEnum.INDICACAO)){
				prolaborePreParticipacao.setPorcentagemIndicacao(p.getValor());
			}
			else if(p.getNome().equals(TipoParticipacaoEnum.PARTICIPACAO_SOCIO)){
				prolaborePreParticipacao.setPorcentagemParticipacaoSocio(p.getValor());
			}
			else if(p.getNome().equals(TipoParticipacaoEnum.PARTICIPACAO_TRABALHO)){
				prolaborePreParticipacao.setPorcentagemParticipacaoTrabalho(p.getValor());
			}
			
		}
		
		
	}

	//carregar o tipo de valor para pre participação do prolabore
	private void carregarComboPreParticipacao() {
		
		if (listValorParticipacaoEnum == null || listValorParticipacaoEnum.size() == 0){
			listValorParticipacaoEnum = new ArrayList<ValorParticipacaoEnum>();
			listValorParticipacaoEnum.add(ValorParticipacaoEnum.PORCENTAGEM);
//			listValorParticipacaoEnum.add(ValorParticipacaoEnum.VALOR);
		}
		
	}

	//Carrega  a lista de usuários para a preParticipação
	private void carregarPreParticiapacao(Prolabore p) {
		List<Usuario> usuarios = usuarioService.listarAdvogados();
		if (p.getListaPreParticipacaoFixos() == null){
			List<PreParticipacaoFixo> listaPre = new ArrayList<PreParticipacaoFixo>();
			for (Usuario usuario : usuarios) {
				PreParticipacaoFixo ppf = new PreParticipacaoFixo();
				ppf.setAdvogado(usuario);
				listaPre.add(ppf);
			}
		}
		else {
			List<PreParticipacaoFixo> listaPre = new ArrayList<PreParticipacaoFixo>();
			for (Usuario usuario : usuarios) {
				boolean inserirUser = true;
				for(PreParticipacaoFixo pre: p.getListaPreParticipacaoFixos()){
					if (pre.getAdvogado().getNome().equals(usuario.getNome())){
						inserirUser = false;
						break;
					}
					
				}
				
				if (inserirUser){
					PreParticipacaoFixo ppf = new PreParticipacaoFixo();
					ppf.setAdvogado(usuario);
					listaPre.add(ppf);
				}
				
				
			}
			
			p.getListaPreParticipacaoFixos().addAll(listaPre);
			
		}
		
	}
	
	public void salvarPreParticipacao(){
		try {
			removerPreParticipacoes();
			prolaboreService.salvarProlaboreComPreParticipacao(prolaborePreParticipacao);
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso ao cadastrar participações.", ""));
		} catch (Exception e) {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro ao salvar participação.", ""));// TODO: handle exception
		}
		
	}
	
	public String salvarPreParticipacaoRetorno(){
		try {
			
			if(saldoParticipacao != 0f) {
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Necessário ajustar os valores de distribuição.", ""));// TODO: handle exception
				return null;
			}
			
			removerPreParticipacoes();
			prolaboreService.salvarProlaboreComPreParticipacao(prolaborePreParticipacao);
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso ao cadastrar distribuição.", ""));
			
			return faturaControl.pesquisarFatura();
		} catch (Exception e) {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro ao salvar participação.", ""));// TODO: handle exception
			return null;
		}
		
	}
	
	
	public void removerPreParticipacoes(){
		removerPreParticipacoes(prolaborePreParticipacao);
	}
	
	public void removerPreParticipacoes(Prolabore p){
		//removendo participações vazias...
		List<PreParticipacaoFixo> listRemover = new ArrayList<PreParticipacaoFixo>();
		for (PreParticipacaoFixo pre : p.getListaPreParticipacaoFixos()) {
			if (pre.getValorParticipacao() == null || pre.getValorParticipacao().equals(0f)){
				listRemover.add(pre);
			} else {
				pre.setProlabore(p);
			} 
			
		}		
		p.getListaPreParticipacaoFixos().removeAll(listRemover);
	}
	
	public String gerarFaturaFixo(){
		
		try {
			if (dataVencimentoEvento != null){
				prolaboreGerarFatura.setDataVencimento(dataVencimentoEvento);
			}
			removerPreParticipacoes(prolaboreGerarFatura);
			Fatura fatura = faturaService.gerarFaturaFixo(prolaboreGerarFatura);
			this.gerarRelatorioFaturaFixo(fatura, true);
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso ao gerar fatura.", ""));// TODO: handle exception
			dataVencimentoEvento = null;
		} 
		catch (ControleException e) {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getMessage(), ""));// TODO: handle exception
		}
		
		catch (Exception e) {
			// TODO Auto-generated catch block
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro ao gerar relatório.", ""));// TODO: handle exception
			e.printStackTrace();
		}
		return pesquisarEmissaoFaturaFixo();
		
	}
	
	
	public String gerarFaturaVisualizacaoFixo(){
		
		try {
			
			Fatura fatura = new Fatura();
			fatura.setListaProlabore(new ArrayList<Prolabore>());
			fatura.getListaProlabore().add(prolaboreGerarFatura);
			fatura.setCliente(prolaboreGerarFatura.getCaso().getCliente());
			fatura.setMesReferencia(" ");
			this.gerarRelatorioFaturaFixo(fatura, false);
			
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso ao gerar fatura de visualização.", ""));// TODO: handle exception
			dataVencimentoEvento = null;
		} 
		catch (ControleException e) {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getMessage(), ""));// TODO: handle exception
		}
		
		catch (Exception e) {
			// TODO Auto-generated catch block
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro ao gerar fatura de visualização.", ""));// TODO: handle exception
			e.printStackTrace();
		}
		return pesquisarEmissaoFaturaFixo();
		
	}
	
	public void gerarRelatorioFaturaFixo(Fatura fatura, Boolean faturamentoFinal){
		try {
			
			ByteArrayOutputStream byteArrayOutputStream  = faturaService.gerarRelatorioFaturaFixo(fatura, faturamentoFinal);
			
			FacesContext facesContext = FacesContext.getCurrentInstance();

			HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();
			response.setContentType("application/pdf");
			response.addHeader("Content-disposition","attachment; filename=\" "+fatura.getCliente().getNome()+"_"+fatura.getMesReferencia() + ".pdf\"");
			
			
			OutputStream os = null;
			os = response.getOutputStream();

			
			byteArrayOutputStream.writeTo(os);  
			os.flush();  
			os.close();  
			byteArrayOutputStream.close();  

			FacesContext.getCurrentInstance().responseComplete();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public void atualizarSaldoPrePart(){
		float soma = 0f;
		System.out.println("Olá!!");
		boolean porcentagem = prolaborePreParticipacao.getValorParticipacaoEnum().equals(ValorParticipacaoEnum.PORCENTAGEM);
		
		for (PreParticipacaoFixo pre : prolaborePreParticipacao.getListaPreParticipacaoFixos()) {
			
			float valorParticipacao = 0;
			if (pre.getValorParticipacao() != null ){
				if (porcentagem){
					valorParticipacao = NumeroUtil.porcentagem(valorParticipacaoTrabalho, pre.getValorParticipacao());
				}
				else {
					valorParticipacao = pre.getValorParticipacao();
				}
				soma = NumeroUtil.somarDinheiro(soma,valorParticipacao , 3);
			}
		}
		
		saldoParticipacao = NumeroUtil.diminuirDinheiro(valorParticipacaoTrabalho, soma, 2);
		
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


	public Boolean getStatusCheckBox() {
		return statusCheckBox;
	}

	public void setStatusCheckBox(Boolean statusCheckBox) {
		this.statusCheckBox = statusCheckBox;
	}

	public List<Cliente> getListaClienteCombo() {
		return listaClienteCombo;
	}

	public void setListaClienteCombo(List<Cliente> listaClienteCombo) {
		this.listaClienteCombo = listaClienteCombo;
	}

	public List<Prolabore> getListProlabore() {
		return listProlabore;
	}

	public void setListProlabore(List<Prolabore> listProlabore) {
		this.listProlabore = listProlabore;
	}

	public Prolabore getProlaborePesquisa() {
		return prolaborePesquisa;
	}

	public void setProlaborePesquisa(Prolabore prolaborePesquisa) {
		this.prolaborePesquisa = prolaborePesquisa;
	}

	public Boolean getEventoPesquisa() {
		return eventoPesquisa;
	}

	public void setEventoPesquisa(Boolean eventoPesquisa) {
		this.eventoPesquisa = eventoPesquisa;
	}

	public Integer getIdClienteComboPesquisa() {
		return idClienteComboPesquisa;
	}

	public void setIdClienteComboPesquisa(Integer idClienteComboPesquisa) {
		this.idClienteComboPesquisa = idClienteComboPesquisa;
	}

	public Prolabore getProlaborePreParticipacao() {
		return prolaborePreParticipacao;
	}

	public void setProlaborePreParticipacao(Prolabore prolaborePreParticipacao) {
		this.prolaborePreParticipacao = prolaborePreParticipacao;
	}

	public List<ValorParticipacaoEnum> getListValorParticipacaoEnum() {
		return listValorParticipacaoEnum;
	}

	public void setListValorParticipacaoEnum(
			List<ValorParticipacaoEnum> listValorParticipacaoEnum) {
		this.listValorParticipacaoEnum = listValorParticipacaoEnum;
	}

	public float getValorParticipacaoTrabalho() {
		return valorParticipacaoTrabalho;
	}

	public void setValorParticipacaoTrabalho(float valorParticipacaoTrabalho) {
		this.valorParticipacaoTrabalho = valorParticipacaoTrabalho;
	}

	public float getSaldoParticipacao() {
		return saldoParticipacao;
	}

	public void setSaldoParticipacao(float saldoParticipacao) {
		this.saldoParticipacao = saldoParticipacao;
	}

	public Prolabore getProlaboreGerarFatura() {
		return prolaboreGerarFatura;
	}

	public void setProlaboreGerarFatura(Prolabore prolaboreGerarFatura) {
		this.prolaboreGerarFatura = prolaboreGerarFatura;
	}

	public Date getDataVencimentoEvento() {
		return dataVencimentoEvento;
	}

	public void setDataVencimentoEvento(Date dataVencimentoEvento) {
		this.dataVencimentoEvento = dataVencimentoEvento;
	}


	

	
}
