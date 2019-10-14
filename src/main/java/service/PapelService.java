package service;

import java.util.List;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import entity.Papel;


@Stateless
@LocalBean
public class PapelService {
	@PersistenceContext(unitName = "controle")
	private EntityManager em;
	
	public List<Papel> listaPapel(){
		
		Query query = em.createNamedQuery("listarPapel");
		
		List<Papel> resultList = query.getResultList();
		
		return resultList;
		
	}

}
