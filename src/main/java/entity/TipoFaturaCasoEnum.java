package entity;

public enum TipoFaturaCasoEnum {

	FIXO("Fixo"),
	VARIAVEL("Variável");
	
	private String descricao;
	
	private TipoFaturaCasoEnum(String descricao) {
		this.descricao = descricao;
	}

	public String getDescricao() {
		return descricao;
	}	
	
}