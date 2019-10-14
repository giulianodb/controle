package service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import util.NumeroUtil;
import control.UsuarioLogadoControl;
import control.exception.ExceptionHandler;
import entity.Caso;
import entity.Fatura;
import entity.PapelEnum;
import entity.ParcelaExito;
import entity.Participacao;
import entity.ParticipacaoDefault;
import entity.Prolabore;
import entity.StatusCaso;
import entity.StatusTrabalho;
import entity.TipoFaturaCasoEnum;
import entity.TipoParticipacaoEnum;
import exception.AlmiranteException;
import exception.ControleException;

@Stateless
@LocalBean
@ExceptionHandler
public class CasoService {
	@PersistenceContext(unitName = "controle")
	private EntityManager em;
	
	@EJB
	private ParticipacaoDefaultService participacaoDefaultService;
	
	@EJB
	private ParticipacaoService participacaoService;
	
	@EJB
	private FaturaService faturaService;
	
	@Inject
	private UsuarioLogadoControl usuarioLogado;
	
	public List<Caso> pesquisarCaso(Caso caso, Date dataInicioPesquisa, Date dataFimPesquisa) {
		List<Caso> listaCaso = new ArrayList<Caso>();
		StringBuffer hql = new StringBuffer();
		
		//definindo se for adv normal lista somente casos abertos
		if (!usuarioLogado.getUsuario().getPapel().getNomePapel().equals(PapelEnum.ADMINISTRADOR.getDescricao())){
			caso.setStatusCaso(StatusCaso.CRIADO);
		}
		
		
		if (caso != null && caso.getCliente() != null && caso.getCliente().getIdCliente() != null) {
			
			StringBuilder query = new StringBuilder(" FROM Caso c ");
			query.append(" LEFT JOIN FETCH c.cliente cliente ");
			query.append(" LEFT JOIN FETCH c.tipoCaso tipoCaso ");
			query.append(" WHERE c.cliente.idCliente = :idCliente ");
			
			listaCaso = em.createQuery(query.toString(),Caso.class).setParameter("idCliente", caso.getCliente().getIdCliente()).getResultList();
			
			
		} else {
			hql = new StringBuffer("FROM Caso c ");
			hql.append(" LEFT JOIN FETCH c.cliente cliente ");
			hql.append(" LEFT JOIN FETCH c.tipoCaso tipoCaso ");
//			hql.append("  LEFT JOIN FETCH c.listaUsuarioIndicacao indicacoes ");
			hql.append(" WHERE  " );

			if (caso.getCliente() != null && caso.getCliente().getNome() != null && !"".equals(caso.getCliente().getNome())) {
				hql.append("  lower (c.cliente.nome) like :nomeCliente AND ");
			}

			if (caso.getNomeCaso() != null && !"".equals(caso.getNomeCaso())) {
				hql.append(" lower (c.nomeCaso) like :nomeCaso AND ");
			}

			if (dataInicioPesquisa != null) {
				hql.append(" c.data >= :dataInicio AND ");
			}

			if (dataFimPesquisa != null) {
				hql.append(" c.data <= :dataFim AND ");
			}

			if (caso.getTipoFaturaCaso() != null) {
				hql.append(" c.tipoFaturaCaso = :tipoFatura AND ");
			}

			if (caso.getStatusCaso() != null) {
				hql.append(" c.statusCaso = :statusCaso AND ");
			}

			hql.append(" 1 = 1 order by c.cliente.nome,c.nomeCaso ");

			Query query = em.createQuery(hql.toString(), Caso.class);

			if (caso.getCliente() != null
					&& caso.getCliente().getNome() != null
					&& !"".equals(caso.getCliente().getNome())) {
				query.setParameter("nomeCliente", "%"
						+ caso.getCliente().getNome().toLowerCase() + "%");
			}

			if (caso.getNomeCaso() != null && !"".equals(caso.getNomeCaso())) {
				query.setParameter("nomeCaso", "%"
						+ caso.getNomeCaso().toLowerCase() + "%");
			}

			if (dataInicioPesquisa != null) {
				query.setParameter("dataInicio", dataInicioPesquisa);
			}

			if (dataFimPesquisa != null) {
				query.setParameter("dataFim", dataFimPesquisa);
			}

			if (caso.getTipoFaturaCaso() != null) {
				query.setParameter("tipoFatura", caso.getTipoFaturaCaso());

			}
			
			if (caso.getStatusCaso() != null) {
				query.setParameter("statusCaso", caso.getStatusCaso());
			}
			listaCaso = query.getResultList();
		}
		
		
		//Trecho para ordenar a lista pelo nome cliente
		Collections.sort(listaCaso, new Comparator<Object>() {

			@Override
			public int compare(Object o1, Object o2) {
				Caso cf1 = (Caso) o1;
				Caso cf2 = (Caso) o2;
				
				
				return new Integer (cf1.getListaUsuarioIndicacao().size()).compareTo(new Integer (cf2.getListaUsuarioIndicacao().size()));
					
			}
		});

		return listaCaso;
	}

	public void cadastrarCaso(Caso caso) {
		caso.setStatusCaso(StatusCaso.CRIADO);
		if (caso.getTipoFaturaCaso() == null) {
			caso.setTipoFaturaCaso(TipoFaturaCasoEnum.VARIAVEL);
		}

		if (caso.getTipoFaturaCaso().equals(TipoFaturaCasoEnum.FIXO)) {

			caso.setParcelasPagas(0);
			// deixando floats com duas casas...
			caso.setValorFixo(NumeroUtil.deixarFloatDuasCasas(caso.getValorFixo()));

			List<Prolabore> removerProlabores = new ArrayList<Prolabore>();

			for (Prolabore p : caso.getListaProlabole()) {
				if (p.getValor() == null || p.getValor().floatValue() < 0.1) {
					removerProlabores.add(p);
				} else {
					carregarPorcentagemDistribuicao(p);
					p.setPreParticipacaoCadastrada(false);
					p.setStatusProlaboreEnum(StatusTrabalho.CRIADO);
					
					Float valorPreParticipacaoSemImposto = participacaoService.obterValorTotalTrabalhoSemImposto(p.getValor().floatValue(),p.getPorcentagemImposto());
					Float valorParticipacaoTrabalho = participacaoService.obterValorParticipacaoTrabalho
							(p.getPorcentagemParticipacaoTrabalho(), valorPreParticipacaoSemImposto, 0f, false, 0);
					p.setValorParticipacaoTrabalho(valorParticipacaoTrabalho);
					
				}
			}
			caso.getListaProlabole().removeAll(removerProlabores);

		} else if (caso.getTipoFaturaCaso().equals(TipoFaturaCasoEnum.VARIAVEL)) {
			// se for variavel não possui prolabores
			caso.setListaProlabole(null);
		}

		em.merge(caso);
	}
	
	private void carregarPorcentagemDistribuicao(Prolabore prolabore) {
		// TODO Auto-generated method stub
		List<ParticipacaoDefault> listarParticipacaoDefault = participacaoDefaultService.listarParticipacaoDefault();
		for (ParticipacaoDefault p : listarParticipacaoDefault) {
			if (p.getNome().equals(TipoParticipacaoEnum.FUNDO)){
				prolabore.setPorcentagemFundo(p.getValor());
			}
			else if(p.getNome().equals(TipoParticipacaoEnum.IMPOSTO)){
				prolabore.setPorcentagemImposto(p.getValor());
			}
			else if(p.getNome().equals(TipoParticipacaoEnum.INDICACAO)){
				prolabore.setPorcentagemIndicacao(p.getValor());
			}
			else if(p.getNome().equals(TipoParticipacaoEnum.PARTICIPACAO_SOCIO)){
				prolabore.setPorcentagemParticipacaoSocio(p.getValor());
			}
			else if(p.getNome().equals(TipoParticipacaoEnum.PARTICIPACAO_TRABALHO)){
				prolabore.setPorcentagemParticipacaoTrabalho(p.getValor());
			}
			
		}
		
		
	}
	
	public Fatura finalizarCaso(Caso caso){
		caso.setStatusCaso(StatusCaso.FINALIZADO);
			
			if (caso.getValorExito() != null && caso.getValorExito() > 0){
				ParcelaExito parcelaExito = caso.getParcelaExito();
				
				caso.setParcelaExito(parcelaExito);
				parcelaExito.setCaso(caso);
				parcelaExito.setStatusParcela(StatusTrabalho.CRIADO);
				parcelaExito.setValor(caso.getValorExito());
				
				em.persist(parcelaExito);
				em.merge(caso);
				
				Fatura fatura = faturaService.gerarFaturaExito(parcelaExito);
				
				return fatura;
			}
			else {
				em.merge(caso);
				return null;
			}
	}
	
	
	public void alterarCaso(Caso caso,boolean alteraSomenteParticipacao) {
		if (caso.getStatusCaso().equals(StatusCaso.FINALIZADO)){
			if (!alteraSomenteParticipacao){
				throw new ControleException("Caso já está finalizado.");
			}
		}
		
		caso.setValorFixo(NumeroUtil.deixarFloatDuasCasas(caso.getValorFixo()));

		if (caso.getTipoFaturaCaso().equals(TipoFaturaCasoEnum.FIXO)) {

			caso.setParcelasPagas(0);
			// deixando floats com duas casas...
			caso.setValorFixo(NumeroUtil.deixarFloatDuasCasas(caso
					.getValorFixo()));

			List<Prolabore> removerProlabores = new ArrayList<Prolabore>();

			for (Prolabore p : caso.getListaProlabole()) {
				if (p.getValor() == null || p.getValor().floatValue() == 0) {
					removerProlabores.add(p);
				}
				else {
					if (p.getId() == null){
						carregarPorcentagemDistribuicao(p);
						p.setPreParticipacaoCadastrada(false);
						p.setStatusProlaboreEnum(StatusTrabalho.CRIADO);
						
						Float valorPreParticipacaoSemImposto = participacaoService.obterValorTotalTrabalhoSemImposto(p.getValor().floatValue(),p.getPorcentagemImposto());
						Float valorParticipacaoTrabalho = participacaoService.obterValorParticipacaoTrabalho
								(p.getPorcentagemParticipacaoTrabalho(), valorPreParticipacaoSemImposto, 0f, false, 0);
						p.setValorParticipacaoTrabalho(valorParticipacaoTrabalho);
					}
				}
			}
			caso.getListaProlabole().remove(removerProlabores);

		} else if (caso.getTipoFaturaCaso().equals(TipoFaturaCasoEnum.VARIAVEL)) {
			// se for variavel não possui prolabores
			caso.setListaProlabole(null);
		}

		em.merge(caso);
	}

	public Caso obterCaso(Integer idCaso) {
		StringBuffer hql = new StringBuffer("FROM Caso c ");
		hql.append(" LEFT JOIN FETCH c.cliente cliente ");
		hql.append(" LEFT JOIN FETCH c.tipoCaso tipoCaso ");
		hql.append(" LEFT JOIN FETCH c.listaUsuarioIndicacao indicacoes ");
	
		hql.append(" WHERE  " );
		hql.append(" c.idCaso = :idCaso ");
		Query query = em.createQuery(hql.toString(), Caso.class);

		query.setParameter("idCaso", idCaso);
		
		
		Caso caso = (Caso) query.getSingleResult();
		caso.getListaProlabole().size();
		return caso;
	}

	public void exlcuirCaso(Caso caso) throws Exception {
		String sql = "FROM Trabalho t where t.caso.idCaso = :idCaso";
		Query query = em.createQuery(sql);
		query.setParameter("idCaso", caso.getIdCaso());

		if (query.getResultList().size() == 0) {
			em.remove(em.find(Caso.class, caso.getIdCaso()));
		} else {
			throw new AlmiranteException(
					"Caso já possui referência não é possivel ser deletado");
		}
	}

	public List<Caso> listarCasoPorCliente(Integer idCliente) {

		String sql = "FROM Caso c WHERE c.cliente.idCliente = :idCliente";
		TypedQuery<Caso> query = em.createQuery(sql, Caso.class);
		query.setParameter("idCliente", idCliente);

		return query.getResultList();
	}
}
