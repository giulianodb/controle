package service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import util.NumeroUtil;
import control.UsuarioLogadoControl;
import entity.Fatura;
import entity.StatusTrabalho;
import entity.TipoFaturaCasoEnum;
import entity.Trabalho;
import exception.ControleException;



@Stateless
//@LocalBean
public class TrabalhoService {
	@PersistenceContext(unitName = "controle")
	private EntityManager em;
	
	@Inject
	private UsuarioLogadoControl usuarioLogadoControl;
	
	@EJB
	private TrabalhoService trabalhoService;
	
	@EJB
	private ParticipacaoDefaultService participacaoDefaultService;
	

	public List<Trabalho> pesquisarTrabalho(Trabalho trabalho, Date dataInicio, Date dataFim, String nomeCliente) {
		List<Trabalho> listaTrabalho = new ArrayList<Trabalho>();
		StringBuffer hql = new StringBuffer();
		
		hql = new StringBuffer("FROM Trabalho t ");
		
		
		// left join cat.kittens as kitten
		
		hql.append(" LEFT JOIN fetch t.advogado as adv");
		hql.append(" LEFT JOIN fetch t.caso as caso");
		hql.append(" LEFT JOIN fetch t.usuarios as users ");
		hql.append(" LEFT JOIN fetch caso.cliente as cliente ");
		hql.append(" WHERE ");
		
		
		//Se usuário for advogado comum somente lista os trabalhos com status criado,.
				//Caso for um administrador lista de acordo com os paremtros passados na pesquisa
				if (usuarioLogadoControl.getUsuario().getPapel().getNomePapel().equals("Administrador")){
					if (trabalho != null && trabalho.getStatusTrabalho() != null ){
						hql.append("  t.statusTrabalho = :statusTrabalho AND ");
					}
					
				}
				else {
					hql.append("  t.statusTrabalho = :statusTrabalho AND ");
				}
		
		
		
		if (trabalho != null && trabalho.getCaso() != null && trabalho.getCaso().getNomeCaso() != null){
			hql.append(" lower (t.caso.nomeCaso) like :nomeCaso AND ");
		}
		
		if (trabalho != null && trabalho.getAdvogado() != null && trabalho.getAdvogado().getNome() != null  && !"".equals(trabalho.getAdvogado().getNome())){
			hql.append(" lower (t.advogado.nome) like :nomeAdvogado AND ");
		}
		
		
		if ( dataInicio != null){
			hql.append(" t.dataTrabalho >= :dataInicio AND ");
		}
		
		if ( dataFim != null){
			hql.append(" t.dataTrabalho <= :dataFim AND ");
		}
		
		if (nomeCliente != null && !"".equals(nomeCliente)){
			hql.append(" lower (t.caso.cliente.nome) like :nome AND ");
		}
		
		//Lista trabalhos do usuário logado. Caso for administrador lista todos
		if (!usuarioLogadoControl.getUsuario().getPapel().getNomePapel().equals("Administrador")){
			hql.append(" t.advogado.idUsuario = :idUsuario AND ");
		}
		
		hql.append(" 1 = 1 order by t.dataTrabalho desc, t.caso.cliente.nome");
		
		
		
		Query query = em.createQuery(hql.toString(),Trabalho.class).setMaxResults(100);
					
		//Se usuário for advogado comum somente lista os trabalhos com status criado,.
		//Caso for um administrador lista de acordo com os paremtros passados na pesquisa
		if (usuarioLogadoControl.getUsuario().getPapel().getNomePapel().equals("Administrador")){
			if (trabalho != null && trabalho.getStatusTrabalho() != null ){
				query.setParameter("statusTrabalho", trabalho.getStatusTrabalho());
			}
			
		}
		else {
			query.setParameter("statusTrabalho", StatusTrabalho.CRIADO);
		}
		
		
		if (trabalho != null && trabalho.getCaso() != null && trabalho.getCaso().getNomeCaso() != null){
			query.setParameter("nomeCaso", "%"+trabalho.getCaso().getNomeCaso().toLowerCase()+"%");
		}
		
		if ( dataInicio != null){
			query.setParameter("dataInicio", dataInicio);
		}
		
		if ( dataFim != null){
			query.setParameter("dataFim", dataFim);
		}
		
		if (trabalho != null && trabalho.getAdvogado() != null && trabalho.getAdvogado().getNome() != null && !"".equals(trabalho.getAdvogado().getNome())){
			query.setParameter("nomeAdvogado", "%"+trabalho.getAdvogado().getNome().toLowerCase() +"%");
		}
		
		if (nomeCliente != null && !"".equals(nomeCliente)){
			query.setParameter("nome", "%"+nomeCliente.toLowerCase()+"%");
		}
		
		//Lista trabalhos do usuário logado. Caso for administrador lista todos
		if (!usuarioLogadoControl.getUsuario().getPapel().getNomePapel().equals("Administrador")){
			query.setParameter("idUsuario", usuarioLogadoControl.getUsuario().getIdUsuario());
		}
		
		System.out.println("HQL: " + hql);
		listaTrabalho = query.getResultList();
		
		
		return listaTrabalho;
	}
	
	//Para o relatório
	public List<Trabalho> listarTrabalhoPorCliente(Integer idCliente, String mesReferencia) throws Exception{
		
		
		List<Trabalho> listaTrabalho = new ArrayList<Trabalho>();
		try {
			Date greg = dataPesquisa(mesReferencia,true);
			Date dataProximoMes = dataPesquisa(mesReferencia,false);
			
			StringBuffer hql = new StringBuffer();
			
			hql = new StringBuffer("FROM Trabalho t WHERE t.caso.cliente.idCliente = :idCliente AND t.dataEnviadoFaturar >= :dataInicial AND t.dataEnviadoFaturar < :dataFinal AND");
			
			Query query = em.createQuery(hql.toString(),Trabalho.class);
			
			query.setParameter("idCliente", idCliente);
			query.setParameter("dataInicial", greg);
			query.setParameter("dataFinal", dataProximoMes);
			
					
			listaTrabalho =	query.getResultList();
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
			throw e;
		}

		return listaTrabalho;
		
	}
	
	/**
	 * Lista de trabalhos para fatuar. Caso o idCliente for nulo lista todos os trabalhos para serem faturados, caso preenchido lista soment
	 * e os trabalhos do cliente passado por parametro
	 * @param idCliente
	 * @return
	 * @throws Exception
	 */
	public List<Trabalho> listarTrabalhoFaturar(Integer idCliente) throws Exception{
		
		
		List<Trabalho> listaTrabalho = new ArrayList<Trabalho>();
		try {
			
			StringBuffer hql = new StringBuffer();
			
			hql = new StringBuffer("FROM Trabalho t WHERE t.statusTrabalho = :status AND t.cobravel = 'true'");
			if (idCliente != null){
				hql.append(" AND t.caso.cliente.idCliente = :idCliente ");
			}
			hql.append(" order by t.dataTrabalho ASC");
			
			Query query = em.createQuery(hql.toString(),Trabalho.class);
			
			query.setParameter("status", StatusTrabalho.ENVIADO_FATURAR);
			if (idCliente != null){
				query.setParameter("idCliente", idCliente);
			}
			
					
			listaTrabalho =	query.getResultList();
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
			throw e;
		}

		return listaTrabalho;
		
	}
	
	public void cadastrarTrabalho(Trabalho trabalho){ 
		
		//TODO mudar o papel para um ennum
		//TODO adicionar adivogado senior senão um cara qualquer.... precisa mesmo??
		Float valorHora = 0f;
		if ("Advogado_Junior".equals(trabalho.getAdvogado().getPapel().getNomePapel())){
			valorHora = trabalho.getCaso().getCliente().getValorHoraJunior();
			
		}
		else if ("Advogado_Pleno".equals(trabalho.getAdvogado().getPapel().getNomePapel())){
			valorHora = trabalho.getCaso().getCliente().getValorHoraPleno();
			
		}
		else if ("Estagiario".equals(trabalho.getAdvogado().getPapel().getNomePapel())){
			valorHora = trabalho.getCaso().getCliente().getValorHoraEstagiario();
			
		}else {
			valorHora = trabalho.getCaso().getCliente().getValorHoraSenior();
			
		}
		
		Float valorTotal = NumeroUtil.multiplicarDinheiro(trabalho.getHorasTrabalho(), valorHora, 3);
		
		trabalho.setValorTotalTrabalho(NumeroUtil.deixarFloatDuasCasas(valorTotal));
		trabalho.setValorHora(valorHora);
		trabalho.setCobravel(true);
		trabalho.setStatusTrabalho(StatusTrabalho.CRIADO);
		
		//Verifica se o caso é fixo ou variavel. se for fixo deixar como não cobravel todos os trabalhos
		
		if (trabalho.getCaso() != null && trabalho.getCaso().getTipoFaturaCaso() != null &&trabalho.getCaso().getTipoFaturaCaso().equals(TipoFaturaCasoEnum.FIXO) ){
			trabalho.setCobravel(false);
		}
		
		em.persist(trabalho);
	}
	public void alterarTrabalho(Trabalho trabalho) throws Exception{
		
		//adicionar compartilhmaneto caso a lista estiver populada.
		if (trabalho.getUsuarios() != null && trabalho.getUsuarios().size() > 0){
			trabalho.setTrabalhoCompartilhado(true);
		}
		else {
			trabalho.setTrabalhoCompartilhado(false);
		}
		
		
		if (trabalho.getStatusTrabalho().equals(StatusTrabalho.CRIADO) || usuarioLogadoControl.getUsuario().getPapel().getNomePapel().equals("Administrador")){
			trabalho.setValorTotalTrabalho(NumeroUtil.deixarFloatDuasCasas(trabalho.getValorTotalTrabalho()));
			em.merge(trabalho);
		}
		else {
			throw new ControleException("Trabalho já enviado para faturamento.");
		}
		
	}
	//TODO alterar esse nome
	public void alterarStatusTrabalho(Trabalho trabalho) throws Exception{
		em.merge(trabalho);
	}
	
	
	public void exlcuirTrabalho(Trabalho trabalho) throws ControleException{
		
		trabalho = trabalhoService.obterTrabalho(trabalho.getIdTrabalho());
		
		if (trabalho.getStatusTrabalho().equals(StatusTrabalho.CRIADO) || usuarioLogadoControl.getUsuario().getPapel().getNomePapel().equals("Administrador")){
			  Query query = em.createQuery(
				      "DELETE FROM Trabalho t WHERE t.idTrabalho = :id");
				  int deletedCount = query.setParameter("id", trabalho.getIdTrabalho()).executeUpdate();

		}
		else {
			throw new ControleException("Trabalho já enviado para faturamento.");
		}
		
	}
	
	
	//TODO alterar nome para enviar para faturar
	public void alterarFaturadoListaTrabalho(List<Trabalho> listaTrabalho) throws Exception {
		for (Trabalho trabalho : listaTrabalho) {
					if (trabalho.getStatusTrabalho().equals(StatusTrabalho.ENVIADO_FATURAR)){
						trabalho.setStatusTrabalho(StatusTrabalho.FATURADO);
					
					}
					else{
						throw new ControleException("Trabalho já faturado");
					}
				//Se não for faturar somente salva as alteraçõs
				trabalhoService.alterarStatusTrabalho(trabalho);
		}
		
	}
	
	public Trabalho obterTrabalho(Integer idTrabalho){
		
		
		StringBuffer hql = new StringBuffer();
		
		hql = new StringBuffer("FROM Trabalho t ");
		
		
		// left join cat.kittens as kitten
		
		hql.append(" LEFT JOIN fetch t.advogado as adv");
		hql.append(" LEFT JOIN fetch t.caso as caso");
		hql.append(" LEFT JOIN fetch caso.cliente as clienteCaso");
		hql.append(" LEFT JOIN fetch t.usuarios as users ");
		hql.append(" WHERE t.idTrabalho = :idTrabalho");
		Query query = em.createQuery(hql.toString(),Trabalho.class);
		
		query.setParameter("idTrabalho", idTrabalho);
		
		return (Trabalho) query.getSingleResult();
	}
    
	//TODO alterar nome para enviar para faturar
	public void alterarListaTrabalho(List<Trabalho> listaTrabalho, Boolean faturar, Fatura fatura) throws Exception {
		for (Trabalho trabalho : listaTrabalho) {
			if (trabalho.getAlterarViaAdm()){
				//Se for faturar, altera o status e depois salva as alterações
				if (faturar){
					if (trabalho.getStatusTrabalho().equals(StatusTrabalho.CRIADO) || usuarioLogadoControl.getUsuario().getPapel().getNomePapel().equals("Administrador")){
						trabalho.setStatusTrabalho(StatusTrabalho.ENVIADO_FATURAR);
						trabalho.setDataEnviadoFaturar(new Date());
					
					}
					else{
						throw new ControleException("Trabalho já faturado");
					}
				}
				//Se não for faturar somente salva as alteraçõs
				trabalhoService.alterarStatusTrabalho(trabalho);
			}
		}
		
	}
	
	/**
	 * Alterar todos os trabalhos para faturar para o status de não cobravel.
	 * @param idCliente
	 * @return
	 * @throws Exception
	 */
	public void altrarFaturaNaoCobravel(Integer idCliente) throws Exception{
		//fatura.setUsuario(usuarioLogadoControl.getUsuario());
		List<Trabalho> listaTrabalhos = trabalhoService.listarTrabalhoFaturar(idCliente);
		
		for (Trabalho trabalho : listaTrabalhos) {
			trabalho.setCobravel(false);
			trabalhoService.alterarStatusTrabalho(trabalho);
		}
	
	}
	public Date dataPesquisa (String data, boolean inicial) throws Exception{
		String[] dataSplit = data.split("-");
		String retorno = "";
		Date dataRetorno = new Date();
		
		if (dataSplit[0].equals("Janeiro")) {
			retorno = "1";
		} else if (dataSplit[0].equals("Fevereiro")) {
			retorno = "2";
		} else if (dataSplit[0].equals("Março")) {
			retorno = "3";
		} else if (dataSplit[0].equals("Abril")) {
			retorno = "4";
		} else if (dataSplit[0].equals("Maio")) {
			retorno = "5";
		} else if (dataSplit[0].equals("Junho")) {
			retorno = "6";
		} else if (dataSplit[0].equals("Julho")) {
			retorno = "7";
		} else if (dataSplit[0].equals("Agosto")) {
			retorno = "8";
		} else if (dataSplit[0].equals("Setembro")) {
			retorno = "9";
		} else if (dataSplit[0].equals("Outubro")) {
			retorno = "10";
		} else if (dataSplit[0].equals("Novembro")) {
			retorno = "11";
		} else if (dataSplit[0].equals("Dezembro")) {
			retorno = "12";
		}
		
		
		
		SimpleDateFormat dataFormat = new SimpleDateFormat("dd/mm/yyyy");
	
		try {
			//Date dataInicial = dataFormat.parse(retorno);
			
			SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy"); 
			
			if (inicial){
				 dataRetorno = formato.parse("01/"+retorno+"/"+dataSplit[1]);  
				
			}
			else{
				if (Integer.parseInt(retorno) == 12){
					int intAno = Integer.parseInt(dataSplit[1]) + 1;
					dataRetorno = formato.parse("01/01/"+intAno);
				}
				else {
					int mesRetorno = Integer.parseInt(retorno) + 1;
					dataRetorno = formato.parse("01/"+mesRetorno+"/"+dataSplit[1]); 
				}
			}
			 
				
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		}
		
		
		return dataRetorno;
	}
}
