package entity;

public enum TipoParticipacaoEnum {

	IMPOSTO("Imposto"),
	PARTICIPACAO_SOCIO("Participação sócio"),
	PARTICIPACAO_TRABALHO("Participação Trabalho"),
	INDICACAO("INDICACAO"),
	FUNDO("Fundo"),
	PARTICIPACAO_TRABALHO_EXITO("Participação Trabalho Êxito"),
	IMPOSTO_EXITO("Imposto Êxito"),
	PARTICIPACAO_SOCIO_EXITO("Participação sócio Êxito"),
	INDICACAO_EXITO("INDICACAO Êxito"),
	FUNDO_EXITO("Fundo Êxito");
	
	private String descricao;
	
	private TipoParticipacaoEnum(String descricao) {
		this.descricao = descricao;
	}

	public String getDescricao() {
		return descricao;
	}	
	
}