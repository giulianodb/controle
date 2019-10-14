package entity;

public enum ProlaboreEnum {

	VENCIMENTO("Data vencimento"),

	EVENTO("Evento");
	
	
	private String descricao;
	
	private ProlaboreEnum(String descricao) {
		this.descricao = descricao;
	}

	public String getDescricao() {
		return descricao;
	}	
	
}