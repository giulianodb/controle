package entity;

public enum StatusCaso {

	CRIADO("Criado"),
	ANDAMENTO("Andamento"),
	FINALIZADO("Encerrado");
	
	private String descricao;
	
	private StatusCaso(String descricao) {
		this.descricao = descricao;
	}

	public String getDescricao() {
		return descricao;
	}	
	
}