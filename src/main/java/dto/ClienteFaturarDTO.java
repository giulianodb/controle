package dto;


public class ClienteFaturarDTO {

	private String nomeCliente;

	private Integer quantidadeCasosFaturar;

	private Float valorTotalFatura;
	
	private Integer idCliente;
	
	private boolean selecionado;
	
	public ClienteFaturarDTO(){
		this.valorTotalFatura = 0f;
		this.quantidadeCasosFaturar = 0;
	}
	
	public Integer getIdCliente() {
		return idCliente;
	}

	public void setIdCliente(Integer idCliente) {
		this.idCliente = idCliente;
	}

	public String getNomeCliente() {
		return nomeCliente;
	}

	public void setNomeCliente(String nomeCliente) {
		this.nomeCliente = nomeCliente;
	}

	public Integer getQuantidadeCasosFaturar() {
		return quantidadeCasosFaturar;
	}

	public void setQuantidadeCasosFaturar(Integer quantidadeCasosFaturar) {
		this.quantidadeCasosFaturar = quantidadeCasosFaturar;
	}

	public Float getValorTotalFatura() {
		return valorTotalFatura;
	}

	public void setValorTotalFatura(Float valorTotalFatura) {
		this.valorTotalFatura = valorTotalFatura;
	}

	@Override
	public String toString() {
		return "ClienteFaturarDTO [nomeCliente=" + nomeCliente
				+ ", quantidadeCasosFaturar=" + quantidadeCasosFaturar
				+ ", valorTotalFatura=" + valorTotalFatura + ", idCliente="
				+ idCliente + "]";
	}

	public Boolean getSelecionado() {
		return selecionado;
	}

	public void setSelecionado(Boolean selecionado) {
		this.selecionado = selecionado;
	}
	
	
}
