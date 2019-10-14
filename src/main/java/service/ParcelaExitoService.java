package service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import control.exception.ExceptionHandler;
import entity.ParcelaExito;
import entity.Prolabore;
import entity.StatusTrabalho;

@Stateless
@LocalBean
@ExceptionHandler
public class ParcelaExitoService {
	@PersistenceContext(unitName = "controle")
	private EntityManager em;
	
	@EJB
	private ParticipacaoService participacaoService;
	
	public List<ParcelaExito> pesquisarParcelaExito(Integer idCliente, Date dataInicioPesquisa, Date dataFimPesquisa) {
		List<ParcelaExito> listaPParcelaExito = new ArrayList<ParcelaExito>();
		StringBuffer hql = new StringBuffer();
		
		hql = new StringBuffer("FROM ParcelaExito p WHERE ");
		
		if (idCliente != null && !idCliente.equals(-1)){
			hql.append("p.caso.cliente.idCliente = :idCliente AND ");
					
		}
			
			if (dataInicioPesquisa != null) {
				hql.append(" p.dataVencimento >= :dataInicio AND ");
			}

			if (dataFimPesquisa != null) {
				hql.append(" p.dataVencimento <= :dataFim AND ");
			}
			
		hql.append(" p.statusParcela= :status order by p.caso.cliente.nome, p.caso.nomeCaso,p.ordem ");

		Query query = em.createQuery(hql.toString(), ParcelaExito.class);

		if (idCliente != null && !idCliente.equals(-1)){
			query.setParameter("idCliente", idCliente);
		}

			if (dataInicioPesquisa != null) {
				query.setParameter("dataInicio", dataInicioPesquisa);
			}

			if (dataFimPesquisa != null) {
				query.setParameter("dataFim", dataFimPesquisa);
			}
			
		query.setParameter("status", StatusTrabalho.CRIADO);
		
		
		listaPParcelaExito = query.getResultList();
		
		return listaPParcelaExito;
	}
	
	
	public void salvarParcelaExito(ParcelaExito p){
		
		em.merge(p);
	}
	
	public ParcelaExito obterParcelaExito(Integer id){
		return em.find(ParcelaExito.class, id);
	}
	
	public void excluirParcelaExito(ParcelaExito parcelaExito){
		em.remove(parcelaExito);
	}
}
