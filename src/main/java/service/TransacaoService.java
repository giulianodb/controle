package service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import util.NumeroUtil;
import control.exception.ExceptionHandler;
import entity.Conta;
import entity.Fatura;
import entity.Participacao;
import entity.StatusTrabalho;
import entity.TipoContaEnum;
import entity.TipoTransacaoEnum;
import entity.Trabalho;
import entity.Transacao;
import exception.ControleException;



@Stateless
@LocalBean
@ExceptionHandler
public class TransacaoService {
	@PersistenceContext(unitName = "controle")
	private EntityManager em;
	
	@EJB
	private ContaService contaService;
	
	@EJB
	private ParticipacaoService participacaoService;
	
	@EJB
	private TransacaoService transacaoService;
	
	@EJB
	private FaturaService faturaService;
	
	@EJB
	private TrabalhoService trabalhoService;
	
	
	//@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void criarTransacaoParaFatura(Fatura fatura, TipoTransacaoEnum tipoTranscao ){
//		float percentagem = fatura.getValorPagamento() / fatura.valorTotalFatura();

		//Criando a transacação
		Transacao transacao = new Transacao();
		transacao.setDataTransacao(fatura.getDataPagamento());
		transacao.setTipoTransacaoEnum(tipoTranscao);
		transacao.setValorTransacao(fatura.getValorPagamento());
		transacao.setFatura(fatura);
		
		transacaoService.inserirTransacao(transacao);
		
		
		for (Participacao participacao : participacaoService.listarParticipacaoPorFatura(fatura.getIdFatura())) {
				//Alterar o valor disponível da participação no momento do pagamento.
				
				BigDecimal valorDisponivelParticipacao = new BigDecimal(participacao.getValorDisponivel().toString());
				BigDecimal valorTotalParticipacao = new BigDecimal(participacao.getValorTotalParticipacao().toString());
			
				participacao.setValorDisponivel((valorDisponivelParticipacao.add(valorTotalParticipacao)).floatValue());
				participacao.setTransacaoPagamentoFatura(transacao);
				participacaoService.alterarParticipacao(participacao);
				
				//Para cada transação devo alterar o valor da conta também.
				Conta conta = new Conta();
				
				if (participacao.getAdvogado() !=null && participacao.getAdvogado().getIdUsuario() != null){
					conta = contaService.obterContaPorAdvogado(participacao.getAdvogado().getIdUsuario());
				}
				else if(participacao.getFundo() != null && participacao.getFundo()){
					conta = contaService.obterContaFundoImposto(TipoContaEnum.FUNDO);
				}
				else {
					conta = contaService.obterContaFundoImposto(TipoContaEnum.IMPOSTO);
				}
					
//				BigDecimal valorDisponivelConta = new BigDecimal(conta.getValorDisponivel().toString());
				
				Float valorDisponivel = NumeroUtil.somarDinheiro(conta.getValorDisponivel(), participacao.getValorTotalParticipacao(), 3);
				
				conta.setValorDisponivel(NumeroUtil.deixarFloatDuasCasas(valorDisponivel));
				
				contaService.alterarConta(conta);
				
			}
	}
	
	public Transacao obterTransacao(Integer idTransacao){
		return em.find(Transacao.class, idTransacao);
	}
	
	public void inserirTransacaoManual(Transacao transacao){
		transacaoService.inserirTransacao(transacao);
		Conta conta = new Conta();
		
		if (transacao.getTipoTransacaoEnum().equals(TipoTransacaoEnum.IMPOSTO)){
			conta = contaService.obterContaFundoImposto(TipoContaEnum.IMPOSTO);
		}
		else {
			conta = contaService.obterContaFundoImposto(TipoContaEnum.FUNDO);
		}
		
		Float valorAtualizado = conta.getValorDisponivel();
		
		if(transacao.getTipoTransacaoEnum().equals(TipoTransacaoEnum.SAIDA) || transacao.getTipoTransacaoEnum().equals(TipoTransacaoEnum.IMPOSTO)){
			valorAtualizado = NumeroUtil.diminuirDinheiro(valorAtualizado, transacao.getValorTransacao(), 3);
			
		} else{
			valorAtualizado = NumeroUtil.somarDinheiro(valorAtualizado, transacao.getValorTransacao(), 3);
		}
		
		conta.setValorDisponivel(valorAtualizado);
		
		contaService.alterarConta(conta);
		
		
		
	}
	
	public void inserirTransacao(Transacao transacao){
		transacao.setValorTransacao(NumeroUtil.deixarFloatDuasCasas(transacao.getValorTransacao()));
		em.persist(transacao);
	}
	
	public List<Transacao> pesquisarTransacaoCliente(Date dataInicialPesquisa,Date dataFinalPesquisa, Integer idCliente){
		
		StringBuilder sb = new StringBuilder("FROM Transacao t ");
		
//		sb.append(" left join fetch t. ");
		
		sb.append(" WHERE ");
		
		if ( dataInicialPesquisa != null){
			sb.append(" t.dataTransacao >= :dataInicio AND ");
		}
		
		if ( dataFinalPesquisa != null){
			sb.append(" t.dataTransacao <= :dataFim AND ");
		}
		
		if ( idCliente != null && idCliente != 0){
			sb.append(" t.fatura.cliente.idCliente = :idCliente AND ");
		}
		
		sb.append(" t.tipoTransacaoEnum = :tipoEnum ");
		
		TypedQuery<Transacao> query = em.createQuery(sb.toString(),Transacao.class);
		
		if ( dataInicialPesquisa != null){
			query.setParameter("dataInicio", dataInicialPesquisa);
		}
		
		if ( dataFinalPesquisa != null){
			query.setParameter("dataFim", dataFinalPesquisa);
		}
		
		if ( idCliente != null && idCliente != 0){
			query.setParameter("idCliente", idCliente);
		}
		
		query.setParameter("tipoEnum", TipoTransacaoEnum.DISPONIBILIZADO);
		
		
		return query.getResultList();
	}
	
	
	
	public List<Transacao> pesquisarTransacao(Date dataInicialPesquisa,Date dataFinalPesquisa, Integer idCliente, Integer idAdvogado, TipoTransacaoEnum tipo){
		
		StringBuilder sb = new StringBuilder("FROM Transacao t ");
		
//		sb.append(" left join fetch t. ");
		
		sb.append(" WHERE ");
		
		if ( dataInicialPesquisa != null){
			sb.append(" t.dataTransacao >= :dataInicio AND ");
		}
		
		if ( dataFinalPesquisa != null){
			sb.append(" t.dataTransacao <= :dataFim AND ");
		}
		
		if ( idCliente != null && idCliente != 0 && tipo.equals(TipoTransacaoEnum.DISPONIBILIZADO)){
			sb.append(" t.fatura.cliente.idCliente = :idCliente AND ");
		}
		
		
		if ( idAdvogado != null && idAdvogado != 0 && tipo.equals(TipoTransacaoEnum.PAGAMENTO)){
			sb.append(" t.conta.advogado.idUsuario = :idUsuario AND ");
		}
		
		if ( tipo != null){
			sb.append(" t.tipoTransacaoEnum = :tipoEnum AND ");
		}
		
		sb.append(" 1 = 1");
		
		
		TypedQuery<Transacao> query = em.createQuery(sb.toString(),Transacao.class);
		
		if ( dataInicialPesquisa != null){
			query.setParameter("dataInicio", dataInicialPesquisa);
		}
		
		if ( dataFinalPesquisa != null){
			query.setParameter("dataFim", dataFinalPesquisa);
		}
		
		if ( idCliente != null && idCliente != 0 && tipo.equals(TipoTransacaoEnum.DISPONIBILIZADO)){
			query.setParameter("idCliente", idCliente);
		}
		if ( idAdvogado != null && idAdvogado != 0 && tipo.equals(TipoTransacaoEnum.PAGAMENTO)){
			query.setParameter("idUsuario", idAdvogado);
		}
		
		if ( tipo != null){
			query.setParameter("tipoEnum", tipo);
		}
		
		
		return query.getResultList();
	}

	
	
	public List<Transacao> pesquisarTransacaoAdvogado(Date dataInicialPesquisa,Date dataFinalPesquisa, Integer idAdvogado){
		
		StringBuilder sb = new StringBuilder("FROM Transacao t WHERE ");
		
		if ( dataInicialPesquisa != null){
			sb.append(" t.dataTransacao >= :dataInicio AND ");
		}
		
		if ( dataFinalPesquisa != null){
			sb.append(" t.dataTransacao <= :dataFim AND ");
		}
		
		if ( idAdvogado != null && idAdvogado != 0){
			sb.append(" t.conta.advogado.idUsuario = :idUsuario AND ");
		}
		
		sb.append(" t.tipoTransacaoEnum = :tipoEnum ");
		
		TypedQuery<Transacao> query = em.createQuery(sb.toString(),Transacao.class);
		
		if ( dataInicialPesquisa != null){
			query.setParameter("dataInicio", dataInicialPesquisa);
		}
		
		if ( dataFinalPesquisa != null){
			query.setParameter("dataFim", dataFinalPesquisa);
		}
		
		if ( idAdvogado != null && idAdvogado != 0){
			query.setParameter("idUsuario", idAdvogado);
		}
		
		query.setParameter("tipoEnum", TipoTransacaoEnum.PAGAMENTO);
		
		
		return query.getResultList();
	}
	
	
	public void deletarTransacao(Integer idTransacao) throws Exception {
		Transacao transacao = transacaoService.obterTransacao(idTransacao);
		
		if (transacao.getTipoTransacaoEnum().equals(TipoTransacaoEnum.PAGAMENTO)){
			deletarTransacaoAdvogado(transacao);
		}else if(transacao.getTipoTransacaoEnum().equals(TipoTransacaoEnum.DISPONIBILIZADO)) {
			deletarTransacaoCliente(transacao);
		}
		else if(transacao.getTipoTransacaoEnum().equals(TipoTransacaoEnum.IMPOSTO)){
			deletarTransacaoImposto(transacao);
		}
		else if(transacao.getTipoTransacaoEnum().equals(TipoTransacaoEnum.ENTRADA)){
			deletarTransacaoEntrada(transacao);
		}
		else if(transacao.getTipoTransacaoEnum().equals(TipoTransacaoEnum.SAIDA)){
			deletarTransacaoSaida(transacao);
		}
		
	}
	
	public void deletarTodasTransacao(List<Transacao> lista) throws Exception {
		int i=0;
		if (lista != null && lista.size() > 0){
			for (Transacao transacao2 : lista) {
				if(transacao2.getTipoTransacaoEnum() != null){
					transacaoService.deletarTransacao(transacao2.getIdTranasacao());
					System.out.println(i++);
				}
			}
			
		}
		
	}
	
/**
 * Deletar a transcaçõa de cliente.
 * Primeiro passo: Verifica se as participações da fatura em questão possui alguma transacação de advogado.
 * se tiver lança exception dizendo que já foram reallizadas pagamanetos para advogado.
 * 
 * Para deletar precisamos obter a lista de participações da fatura da transcao;
 * obter a conta do advogado da participação, reverter o valor disponível para 0
 * 
 * 
 * @param transacao
 * @throws Exception 
 */
	
	public void deletarTransacaoCliente(Transacao transacao) throws Exception{
//		transacao.getFatura().
		
		List<Participacao> listaParticipacao = participacaoService.listarParticipacaoPorFatura(transacao.getFatura().getIdFatura());
		
		for (Participacao participacao : listaParticipacao) {
			//verifica se possui pagamento para advogado
			if (participacao.getTransacaoPagamentoAdvogado() != null){
				throw new ControleException("Não é possível deletar pagamento de cliente. Fatura já foi paga para advogado.");
			}
			
			//else, continua processo
			
			Conta conta;
			
			//caso for advogado...
			if (participacao.getAdvogado() != null){
				conta = contaService.obterContaPorAdvogado(participacao.getAdvogado().getIdUsuario());
			}
			else {
				if (participacao.getFundo()!=null && participacao.getFundo()){
					conta = contaService.obterContaFundoImposto(TipoContaEnum.FUNDO);
				}
				else {
					conta = contaService.obterContaFundoImposto(TipoContaEnum.IMPOSTO);
				}
			}
			
			
			Float valorCorrigidoConta = NumeroUtil.diminuirDinheiro(conta.getValorDisponivel(), participacao.getValorDisponivel(), 3);
			
			conta.setValorDisponivel(NumeroUtil.deixarFloatDuasCasas(valorCorrigidoConta));
			
			//altera valor disponível de advogado.
			contaService.alterarConta(conta);
			
			
			//alterando as participações
			participacao.setValorDisponivel(0f);
			participacao.setTransacaoPagamentoFatura(null);
			
			participacaoService.alterarParticipacao(participacao);
			
		}
		
		Fatura fatura = transacao.getFatura();
		fatura.setValorPago(0f);
		fatura.setStatusFatura(StatusTrabalho.FATURADO);
		faturaService.alterarFaturaParaPago(fatura);
		
		for (Trabalho trabalho : fatura.getListaTrabalhos()) {
			trabalho.setStatusTrabalho(fatura.getStatusFatura());
			trabalhoService.alterarStatusTrabalho(trabalho);
		}
		
		em.remove(transacao);
		
	}
	
	public void deletarTransacaoAdvogado(Transacao transacao){
		
		List<Participacao> listaParticipacao = participacaoService.listarParticipacaoPorTransacao(transacao.getIdTranasacao());
		
		for (Participacao participacao : listaParticipacao) {
			
			participacao.setTransacaoPagamentoAdvogado(null);
			
			participacao.setValorDisponivel(participacao.getValorTotalPago());
			participacao.setValorTotalPago(0f);
			
			participacaoService.alterarParticipacao(participacao);
			
			Conta conta = contaService.obterContaPorAdvogado(participacao.getAdvogado().getIdUsuario());
			Float novoValorDisponivel = NumeroUtil.somarDinheiro(conta.getValorDisponivel(), participacao.getValorDisponivel(), 3);
			
			
			conta.setValorDisponivel(NumeroUtil.deixarFloatDuasCasas(novoValorDisponivel));
			contaService.alterarConta(conta);
			
		}
		
		em.remove(transacao);
		
	}
	
	public void deletarTransacaoEntrada(Transacao transacao){
		Conta conta = contaService.obterContaFundoImposto(TipoContaEnum.FUNDO);
		
		Float valorAtualizado = conta.getValorDisponivel();
		
		valorAtualizado = NumeroUtil.diminuirDinheiro(valorAtualizado, transacao.getValorTransacao(), 3);
		
		conta.setValorDisponivel(valorAtualizado);
		contaService.alterarConta(conta);
		em.remove(transacao);
		
	}
	public void deletarTransacaoSaida(Transacao transacao){
		Conta conta = contaService.obterContaFundoImposto(TipoContaEnum.FUNDO);
		
		Float valorAtualizado = conta.getValorDisponivel();
		
		valorAtualizado = NumeroUtil.somarDinheiro(valorAtualizado, transacao.getValorTransacao(), 3);
		
		conta.setValorDisponivel(valorAtualizado);
		contaService.alterarConta(conta);
		em.remove(transacao);
	}
	public void deletarTransacaoImposto(Transacao transacao){
		Conta conta = contaService.obterContaFundoImposto(TipoContaEnum.IMPOSTO);
		
		Float valorAtualizado = conta.getValorDisponivel();
		
		valorAtualizado = NumeroUtil.somarDinheiro(valorAtualizado, transacao.getValorTransacao(), 3);
		
		conta.setValorDisponivel(valorAtualizado);
		contaService.alterarConta(conta);
		em.remove(transacao);
	}
	
	public List<TipoTransacaoEnum> listaTipoTransacao(){
		List<TipoTransacaoEnum> lista = new ArrayList<TipoTransacaoEnum>();
		
		lista.add(TipoTransacaoEnum.DISPONIBILIZADO);
		lista.add(TipoTransacaoEnum.PAGAMENTO);
		lista.add(TipoTransacaoEnum.ENTRADA);
		lista.add(TipoTransacaoEnum.SAIDA);
		lista.add(TipoTransacaoEnum.IMPOSTO);
		
		return lista;
	}
	public List<TipoTransacaoEnum> listaTipoTransacaoManual(){
		List<TipoTransacaoEnum> lista = new ArrayList<TipoTransacaoEnum>();
		
		lista.add(TipoTransacaoEnum.ENTRADA);
		lista.add(TipoTransacaoEnum.SAIDA);
		lista.add(TipoTransacaoEnum.IMPOSTO);
		
		return lista;
	}
	
}
