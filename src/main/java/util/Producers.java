package util;

import javax.enterprise.inject.Produces;
import javax.inject.Named;

import entity.StatusCaso;

public class Producers {
	
	
	@Produces
	@Named("statusCasoCriado")
	public StatusCaso statusCasoCriado(){
		return StatusCaso.CRIADO;
	}
	
	@Produces
	@Named("statusCasoFinalizado")
	public StatusCaso statusCasoFinalizado(){
		return StatusCaso.FINALIZADO;
	}
}
