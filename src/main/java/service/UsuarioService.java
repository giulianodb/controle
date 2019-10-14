package service;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.DuplicateKeyException;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import util.NumeroUtil;

import dto.participacao.ParticipacaoUsuarioDTO;
import entity.Conta;
import entity.Participacao;
import entity.StatusUsuario;
import entity.TipoContaEnum;
import entity.Usuario;

@Stateless
@LocalBean
public class UsuarioService {
	
	@PersistenceContext(unitName = "controle")
	private EntityManager em;
	
	@EJB
	private UsuarioService usuarioService;
	
	@EJB
	private ParticipacaoService participacaoService;

	@EJB
	private ContaService contaService;
	
	public Usuario obterUsuarioPorLogin(String login){
		
		Query query = em.createNamedQuery("obterPorLogin");
		query.setParameter("login", login);
		
		return (Usuario)query.getSingleResult();
	}
	
	public Usuario obterUsuarioPorId(Integer id){
		
		TypedQuery<Usuario> query = em.createQuery("FROM Usuario u WHERE u.idUsuario = :id",Usuario.class);
		query.setParameter("id", id);
		
		return query.getSingleResult();
	}
	
	public void incluirRecuso(Usuario usuario) throws DuplicateKeyException {
		usuario.setStatusUsuario(StatusUsuario.ATIVO);
		
		if (usuarioService.loginDisponivel(usuario, true)){
			Conta conta = new Conta();
			conta.setAdvogado(usuario);
			conta.setTipoContaEnum(TipoContaEnum.ADVOGADO);
			conta.setValorDisponivel(0F);
			em.persist(conta);
			em.persist(usuario);
			
		}
		else {
			throw new DuplicateKeyException("Login já cadastrado.");
		}
		
		
	}

	public void excluirUsuario(Usuario usuario) throws Exception {
		
		//Verifica se usuário possui alguma dependencia
		
		boolean delecaoLogica = true;
		
		String stringQuery = "FROM Caso c Where c.advogado.id = :idUsuario";
		Query query = em.createQuery(stringQuery);
		query.setParameter("idUsuario", usuario.getIdUsuario());
		
		if (query.getResultList().size() == 0){
			delecaoLogica = false;
		} 
		
		//Caso não for lógica realiza a pesquisa para encontrar trabalhos.
		// caso não f
		if(!delecaoLogica){
		
			String stringQueryTrabalho = "FROM Trabalho t Where t.advogado.id = :idUsuario";
			Query queryTrabalho = em.createQuery(stringQueryTrabalho);
			queryTrabalho.setParameter("idUsuario", usuario.getIdUsuario());

			if (queryTrabalho.getResultList().size() == 0){
				delecaoLogica = false;
			}
			else {
				delecaoLogica = true;
			}
		}
		if (delecaoLogica){
			usuario.setStatusUsuario(StatusUsuario.INATIVO);
			
			em.merge(usuario);
		}
		else {
			usuario = em.find(Usuario.class, usuario.getIdUsuario());
			contaService.exlcuirContaPorAdvogado(usuario.getIdUsuario());
			
			em.remove(usuario);
		}
	}

	public void alterarUsuario(Usuario usuario) throws DuplicateKeyException {
		
		if (usuarioService.loginDisponivel(usuario, false)){
			em.merge(usuario);
		}
		else {
			throw new DuplicateKeyException("Login já cadastrado.");
		}
	}
	
	public List<Usuario> listarAdvogados() {
		StringBuilder stringQuery = new StringBuilder();
		stringQuery.append("FROM Usuario u ");
		
			stringQuery.append("WHERE u.statusUsuario = :status AND (u.papel.nomePapel = :papelJunior OR u.papel.nomePapel = :papelPleno OR u.papel.nomePapel = :papelSenior OR  u.papel.nomePapel = :papelEstagiario) ");
			stringQuery.append(" ORDER BY u.nome ");
			
			//query.setParameter("nomeCliente", "%"+caso.getCliente().getNome()+"%");
		
		
		//TODO criar order by nome
		Query query = em.createQuery(stringQuery.toString());
		
			
			query.setParameter("papelJunior", "Advogado_Junior");
			query.setParameter("papelPleno", "Advogado_Pleno");
			query.setParameter("papelSenior", "Advogado_Senior");
			query.setParameter("papelEstagiario", "Estagiario");
			query.setParameter("status", StatusUsuario.ATIVO);
		
		List<Usuario> lista = query.getResultList(); 
		
		return lista;
	}
	
	
	public List<Usuario> pesquisarUsuario(Usuario usuario) {
		
		StringBuilder stringQuery = new StringBuilder();
		stringQuery.append("FROM Usuario u ");
		
		if (usuario != null && usuario.getNome()!=null && !"".equals(usuario.getNome())){
			stringQuery.append("WHERE lower (u.nome) like :nome ");
			
			//query.setParameter("nomeCliente", "%"+caso.getCliente().getNome()+"%");
		}
		
		
		//TODO criar order by nome
		Query query = em.createQuery(stringQuery.toString());
		
		if (usuario != null && usuario.getNome()!=null && !"".equals(usuario.getNome())){
			
			query.setParameter("nome", "%"+usuario.getNome().toLowerCase()+"%");
		}
		
		List<Usuario> lista = query.getResultList(); 
		
		return lista;
		
	}
	
	public boolean loginDisponivel(Usuario usuario, boolean incluir){
		if (incluir){
			Query query = em.createQuery("FROM Usuario u WHERE u.loginUsuario = :login");
			
			query.setParameter("login", usuario.getLoginUsuario());
			
			if (query.getResultList().size() > 0){
				return false;
			}
			else {
				return true;
			}
			
		}
		else {
			Query query = em.createQuery("FROM Usuario u WHERE u.loginUsuario = :login AND u.idUsuario <> :id");
			
			query.setParameter("login", usuario.getLoginUsuario());
			query.setParameter("id", usuario.getIdUsuario());
			
			if (query.getResultList().size() > 0){
				return false;
			}
			else {
				return true;
			}
		}
	}
	
	public List<Usuario> listarSocios () {
		
		StringBuilder stringQuery = new StringBuilder();
		stringQuery.append("FROM Usuario u ");
		
		stringQuery.append("WHERE u.participacao > 0 ");
		
		//TODO criar order by nome
		Query query = em.createQuery(stringQuery.toString());
		
		List<Usuario> lista = query.getResultList(); 
		
		return lista;
		
	}
	
	//Lista todos os advogados e situação de participação (valor disponível e valor pendente)
	public List<ParticipacaoUsuarioDTO> listarSituacaoParticipacaoUsuario(){
		
		//TODO mudar para buscar somente os advogados.
		List<Usuario> listUsuarios = usuarioService.pesquisarUsuario(null);
		List<ParticipacaoUsuarioDTO> listUsuariosParticipacao = new ArrayList<ParticipacaoUsuarioDTO>();
		
		for (Usuario usuario : listUsuarios) {
			//Obtém a conta do usuário
			
			Conta conta = contaService.obterContaPorAdvogado(usuario.getIdUsuario());
			
			//obtém todas as participações com status da fatura como 'faturadao' ou 'parcialmante pago' pois estão pendentes de pagamento do cliente
			List<Participacao> listaParticipacaoUsuario = participacaoService.listarParticipacaoPendentePorAdvogado(usuario.getIdUsuario());
			float valorDisponivel = conta.getValorDisponivel();
			float valorPendente = 0f;
						
			for (Participacao participacao : listaParticipacaoUsuario) {
				Float valor1 = NumeroUtil.somarDinheiro(valorPendente, participacao.getValorTotalParticipacao(), 3);
				Float valor2 = NumeroUtil.diminuirDinheiro(valor1, participacao.getValorTotalPago(), 3);
				Float valor3 = NumeroUtil.diminuirDinheiro(valor2, participacao.getValorDisponivel(), 3);
				
				valorPendente = NumeroUtil.deixarFloatDuasCasas(valor3);
				
//				valorPendente = valorPendente + participacao.getValorTotalParticipacao() - participacao.getValorTotalPago() - participacao.getValorDisponivel();
				
			}
			
			ParticipacaoUsuarioDTO participacaoUsuarioDTO = new ParticipacaoUsuarioDTO();
			participacaoUsuarioDTO.setIdUsuario(usuario.getIdUsuario());
			participacaoUsuarioDTO.setNomeUsuario(usuario.getNome());
			participacaoUsuarioDTO.setValorDisponivel(valorDisponivel);
			participacaoUsuarioDTO.setValorPendente(valorPendente);
			
			listUsuariosParticipacao.add(participacaoUsuarioDTO);
		
		}
		
		return listUsuariosParticipacao;
	}
	
}
