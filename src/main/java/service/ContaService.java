package service;

import java.util.List;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import util.NumeroUtil;
import control.exception.ExceptionHandler;
import dto.ResumoFinanceiroDTO;
import entity.Conta;
import entity.TipoContaEnum;



@Stateless
@LocalBean
@ExceptionHandler
public class ContaService {
	@PersistenceContext(unitName = "controle")
	private EntityManager em;
	
	@EJB
	private ContaService contaService;
	
	public void alterarConta(Conta conta){
		conta.setValorDisponivel(NumeroUtil.deixarFloatDuasCasas(conta.getValorDisponivel()));
		em.merge(conta);
	}
	
	public Conta obterConta(Integer idConta){
		
		return em.find(Conta.class, idConta);
	}
	
	public void exlcuirContaPorAdvogado(Integer idAdvogado) throws Exception{
		String sql = "FROM Conta c where c.advogado.idUsuario = :idUsuario";
		Query query = 	em.createQuery(sql);
		query.setParameter("idUsuario", idAdvogado);
		
		if (query.getResultList().size() == 1){
			em.remove(em.find(Conta.class, ((Conta)query.getSingleResult()).getIdConta()));
		}
		else {
			System.out.println("Mais de uma conta encontrada para mesmo user");
			throw new Exception("Não foi possível deletar conta.");
		}
	}
	
	public Conta obterContaPorAdvogado(Integer idAdvogado){
		try{
			String sql = "FROM Conta c WHERE c.advogado.idUsuario = :idAdvogado";
			TypedQuery<Conta> query = 	em.createQuery(sql,Conta.class);
			query.setParameter("idAdvogado", idAdvogado);
			
			return query. getSingleResult();
			
		}
		catch (NoResultException e) {
			// TODO: handle exception
			return null;
		}
	}
	
	public Conta obterContaFundoImposto(TipoContaEnum tipoConta){
		
		String sql = "FROM Conta c WHERE c.tipoContaEnum = :tipoConta";
		TypedQuery<Conta> query = 	em.createQuery(sql,Conta.class);
		query.setParameter("tipoConta", tipoConta);
		
		return query.getSingleResult();
	}
	
	public List<Conta> listarContas(){
		
		String sql = "FROM Conta c ";
		TypedQuery<Conta> query = 	em.createQuery(sql,Conta.class);
		
		return query.getResultList();
	}
	
	public ResumoFinanceiroDTO obterResumoFinanceiro(){
		
		ResumoFinanceiroDTO resumo = new ResumoFinanceiroDTO();
		
		List<Conta> contas = contaService.listarContas();
		
		for (Conta conta : contas) {
			Float valor = conta.getValorDisponivel();
			
			resumo.setSaldoEmConta(NumeroUtil.somarDinheiro(resumo.getSaldoEmConta(), valor, 3));
			
			if (conta.getTipoContaEnum().equals(TipoContaEnum.IMPOSTO)){
				resumo.setSaldoReservadoImposto(valor);
			} else if (conta.getTipoContaEnum().equals(TipoContaEnum.FUNDO)){
				resumo.setSaldoDescontado(valor);
			} else{
				resumo.setSaldoReservadoAdvogado(NumeroUtil.somarDinheiro(resumo.getSaldoReservadoAdvogado(), valor, 3));
			}
			
		
		}
		
		return resumo;
	}
	

}
