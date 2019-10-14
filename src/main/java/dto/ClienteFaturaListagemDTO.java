package dto;

public class ClienteFaturaListagemDTO {
	private String nomeCliente;
	
	private Integer idCliente;
	
	private Integer quantidadeCasos;

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

	public Integer getQuantidadeCasos() {
		return quantidadeCasos;
	}

	public void setQuantidadeCasos(Integer quantidadeCasos) {
		this.quantidadeCasos = quantidadeCasos;
	}
	
	
}
