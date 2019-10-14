package dto.participacao;

public class ResumoParticipacaoAdvogadoDTO {
	
	private String nomeCliente;
	
	private Integer idCliente;
	
	private Integer idAdvogado;
	
	private String nomeAdvogado;
	
	private Float valorPago;
	
	private Float valorPendente;
	
	private Float valorDisponivel;
	
	public ResumoParticipacaoAdvogadoDTO() {
		valorPago = 0f;
		valorDisponivel = 0f;
		valorPendente = 0f;
	}


	public String getNomeCliente() {
		return nomeCliente;
	}

	public void setNomeCliente(String nomeCliente) {
		this.nomeCliente = nomeCliente;
	}

	public Integer getIdAdvogado() {
		return idAdvogado;
	}

	public void setIdAdvogado(Integer idAdvogado) {
		this.idAdvogado = idAdvogado;
	}

	public String getNomeAdvogado() {
		return nomeAdvogado;
	}

	public void setNomeAdvogado(String nomeAdvogado) {
		this.nomeAdvogado = nomeAdvogado;
	}

	public Float getValorPago() {
		return valorPago;
	}

	public void setValorPago(Float valorPago) {
		this.valorPago = valorPago;
	}

	public Float getValorPendente() {
		return valorPendente;
	}

	public void setValorPendente(Float valorPendente) {
		this.valorPendente = valorPendente;
	}

	public Float getValorDisponivel() {
		return valorDisponivel;
	}

	public void setValorDisponivel(Float valorDisponivel) {
		this.valorDisponivel = valorDisponivel;
	}


	public Integer getIdCliente() {
		return idCliente;
	}


	public void setIdCliente(Integer idCliente) {
		this.idCliente = idCliente;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((idAdvogado == null) ? 0 : idAdvogado.hashCode());
		result = prime * result
				+ ((idCliente == null) ? 0 : idCliente.hashCode());
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ResumoParticipacaoAdvogadoDTO other = (ResumoParticipacaoAdvogadoDTO) obj;
		if (idAdvogado == null) {
			if (other.idAdvogado != null)
				return false;
		} else if (!idAdvogado.equals(other.idAdvogado))
			return false;
		if (idCliente == null) {
			if (other.idCliente != null)
				return false;
		} else if (!idCliente.equals(other.idCliente))
			return false;
		return true;
	}
	
}	
