package util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import entity.ValorParticipacaoEnum;

public class CarregarCombos implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public static List<ValorParticipacaoEnum> carregarComboPreParticipacao() {
		List<ValorParticipacaoEnum> listValorParticipacaoEnum = new ArrayList<ValorParticipacaoEnum>();
		if (listValorParticipacaoEnum == null || listValorParticipacaoEnum.size() == 0){
			listValorParticipacaoEnum = new ArrayList<ValorParticipacaoEnum>();
			listValorParticipacaoEnum.add(ValorParticipacaoEnum.VALOR);
			listValorParticipacaoEnum.add(ValorParticipacaoEnum.PORCENTAGEM);
		}
		return listValorParticipacaoEnum;
		
	}
	
	public static List<ValorParticipacaoEnum> carregarComboTipoExito() {
		List<ValorParticipacaoEnum> listValorParticipacaoEnum = new ArrayList<ValorParticipacaoEnum>();
		if (listValorParticipacaoEnum == null || listValorParticipacaoEnum.size() == 0){
			listValorParticipacaoEnum = new ArrayList<ValorParticipacaoEnum>();
			listValorParticipacaoEnum.add(ValorParticipacaoEnum.VALOR);
			listValorParticipacaoEnum.add(ValorParticipacaoEnum.PORCENTAGEM);
		}
		return listValorParticipacaoEnum;
		
	}
	
}
