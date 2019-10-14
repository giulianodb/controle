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
import javax.faces.model.SelectItem;
import javax.inject.Inject;
import javax.inject.Named;

import service.ClienteService;
import service.ParticipacaoService;
import service.UsuarioService;
import util.NumeroUtil;
import dto.participacao.DetalhesParticipacaoAdvogadoDTO;
import dto.participacao.ItemParticipacaoUsuarioDTO;
import dto.participacao.ParticipacaoFaturaDTO;
import dto.participacao.ParticipacaoUsuarioDTO;
import dto.participacao.ResumoParticipacaoAdvogadoDTO;
import entity.Caso;
import entity.Cliente;
import entity.Fatura;
import entity.Participacao;
import entity.Usuario;
import exception.ControleException;

@Named
@SessionScoped
public class ParticipacaoControl implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -2122840295784888102L;

	private ParticipacaoFaturaDTO participacaoFaturaDTO;
	
	@EJB
	private ParticipacaoService participacaoService;
	
	@EJB
	private UsuarioService usuarioService;
	
	@EJB
	private ClienteService clienteService;
	
	@Inject
	private FaturaControl faturaControl;
	
	private List<Participacao> listParticipacao;
	
	private List<ParticipacaoUsuarioDTO> listParticipacaoUsuarioDTO;
	
	private List<SelectItem> testeItem;
	
	//atributos utilizados para a popup
	
	private ItemParticipacaoUsuarioDTO itemPesquisaParticipacao;
	
	//atributos para pesquisa de resumo
	
	private List<Usuario> listaAdvogadosCombo;
	
	private List<Cliente> listaClienteCombo;
	
	private List<Caso> listaCasoCombo;
	
	private Date dataInicioPesquisa;
	
	private Date dataFimPesquisa;
	
	private Usuario usuarioPesquisa;
	
	private Cliente clientePesquisa = new Cliente();
	
	private Caso casoPesquisa = new Caso();
	
	private Integer idAdvogadoPesquisa;
	
	private Integer idClientePesquisa;
	
	private Integer idCasoPesquisa;
	
	private List<DetalhesParticipacaoAdvogadoDTO> listaDetalhesParticipacaoAdvogadoDTO;
	
	private List<ResumoParticipacaoAdvogadoDTO> listaResumoParticipacaoAdvogadoDTO = new ArrayList<ResumoParticipacaoAdvogadoDTO>();
	
	//atributo para selecionar vários
	private Boolean statusCheckBox;
	
	//usado para valor total de transferência para advogado.
	private Float valorTransferenciaAdvogado;
	
	public void listarParticipacaoPorFatura(Fatura fatura){
		
		listParticipacao = participacaoService.listarParticipacaoPorFatura(fatura.getIdFatura());
	}
	
	/**
	 * Lista de usuários para a participação
	 * @return
	 */
	public String pesquisarUsuarioParticipacao(){
		try {
			if(itemPesquisaParticipacao == null){
				itemPesquisaParticipacao = new ItemParticipacaoUsuarioDTO();
			}
			if (faturaControl.getListaStatusFaturaPesquisa() == null) {
				faturaControl.popularListaStatus();
			}
			
			//Lista todos os usuários com as informações de valor pendente e valores disponíveis
			this.listParticipacaoUsuarioDTO = usuarioService.listarSituacaoParticipacaoUsuario();
			
		} catch (Exception e) {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro ao carregar lista de usuário", ""));
			return "/pages/inicial";
		}
		
		return "/pages/participacao/listar_participacao_usuario"; 
	}
	
  
	public String pesquisarDetalhesParticipacaoPorAdvogado(){
		//popular combo
		
		listaDetalhesParticipacaoAdvogadoDTO = participacaoService.pesquisarParticipacao(idClientePesquisa, idAdvogadoPesquisa, dataInicioPesquisa, dataFimPesquisa);
		
		statusCheckBox = false;
		
		return "/pages/participacao/listar_detalhes_participacao_usuario_fatura";
	}
	
	public String iniciarPesquisaDetalhesParticipacao(){
		return iniciarPesquisaDetalhesParticipacao(0);
	}
	
	public String iniciarPesquisaDetalhesParticipacao(Integer idAdvogado){
		statusCheckBox = false;
		idAdvogadoPesquisa = idAdvogado;
		idClientePesquisa = 0;
		
		listaDetalhesParticipacaoAdvogadoDTO = new ArrayList<DetalhesParticipacaoAdvogadoDTO>();
		
		listaClienteCombo = clienteService.pesquisarCliente(new Cliente());
		listaAdvogadosCombo = usuarioService.pesquisarUsuario(new Usuario());
		
		
		return "/pages/participacao/listar_detalhes_participacao_usuario_fatura";
	}
	
	//TODO remover, antiga forma de pagar os advogados que deve ser removida
	public String pagarListaParticipacao(){
		try {
//			participacaoService.pagarParticipacao(listParticipacaoUsuarioDTO);
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso ao realizar pagamentos.", ""));
		}
		catch(ControleException e) {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getMessage(), ""));
		}
		catch (Exception e) {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Problemas ao realizar pagamamento.", ""));
			e.printStackTrace();
		}
		
		return pesquisarUsuarioParticipacao();
	}
	
	
	public String pagarParticipacao(){
		try {
			participacaoService.pagarParticipacao(listaDetalhesParticipacaoAdvogadoDTO,idClientePesquisa, idAdvogadoPesquisa);
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso ao realizar pagamentos.", ""));
		}
		catch(ControleException e) {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getMessage(), ""));
		}
		catch (Exception e) {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Problemas ao realizar pagamamento.", ""));
			e.printStackTrace();
		}
		
		return iniciarPesquisaDetalhesParticipacao();
	}
	
	//TODO remover quando a tela de listagem não existir mais...
	//Responsável em selecionar toda a lista de participações
	public void selecionarListaParticipacao (ActionEvent actionEvent){
		
		for (ParticipacaoUsuarioDTO participacaoDTO : listParticipacaoUsuarioDTO) {
			participacaoDTO.setDtoSelecionado(statusCheckBox);
		}
		
	}
	

	
	public void selecionarParticipacaoPagamento (ActionEvent actionEvent){
		
		for (DetalhesParticipacaoAdvogadoDTO dto : listaDetalhesParticipacaoAdvogadoDTO) {
			dto.setDtoSelecionada(statusCheckBox);
		}
		
	}
	
	public ParticipacaoFaturaDTO getParticipacaoFaturaDTO() {
		return participacaoFaturaDTO;
	}


	public void setParticipacaoFaturaDTO(ParticipacaoFaturaDTO participacaoFaturaDTO) {
		this.participacaoFaturaDTO = participacaoFaturaDTO;
	}

	public List<Participacao> getListParticipacao() {
		return listParticipacao;
	}

	public void setListParticipacao(List<Participacao> listParticipacao) {
		this.listParticipacao = listParticipacao;
	}

	public List<ParticipacaoUsuarioDTO> getListParticipacaoUsuarioDTO() {
		return listParticipacaoUsuarioDTO;
	}

	public void setListParticipacaoUsuarioDTO(
			List<ParticipacaoUsuarioDTO> listParticipacaoUsuarioDTO) {
		this.listParticipacaoUsuarioDTO = listParticipacaoUsuarioDTO;
	}

	public ItemParticipacaoUsuarioDTO getItemPesquisaParticipacao() {
		return itemPesquisaParticipacao;
	}

	public void setItemPesquisaParticipacao(
			ItemParticipacaoUsuarioDTO itemPesquisaParticipacao) {
		this.itemPesquisaParticipacao = itemPesquisaParticipacao;
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

	public Usuario getUsuarioPesquisa() {
		return usuarioPesquisa;
	}

	public void setUsuarioPesquisa(Usuario usuarioPesquisa) {
		this.usuarioPesquisa = usuarioPesquisa;
	}

	public Cliente getClientePesquisa() {
		return clientePesquisa;
	}

	public void setClientePesquisa(Cliente clientePesquisa) {
		this.clientePesquisa = clientePesquisa;
	}

	public Caso getCasoPesquisa() {
		return casoPesquisa;
	}

	public void setCasoPesquisa(Caso casoPesquisa) {
		this.casoPesquisa = casoPesquisa;
	}

	public List<ResumoParticipacaoAdvogadoDTO> getListaResumoParticipacaoAdvogadoDTO() {
		return listaResumoParticipacaoAdvogadoDTO;
	}

	public void setListaResumoParticipacaoAdvogadoDTO(
			List<ResumoParticipacaoAdvogadoDTO> listaResumoParticipacaoAdvogadoDTO) {
		this.listaResumoParticipacaoAdvogadoDTO = listaResumoParticipacaoAdvogadoDTO;
	}

	public List<SelectItem> getTesteItem() {
		return testeItem;
	}

	public void setTesteItem(List<SelectItem> testeItem) {
		this.testeItem = testeItem;
	}

	public Integer getIdAdvogadoPesquisa() {
		return idAdvogadoPesquisa;
	}

	public void setIdAdvogadoPesquisa(Integer idAdvogadoPesquisa) {
		this.idAdvogadoPesquisa = idAdvogadoPesquisa;
	}

	public Integer getIdClientePesquisa() {
		return idClientePesquisa;
	}

	public void setIdClientePesquisa(Integer idClientePesquisa) {
		this.idClientePesquisa = idClientePesquisa;
	}

	public Integer getIdCasoPesquisa() {
		return idCasoPesquisa;
	}

	public void setIdCasoPesquisa(Integer idCasoPesquisa) {
		this.idCasoPesquisa = idCasoPesquisa;
	}

	public List<DetalhesParticipacaoAdvogadoDTO> getListaDetalhesParticipacaoAdvogadoDTO() {
		return listaDetalhesParticipacaoAdvogadoDTO;
	}

	public void setListaDetalhesParticipacaoAdvogadoDTO(
			List<DetalhesParticipacaoAdvogadoDTO> listaDetalhesParticipacaoAdvogadoDTO) {
		this.listaDetalhesParticipacaoAdvogadoDTO = listaDetalhesParticipacaoAdvogadoDTO;
	}

	public Boolean getStatusCheckBox() {
		return statusCheckBox;
	}

	public void setStatusCheckBox(Boolean statusCheckBox) {
		this.statusCheckBox = statusCheckBox;
	}

	public Float getValorTransferenciaAdvogado() {
		Float valor = 0f;
		
		if (listaDetalhesParticipacaoAdvogadoDTO != null){
			for (DetalhesParticipacaoAdvogadoDTO dto : listaDetalhesParticipacaoAdvogadoDTO) {
				if (dto.getDtoSelecionada() != null && dto.getDtoSelecionada()){
					if(dto.getValorDisponivel() > 0){
						valor = NumeroUtil.somarDinheiro(valor, dto.getValorDisponivel(), 3) ;
					}else if(dto.getValorDisponivel() == 0){
						valor = NumeroUtil.somarDinheiro(valor, dto.getValorPendente(),3);
					}
				}
			}
		}

		
		
		valorTransferenciaAdvogado = NumeroUtil.deixarFloatDuasCasas(valor);
		
		return valorTransferenciaAdvogado;
	}

	public void setValorTransferenciaAdvogado(Float valorTransferenciaAdvogado) {
		this.valorTransferenciaAdvogado = valorTransferenciaAdvogado;
	}


}
