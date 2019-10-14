package dto.participacao;

import java.util.Date;


public class ParticipacaoUsuarioDTO {
	
	private Integer idUsuario;
	
	private String nomeUsuario;
	
	//valor já reapssado para o advogado
	private Float valorPagoParticipacao;
	
	//valor já recebido do cliente e pronto para ser repassado
	private Float valorDisponivel;
	
	//valor a receber do cliente e referente ao advogado em questão
	private Float valorPendente;
	
	//Define se o atributo foi selecionado
	private Boolean dtoSelecionado;
	
	//Valor que será repassado para o advogado
	private Float valorParaReceber;
	
	//Data que o valor será repassado para advogado.
	private Date dataRecebimento;

	public Integer getIdUsuario() {
		return idUsuario;
	}

	public void setIdUsuario(Integer idUsuario) {
		this.idUsuario = idUsuario;
	}

	public String getNomeUsuario() {
		return nomeUsuario;
	}

	public void setNomeUsuario(String nomeUsuario) {
		this.nomeUsuario = nomeUsuario;
	}

	public Float getValorPagoParticipacao() {
		return valorPagoParticipacao;
	}

	public void setValorPagoParticipacao(Float valorPagoParticipacao) {
		this.valorPagoParticipacao = valorPagoParticipacao;
	}

	public Float getValorDisponivel() {
		return valorDisponivel;
	}

	public void setValorDisponivel(Float valorDisponivel) {
		this.valorDisponivel = valorDisponivel;
	}

	public Float getValorPendente() {
		return valorPendente;
	}

	public void setValorPendente(Float valorPendente) {
		this.valorPendente = valorPendente;
	}

	public Boolean getDtoSelecionado() {
		return dtoSelecionado;
	}

	public void setDtoSelecionado(Boolean dtoSelecionado) {
		this.dtoSelecionado = dtoSelecionado;
	}

	public Float getValorParaReceber() {
		return valorParaReceber;
	}

	public void setValorParaReceber(Float valorParaReceber) {
		this.valorParaReceber = valorParaReceber;
	}

	public Date getDataRecebimento() {
		return dataRecebimento;
	}

	public void setDataRecebimento(Date dataRecebimento) {
		this.dataRecebimento = dataRecebimento;
	}
	
}
