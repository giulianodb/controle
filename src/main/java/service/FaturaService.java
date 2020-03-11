package service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.oasis.CellStyle;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import util.DateUtil;
import util.NumeroUtil;
import util.PropertiesLoaderImpl;
import control.UsuarioLogadoControl;
import control.exception.ExceptionHandler;
import converter.HoraConverter;
import converter.MoedaConverter;
import dto.ClienteFaturarDTO;
import dto.RelatorioJasperDTO;
import entity.Caso;
import entity.Fatura;
import entity.Papel;
import entity.ParcelaExito;
import entity.Participacao;
import entity.Prolabore;
import entity.StatusCaso;
import entity.StatusTrabalho;
import entity.TipoParticipacaoEnum;
import entity.TipoTransacaoEnum;
import entity.Trabalho;
import entity.Usuario;
import entity.ValorParticipacaoEnum;
import exception.ControleException;


@Stateless
@LocalBean
@ExceptionHandler
public class FaturaService {
	@PersistenceContext(unitName = "controle")
	private EntityManager em;
	
	@Inject
	private UsuarioLogadoControl usuarioLogadoControl;
	
	@EJB
	private TrabalhoService trabalhoService;
	
	@EJB
	private ClienteService clienteService;
	
	@EJB
	private ParticipacaoService participacaoService;
	
	@EJB
	private TransacaoService transacaoService;
	
	@EJB
	private ParticipacaoDefaultService participacaoDefaultService;
	
	@EJB
	private CasoService casoService;
	
	@EJB
	private ProlaboreService prolaboreService;
	
	@EJB
	private ParcelaExitoService parcelaExitoService;
	
	public List<Papel> listaPapel(){
		
		TypedQuery<Papel> query = em.createNamedQuery("listarPapel",Papel.class);
		
		List<Papel> resultList = query.getResultList();
		
		return resultList;
		
	}
	
	//utilizado somente para alterar o status da fatura para pago.
	public void alterarFaturaParaPago(Fatura fatura){
		em.merge(fatura);
		if (fatura.getStatusFatura().equals(StatusTrabalho.PAGO)){
			transacaoService.criarTransacaoParaFatura(fatura, TipoTransacaoEnum.DISPONIBILIZADO);
			
		}
		//Realiza alteracao fixa ou variavel de acordo com lista de trabalho
		if (fatura.getListaTrabalhos() != null && fatura.getListaTrabalhos().size() > 0){
			for (Trabalho trabalho : fatura.getListaTrabalhos()) {
				trabalho.setStatusTrabalho(fatura.getStatusFatura());
				em.merge(trabalho);
			}
		}
		else{
			for (Prolabore p : fatura.getListaProlabore()) {
				p.setStatusProlaboreEnum(fatura.getStatusFatura());
				prolaboreService.salvarProlabore(p);
			}
		}
	}
	
	
	////utilizado somente para alterar o status da fatura para pago.

	public void alterarListaFatura(List<Fatura> listaFatura) throws ControleException {
		for (Fatura fatura : listaFatura) {
			if (fatura.getFaturaSelecionada() != null && fatura.getFaturaSelecionada()) {
				if (fatura.getListParticipacao() == null || fatura.getListParticipacao().size() < 1){
					throw new ControleException("Para realizar o recebimento será necessário definir as distribuições pendentes.");
				}
				
				if (fatura.getStatusFatura() != StatusTrabalho.PAGO){
				
				if ((fatura.getValorPagamento() + fatura.getValorPago()) == fatura.valorTotalFatura()){
					fatura.setStatusFatura(StatusTrabalho.PAGO);
				} else{
					fatura.setStatusFatura(StatusTrabalho.PARCIAL_PAGO);
				}
				
				transacaoService.criarTransacaoParaFatura(fatura, TipoTransacaoEnum.DISPONIBILIZADO);
				
				if (fatura.getListaTrabalhos() != null && fatura.getListaTrabalhos().size() > 0){
					for (Trabalho trabalho : fatura.getListaTrabalhos()) {
						trabalho.setStatusTrabalho(fatura.getStatusFatura());
						em.merge(trabalho);
					}
				}
				else {
					for (Prolabore p : fatura.getListaProlabore()) {
						p.setStatusProlaboreEnum(fatura.getStatusFatura());
						prolaboreService.salvarProlabore(p);
					}
				}
				
				Float valorPago = NumeroUtil.somarDinheiro(fatura.getValorPagamento(), fatura.getValorPago(), 3);
				
				fatura.setValorPago( NumeroUtil.deixarFloatDuasCasas(valorPago));
				
				//Se não for faturar somente salva as alteraçõs
				em.merge(fatura);
				}
				else {
					throw new ControleException("Fatura já está 100% paga.");
				}
			}
		}
	}
	
	
	public List<ClienteFaturarDTO> listarClientesFaturar(){
		
		Map<Integer, ClienteFaturarDTO> mapa = new HashMap<Integer, ClienteFaturarDTO>();
		
		try {
			List<Trabalho> listaTrabalho = trabalhoService.listarTrabalhoFaturar(null);
			
	
			for (Trabalho trabalho : listaTrabalho) {
				if (mapa.containsKey(trabalho.getCaso().getCliente().getIdCliente())){
					Integer idCliente = trabalho.getCaso().getCliente().getIdCliente();
					
					mapa.get(idCliente).setQuantidadeCasosFaturar(mapa.get(idCliente).getQuantidadeCasosFaturar()+1);
					Float valorTotalFatura = NumeroUtil.somarDinheiro(mapa.get(idCliente).getValorTotalFatura(), trabalho.getValorTotalTrabalho(), 3);
					valorTotalFatura = NumeroUtil.deixarFloatDuasCasas(valorTotalFatura);
					
					mapa.get(idCliente).setValorTotalFatura(valorTotalFatura);
					
				}
				else {
					ClienteFaturarDTO cliente = new ClienteFaturarDTO();
					cliente.setNomeCliente(trabalho.getCaso().getCliente().getNome());
					cliente.setQuantidadeCasosFaturar(1);
					cliente.setValorTotalFatura(trabalho.getValorTotalTrabalho());
					cliente.setIdCliente(trabalho.getCaso().getCliente().getIdCliente());
					
					mapa.put(trabalho.getCaso().getCliente().getIdCliente(), cliente);
					
				}
				
			}
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		List<ClienteFaturarDTO> listaClienteFaturar = new ArrayList<ClienteFaturarDTO>();
		
		Collection<ClienteFaturarDTO> cont = mapa.values();  
		Iterator<ClienteFaturarDTO> i = cont.iterator();  
		while (i.hasNext()) {  
	            if(i != null){
	            	ClienteFaturarDTO item = i.next();
	            	listaClienteFaturar.add(item);
	            }
		} 

		//Trecho para ordenar a lista pelo nome cliente
				Collections.sort(listaClienteFaturar, new Comparator<Object>() {

					@Override
					public int compare(Object o1, Object o2) {
						ClienteFaturarDTO cf1 = (ClienteFaturarDTO) o1;
						ClienteFaturarDTO cf2 = (ClienteFaturarDTO) o2;
						
						return cf1.getNomeCliente().compareTo(cf2.getNomeCliente());
							
					}
				});
		
		return listaClienteFaturar;
	}
	
	public Fatura gerarFaturaCliente(Integer idCliente) throws Exception{
		Fatura fatura = new Fatura();
		
		fatura.setDataFatura(new Date());
		SimpleDateFormat formato = new SimpleDateFormat("yyyy");
		
		fatura.setStatusFatura(StatusTrabalho.FATURADO);
		fatura.setUsuario(usuarioLogadoControl.getUsuario());
		fatura.setCliente(clienteService.obterCliente(idCliente));
		
		List<Trabalho> listaTrabalhos = trabalhoService.listarTrabalhoFaturar(idCliente);
		
		List<String> listMeses = new ArrayList<String>();		
		
		//obter os meses de referência dos trabalhos pesquisados
		for (Trabalho trabalho : listaTrabalhos) {
			String temp = mesPorExtenso(trabalho.getDataTrabalho().getMonth())+" "+formato.format(trabalho.getDataTrabalho());
			if (!listMeses.contains(temp)){
				listMeses.add(temp);
			}
		}
		
		StringBuilder meses = new StringBuilder("");
		for (String mes : listMeses) {
			if (meses.length() != 0){
				meses.append(", ");
			}
			meses.append(mes);
			   
		}
		
		fatura.setMesReferencia(meses.toString());
	 
		
		fatura.setListaTrabalhos(listaTrabalhos);
		
		//inserir valores default da participacao
		
		fatura.setPorcentagemFundo(participacaoDefaultService.obterValorPorNome(TipoParticipacaoEnum.FUNDO));
		fatura.setPorcentagemImposto(participacaoDefaultService.obterValorPorNome(TipoParticipacaoEnum.IMPOSTO));
		fatura.setPorcentagemIndicacao(participacaoDefaultService.obterValorPorNome(TipoParticipacaoEnum.INDICACAO));
		fatura.setPorcentagemParticipacaoSocio(participacaoDefaultService.obterValorPorNome(TipoParticipacaoEnum.PARTICIPACAO_SOCIO));
		fatura.setPorcentagemParticipacaoTrabalho(participacaoDefaultService.obterValorPorNome(TipoParticipacaoEnum.PARTICIPACAO_TRABALHO));
		fatura.setValorPago(0f);
		em.persist(fatura);
		
		trabalhoService.alterarFaturadoListaTrabalho(listaTrabalhos);
		
		//criar participacao para todos os trabalhos
		participacaoService.cadastrarParticipacao(fatura);
		
		return fatura;
	}
	
	public Fatura gerarFaturaFixo(Prolabore prolabore) throws ControleException{
		//Verifica se possui pre participação cadastrada e indicação...
//		
//		if (prolabore.getListaPreParticipacaoFixos() == null || prolabore.getListaPreParticipacaoFixos().size() == 0){
//			throw new ControleException("Necessário cadastrar as participações.");
//		}
		
		if (prolabore.getFatura() != null){
			throw new ControleException("Fatura já foi gerada.");
		}
//		
		Fatura fatura = new Fatura();
		
		fatura.setDataFatura(new Date());
		SimpleDateFormat formato = new SimpleDateFormat("yyyy");
		
		fatura.setStatusFatura(StatusTrabalho.FATURADO);
		fatura.setUsuario(usuarioLogadoControl.getUsuario());
		fatura.setCliente(prolabore.getCaso().getCliente());
		List<Prolabore> listaP = new ArrayList<Prolabore>();
		listaP.add(prolabore);
		fatura.setListaProlabore(listaP);
		
		prolabore.setFatura(fatura);
		
		
		fatura.setMesReferencia(mesPorExtenso(fatura.getDataFatura().getMonth())+" "+formato.format(fatura.getDataFatura()));
		
		//inserir valores default da participacao
		
		fatura.setPorcentagemFundo(prolabore.getPorcentagemFundo());
		fatura.setPorcentagemImposto(prolabore.getPorcentagemImposto());
		fatura.setPorcentagemIndicacao(prolabore.getPorcentagemIndicacao());
		fatura.setPorcentagemParticipacaoSocio(prolabore.getPorcentagemParticipacaoSocio());
		fatura.setPorcentagemParticipacaoTrabalho(prolabore.getPorcentagemParticipacaoTrabalho());
		fatura.setValorPago(0f);
		
		//inserindo previsão de pagamento:
		
		if (prolabore.getDataVencimento() != null){
			fatura.setDataPrevisaoPagamento(prolabore.getDataVencimento());
		}else {
			fatura.setDataPrevisaoPagamento(DateUtil.somaDias(new Date(), 10));
		}
		
		em.persist(fatura);
		
		prolabore.setFatura(fatura);
		prolabore.setStatusProlaboreEnum(StatusTrabalho.FATURADO);
		
		
		prolaboreService.salvarProlabore(prolabore);
		//criar participacao para todos os trabalhos
		participacaoService.cadastrarParticipacaoFixo(fatura);
		
		return fatura;
	}
	
	
	
	public Fatura gerarFaturaExito(ParcelaExito parcela) throws ControleException{
		//Verifica se possui pre participação cadastrada e indicação...
//		
//		if (prolabore.getListaPreParticipacaoFixos() == null || prolabore.getListaPreParticipacaoFixos().size() == 0){
//			throw new ControleException("Necessário cadastrar as participações.");
//		}
//		
		Fatura fatura = new Fatura();
		
		fatura.setDataFatura(new Date());
		SimpleDateFormat formato = new SimpleDateFormat("yyyy");
		
		fatura.setStatusFatura(StatusTrabalho.FATURADO);
		fatura.setUsuario(usuarioLogadoControl.getUsuario());
		fatura.setCliente(parcela.getCaso().getCliente());
		
		fatura.setMesReferencia(mesPorExtenso(fatura.getDataFatura().getMonth())+" "+formato.format(fatura.getDataFatura()));
		
		//inserir valores default da participacao
		
		fatura.setValorPago(0f);
		fatura.setParcelaExito(parcela);
		em.persist(fatura);
		parcela.setStatusParcela(StatusTrabalho.FATURADO);
		
		parcelaExitoService.salvarParcelaExito(parcela);
		
		return fatura;
	}
	
	
	//Responsavel em deixar todos os trabalhos como não cobravel.
	
	public Fatura visualizarFaturaCliente(Integer idCliente) throws Exception{
		Fatura fatura = new Fatura();
		
		fatura.setDataFatura(new Date());
		SimpleDateFormat formato = new SimpleDateFormat("yyyy");
		
		//fatura.setUsuario(usuarioLogadoControl.getUsuario());
		fatura.setCliente(clienteService.obterCliente(idCliente));
		List<Trabalho> listaTrabalhos = trabalhoService.listarTrabalhoFaturar(idCliente);
		
		fatura.setListaTrabalhos(listaTrabalhos);
		
		
		List<String> listMeses = new ArrayList<String>();		
		
		//obter os meses de referência dos trabalhos pesquisados
		for (Trabalho trabalho : listaTrabalhos) {
			String temp = mesPorExtenso(trabalho.getDataTrabalho().getMonth())+" "+formato.format(trabalho.getDataTrabalho());
			if (!listMeses.contains(temp)){
				listMeses.add(temp);
			}
		}
		
		StringBuilder meses = new StringBuilder();
		for (String mes : listMeses) {
			if (meses.length() != 0){
				meses.append(", ");
			}
			meses.append(mes);
			   
		}
		
		fatura.setMesReferencia(meses.toString());
	 
		
		return fatura;
	}
	
	
	public String obterStringRelatorioFatura(Fatura fatura, Boolean faturamentoFinal) throws Exception{
		
		if(faturamentoFinal){
			fatura = em.find(Fatura.class, fatura.getIdFatura());
		}
		
		float totalHorasGeral = 0f;
		float valorTotalGeral = 0f;
		//fatura = this.obterFaturaPorIdFatura(fatura.getIdFatura());
		//separando os casos
		Map<Integer, List<Trabalho>> mapaFatura = new HashMap<Integer, List<Trabalho>>();
		
		for (Trabalho trabalho : fatura.getListaTrabalhos()) {
			trabalho = trabalhoService.obterTrabalho(trabalho.getIdTrabalho());
			if (mapaFatura.containsKey(trabalho.getCaso().getIdCaso())){
				mapaFatura.get(trabalho.getCaso().getIdCaso()).add(trabalho);
				totalHorasGeral = totalHorasGeral + trabalho.getHorasTrabalho();
    			valorTotalGeral = valorTotalGeral + trabalho.getValorTotalTrabalho();
			}
			else {
				List<Trabalho> listaTrabalho = new ArrayList<Trabalho>();
				listaTrabalho.add(trabalho);
				
				//Populando valor total para ser calculado
				totalHorasGeral = totalHorasGeral + trabalho.getHorasTrabalho();
    			valorTotalGeral = valorTotalGeral + trabalho.getValorTotalTrabalho();
				
				mapaFatura.put(trabalho.getCaso().getIdCaso(), listaTrabalho);
			}
		}
		
		StringBuffer relatorioHtlm = new StringBuffer();
		
		relatorioHtlm.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\"> ");
		relatorioHtlm.append("<html> " );
		relatorioHtlm.append("<head> " );
		relatorioHtlm.append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"> " ); 
		relatorioHtlm.append("<title>Insert title here</title> " );
		relatorioHtlm.append(" <link rel=\"stylesheet\" href=\" http://"+PropertiesLoaderImpl.getValor("endereco")+"/body.css\" media=\"print\"> ");
		
		relatorioHtlm.append("</head> " );
		relatorioHtlm.append("<body> " );
		
//		relatorioHtlm.append("<div id=\"footer\" style=\"\">  P&aacute;gina <span id=\"pagenumber\"/> de <span id=\"pagecount\"/> </div>");

		
		//String caminhoLogo = ((HttpSession)FacesContext.getCurrentInstance().getExternalContext().getSession(false)).getServletContext().getRealPath("/resources/images/logo.jpg");
//		String separator = System.getProperty("file.separator");
		
		
		
		String caminhoLogo = "http://"+PropertiesLoaderImpl.getValor("endereco")+"/logo.png";
		
		
		System.out.println("Caminho da logo:" + caminhoLogo);
		relatorioHtlm.append(	"<center> <img src='"+ caminhoLogo+"' width='120' alt='Logo'/>  </center> " ); 
		// Fazer a iteraap do mapa
		//
		
		
		relatorioHtlm.append(  "<center><label style=\"font-family:Calibri;font-size:10pt; \"> <b>FATURA - HONOR&Aacute;RIOS ADVOCAT&Iacute;CIOS </b></label>");
		
		if (!faturamentoFinal){
			relatorioHtlm.append(  " (<b> VISUALIZA&Ccedil;&Atilde;O DE FATURAMENTO </b>) ");
		}
		
		relatorioHtlm.append(  " </center>");
		
		relatorioHtlm.append(	" <br><br>    ");
		
		relatorioHtlm.append(" <table border=\"0\" align=\"left\">  ");
		relatorioHtlm.append("<tr>   ");
		relatorioHtlm.append("<td align=\"right\"><label style=\"font-family:Calibri;font-size:9pt; \"> Cliente: <label></td> ");
		relatorioHtlm.append("<td> <label style=\"font-family:Calibri;font-size:10pt; \"> "+parserStringHtml(fatura.getCliente().getNome())+" </label> </td> ");
		relatorioHtlm.append("</tr>   ");
		relatorioHtlm.append("<tr>    ");
		relatorioHtlm.append("<td  align=\"right\"><label style=\"font-family:Calibri;font-size:9pt; \"> M&ecirc;s refer&ecirc;ncia: </label></td>   ");
		relatorioHtlm.append("<td>  <label style=\"font-family:Calibri;font-size:10pt; \"> "+(parserStringHtml(fatura.getMesReferencia())).replaceAll("_", " ")+"</label></td>   ");
		relatorioHtlm.append("</tr>   ");
		relatorioHtlm.append("</table>   ");
		relatorioHtlm.append("<br><br><br>  ");
		
		
	      Set<Integer> chaves = mapaFatura.keySet();  
	      
	      for (Integer chave : chaves)  
	        {  
	            if(chave != null) {
	            	List<Trabalho> listaTrabalhoMap = ((mapaFatura.get(chave)));  
	        		relatorioHtlm.append("<table style=\"border: 1px solid black; border-collapse: collapse;font-family:Calibri;font-size:10pt; \" width=\"100%\">   ");
	        		relatorioHtlm.append("<tr   style=\"border: 1px solid black;\" >   ");
	        		relatorioHtlm.append("	<td style=\"border: 1px solid black; font-size:9pt; \" bgcolor=\"#BEBEBE\" align=\"center\" width=\"15% \"> <b>DATA </b></td>  ");
	        		relatorioHtlm.append("	<td style=\"border: 1px solid black; font-size:9pt; \" bgcolor=\"#BEBEBE\" align=\"center\" width=\"15% \" ><b>TEMPO</b></td>   ");
	        		relatorioHtlm.append("	<td style=\"border: 1px solid black; font-size:9pt; \" bgcolor=\"#BEBEBE\" align=\"center\" width=\"25% \"> <b>CASO</b></td>  ");
	        		relatorioHtlm.append("	<td style=\"border: 1px solid black; font-size:9pt; \" bgcolor=\"#BEBEBE\" align=\"center\"><b>DESCRI&Ccedil;&Atilde;O</b></td>  ");
	          		if (!faturamentoFinal){
	        			relatorioHtlm.append("	<td style=\"border: 1px solid black; font-size:9pt; \" bgcolor=\"#BEBEBE\" align=\"center\" width=\"5% \" ><b>Advogado</b></td>  ");
	        			relatorioHtlm.append("	<td style=\"border: 1px solid black; font-size:9pt; \" bgcolor=\"#BEBEBE\" align=\"center\" width=\"5% \" ><b>Valor</b></td>  ");
	        		}
	        		
	        		relatorioHtlm.append("</tr>  ");
	        		
	        		Float totalHoras = 0f;
	        		
	        		SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy");
	        		HoraConverter hc = new HoraConverter();
	        		
	        		//valores para serem usados para descobris valores diferente do padãro
	            	boolean valorDiferenciado = false;
	        		
	            	TreeMap<Float, Float> mapaValores = new TreeMap<Float, Float>();
	            	
	        		for (Trabalho trabalho : listaTrabalhoMap) {
		            	
	        			//Regra para relatório caso o valor do trabalho for diferente do padrão.
	        			// caso for diferente deve mostrar o valor total de todos os grau separadamente para o cliente estar ciente
	        			
		            	if (mapaValores.size() > 1){
		            		valorDiferenciado = true;
		            	}
		            	
		            	if (mapaValores.get(trabalho.getValorHora()) == null ){
		            		mapaValores.put(trabalho.getValorHora(), trabalho.getHorasTrabalho());
		            	}
		            	else {
		            		mapaValores.put(trabalho.getValorHora(), mapaValores.get(trabalho.getValorHora()) + trabalho.getHorasTrabalho());
		            	}
		            	
		              	//fim regra
	        			
		            	
	        			relatorioHtlm.append("<tr style=\" border: 1px solid black;\" align=\"center\">");
	        			relatorioHtlm.append("<td style=\" border: 1px solid black;\">");
	        			relatorioHtlm.append(formato.format(trabalho.getDataTrabalho()));
	        			relatorioHtlm.append("</td>");
	        			
	        			relatorioHtlm.append("<td style=\" border: 1px solid black;\" align=\"center\" >");
	        			relatorioHtlm.append(hc.getAsString(null,null,trabalho.getHorasTrabalho()));
	        			relatorioHtlm.append("</td>");
	        			
	        			relatorioHtlm.append("<td style=\" border: 1px solid black;\" align=\"center\" >");
	        			relatorioHtlm.append(parserStringHtml(trabalho.getCaso().getNomeCaso()));
	        			relatorioHtlm.append("</td>");
	        			
	        			relatorioHtlm.append("<td style=\" border: 1px solid black;\" align=\"left\" >");
	        			relatorioHtlm.append(parserStringHtml(trabalho.getDescricaoTrabalho()));
	        			relatorioHtlm.append("</td>");
	        			
	        			if (!faturamentoFinal){
	        				relatorioHtlm.append("<td style=\"border: 1px solid black;\" align=\"left\" >");
		        			
	        				
	        				relatorioHtlm.append(parserStringHtml(trabalho.getAdvogado().getNome()));
		        			if (trabalho.getTrabalhoCompartilhado() != null && trabalho.getTrabalhoCompartilhado()){
		        				for (Usuario advogado : trabalho.getUsuarios()) {
		        					relatorioHtlm.append(" - ");
		        					relatorioHtlm.append(advogado.getNome());
								}
		        				
		        			}
		        			
		        			relatorioHtlm.append("</td>");
		        			relatorioHtlm.append("<td style=\"border: 1px solid black;\" align=\"left\" >");
		        			relatorioHtlm.append(parserStringHtml(trabalho.getValorTotalTrabalho().toString()));
		        			relatorioHtlm.append("</td>");
	        				
		        		}
		        		
	        			
	        			relatorioHtlm.append("</tr>");
	        			
	        			totalHoras = totalHoras + trabalho.getHorasTrabalho();
	        			
	        		}
	        		
	        		relatorioHtlm.append("</table>");
	        		relatorioHtlm.append("<br>");
	        		MoedaConverter mc = new MoedaConverter();
	        		
	        		relatorioHtlm.append("<table style=\"border: 1px solid black; border-collapse: collapse \" border=\"1px\" width=\"45%\"> ");
	        		relatorioHtlm.append(	"<tr style=\"border: 1px solid black;\"> ");
	        		relatorioHtlm.append(	"	<td colspan=\"2\" bgcolor=\"#BEBEBE\"  align=\"center\" style=\"border: 1px solid black;FONT-FAMILY: 'Calibri';font-size:8pt;\"><b>TOTAL DE HORAS</b></td> ");
	        				
	        		relatorioHtlm.append(	"</tr> ");
	        		relatorioHtlm.append(	"<tr style=\"border: 1px solid black; FONT-FAMILY: 'Calibri';font-size:8pt; \">  ");
	        		relatorioHtlm.append(	"	<td style=\"border: 1px solid black; FONT-FAMILY: 'Calibri';font-size:8pt; \">Total de horas </td>  ");
	        		relatorioHtlm.append(	"	<td style=\"border: 1px solid black; FONT-FAMILY: 'Calibri';font-size:8pt; \"> "+hc.getAsString(null,null,totalHoras)+"</td>  ");
	        		relatorioHtlm.append(	"</tr> ");
	        		
	        		//Caso os valores forem diferenciados do padrão, mostrar para cliente os valores diferenciados por senioridade do advogado.
	        		
	        			
		        			Set<Float> setValores = mapaValores.keySet();
		        		    int contador = 1;
		        			Float valorTotalCaso = 0f;
		        			for (Float valor : setValores) {
		        				System.out.println(mapaValores.get(valor));
		        				valorTotalCaso = valorTotalCaso + (valor*mapaValores.get(valor));
		        				if (valorDiferenciado){
		        					relatorioHtlm.append(	"<tr style=\"border: 1px solid black;\"> ");
		        					relatorioHtlm.append(	"	<td style=\"border: 1px solid black; FONT-FAMILY: 'Calibri';font-size:8pt; \">Valor total Advogado Grau "+contador+"</td>  ");
		        					relatorioHtlm.append(	"	<td style=\"border: 1px solid black; FONT-FAMILY: 'Calibri';font-size:8pt; \"> "+NumberFormat.getCurrencyInstance().format(valor*mapaValores.get(valor))+"</td>  ");
		        					relatorioHtlm.append("	</tr> ");
				        		
		        					contador ++;
		        				}
				          		else {
				        			
					        		relatorioHtlm.append(	"<tr style=\"border: 1px solid black;\"> ");
					        		relatorioHtlm.append(	"	<td style=\"border: 1px solid black; FONT-FAMILY: 'Calibri';font-size:8pt; \">R$ por hora</td>  ");
					        		relatorioHtlm.append(	"	<td style=\"border: 1px solid black; FONT-FAMILY: 'Calibri';font-size:8pt; \"> "+NumberFormat.getCurrencyInstance().format(valor)+"</td>  ");
					        		relatorioHtlm.append("	</tr> ");
				        			
				        		}
				        		
		        			}
	      
	        
	        		relatorioHtlm.append("	<tr style=\"border: 1px solid black; FONT-FAMILY: 'Calibri';font-size:8pt; \"> ");
	        		relatorioHtlm.append("		<td style=\"border: 1px solid black; FONT-FAMILY: 'Calibri';font-size:8pt; \"> <b> Total Honor&aacute;rios Advogados</b> </td>  ");
	        		relatorioHtlm.append("		<td style=\"border: 1px solid black; FONT-FAMILY: 'Calibri';font-size:8pt; \"><b> "+NumberFormat.getCurrencyInstance().format(valorTotalCaso)+"</b></td>  ");
	        		relatorioHtlm.append("	</tr> ");
	        		relatorioHtlm.append("</table> ");
	        		
	        		relatorioHtlm.append("<br/><br/><br/> ");
	        	            	
	            }
	          
	        }
	        
	        
	   		relatorioHtlm.append("<table style=\"border: 1px solid black; border-collapse: collapse \" border=\"1px\" width=\"45%\"> ");
    	

    		relatorioHtlm.append("	<tr style=\"border: 1px solid black; FONT-FAMILY: 'Calibri';font-size:8pt; \"> ");
    		relatorioHtlm.append("		<td style=\"border: 1px solid black; FONT-FAMILY: 'Calibri';font-size:8pt; \"> <b> Total Honor&aacute;rios </b> </td>  ");
    		relatorioHtlm.append("		<td style=\"border: 1px solid black; FONT-FAMILY: 'Calibri';font-size:8pt; \"><b> "+NumberFormat.getCurrencyInstance().format(valorTotalGeral)+"</b></td>  ");
    		relatorioHtlm.append("	</tr> ");
    		relatorioHtlm.append("</table> ");

		relatorioHtlm.append("</body></html>");
		
		return relatorioHtlm.toString();
		
	}
	
	
	public ByteArrayOutputStream gerarExcelFixo(Fatura fatura, Boolean faturamentoFinal){
		
		HSSFWorkbook workbook = new HSSFWorkbook();
		SimpleDateFormat formato = new SimpleDateFormat("dd-MM-yyyy");
		HSSFSheet firstSheet = workbook.createSheet(fatura.getCliente()
				.getNome() + " - " + formato.format(fatura.getDataFatura()));

		FileOutputStream fos = null;
		final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

		try {

			List<Trabalho> listaTrabalho = this.obterRelatorioFaturaExcel(
					fatura, faturamentoFinal);

			fos = new FileOutputStream(new File(PropertiesLoaderImpl.getValor("temp_csv")));

			
			HSSFRow row = firstSheet.createRow(0);
			
			row.createCell(0).setCellValue("Relatório Fatura cliente : " + fatura.getCliente().getNome());
			
			int i = 3;
			
			row = firstSheet.createRow(2);
			
			row.createCell(0).setCellValue("Data");
			row.createCell(1).setCellValue("Horas trabalho");
			row.createCell(2).setCellValue("Caso");
			row.createCell(3).setCellValue("Descrição trabalho");
			row.createCell(4).setCellValue("Advogados");
			row.createCell(5).setCellValue("Valor total");
			
			
			for (Trabalho trabalho : listaTrabalho) {
				row = firstSheet.createRow(i);

				row.createCell(0).setCellValue(formato.format(trabalho.getDataTrabalho()));
				row.createCell(1).setCellValue(trabalho.getHorasTrabalho());
				row.createCell(2).setCellValue(trabalho.getCaso().getNomeCaso());
				row.createCell(3).setCellValue(trabalho.getDescricaoTrabalho());
				row.createCell(4).setCellValue(trabalho.getAdvogado().getNome());
				
				
				row.createCell(5).setCellValue(trabalho.getValorTotalTrabalho());

				i++;

			} 
			
			workbook.write(byteArrayOutputStream);

		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Erro ao exportar arquivo");
		} finally {
			try {
				fos.flush();
				fos.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return byteArrayOutputStream;
		
	}
	
	
	public ByteArrayOutputStream gerarExcelDistribuicao(List<Fatura> listFatura, Date dataInicio, Date dataFim){
		
		//Mudar os status:
		
		
		HSSFWorkbook workbook = new HSSFWorkbook();
		SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy");
		HSSFSheet firstSheet = workbook.createSheet("Distribuição");
		
		String dataInicioPesquisa = formato.format(dataInicio);
		String dataFimPesquisa = formato.format(dataFim);
		
		
		FileOutputStream fos = null;
		final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

		try {

			fos = new FileOutputStream(new File(PropertiesLoaderImpl.getValor("temp_csv")));

			
			HSSFRow row = firstSheet.createRow(0);
			
			row.createCell(0).setCellValue("Relatório Distribuição de faturas de : " + dataInicioPesquisa + " a: "+ dataFimPesquisa);
			
			int i = 3;
			
			row = firstSheet.createRow(2);
			
			row.createCell(0).setCellValue("Cod fatura");
			row.createCell(1).setCellValue("Cliente");
			row.createCell(2).setCellValue("Data fatura");
			row.createCell(3).setCellValue("Valor total fatura");
			row.createCell(4).setCellValue("Advogado");
			row.createCell(5).setCellValue("Sócio");
			row.createCell(6).setCellValue("Trabalho");
			row.createCell(7).setCellValue("Indicação");
			row.createCell(8).setCellValue("Valor total distribuição");
			
			MoedaConverter mc = new MoedaConverter();
			
			for (Fatura faturaTemp : listFatura) {
				
//				if(faturaTemp.getFaturaSelecionada() && !faturaTemp.temQualquerPendendicia()){
				if(faturaTemp.getFaturaSelecionada()) {
					if (faturaTemp.getIdFatura() != null) {
						faturaTemp = obterFaturaPorIdFatura(faturaTemp.getIdFatura());
					}
					//Variaveis para controlar se existem dados de participação de trabalho e indicação caso não existir, manda para próxima fatura
					
					Integer participacaoIndicacao = new Integer(0);
					Integer participacaoTrabalho = 0;
					
					for (Participacao participacao : faturaTemp.getListParticipacao()) {
						participacaoIndicacao = participacaoIndicacao + participacao.getPorcentagemIndicacao().intValue();
						participacaoTrabalho = participacaoTrabalho + participacao.getPorcentagemParticipacaoTrabalho().intValue();
					}
					//Nesse  caso falta os valroes de indicacao de caso e criterio trabalho, não pode gerar distribuição --- TRIADO.
//					if (participacaoIndicacao == 0 || participacaoTrabalho == 0){
//						continue;
//					}
//					
					//caso fatura já foi distribuida não realiza distribuicao novamente
					
					if (faturaTemp.getStatusFatura().equals(StatusTrabalho.DISTRIBUIDO)){
						continue;
					}
					
					
					
					//Alterando o status
					faturaTemp.setStatusFatura(StatusTrabalho.DISTRIBUIDO);
					if (faturaTemp.getListaTrabalhos() != null && faturaTemp.getListaTrabalhos().size() > 0){
						for (Trabalho trabalho : faturaTemp.getListaTrabalhos()) {
							trabalho.setStatusTrabalho(faturaTemp.getStatusFatura());
							em.merge(trabalho);
						}
					}
					else {
						for (Prolabore p : faturaTemp.getListaProlabore()) {
							p.setStatusProlaboreEnum(faturaTemp.getStatusFatura());
							prolaboreService.salvarProlabore(p);
						}
					}
					
					em.merge(faturaTemp);
					
					
					for (Participacao participacao : faturaTemp.getListParticipacao()) {
						
						row = firstSheet.createRow(i);
						
						row.createCell(0).setCellValue(faturaTemp.getIdFatura());
						row.createCell(1).setCellValue(faturaTemp.getCliente().getNome());
						row.createCell(2).setCellValue(formato.format(faturaTemp.getDataFatura()));
						row.createCell(3).setCellValue(faturaTemp.valorTotalFatura());
						
						if (participacao.getAdvogado() != null){
							row.createCell(4).setCellValue(participacao.getAdvogado().getNome());
						}
						else if (participacao.getFundo() != null && participacao.getFundo()) {
							row.createCell(4).setCellValue("Fundo");
						}
						else {
							row.createCell(4).setCellValue("Imposto");
						}
						
						row.createCell(5).setCellValue(NumeroUtil.deixarFloatDuasCasas(participacao.getPorcentagemParticipacaoSocio()));
						row.createCell(6).setCellValue(NumeroUtil.deixarFloatDuasCasas(participacao.getPorcentagemParticipacaoTrabalho()));
						row.createCell(7).setCellValue(NumeroUtil.deixarFloatDuasCasas(participacao.getPorcentagemIndicacao()));
						row.createCell(8).setCellValue(NumeroUtil.deixarFloatDuasCasas(participacao.getValorTotalParticipacao()));
						
						i++;
					}
				}
				
				i++;i++;

			} 
			
			workbook.write(byteArrayOutputStream);

		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Erro ao exportar arquivo");
		} finally {
			try {
				fos.flush();
				fos.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return byteArrayOutputStream;
		
	}
	
	
	
	
	
	
	
	
	public List<Trabalho> obterRelatorioFaturaExcel (Fatura fatura, Boolean faturamentoFinal) throws Exception{
		
		if(faturamentoFinal){
			fatura = em.find(Fatura.class, fatura.getIdFatura());
		}
		
		float totalHorasGeral = 0f;
		float valorTotalGeral = 0f;
		
		//separando os casos
		Map<Integer, List<Trabalho>> mapaFatura = new HashMap<Integer, List<Trabalho>>();
		
		for (Trabalho trabalho : fatura.getListaTrabalhos()) {
			if (mapaFatura.containsKey(trabalho.getCaso().getIdCaso())){
				mapaFatura.get(trabalho.getCaso().getIdCaso()).add(trabalho);
				totalHorasGeral = totalHorasGeral + trabalho.getHorasTrabalho();
    			valorTotalGeral = valorTotalGeral + trabalho.getValorTotalTrabalho();
			}
			else {
				List<Trabalho> listaTrabalho = new ArrayList<Trabalho>();
				listaTrabalho.add(trabalho);
				
				//Populando valor total para ser calculado
				totalHorasGeral = totalHorasGeral + trabalho.getHorasTrabalho();
    			valorTotalGeral = valorTotalGeral + trabalho.getValorTotalTrabalho();
				
				mapaFatura.put(trabalho.getCaso().getIdCaso(), listaTrabalho);
			}
		}
			
		List<Trabalho> listaRetorno = new ArrayList<Trabalho>();
		
	      Set<Integer> chaves = mapaFatura.keySet();  
	      
	      for (Integer chave : chaves)  
	        {  
	            if(chave != null) {
	            	List<Trabalho> listaTrabalhoMap = ((mapaFatura.get(chave)));  
	            	
	        		for (Trabalho trabalho : listaTrabalhoMap) {
	        			listaRetorno.add(trabalho);
	        		}
	            }
	            
	        }
	        			
	        			
		return listaRetorno;
		
	}
	
	
	public ByteArrayOutputStream gerarRelatorioFaturaFixo(Fatura fatura, Boolean faturamentoFinal){
		Prolabore prolabore = fatura.getListaProlabore().get(0);
		prolabore = prolaboreService.obterProlaborePorFatura(fatura.getIdFatura());
		StringBuilder conteudoRelatorio = new StringBuilder();
		MoedaConverter mc = new MoedaConverter();
		
		boolean parcelaUnica = prolabore.getCaso().getListaProlabole().size() == 1;
		boolean temEvento = prolabore.getEvento() != null && prolabore.getEvento() !="";
		
		if(!faturamentoFinal){
			conteudoRelatorio.append("                                (FATURAMENTO DE VISUALIZAÇÃO) \n");
			
		}
		
		conteudoRelatorio.append("                                 Prezados Senhores:");
		conteudoRelatorio.append("\n");
		conteudoRelatorio.append("\n");
	
		conteudoRelatorio.append("                     Segue abaixo abaixo descrição dos honorários advocatícios que estão sendo faturados na presente data: ");

		conteudoRelatorio.append("\n");
		conteudoRelatorio.append("\n");
		
		conteudoRelatorio.append(prolabore.getCaso().getDescricao());
		conteudoRelatorio.append("\n");
		
		if (parcelaUnica && temEvento){
			conteudoRelatorio.append("                     Parcela única referente ");
			conteudoRelatorio.append(prolabore.getEvento());
			conteudoRelatorio.append(" , no valor de ");
			conteudoRelatorio.append("R$ ");
			conteudoRelatorio.append(mc.getAsString(null, null, prolabore.getValor()));
		} else if(!parcelaUnica && temEvento){
			conteudoRelatorio.append("                     Parcela "); 
			conteudoRelatorio.append(prolabore.getOrdem());		
			conteudoRelatorio.append("/ ");
			conteudoRelatorio.append(prolabore.getCaso().getListaProlabole().size());
			conteudoRelatorio.append(" referente ");
			conteudoRelatorio.append(prolabore.getEvento());
			conteudoRelatorio.append(", no valor de ");
			conteudoRelatorio.append("R$ ");
			conteudoRelatorio.append(mc.getAsString(null, null, prolabore.getValor()));
		} else if(!temEvento){ 
			conteudoRelatorio.append("                     Parcela "); 
			conteudoRelatorio.append(prolabore.getOrdem());		
			conteudoRelatorio.append("/ ");
			conteudoRelatorio.append(prolabore.getCaso().getListaProlabole().size());
			conteudoRelatorio.append(" no valor de ");
			conteudoRelatorio.append("R$ ");
			conteudoRelatorio.append(mc.getAsString(null, null, prolabore.getValor()));
			
		}
		
		conteudoRelatorio.append("\n");
		conteudoRelatorio.append("\n");
		conteudoRelatorio.append("\n");
		conteudoRelatorio.append("\n");
		conteudoRelatorio.append("\n");
		conteudoRelatorio.append("Curitiba, ");
		conteudoRelatorio.append(DateUtil.dataToString(new Date(), "dd"));
		conteudoRelatorio.append(" de ");
		conteudoRelatorio.append(mesPorExtenso(new Date().getMonth()));
		conteudoRelatorio.append(", de ");
		conteudoRelatorio.append(DateUtil.dataToString(new Date(), "yyyy"));
		
		
		RelatorioJasperDTO jasper = new RelatorioJasperDTO();
		jasper.setCliente(fatura.getCliente().getNome());
		jasper.setConteudo(conteudoRelatorio.toString());
		jasper.setCaso(prolabore.getCaso().getNomeCaso());
		
		List<RelatorioJasperDTO> lista = new ArrayList<RelatorioJasperDTO>();
		lista.add(jasper);
		
		JasperReport report;
		try {
			
			
			
			report = JasperCompileManager
							 .compileReport(PropertiesLoaderImpl.getValor("relatorio_fixo"));
			// // preenchimento do relatorio, note que o metodo
			// recebe 3 parametros: // 1 - o relatorio // // 2 - um
			// Map, com parametros que sao passados ao relatorio //
			// no momento do preenchimento. No nosso caso eh null,
			// pois nao // estamos usando nenhum parametro // // 3 -
			// o data source. Note que nao devemos passar a lista
			// diretamente, // e sim "transformar" em um data source
			// utilizando a classe // 
//			JRBeanCollectionDataSource
			 JasperPrint print = JasperFillManager.fillReport(report, null, new  JRBeanCollectionDataSource(lista)); // exportacao do
			// relatorio para outro formato, no caso PDF]]
			 
			 
//			 JasperExportManager.exportReportToPdfFile(print, "~/RelatorioClientes.pdf");
			 System.out.println("Relatório gerado.");
			 
		      final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();  
              byteArrayOutputStream.write(JasperExportManager.exportReportToPdf(print));
			 
              return byteArrayOutputStream;
			 
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ControleException("Erro ao gerar relaltório",e);
		}
		
		
	}
	
	
	public ByteArrayOutputStream gerarRelatorioFaturaExito(Fatura fatura,Boolean faturamentoFinal) {
		
//		float valorExito = fatura.getParcelaExito().getCaso().getValorExito();
//		float valorSucumbencia = fatura.getParcelaExito().getCaso().getValorSucumbencia();
//		float valorParcela = fatura.getParcelaExito().getValor();
		ParcelaExito parcela = fatura.getParcelaExito();
		
		StringBuilder conteudoRelatorio = new StringBuilder();
		MoedaConverter mc = new MoedaConverter();

		conteudoRelatorio
				.append("                                 Prezados Senhores:");
		conteudoRelatorio.append("\n");
		conteudoRelatorio.append("\n");

		conteudoRelatorio
				.append("                  Segue abaixo relatório de honorários advocatícios");
		conteudoRelatorio.append(" de êxitos relativo ao caso ");
		conteudoRelatorio.append(parcela.getCaso().getNomeCaso());
		conteudoRelatorio.append(" no valor de R$ ");
		conteudoRelatorio.append(mc.getAsString(null, null,	parcela.getValor()));
		if(parcela.getCaso().getTipoExito().equals(ValorParticipacaoEnum.PORCENTAGEM)){
			conteudoRelatorio.append(" correspondente ao porcentual de ");
			conteudoRelatorio.append(mc.getAsString(null, null,	parcela.getCaso().getValorPorcetagemExito()));
			conteudoRelatorio.append("% calculado sobre o valor de ");
			conteudoRelatorio.append(mc.getAsString(null, null,	parcela.getCaso().getBasePorcetegemExito()));
		}
		
		conteudoRelatorio.append(" com vencimento para o dia ");
		conteudoRelatorio.append(DateUtil.dataToString(parcela.getDataVencimento()));
		conteudoRelatorio.append(", conforme contrato de ");
		conteudoRelatorio.append("prestação de serviços e proposta aprovada em ");
		conteudoRelatorio.append(DateUtil.dataToString(parcela.getCaso().getData()));
		conteudoRelatorio.append("\n");
		conteudoRelatorio.append("\n");
		conteudoRelatorio.append("\n");
		conteudoRelatorio.append("\n");
		conteudoRelatorio.append("\n");
		conteudoRelatorio.append("Curitiba, ");
		conteudoRelatorio.append(DateUtil.dataToString(new Date(), "dd"));
		conteudoRelatorio.append(" de ");
		conteudoRelatorio.append(mesPorExtenso(new Date().getMonth()));
		conteudoRelatorio.append(", de ");
		conteudoRelatorio.append(DateUtil.dataToString(new Date(), "yyyy"));

		RelatorioJasperDTO jasper = new RelatorioJasperDTO();
		jasper.setCliente(fatura.getCliente().getNome());
		jasper.setConteudo(conteudoRelatorio.toString());
		jasper.setCaso(parcela.getCaso().getNomeCaso());

		List<RelatorioJasperDTO> lista = new ArrayList<RelatorioJasperDTO>();
		lista.add(jasper);

		JasperReport report;
		try {
			report = JasperCompileManager
					.compileReport(PropertiesLoaderImpl.getValor("relatorio_exito"));
			// // preenchimento do relatorio, note que o metodo
			// recebe 3 parametros: // 1 - o relatorio // // 2 - um
			// Map, com parametros que sao passados ao relatorio //
			// no momento do preenchimento. No nosso caso eh null,
			// pois nao // estamos usando nenhum parametro // // 3 -
			// o data source. Note que nao devemos passar a lista
			// diretamente, // e sim "transformar" em um data source
			// utilizando a classe //
			// JRBeanCollectionDataSource
			JasperPrint print = JasperFillManager.fillReport(report, null,
					new JRBeanCollectionDataSource(lista)); // exportacao do
			// relatorio para outro formato, no caso PDF]]

			// JasperExportManager.exportReportToPdfFile(print,
			// "~/RelatorioClientes.pdf");
			System.out.println("Relatório gerado.");

			final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			byteArrayOutputStream.write(JasperExportManager
					.exportReportToPdf(print));

			return byteArrayOutputStream;

		} catch (Exception e) {
			e.printStackTrace();
			throw new ControleException("Erro ao gerar relaltório", e);
		}

	}
	
	public List<Fatura> pesquisarFatura(Fatura fatura, Date dataInicioPesquisa, Date dataFimPesquisa){
		StringBuffer hql = new StringBuffer();
		hql.append("FROM Fatura f WHERE");
		
		
		if (dataInicioPesquisa != null && dataFimPesquisa != null) {
			hql.append(" ( (f.dataFatura >= :dataInicio AND f.dataFatura <= :dataFim) OR (f.dataPrevisaoPagamento >= :dataInicio AND f.dataPrevisaoPagamento <= :dataFim))  AND ");
		}
		else {
			if ( dataInicioPesquisa != null){
				hql.append(" (f.dataFatura >= :dataInicio OR f.dataPrevisaoPagamento >= :dataInicio) AND ");
			}
			
			if ( dataFimPesquisa != null){
				hql.append(" (f.dataFatura <= :dataFim OR f.dataPrevisaoPagamento <= :dataFim) AND ");
			}
		}
		
		
		if (fatura.getCliente() != null && fatura.getCliente().getNome() != null && !"".equals(fatura.getCliente().getNome())){
			hql.append(" lower (f.cliente.nome) like :nome AND ");
		}
		
		if (fatura != null && fatura.getStatusFatura() != null ){
			hql.append("  f.statusFatura = :statusFatura AND ");
		}
		
		hql.append(" 1 = 1 order by f.cliente.nome, f.dataFatura desc");
		
		System.out.println(hql.toString());
		
		Query query = em.createQuery(hql.toString(),Fatura.class);
		
		
		if ( dataInicioPesquisa != null){
			query.setParameter("dataInicio", dataInicioPesquisa);
		}
		
		if ( dataFimPesquisa != null){
			query.setParameter("dataFim", dataFimPesquisa);
		}
		
		if (fatura.getCliente() != null && fatura.getCliente().getNome() != null && !"".equals(fatura.getCliente().getNome())){
			query.setParameter("nome", "%"+fatura.getCliente().getNome().toLowerCase()+"%");
		}
		
		if (fatura != null && fatura.getStatusFatura() != null ){
			query.setParameter("statusFatura", fatura.getStatusFatura());
		}
		
		 List<Fatura> faturas = query.getResultList();
		
		 for (Fatura fatura2 : faturas) {
			 List<Trabalho> tblos = fatura2.getListaTrabalhos();
			 for (Trabalho t : tblos) {
				t.getCaso().getNomeCaso();
				t.getCaso().getData();
			}
			}
		 
		 
		return faturas;
	}
	
	

	
	
	public void excluirFatura(Integer idFatura) throws ControleException,Exception{
		Fatura fatura = em.find(Fatura.class, idFatura);
		
		//Se for distribuido mudar para faturado
		if(fatura.getStatusFatura().equals(StatusTrabalho.DISTRIBUIDO)){
			
			fatura.setStatusFatura(StatusTrabalho.FATURADO);
			this.atualizarFatura(fatura);
			
			
		} else if (fatura.getStatusFatura().equals(StatusTrabalho.FATURADO) ){
			
			//Realiza exclusão fixa ou variavel de acordo com lista de trabalho
			if (fatura.getListaTrabalhos() != null && fatura.getListaTrabalhos().size() > 0){
				for (Trabalho trabalho  : fatura.getListaTrabalhos()) {
					trabalho.setStatusTrabalho(StatusTrabalho.ENVIADO_FATURAR);
					trabalhoService.alterarStatusTrabalho(trabalho);
				}
			}
			//É fixo, alterar os prolabores somente
			else if(fatura.getListaProlabore() != null && fatura.getListaProlabore().size() > 0){
				
				for (Prolabore prolabore  : fatura.getListaProlabore()) {
					prolabore.setFatura(null);
					prolabore.setStatusProlaboreEnum(StatusTrabalho.CRIADO);
					prolaboreService.salvarProlaboreComPreParticipacao(prolabore);
				}
				
			} 
			//senão é exito
			else {
				ParcelaExito parcela = parcelaExitoService.obterParcelaExito(fatura.getParcelaExito().getId());
				
				//deve voltar status do caso para aberto
			    Caso caso = casoService.obterCaso(parcela.getCaso().getIdCaso());
			    caso.setStatusCaso(StatusCaso.CRIADO);
				casoService.alterarCaso(caso,true);
//				parcelaExitoService.excluirParcelaExito(parcela);
				em.remove(parcela);
			
			}
			
			participacaoService.exlcuirParticipacaoPorFatura(fatura.getIdFatura(),false);
			
			em.remove(fatura);
		}
		else {
			throw new ControleException("Não é possível excluir fatura. Já existem pagamentos vinculados.");
		}
		//Alterar o status dos trabalhos
	}
	
	public void atualizarFatura(Fatura fatura){
		em.merge(fatura);
	}
	
	public Fatura obterFaturaPorParcelaExito(ParcelaExito parcela){
		StringBuffer hql = new StringBuffer();
		hql.append("FROM Fatura f WHERE");
		
		hql.append("  f.parcelaExito = :parcelaExito  ");
		
		Query query = em.createQuery(hql.toString(),Fatura.class);
		query.setParameter("parcelaExito", parcela);
		
		return (Fatura) query.getSingleResult();
	}
	
	public Fatura obterFaturaPorIdFatura(Integer idFatura){
		StringBuffer hql = new StringBuffer();
		hql.append("FROM Fatura f ");
		hql.append(" LEFT JOIN FETCH f.listParticipacao participacao ");
		
		
		hql.append(" WHERE  f.idFatura = :idFatura ");
		
		Query query = em.createQuery(hql.toString(),Fatura.class);
		query.setParameter("idFatura", idFatura);
		
		return (Fatura) query.getSingleResult();
	}
	
	/**
	 * 
	 * retorna o mes por extenso
	 */
	
	@TransactionAttribute(TransactionAttributeType.NEVER)
	public String mesPorExtenso(int mesAtual){
		switch (mesAtual) {
		case 0:
			return "Janeiro";
		case 1:
			return "Fevereiro";
		case 2:
			return "Março";
		case 3:
			return "Abril";
		case 4:
			return "Maio";
		case 5:
			return "Junho";
		case 6:
			return "Julho";
		case 7:
			return "Agosto";
		case 8:
			return "Setembro";
		case 9:
			return "Outubro";
		
		case 10:
			return "Novembro";
	
		}
		
		return "Dezembro";
		
	}
	
	@TransactionAttribute(TransactionAttributeType.NEVER)
	public String parserStringHtml(String texto){
		if(texto != null){
			texto = texto.replaceAll("á", "&aacute;");
			texto = texto.replaceAll("à", "&agrave;");
			texto = texto.replaceAll("í", "&iacute;");
			texto = texto.replaceAll("ô", "&ocirc;");
			texto = texto.replaceAll("Á", "&Aacute;");
			texto = texto.replaceAll("À", "&Agrave;");
			texto = texto.replaceAll("Í", "&Iacute;");
			texto = texto.replaceAll("Ô", "&Ocirc;");
			texto = texto.replaceAll("ã", "&atilde;");
			texto = texto.replaceAll("é", "&eacute;");
			texto = texto.replaceAll("ó", "&oacute;");
			texto = texto.replaceAll("ú", "&uacute;");
			texto = texto.replaceAll("Ã", "&Atilde;");
			texto = texto.replaceAll("É", "&Eacute;");
			texto = texto.replaceAll("Ó", "&Oacute;");
			texto = texto.replaceAll("Ú", "&Uacute;");
			texto = texto.replaceAll("â", "&acirc;");
			texto = texto.replaceAll("ê", "&ecirc;");
			texto = texto.replaceAll("õ", "&otilde;");
			texto = texto.replaceAll("ç", "&ccedil;");
			texto = texto.replaceAll("Â", "&Acirc;");
			texto = texto.replaceAll("Ê", "&Ecirc;");
			texto = texto.replaceAll("Õ", "&Otilde;");
			texto = texto.replaceAll("Ç", "&Ccedil;");
			texto = texto.replaceAll("-", "&#45;");
			texto = texto.replaceAll("–", "&#45;");
			texto = texto.replaceAll("_", "&#95;");
			
			
		}
		return texto;
		
		
	}
}
