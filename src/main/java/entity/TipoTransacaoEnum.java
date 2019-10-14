package entity;

public enum TipoTransacaoEnum {
	
	PAGAMENTO("Advogado"),
	DISPONIBILIZADO("Cliente"),
	ADIANTAMENTO("Adiantamento"),
	ENTRADA("Entrada"),
	SAIDA("Saída"),
	IMPOSTO("Imposto");
	
	
	private String descricao;
	
	private TipoTransacaoEnum(String descricao) {
		this.descricao = descricao;
	}

	public String getDescricao() {
		return descricao;
	}	
	
}