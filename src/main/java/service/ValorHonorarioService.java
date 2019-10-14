package service;

import java.util.List;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import entity.Cliente;
import entity.PapelEnum;
import entity.ValorHonorario;

@Stateless
@LocalBean
public class ValorHonorarioService {
	
	@PersistenceContext(unitName = "controle")
	private EntityManager em;
	
	@EJB
	private ClienteService clienteService;
	
	public void alterarListaHonorarios(List<ValorHonorario> listValorHonorario){
		
		//Alterar o valor alterar todos os clientes também.
		
		List<Cliente> listaCliente = clienteService.pesquisarCliente(new Cliente());
		
		
		for (ValorHonorario valorHonorario : listValorHonorario) {
			
			for (Cliente cliente : listaCliente) {
				if (valorHonorario.getNome().equals(PapelEnum.ADVOGADO_JUNIOR.getDescricao())){
					cliente.setValorHoraJunior(valorHonorario.getValorHora());
				}
				else if(valorHonorario.getNome().equals(PapelEnum.ESTAGIARIO.getDescricao())){
					cliente.setValorHoraEstagiario(valorHonorario.getValorHora());
				}
				else if(valorHonorario.getNome().equals(PapelEnum.ADVOGADO_PLENO.getDescricao())){
					cliente.setValorHoraPleno(valorHonorario.getValorHora());
				}
				else {
					cliente.setValorHoraSenior(valorHonorario.getValorHora());
				}
				
				em.merge(cliente);
			}
			
			
			em.merge(valorHonorario);
		
		
		}
	}
	
	public List<ValorHonorario> listarHonorarios(){
		
		List resultList = em.createQuery("FROM ValorHonorario order by descricao").getResultList();
		
		return resultList;
		
	}
	
	public Float obterValorHoraPorPapel(String nomePapel){
		Query query = em.createQuery("FROM ValorHonorario vh WHERE vh.nome = :nomePapel");
		query.setParameter( "nomePapel",nomePapel);
		
		ValorHonorario vh = (ValorHonorario) query.getSingleResult();
		 
		return vh.getValorHora();
	}
}
