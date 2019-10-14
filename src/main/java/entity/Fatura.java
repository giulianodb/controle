package entity;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.hibernate.engine.profile.Fetch;

import util.NumeroUtil;

@Entity
@Table(name="fatura",schema="controle")
public class Fatura implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 8996467323447003284L;
	@Id
	@SequenceGenerator(name = "FATURA_ID", sequenceName = "id_fatura_seq", schema="controle",allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "FATURA_ID")
	@Column(name = "id_fatura")
	private Integer idFatura;
	
	private Date dataFatura; 
	
	@ManyToMany(fetch=FetchType.EAGER)
    @JoinTable(name="fatura_trabalho", schema="controle", 
            joinColumns=@JoinColumn(name="id_fatura"), 
            inverseJoinColumns=@JoinColumn(name="id_trabalho"))
	@OrderBy("dataTrabalho ASC ")
	private List<Trabalho> listaTrabalhos;
	
	@ManyToOne
	private Usuario usuario;
	
	@Enumerated
	@Column(name="status")
	private StatusTrabalho statusFatura;
	
	@ManyToOne
	private Cliente cliente;
	
	private String mesReferencia;
	
	private Date dataPagamentoFatura;
	
	private Date dataPrevisaoPagamento;
	
	//atributos para definir porcentagens de participação
	
	private Float porcentagemParticipacaoSocio;
	
	private Float porcentagemParticipacaoTrabalho;
	
	private Float porcentagemIndicacao;
	
	private Float porcentagemImposto;
	
	private Float porcentagemFundo;
	
	
	@OneToMany(mappedBy="fatura")
	@LazyCollection(LazyCollectionOption.TRUE) //para funcionar eager num mtm
	private List<Participacao> listParticipacao;
	
	private Float valorPago;
	
	//Usado somente para obter o valor que será realizado o pagamento para a fatura
	@Transient
	private Float valorPagamento;
	
	//data que foi realizado o pagamento no momento para ser inserido na transcação
	@Transient
	private Date dataPagamento = new Date();
	
	@Transient
	private Boolean faturaSelecionada;
	
	@OneToMany(mappedBy="fatura")
	@LazyCollection(LazyCollectionOption.FALSE) //para funcionar eager num mtm
	private List<Prolabore> listaProlabore;
	
	@ManyToOne(fetch=FetchType.EAGER,cascade=CascadeType.REMOVE)
	private ParcelaExito parcelaExito;
	
	@Transient
	//indica se a fatura possui pendencia com indicação nos casos
	private boolean pendenciaIndicacao;
	
	//INdica se a fatura possui pendencia em criterio de trabalho
	@Transient
	private boolean pendenciaCriterioTrabalho;
	
	public boolean isPendenciaIndicacao() {
		return pendenciaIndicacao;
	}

	public void setPendenciaIndicacao(boolean pendenciaIndicacao) {
		this.pendenciaIndicacao = pendenciaIndicacao;
	}

	public boolean isPendenciaCriterioTrabalho() {
		return pendenciaCriterioTrabalho;
	}

	public void setPendenciaCriterioTrabalho(boolean pendenciaCriterioTrabalho) {
		this.pendenciaCriterioTrabalho = pendenciaCriterioTrabalho;
	}

	public ParcelaExito getParcelaExito() {
		return parcelaExito;
	}

	public void setParcelaExito(ParcelaExito parcelaExito) {
		this.parcelaExito = parcelaExito;
	}

	public String getMesReferencia() {
		return mesReferencia;
	}

	public void setMesReferencia(String mesReferencia) {
		this.mesReferencia = mesReferencia;
	}

	public Float valorTotalFatura(){
		Float valor = 0f;
		if (listaTrabalhos != null){
			for (Trabalho trabalhoItem : listaTrabalhos) {
				valor = NumeroUtil.somarDinheiro(valor, trabalhoItem.getValorTotalTrabalho(), 2);
			}
		}
		if(listaProlabore != null){
			for (Prolabore item : listaProlabore) {
				valor = NumeroUtil.somarDinheiro(valor, item.getValor().floatValue(), 2);
			}
			
		}
		
		if (parcelaExito != null && parcelaExito.getCaso()!= null && parcelaExito.getCaso().getIdCaso() != null){
//			Float exito = NumeroUtil.somarDinheiro(parcelaExito.getCaso().getValorSucumbencia(), parcelaExito.getCaso().getValorExito(), 3);
			valor = NumeroUtil.somarDinheiro(valor, parcelaExito.getValor(), 3);
		}
		
		return NumeroUtil.deixarFloatDuasCasas(valor);
	}
	
	public Integer getIdFatura() {
		return idFatura;
	}

	public void setIdFatura(Integer idFatura) {
		this.idFatura = idFatura;
	}

	public Date getDataFatura() {
		return dataFatura;
	}

	public void setDataFatura(Date dataFatura) {
		this.dataFatura = dataFatura;
	}

	public List<Trabalho> getListaTrabalhos() {
		return listaTrabalhos;
	}

	public void setListaTrabalhos(List<Trabalho> listaTrabalhos) {
		this.listaTrabalhos = listaTrabalhos;
	}

	public Usuario getUsuario() {
		return usuario;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}

	public StatusTrabalho getStatusFatura() {
		return statusFatura;
	}

	public void setStatusFatura(StatusTrabalho statusFatura) {
		this.statusFatura = statusFatura;
	}

	public Cliente getCliente() {
		return cliente;
	}

	public void setCliente(Cliente cliente) {
		this.cliente = cliente;
	}

	public Date getDataPagamentoFatura() {
		return dataPagamentoFatura;
	}

	public void setDataPagamentoFatura(Date dataPagamentoFatura) {
		this.dataPagamentoFatura = dataPagamentoFatura;
	}

	public Date getDataPrevisaoPagamento() {
		return dataPrevisaoPagamento;
	}

	public void setDataPrevisaoPagamento(Date dataPrevisaoPagamento) {
		this.dataPrevisaoPagamento = dataPrevisaoPagamento;
	}

	public Float getValorPagamento() {
		return valorPagamento;
	}

	public void setValorPagamento(Float valorPagamento) {
		this.valorPagamento = NumeroUtil.deixarFloatDuasCasas(valorPagamento);
	}

	public Float getPorcentagemParticipacaoSocio() {
		return porcentagemParticipacaoSocio;
	}

	public void setPorcentagemParticipacaoSocio(Float porcentagemParticipacaoSocio) {
		this.porcentagemParticipacaoSocio = NumeroUtil.deixarFloatDuasCasas(porcentagemParticipacaoSocio);
	}

	public Float getPorcentagemParticipacaoTrabalho() {
		return porcentagemParticipacaoTrabalho;
	}

	public void setPorcentagemParticipacaoTrabalho(	Float porcentagemParticipacaoTrabalho) {
		this.porcentagemParticipacaoTrabalho = NumeroUtil.deixarFloatDuasCasas(porcentagemParticipacaoTrabalho);
	}

	public Float getPorcentagemIndicacao() {
		return porcentagemIndicacao;
	}

	public void setPorcentagemIndicacao(Float porcentagemIndicacao) {
		this.porcentagemIndicacao = NumeroUtil.deixarFloatDuasCasas(porcentagemIndicacao);
	}

	public Float getPorcentagemImposto() {
		return porcentagemImposto;
	}

	public void setPorcentagemImposto(Float porcentagemImposto) {
		this.porcentagemImposto = NumeroUtil.deixarFloatDuasCasas(porcentagemImposto);
	}

	public Float getPorcentagemFundo() {
		return porcentagemFundo;
	}

	public void setPorcentagemFundo(Float porcentagemFundo) {
		this.porcentagemFundo = NumeroUtil.deixarFloatDuasCasas(porcentagemFundo);
	}

	public List<Participacao> getListParticipacao() {
		return listParticipacao;
	}

	public void setListParticipacao(List<Participacao> listParticipacao) {
		this.listParticipacao = listParticipacao;
	}

	public Boolean getFaturaSelecionada() {
		return faturaSelecionada;
	}

	public void setFaturaSelecionada(Boolean faturaSelecionada) {
		this.faturaSelecionada = faturaSelecionada;
	}

	public Float getValorPago() {
		return valorPago;
	}

	public void setValorPago(Float valorPago) {
		this.valorPago = NumeroUtil.deixarFloatDuasCasas(valorPago);
	}

	public Date getDataPagamento() {
		return dataPagamento;
	}

	public void setDataPagamento(Date dataPagamento) {
		this.dataPagamento = dataPagamento;
	}

	public List<Prolabore> getListaProlabore() {
		return listaProlabore;
	}

	public void setListaProlabore(List<Prolabore> listaProlabore) {
		this.listaProlabore = listaProlabore;
	}
	
	
	public Boolean temPendenciaTrabalho(){
		int participacaoTrabalho = 0;
		for (Participacao participacao : getListParticipacao()) {
			participacaoTrabalho = participacaoTrabalho + participacao.getPorcentagemParticipacaoTrabalho().intValue();
		}
	
		if (participacaoTrabalho == 0){
			return true;
		}
		else{
			return false;
		}
			
	}
	
	public Boolean temPendenciaIndicacao(){
		
		int participacaoIndicacao = 0;
		for (Participacao participacao : getListParticipacao()) {
			participacaoIndicacao = participacaoIndicacao + participacao.getPorcentagemIndicacao().intValue();
		}
		//Adiciona na lista as faturas com pendencias
		if (participacaoIndicacao == 0){ 
			return true;
		}
		else {
			return false;
		}
	}
	
	
	public Boolean permissaoAlterarCriterioTrabalho(){
		
		if(tipoFatura().equals("Caso fixo") && StatusTrabalho.FATURADO.equals(statusFatura)){
			return true;
		}
		else {
			return false;
		}
	}
	
	public Boolean temQualquerPendendicia(){
		if (permissaoAlterarCriterioTrabalho() && temPendenciaIndicacao()){
			return true;
		}
		else {
			return false;
		}
		
		
	}
	
	public String tipoFatura(){
		if (parcelaExito != null && parcelaExito.getCaso() != null){
			return "Fechamento caso";
		} else if(listaProlabore != null && listaProlabore.size() > 0) {
			return "Caso fixo";
		}
		else {
			return "Caso variável";
		}
	}

	
}
