package service;

import java.util.List;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import entity.Cliente;



@Stateless
@LocalBean
public class ClienteService {
	@PersistenceContext(unitName = "controle")
	private EntityManager em;
	
	public Cliente obterCliente(Integer idCliente){
		return em.find(Cliente.class, idCliente);
	}

	public List<Cliente> pesquisarCliente(Cliente cliente) {
		List<Cliente> listaCliente;
		
		StringBuffer hql = new StringBuffer("FROM Cliente c  ");
		//TODO tirar a gambiarra..
		if (cliente.getNome() != null && !"".equals(cliente.getNome())){
			hql.append(" WHERE lower (c.nome) like :nome");
		}
		
		hql.append(" order by c.nome ");
		Query query = em.createQuery(hql.toString());
		if (cliente.getNome() != null  && !"".equals(cliente.getNome())){
			query.setParameter("nome", "%"+cliente.getNome().toLowerCase()+"%");
		}
		
		listaCliente = query.getResultList();
		
		return listaCliente;
	}
	
	public void cadastrarCliente(Cliente cliente){
		em.persist(cliente);
	}
	public void alterarCliente(Cliente cliente){
		em.merge(cliente);
	}
	
	public void exlcuirCliente(Cliente cliente) throws Exception{
		String stringQuery = "FROM Caso c WHERE c.cliente.idCliente = :idCliente";
		Query query = em.createQuery(stringQuery);
		query.setParameter("idCliente", cliente.getIdCliente());
		if (query.getResultList().size() == 0){
			em.remove(em.find(Cliente.class, cliente.getIdCliente()));
			
		}
		else {
			throw new Exception("Cliente já vinculado a um caso");
		}
		
	}
}
