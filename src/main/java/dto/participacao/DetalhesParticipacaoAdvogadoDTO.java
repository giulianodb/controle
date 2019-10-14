package dto.participacao;

import java.util.ArrayList;
import java.util.List;

import entity.Participacao;


public class DetalhesParticipacaoAdvogadoDTO {
	
	private String nomeAdvogado;
	
	private Integer idAdvogado;
	
	private String nomeCliente;
	
	private Integer idCliente;
	
	private Float valorPago;
	
	private Float valorPendente;
	
	private Float valorDisponivel;
	
	private Boolean dtoSelecionada;
	
	private List<Participacao> listaParticipacao;

	public DetalhesParticipacaoAdvogadoDTO(){
		valorPago = 0f;
		valorPendente = 0f;
		valorDisponivel = 0f;
		
		listaParticipacao = new ArrayList<Participacao>();
		
	}

	public String getNomeAdvogado() {
		return nomeAdvogado;
	}

	public void setNomeAdvogado(String nomeAdvogado) {
		this.nomeAdvogado = nomeAdvogado;
	}

	public Integer getIdAdvogado() {
		return idAdvogado;
	}

	public void setIdAdvogado(Integer idAdvogado) {
		this.idAdvogado = idAdvogado;
	}

	public String getNomeCliente() {
		return nomeCliente;
	}

	public void setNomeCliente(String nomeCliente) {
		this.nomeCliente = nomeCliente;
	}

	public Integer getIdCliente() {
		return idCliente;
	}

	public void setIdCliente(Integer idCliente) {
		this.idCliente = idCliente;
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
		DetalhesParticipacaoAdvogadoDTO other = (DetalhesParticipacaoAdvogadoDTO) obj;
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

	public Boolean getDtoSelecionada() {
		return dtoSelecionada;
	}

	public void setDtoSelecionada(Boolean dtoSelecionada) {
		this.dtoSelecionada = dtoSelecionada;
	}

	public List<Participacao> getListaParticipacao() {
		return listaParticipacao;
	}

	public void setListaParticipacao(List<Participacao> listaParticipacao) {
		this.listaParticipacao = listaParticipacao;
	}

}
