package service;

import java.util.List;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import control.exception.ExceptionHandler;
import entity.Conta;
import entity.Fatura;
import entity.ParticipacaoDefault;
import entity.TipoContaEnum;
import entity.TipoParticipacaoEnum;
import entity.Trabalho;
import entity.Usuario;

@Stateless
@LocalBean
@ExceptionHandler
public class AdmService {
	
	@PersistenceContext(unitName = "controle")
	private EntityManager em;
	
	@EJB
	private FaturaService faturaService;
	
	@EJB
	private ParticipacaoDefaultService participacaoDefaultService;
	
	@EJB
	private ParticipacaoService participacaoService;
	
	@EJB
	private UsuarioService usuarioService;
	
	@EJB
	private TrabalhoService trabalhoService;


	@EJB
	private ContaService contaService;
	
	public void popularBase() throws Exception{
		
		Float valor = participacaoDefaultService.obterValorPorNome(TipoParticipacaoEnum.FUNDO);
		if (valor == null){

			List<Fatura> listFatura = faturaService.pesquisarFatura(new Fatura(), null, null);
			
			List<Usuario> listaUser = usuarioService.pesquisarUsuario(null);
			
			List<Trabalho> listaTrabalho = trabalhoService.pesquisarTrabalho(null, null, null, null);
			
			for (Trabalho trabalho : listaTrabalho) {
				trabalho.getValorTotalTrabalho();
				trabalhoService.alterarStatusTrabalho(trabalho);
			}
			
			//inserindo participação default;
			
			ParticipacaoDefault pd1 = new ParticipacaoDefault();
			pd1.setDescricao("Fundo");
			pd1.setNome(TipoParticipacaoEnum.FUNDO);
			pd1.setValor(27.5f);
			
			ParticipacaoDefault pd2 = new ParticipacaoDefault();
			pd2.setDescricao("Imposto");
			pd2.setNome(TipoParticipacaoEnum.IMPOSTO);
			pd2.setValor(12f);
			
			ParticipacaoDefault pd3 = new ParticipacaoDefault();
			pd3.setDescricao("Indicação");
			pd3.setNome(TipoParticipacaoEnum.INDICACAO);
			pd3.setValor(10f);
			
			ParticipacaoDefault pd4 = new ParticipacaoDefault();
			pd4.setDescricao("Fundo participação");
			pd4.setNome(TipoParticipacaoEnum.PARTICIPACAO_SOCIO);
			pd4.setValor(32.5f);
			
			ParticipacaoDefault pd5 = new ParticipacaoDefault();
			pd5.setDescricao("Critério Trabalho");
			pd5.setNome(TipoParticipacaoEnum.PARTICIPACAO_TRABALHO);
			pd5.setValor(30f);
			
			em.persist(pd1);
			em.persist(pd2);
			em.persist(pd3);
			em.persist(pd4);
			em.persist(pd5);
			
			for (Usuario usuario : listaUser) {
				if (contaService.obterContaPorAdvogado(usuario.getIdUsuario()) == null){
					Conta conta = new Conta();
					conta.setAdvogado(usuario);
					conta.setTipoContaEnum(TipoContaEnum.ADVOGADO);
					conta.setValorDisponivel(0f);
					
					em.persist(conta);
					
				}
			}
			Conta contaImposto = new Conta();
			
			contaImposto.setTipoContaEnum(TipoContaEnum.IMPOSTO);
			contaImposto.setValorDisponivel(0f);
			
			Conta contaFundo = new Conta();
			
			contaFundo.setTipoContaEnum(TipoContaEnum.FUNDO);
			contaFundo.setValorDisponivel(0f);
			
			em.persist(contaFundo);
			em.persist(contaImposto);
			
			for (Fatura fatura : listFatura) {
				fatura.setPorcentagemFundo(pd1.getValor());
				fatura.setPorcentagemImposto(pd2.getValor());
				fatura.setPorcentagemIndicacao(pd3.getValor());
				fatura.setPorcentagemParticipacaoSocio(pd4.getValor());
				fatura.setPorcentagemParticipacaoTrabalho(pd5.getValor());
				fatura.setValorPago(0f);
				
				participacaoService.cadastrarParticipacao(fatura);
				
				em.merge(fatura);
			}
		
		}
		else {
			
		}
			
	}
}
