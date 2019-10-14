package dto.participacao;

import java.util.Date;

import entity.StatusTrabalho;

public class ItemParticipacaoUsuarioDTO {
	private String nome;
	private Integer idItem;
	private StatusTrabalho statusTrabalho;
	private Date dataInicioPesquisaTrabalho;
	private Date dataFimPesquisaTrabalho;
	private Date dataInicioPesquisaPagamentoParticipacao;
	private Date dataFimPesquisaPagamentoParticipacao;
	
	
	private Float valor;
	public String getNome() {
		return nome;
	}
	public void setNome(String nome) {
		this.nome = nome;
	}
	public Float getValor() {
		return valor;
	}
	public void setValor(Float valor) {
		this.valor = valor;
	}
	public Integer getIdItem() {
		return idItem;
	}
	public void setIdItem(Integer idItem) {
		this.idItem = idItem;
	}
	public StatusTrabalho getStatusTrabalho() {
		return statusTrabalho;
	}
	public void setStatusTrabalho(StatusTrabalho statusTrabalho) {
		this.statusTrabalho = statusTrabalho;
	}
	public Date getDataInicioPesquisaTrabalho() {
		return dataInicioPesquisaTrabalho;
	}
	public void setDataInicioPesquisaTrabalho(Date dataInicioPesquisaTrabalho) {
		this.dataInicioPesquisaTrabalho = dataInicioPesquisaTrabalho;
	}
	public Date getDataFimPesquisaTrabalho() {
		return dataFimPesquisaTrabalho;
	}
	public void setDataFimPesquisaTrabalho(Date dataFimPesquisaTrabalho) {
		this.dataFimPesquisaTrabalho = dataFimPesquisaTrabalho;
	}
	public Date getDataInicioPesquisaPagamentoParticipacao() {
		return dataInicioPesquisaPagamentoParticipacao;
	}
	public void setDataInicioPesquisaPagamentoParticipacao(
			Date dataInicioPesquisaPagamentoParticipacao) {
		this.dataInicioPesquisaPagamentoParticipacao = dataInicioPesquisaPagamentoParticipacao;
	}
	public Date getDataFimPesquisaPagamentoParticipacao() {
		return dataFimPesquisaPagamentoParticipacao;
	}
	public void setDataFimPesquisaPagamentoParticipacao(
			Date dataFimPesquisaPagamentoParticipacao) {
		this.dataFimPesquisaPagamentoParticipacao = dataFimPesquisaPagamentoParticipacao;
	}
}
