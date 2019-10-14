package entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(schema="controle")
public class ParcelaExito implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -544993834084847674L;
	
	public ParcelaExito(){
		
	}
	
	public ParcelaExito(Integer ordem){
		this.ordem = ordem;
	}
	
	@Id
	@SequenceGenerator(name = "PARCELAEXITO_ID", sequenceName = "id_parcelaexito_seq", schema="controle",allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "PARCELAEXITO_ID")
	@Column
	private Integer id;
	
	private Date dataVencimento;
	
	private Float valor;
	
	private Integer ordem;
	
	@ManyToOne
	private Caso caso;
	
	//distribuição
	
	private Float porcentagemParticipacaoSocio;
	
	private Float porcentagemParticipacaoTrabalho;
	
	private Float porcentagemIndicacao;
	
	private Float porcentagemImposto;
	
	private Float porcentagemFundo;
	
	@Enumerated
	private StatusTrabalho statusParcela;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Date getDataVencimento() {
		return dataVencimento;
	}

	public void setDataVencimento(Date dataVencimento) {
		this.dataVencimento = dataVencimento;
	}

	public Float getValor() {
		return valor;
	}

	public void setValor(Float valor) {
		this.valor = valor;
	}

	public Integer getOrdem() {
		return ordem;
	}

	public void setOrdem(Integer ordem) {
		this.ordem = ordem;
	}

	public Caso getCaso() {
		return caso;
	}

	public void setCaso(Caso caso) {
		this.caso = caso;
	}

	public Float getPorcentagemParticipacaoSocio() {
		return porcentagemParticipacaoSocio;
	}

	public void setPorcentagemParticipacaoSocio(Float porcentagemParticipacaoSocio) {
		this.porcentagemParticipacaoSocio = porcentagemParticipacaoSocio;
	}

	public Float getPorcentagemParticipacaoTrabalho() {
		return porcentagemParticipacaoTrabalho;
	}

	public void setPorcentagemParticipacaoTrabalho(
			Float porcentagemParticipacaoTrabalho) {
		this.porcentagemParticipacaoTrabalho = porcentagemParticipacaoTrabalho;
	}

	public Float getPorcentagemIndicacao() {
		return porcentagemIndicacao;
	}

	public void setPorcentagemIndicacao(Float porcentagemIndicacao) {
		this.porcentagemIndicacao = porcentagemIndicacao;
	}

	public Float getPorcentagemImposto() {
		return porcentagemImposto;
	}

	public void setPorcentagemImposto(Float porcentagemImposto) {
		this.porcentagemImposto = porcentagemImposto;
	}

	public Float getPorcentagemFundo() {
		return porcentagemFundo;
	}

	public void setPorcentagemFundo(Float porcentagemFundo) {
		this.porcentagemFundo = porcentagemFundo;
	}

	public StatusTrabalho getStatusParcela() {
		return statusParcela;
	}

	public void setStatusParcela(StatusTrabalho statusParcela) {
		this.statusParcela = statusParcela;
	}
	
}
