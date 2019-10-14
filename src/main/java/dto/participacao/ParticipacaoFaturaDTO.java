package dto.participacao;

import java.util.List;

import entity.Fatura;

public class ParticipacaoFaturaDTO {
	private Fatura fatura;
	
	private List<ItemParticipacaoFaturaDTO> listItem;

	public Fatura getFatura() {
		return fatura;
	}

	public void setFatura(Fatura fatura) {
		this.fatura = fatura;
	}

	public List<ItemParticipacaoFaturaDTO> getListItem() {
		return listItem;
	}

	public void setListItem(List<ItemParticipacaoFaturaDTO> listItem) {
		this.listItem = listItem;
	}
}
