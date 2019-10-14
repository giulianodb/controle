package entity;

public enum StatusUsuario {

	ATIVO("Ativo"),
	INATIVO("Inativo");
	
	private String descricao;
	
	private StatusUsuario(String descricao) {
		this.descricao = descricao;
	}

	public String getDescricao() {
		return descricao;
	}	
	
}