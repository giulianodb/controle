package entity;

public enum StatusTrabalho {

	CRIADO("Criado"),
	ENVIADO_FATURAR("Faturar"),
	FATURADO("Faturado"),
	PARCIAL_PAGO("Parcilamente Pago"),
	PAGO("Pago"),
	DISTRIBUIDO("Distribuído");
	
	private String descricao;
	
	private StatusTrabalho(String descricao) {
		this.descricao = descricao;
	}

	public String getDescricao() {
		return descricao;
	}	
	
}