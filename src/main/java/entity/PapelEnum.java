package entity;

public enum PapelEnum {

	ADVOGADO_JUNIOR("Advogado_Junior"),
	ADVOGADO_PLENO("Advogado_Pleno"),
	ADVOGADO_SENIOR("Advogado_Senior"),
	ADMINISTRADOR("Administrador"),
	FINANCEIRO("Financeiro"),
	ESTAGIARIO("Estagiario"),
	RECEPCAO("Recepcao");
	
	
	private String descricao;
	
	private PapelEnum(String descricao) {
		this.descricao = descricao;
	}

	public String getDescricao() {
		return descricao;
	}	
	
}