package entity;

public enum TipoContaEnum {

	ADVOGADO("Advogado"),
	IMPOSTO("Imposto"),
	FUNDO("Fundo");
	
	
	private String descricao;
	
	private TipoContaEnum(String descricao) {
		this.descricao = descricao;
	}

	public String getDescricao() {
		return descricao;
	}	
	
}