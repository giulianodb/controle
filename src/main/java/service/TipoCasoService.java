package service;

import java.util.List;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import entity.TipoCaso;



@Stateless
@LocalBean
public class TipoCasoService {
	@PersistenceContext(unitName = "controle")
	private EntityManager em;
	

	public List<TipoCaso> pesquisarTipoCaso(TipoCaso TipoCaso) {
		
		List<TipoCaso> listaTipoCaso = em.createQuery("FROM TipoCaso").getResultList();
		
		
		return listaTipoCaso;
	}
	
	public void cadastrarTipoCaso(TipoCaso TipoCaso){
		em.persist(TipoCaso);
	}
	public void alterarTipoCaso(TipoCaso TipoCaso){
		em.merge(TipoCaso);
	}
	
	public void exlcuirTipoCaso(TipoCaso TipoCaso){
		em.remove(TipoCaso);
	}
}
