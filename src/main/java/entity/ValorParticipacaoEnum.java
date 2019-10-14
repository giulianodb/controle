package entity;

//Usado para a preParticipação
//define se será usado valor ou percentagem

//tambéum usado para definir como será a parte de êxito
public enum ValorParticipacaoEnum {

	VALOR("Valor"),
	PORCENTAGEM("Porcentagem"),
	FIXO("Fixo");
	
	
	private String descricao;
	
	private ValorParticipacaoEnum(String descricao) {
		this.descricao = descricao;
	}

	public String getDescricao() {
		return descricao;
	}	
	
}