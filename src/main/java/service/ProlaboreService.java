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
import javax.persistence.TypedQuery;

import util.NumeroUtil;
import control.exception.ExceptionHandler;
import entity.Caso;
import entity.Prolabore;
import entity.StatusCaso;
import entity.StatusTrabalho;
import entity.TipoFaturaCasoEnum;

@Stateless
@LocalBean
@ExceptionHandler
public class ProlaboreService {
	@PersistenceContext(unitName = "controle")
	private EntityManager em;
	
	@EJB
	private ParticipacaoService participacaoService;
	
	public List<Prolabore> pesquisarProlabore(Integer idCliente, Date dataInicioPesquisa, Date dataFimPesquisa, Boolean evento) {
		List<Prolabore> listaProlabore = new ArrayList<Prolabore>();
		StringBuffer hql = new StringBuffer();
		
		hql = new StringBuffer("FROM Prolabore p  ");
		
		hql.append(" LEFT JOIN FETCH p.caso caso ");
		hql.append(" LEFT JOIN FETCH caso.cliente cliente ");
		hql.append(" LEFT JOIN FETCH p.fatura fatura ");
		hql.append(" LEFT JOIN FETCH p.listaPreParticipacaoFixos listaPre ");
		
		
		hql.append(" WHERE ");
		if (idCliente != null && !idCliente.equals(-1)){
			hql.append("p.caso.cliente.idCliente = :idCliente AND ");
					
		}
			
		if (evento!=null && evento) {
			hql.append(" p.evento <> '' AND ");
		} else {
			if (dataInicioPesquisa != null) {
				hql.append(" p.dataVencimento >= :dataInicio AND ");
			}

			if (dataFimPesquisa != null) {
				hql.append(" p.dataVencimento <= :dataFim AND ");
			}
		}
			
		hql.append(" (p.statusProlaboreEnum= :status OR p.preParticipacaoCadastrada = false) order by p.caso.cliente.nome,p.caso.nomeCaso,p.ordem ");

		Query query = em.createQuery(hql.toString(), Prolabore.class);

		if (idCliente != null && !idCliente.equals(-1)){
			query.setParameter("idCliente", idCliente);
		}

		if ((evento!=null && !evento)) {
			if (dataInicioPesquisa != null) {
				query.setParameter("dataInicio", dataInicioPesquisa);
			}

			if (dataFimPesquisa != null) {
				query.setParameter("dataFim", dataFimPesquisa);
			}
		}
		query.setParameter("status", StatusTrabalho.CRIADO);
		
		
		listaProlabore = query.getResultList();
		
		return listaProlabore;
	}
	
	public void salvarProlaboreComPreParticipacao(Prolabore p) throws Exception{
		if (!p.getStatusProlaboreEnum().equals(StatusTrabalho.CRIADO)){
			//Deletar as participaoes antigas
			if (p.getFatura() != null & p.getFatura().getIdFatura() != null) {
				participacaoService.exlcuirCriterioTrabalhoPorFatura(p.getFatura().getIdFatura(),p);
				
			}
			
			participacaoService.salvarParticipacaoPosterior(p);
		}
		
		if(p.participacoes().equals("OK")){
			p.setPreParticipacaoCadastrada(true);
		}
		else {
			p.setPreParticipacaoCadastrada(false);
		}
		
		em.merge(p);
	}
	
	public void salvarProlabore(Prolabore p){
		
		em.merge(p);
	}
	
	public Prolabore obterProlabore(Integer id){
		return em.find(Prolabore.class, id);
		
	}
	
	public Prolabore obterProlaborePorFatura(Integer idFatura) {
		List<Prolabore> listaProlabore = new ArrayList<Prolabore>();
		StringBuffer hql = new StringBuffer();
		
		hql = new StringBuffer("FROM Prolabore p  ");
		
		hql.append(" LEFT JOIN FETCH p.listaPreParticipacaoFixos listaFixos ");
		hql.append(" LEFT JOIN FETCH p.caso caso ");
		hql.append(" LEFT JOIN FETCH caso.cliente cliente ");
		hql.append(" LEFT JOIN FETCH p.fatura fatura ");
			
		hql.append(" WHERE p.fatura.idFatura = :idFatura ");

		Query query = em.createQuery(hql.toString(), Prolabore.class);


		query.setParameter("idFatura", idFatura);
		
		listaProlabore = query.getResultList();
		
		return listaProlabore.get(0);
	}
}
