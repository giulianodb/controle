package entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import util.NumeroUtil;

@Entity
@Table(name="participacao",schema="controle")
public class Participacao implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1427967105040734086L;

	@Id
	@SequenceGenerator(name = "PARTICIPACAO_ID", sequenceName = "id_participacao_seq", schema="controle",allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "PARTICIPACAO_ID")
	@Column(name = "id_participacao")
	private Integer idParticipacao;
	
	private Date dataPagamentoParticipacao;
	
	@Column(precision=10, scale=2)
	private Float porcentagemParticipacaoSocio;
	@Column(precision=10, scale=2)
	private Float porcentagemParticipacaoTrabalho;
	@Column(precision=10, scale=2)
	private Float porcentagemIndicacao;
	@Column(precision=10, scale=2, length=13)
	private Float valorTotalParticipacao;
	
	//Define todo o valor já repassado para o advogado.
	@Column(precision=10, scale=2)
	private Float valorTotalPago;

	private Boolean imposto;
	
	private Boolean fundo;
	
	private Float valorDisponivel;
	
	@ManyToOne
	private Fatura fatura;
	
	@ManyToOne
	private Usuario advogado;
	
	/**
	 * Responsavel por definir qual transacao essa participação faz parte. Usado para ao deletar uma transacao poder voltar todas as participções
	 * atreladas a a transacao
	 */
	@ManyToOne
	private Transacao transacaoPagamentoAdvogado;
	
	//define qual transacao o pgamento da fatura representa
	@ManyToOne
	private Transacao transacaoPagamentoFatura;
	
	
	public Participacao (){
		this.porcentagemIndicacao = 0f;
		this.porcentagemParticipacaoSocio = 0f;
		this.porcentagemParticipacaoTrabalho = 0f;
	}
	
	public Integer getIdParticipacao() {
		return idParticipacao;
	}

	public void setIdParticipacao(Integer idParticipacao) {
		this.idParticipacao = idParticipacao;
	}

	public Date getDataPagamentoParticipacao() {
		return dataPagamentoParticipacao;
	}

	public void setDataPagamentoParticipacao(Date dataPagamentoParticipacao) {
		this.dataPagamentoParticipacao = dataPagamentoParticipacao;
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

	public void setPorcentagemParticipacaoTrabalho(
			Float porcentagemParticipacaoTrabalho) {
		this.porcentagemParticipacaoTrabalho = NumeroUtil.deixarFloatDuasCasas(porcentagemParticipacaoTrabalho);
	}

	public Float getPorcentagemIndicacao() {
		return porcentagemIndicacao;
	}

	public void setPorcentagemIndicacao(Float porcentagemIndicacao) {
		this.porcentagemIndicacao = NumeroUtil.deixarFloatDuasCasas(porcentagemIndicacao);
	}

	public Float getValorTotalParticipacao() {
		return valorTotalParticipacao;
	}

	public void setValorTotalParticipacao(Float valorTotalParticipacao) {
		this.valorTotalParticipacao = NumeroUtil.deixarFloatDuasCasas(valorTotalParticipacao);
	}


	public Usuario getAdvogado() {
		return advogado;
	}

	public void setAdvogado(Usuario advogado) {
		this.advogado = advogado;
	}

	public Boolean getImposto() {
		return imposto;
	}

	public void setImposto(Boolean imposto) {
		this.imposto = imposto;
	}

	public Boolean getFundo() {
		return fundo;
	}

	public void setFundo(Boolean fundo) {
		this.fundo = fundo;
	}

	public Fatura getFatura() {
		return fatura;
	}

	public void setFatura(Fatura fatura) {
		this.fatura = fatura;
	}

	public Float getValorDisponivel() {
		return valorDisponivel;
	}

	public void setValorDisponivel(Float valorDisponivel) {
		this.valorDisponivel = NumeroUtil.deixarFloatDuasCasas(valorDisponivel);
	}
	
	public Float getValorTotalPago() {
		return valorTotalPago;
	}

	public void setValorTotalPago(Float valorTotalPago) {
		this.valorTotalPago = NumeroUtil.deixarFloatDuasCasas(valorTotalPago);
	}


	public Transacao getTransacaoPagamentoAdvogado() {
		return transacaoPagamentoAdvogado;
	}

	public void setTransacaoPagamentoAdvogado(Transacao transacaoPagamentoAdvogado) {
		this.transacaoPagamentoAdvogado = transacaoPagamentoAdvogado;
	}

	public Transacao getTransacaoPagamentoFatura() {
		return transacaoPagamentoFatura;
	}

	public void setTransacaoPagamentoFatura(Transacao transacaoPagamentoFatura) {
		this.transacaoPagamentoFatura = transacaoPagamentoFatura;
	}

}
