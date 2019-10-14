package service;

import java.util.List;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import util.NumeroUtil;
import entity.ParticipacaoDefault;
import entity.TipoParticipacaoEnum;
import exception.ControleException;

@Stateless
@LocalBean
public class ParticipacaoDefaultService {

	@PersistenceContext(unitName = "controle")
	private EntityManager em;

	public void alterarListaParticipacaoDefault( List<ParticipacaoDefault> listParticipacaoDefault) throws Exception {
		Float porcentagem = 0f;
		for (ParticipacaoDefault participacaoDefault : listParticipacaoDefault) {
			if ((isParticipacaoSemImposto(participacaoDefault))){
				porcentagem = NumeroUtil.somarDinheiro(porcentagem, participacaoDefault.getValor(), 3);
			}
			em.merge(participacaoDefault);
		}
		
		if (!porcentagem.equals(100.0F)){
			throw new ControleException("Valor de participação deve ser de 100%");
		}

	}

	private boolean isParticipacaoSemImposto(
			ParticipacaoDefault participacaoDefault) {
		
		if (participacaoDefault == null){
			return false; 
		} else if(participacaoDefault.getIdParticipacaoDefault().equals(1) || participacaoDefault.getIdParticipacaoDefault().equals(3) || participacaoDefault.getIdParticipacaoDefault().equals(4) || participacaoDefault.getIdParticipacaoDefault().equals(5) ) {
			return true;
		}
		
		return false;
	}

	public List<ParticipacaoDefault> listarParticipacaoDefault() {

		List<ParticipacaoDefault> resultList = em.createQuery("FROM ParticipacaoDefault order by idParticipacaoDefault",ParticipacaoDefault.class).getResultList();

		return resultList;

	}

	public Float obterValorPorNome(TipoParticipacaoEnum tipoParticipacaoEnum) {
		Query query = em	.createQuery("FROM ParticipacaoDefault pd WHERE pd.nome = :nome");
		query.setParameter("nome", tipoParticipacaoEnum);
        
		try {
			ParticipacaoDefault pd = (ParticipacaoDefault) query.getSingleResult();
			return pd.getValor();
			
		} catch (NoResultException e) {
			return null;
		}

	}
}
