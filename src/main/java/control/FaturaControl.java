package control;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletResponse;

import service.FaturaService;
import service.TrabalhoService;
import util.DateUtil;
import util.NumeroUtil;
import util.PropertiesLoaderImpl;
import dto.ClienteFaturarDTO;
import dto.participacao.DetalhesParticipacaoAdvogadoDTO;
import entity.Cliente;
import entity.Fatura;
import entity.Participacao;
import entity.StatusTrabalho;
import entity.Trabalho;
import entity.Usuario;
import exception.ControleException;

@Named
@SessionScoped
public class FaturaControl implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6114180711242496113L;

	private List<Fatura> listFatura;

	private List<ClienteFaturarDTO> listaClienteFaturarDTO;

	private Fatura fatura;

	private Integer idFatura;

	private Date dataInicioPesquisa;

	private Date dataFimPesquisa;

	private List<StatusTrabalho> listaStatusFaturaPesquisa;

	@EJB
	private FaturaService faturaService;

	@EJB
	private TrabalhoService trabalhoService;

	private DetalhesParticipacaoAdvogadoDTO detalhesParticipacaoAdvogadoDTO;

	private Date dataInicioDetalhesPesquisa;

	private Date dataFimDetalhesPesquisa;

	private Boolean statusCheckBox;
	
	private Boolean pesquisarComPendencias;

	private Date currentDate = new Date();

	private Float valorFaturaFixo;

	private ClienteFaturarDTO clienteFaturarDto;

	@Inject
	private FaturaExitoControl faturaExito;

	/**
	 * Pesquisa dos clientes com faturas variaveis para serem faturados
	 * 
	 * @return
	 */

	public String listarClientesFaturar() {

		Cliente cliente = new Cliente();
		fatura = new Fatura();
		fatura.setCliente(cliente);

		listaClienteFaturarDTO = faturaService.listarClientesFaturar();

		return "/pages/relatorio/relatorio_faturamento_";
	}

	public String pesquisarEmissaoFaturaFixa() {
		return "";
	}

	public Boolean mostrarListaCriarFaturamento() {

		if (listaClienteFaturarDTO != null && listaClienteFaturarDTO.size() > 0) {
			return true;
		} else {
			return false;
		}

	}

	public void gerarFaturaCliente() {
		try {
			fatura = faturaService.gerarFaturaCliente(fatura.getCliente()
					.getIdCliente());
			this.gerarRelatorioFatura(fatura, true, true);

		} catch (ControleException e) {
			FacesContext.getCurrentInstance().addMessage(
					null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, e
							.getMessage(), ""));
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public String alterarNaoCobravel() {
		try {
			trabalhoService.altrarFaturaNaoCobravel(fatura.getCliente()
					.getIdCliente());
			FacesContext.getCurrentInstance().addMessage(
					null,
					new FacesMessage(FacesMessage.SEVERITY_INFO,
							"Sucesso ao alterar trabalho para não cobrável.",
							""));
		} catch (ControleException e) {
			FacesContext.getCurrentInstance().addMessage(
					null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, e
							.getMessage(), ""));
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			FacesContext.getCurrentInstance().addMessage(
					null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, e
							.getMessage(), ""));
			e.printStackTrace();
		}

		return listarClientesFaturar();

	}

	public void gerarRelatorioFaturaExcel(Fatura fatura,
			Boolean faturamentoFinal) {
		try {

			if (fatura.tipoFatura().equals("Caso fixo")) {
				// TODO fazer excel para variavel
				// ByteArrayOutputStream byteArrayOutputStream =
				// faturaService.gerarRelatorioFaturaFixo(fatura,
				// faturamentoFinal);
				//
				// FacesContext facesContext =
				// FacesContext.getCurrentInstance();
				//
				// HttpServletResponse response = (HttpServletResponse)
				// facesContext.getExternalContext().getResponse();
				// response.setContentType("application/pdf");
				// response.addHeader("Content-disposition","attachment; filename=\" "+fatura.getCliente().getNome()+"_"+fatura.getMesReferencia()
				// + ".pdf\"");
				//
				//
				// OutputStream os = null;
				// os = response.getOutputStream();
				//
				//
				// byteArrayOutputStream.writeTo(os);
				// os.flush();
				// os.close();
				// byteArrayOutputStream.close();
				// facesContext.responseComplete();

			} else if (fatura.tipoFatura().equals("Fechamento caso")) {
				// faturaExito.gerarRelatorioFaturaExito(fatura,
				// faturamentoFinal);

				// TODO Fazer excel fechamento de caso

			} else {

				FacesContext facesContext = FacesContext.getCurrentInstance();
				ByteArrayOutputStream byteArrayOutputStream = faturaService
						.gerarExcelFixo(fatura, faturamentoFinal);
				HttpServletResponse response = (HttpServletResponse) facesContext
						.getExternalContext().getResponse();

				response.setContentType("application/vnd.ms-excel");
				response.addHeader(
						"Content-disposition",
						"attachment; filename=\" "
								+ fatura.getCliente().getNome() + "_"
								+ fatura.getMesReferencia() + ".xls\"");

				OutputStream os = null;

				os = response.getOutputStream();
				byteArrayOutputStream.writeTo(os);
				os.flush();
				os.close();
				byteArrayOutputStream.close();
				facesContext.responseComplete();

				FacesContext.getCurrentInstance().responseComplete();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void gerarRelatorioFatura(Fatura fatura, Boolean faturamentoFinal,
			Boolean download) {
		try {

			if (fatura.tipoFatura().equals("Caso fixo")) {
				ByteArrayOutputStream byteArrayOutputStream = faturaService
						.gerarRelatorioFaturaFixo(fatura, faturamentoFinal);

				FacesContext facesContext = FacesContext.getCurrentInstance();

				if (download) {

					HttpServletResponse response = (HttpServletResponse) facesContext
							.getExternalContext().getResponse();
					response.setContentType("application/pdf");
					response.addHeader("Content-disposition",
							"attachment; filename=\" "
									+ fatura.getCliente().getNome() + "_"
									+ fatura.getMesReferencia() + ".pdf\"");

					OutputStream os = null;
					os = response.getOutputStream();

					byteArrayOutputStream.writeTo(os);
					os.flush();
					os.close();
					byteArrayOutputStream.close();
					FacesContext.getCurrentInstance().addMessage(
							null,
							new FacesMessage(FacesMessage.SEVERITY_INFO,
									"Sucesso ao gerar fatura.", ""));
					facesContext.responseComplete();

				}

			} else if (fatura.tipoFatura().equals("Fechamento caso")) {
				faturaExito.gerarRelatorioFaturaExito(fatura, faturamentoFinal,
						download);

			} else {

				String relatorioFaturamento = faturaService
						.obterStringRelatorioFatura(fatura, faturamentoFinal);

				FacesContext facesContext = FacesContext.getCurrentInstance();

				if (download) {
					HttpServletResponse response = (HttpServletResponse) facesContext
							.getExternalContext().getResponse();

					response.setContentType("application/pdf");
					response.addHeader("Content-disposition",
							"attachment; filename=\" "
									+ fatura.getCliente().getNome() + "_"
									+ fatura.getMesReferencia() + ".pdf\"");

					OutputStream os = null;

					os = response.getOutputStream();

					Teste.convert(relatorioFaturamento, os);

					os.flush();
					os.close();
					FacesContext.getCurrentInstance().addMessage(
							null,
							new FacesMessage(FacesMessage.SEVERITY_INFO,
									"Sucesso ao gerar fatura.", ""));
					FacesContext.getCurrentInstance().responseComplete();
				}
			}
		} catch (Exception e) {
			FacesContext.getCurrentInstance().addMessage(
					null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR,
							"Erro ao gerar fatura.", e.getMessage()));
			e.printStackTrace();
		}

	}

	public void visualizarRelatorio() {
		try {
			fatura = faturaService.visualizarFaturaCliente(fatura.getCliente()
					.getIdCliente());
			this.gerarRelatorioFatura(fatura, false, true);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public String deletarFatura() {
		try {
			faturaService.excluirFatura(idFatura);
			FacesContext.getCurrentInstance().addMessage(
					null,
					new FacesMessage(FacesMessage.SEVERITY_INFO,
							"Sucesso ao excluir fatura.", ""));
		} catch (ControleException e) {
			FacesContext.getCurrentInstance().addMessage(
					null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, e
							.getMessage(), ""));
		}

		catch (Exception e) {
			FacesContext
					.getCurrentInstance()
					.addMessage(
							null,
							new FacesMessage(
									FacesMessage.SEVERITY_ERROR,
									"Problemas ao excluir fatura. Entre em contato com o administrador.",
									""));
			e.printStackTrace();
		}

		return listarFaturas();

	}

	public String listarFaturas() {
		popularListaStatus();
		if (fatura == null) {
			fatura = new Fatura();
			fatura.setCliente(new Cliente());
			fatura.setStatusFatura(StatusTrabalho.FATURADO);
		}

		Calendar c = Calendar.getInstance();
		c.add(Calendar.DAY_OF_YEAR, -30); // removento dois dias;

		dataInicioPesquisa = c.getTime();
		dataFimPesquisa = new Date();

		listFatura = faturaService.pesquisarFatura(fatura, dataInicioPesquisa,
				dataFimPesquisa);

		return "/pages/relatorio/lista_fatura";
	}

	public String pesquisarFatura() {
		popularListaStatus();
		if (fatura == null) {
			fatura = new Fatura();
			fatura.setCliente(new Cliente());

		}
		if (dataInicioPesquisa != null) {
			dataInicioPesquisa = DateUtil.adicionarHoraInicio(dataInicioPesquisa);
			
		}
		
		if (dataFimPesquisa != null) {
			dataFimPesquisa = DateUtil.adicionarHoraFim(dataFimPesquisa);
		}

		
		if (pesquisarComPendencias != null && pesquisarComPendencias == true){
			List<Fatura> faturas = faturaService.pesquisarFatura(fatura, dataInicioPesquisa,
					dataFimPesquisa);
			
			listFatura = new ArrayList<Fatura>();
			
			for (Fatura fatura : faturas) {
				
				if (fatura.getStatusFatura().equals(StatusTrabalho.DISTRIBUIDO)) {
					continue;
				}
				
				//Variaveis para controlar se existem dados de participação de trabalho e indicação caso não existir, manda para próxima fatura
				
				Integer participacaoIndicacao = new Integer(0);
				Integer participacaoTrabalho = 0;
				
				for (Participacao participacao : fatura.getListParticipacao()) {
					participacaoIndicacao = participacaoIndicacao + participacao.getPorcentagemIndicacao().intValue();
					participacaoTrabalho = participacaoTrabalho + participacao.getPorcentagemParticipacaoTrabalho().intValue();
				}
				//Adiciona na lista as faturas com pendencias
				if (participacaoIndicacao == 0){ 
					fatura.setPendenciaIndicacao(true);
					listFatura.add(fatura);
				}
				if (participacaoTrabalho == 0){
					fatura.setPendenciaCriterioTrabalho(true);
					listFatura.add(fatura);
				}
				
				
			}
		}
		else{
			
			listFatura = faturaService.pesquisarFatura(fatura, dataInicioPesquisa,
					dataFimPesquisa);
		}
		
		

		return "/pages/relatorio/lista_fatura";
	}

	/**
	 * Renderiza tela que lista todas as faturas com pendências
	 * 
	 * @return
	 */
	public String listarFaturasPendentes(){
		popularListaStatus();
		listFatura = new ArrayList<Fatura>();
		if(fatura == null){
			fatura = new Fatura();
			fatura.setCliente(new Cliente());
			fatura.setStatusFatura(StatusTrabalho.FATURADO);
		}
		
		Calendar c = Calendar.getInstance();
		c.add(Calendar.DAY_OF_YEAR,-90 ); //removento dois dias;
		
		dataInicioPesquisa = c.getTime();
		dataFimPesquisa = new Date();
		
		List<Fatura> faturas = faturaService.pesquisarFatura(fatura,dataInicioPesquisa,dataFimPesquisa);
		
		for (Fatura fatura : faturas) {
			
			//Variaveis para controlar se existem dados de participação de trabalho e indicação caso não existir, manda para próxima fatura
			
			Integer participacaoIndicacao = new Integer(0);
			Integer participacaoTrabalho = 0;
			
			for (Participacao participacao : fatura.getListParticipacao()) {
				participacaoIndicacao = participacaoIndicacao + participacao.getPorcentagemIndicacao().intValue();
				participacaoTrabalho = participacaoTrabalho + participacao.getPorcentagemParticipacaoTrabalho().intValue();
			}
			//Adiciona na lista as faturas com pendencias
			if (participacaoIndicacao == 0){ 
				fatura.setPendenciaIndicacao(true);
				listFatura.add(fatura);
			}
			if (participacaoTrabalho == 0){
				fatura.setPendenciaCriterioTrabalho(true);
				listFatura.add(fatura);
			}
			
			
		}
		
		
		return "/pages/relatorio/lista_fatura_pendencia";
	}
	

	public void popularListaStatus() {
		if (listaStatusFaturaPesquisa == null
				|| listaStatusFaturaPesquisa.size() == 0) {
			listaStatusFaturaPesquisa = new ArrayList<StatusTrabalho>();
			listaStatusFaturaPesquisa.add(StatusTrabalho.FATURADO);
			listaStatusFaturaPesquisa.add(StatusTrabalho.DISTRIBUIDO);
		}
	}

	// Usado para alterar o status da fatura
	public String alterarFatura() {
		try {
			faturaService.alterarFaturaParaPago(fatura);
			FacesContext.getCurrentInstance().addMessage(
					null,
					new FacesMessage(FacesMessage.SEVERITY_INFO,
							"Sucesso ao alterar fatura.", ""));

		} catch (Exception e) {
			FacesContext.getCurrentInstance().addMessage(
					null,
					new FacesMessage(FacesMessage.SEVERITY_FATAL,
							"Problemas ao alterar fatura.", ""));
			e.printStackTrace();
		}

		return this.listarFaturas();
	}

	public void selecionarListaFatura(ActionEvent actionEvent) {

		for (Fatura fatura : listFatura) {
			fatura.setFaturaSelecionada(statusCheckBox);
		}

	}

	public List<Fatura> getListFatura() {
		if (listFatura != null) {
			for (Fatura fatura : listFatura) {
				fatura.setValorPagamento(fatura.valorTotalFatura()
						- fatura.getValorPago());
			}

		}

		return listFatura;
	}

	public String pagarFaturas() {

		try {
			faturaService.alterarListaFatura(listFatura);
			FacesContext.getCurrentInstance().addMessage(
					null,
					new FacesMessage(FacesMessage.SEVERITY_INFO,
							"Sucesso ao receber pagamentos de faturas .", ""));
		} catch (ControleException e) {
			e.printStackTrace();
			FacesContext.getCurrentInstance().addMessage(
					null,
					new FacesMessage(FacesMessage.SEVERITY_FATAL, e
							.getMessage(), ""));

		} catch (Exception e) {
			e.printStackTrace();
			FacesContext.getCurrentInstance().addMessage(
					null,
					new FacesMessage(FacesMessage.SEVERITY_FATAL,
							"Problemas ao receber pagamentos de faturas.", ""));

		}

		return listarFaturas();
	}

	/**
	 * Integração com planilhas da consultoria
	 * 
	 * @return
	 */
	public String gerarArquivoDistribuicao() {

		try {

			FileWriter arq = new FileWriter(
					PropertiesLoaderImpl.getValor("temp_txt"));
			PrintWriter gravarArq = new PrintWriter(arq);

			for (Fatura fatura : listFatura) {
				if (fatura.getFaturaSelecionada()) {
					for (Trabalho trabalho : fatura.getListaTrabalhos()) {

						Float valorTotalFatura = fatura.valorTotalFatura();

						if (trabalho.getTrabalhoCompartilhado() != null
								&& trabalho.getTrabalhoCompartilhado()) {
							int size = trabalho.getUsuarios().size();
							size++;

							valorTotalFatura = NumeroUtil.DividirDinheiro(
									valorTotalFatura, Float.valueOf(size), 2);
						}

						List<Usuario> advogados = new ArrayList<Usuario>();
						advogados.add(trabalho.getAdvogado());

						for (Usuario usuarioComp : trabalho.getUsuarios()) {
							advogados.add(usuarioComp);
						}

						for (Usuario usuario : advogados) {
							gravarArq.print(trabalho.getIdTrabalho());
							gravarArq.print("|");
							gravarArq.print(fatura.getCliente().getNome());
							gravarArq.print("|");
							gravarArq.print(DateUtil.dataToString(fatura
									.getDataFatura()));
							gravarArq.print("|");
							gravarArq.print(NumeroUtil.deixarFloatDuasCasas(
									valorTotalFatura).toString());
							gravarArq.print("|");
							gravarArq.print(trabalho.getCaso().getNomeCaso());
							gravarArq.print("|");
							gravarArq.print(usuario.getNome());
							gravarArq.print("|");
							gravarArq.print(trabalho.getValorTotalTrabalho());
							gravarArq.printf("\n");

						}

					}

				}

			}

			arq.close();

			FacesContext facesContext = FacesContext.getCurrentInstance();
			HttpServletResponse response = (HttpServletResponse) facesContext
					.getExternalContext().getResponse();

			response.setContentType("application/txt");
			response.addHeader("Content-disposition",
					"attachment; filename=\" distribuicao.txt\"");

			OutputStream os = null;

			os = response.getOutputStream();

			InputStream fileInputStream = new FileInputStream(new File(
					PropertiesLoaderImpl.getValor("temp_txt")));

			byte[] bytesBuffer = new byte[4048];
			int bytesRead;
			while ((bytesRead = fileInputStream.read(bytesBuffer)) > 0) {
				os.write(bytesBuffer, 0, bytesRead);
			}

			os.flush();

			fileInputStream.close();
			os.close();

			FacesContext.getCurrentInstance().responseComplete();

			FacesContext.getCurrentInstance().addMessage(
					null,
					new FacesMessage(FacesMessage.SEVERITY_INFO,
							"Sucesso ao emitir arquivo.", ""));
			facesContext.responseComplete();

		} catch (ControleException e) {
			e.printStackTrace();
			FacesContext.getCurrentInstance().addMessage(
					null,
					new FacesMessage(FacesMessage.SEVERITY_FATAL, e
							.getMessage(), ""));

		} catch (Exception e) {
			e.printStackTrace();
			FacesContext.getCurrentInstance().addMessage(
					null,
					new FacesMessage(FacesMessage.SEVERITY_FATAL,
							"Problemas ao emitir arquivo.", ""));

		}

		return listarFaturas();
	}

	public String gerarArquivoCSVDistribuicao() {

		try {

			FacesContext facesContext = FacesContext.getCurrentInstance();
			ByteArrayOutputStream byteArrayOutputStream = faturaService.gerarExcelDistribuicao(listFatura, dataInicioPesquisa,dataFimPesquisa);
			
			HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();

			response.setContentType("application/vnd.ms-excel");
			response.addHeader("Content-disposition",
					"attachment; filename=\" distribuicao.xls\"");

			OutputStream os = null;

			os = response.getOutputStream();
			byteArrayOutputStream.writeTo(os);
			os.flush();
			os.close();
			byteArrayOutputStream.close();
			facesContext.responseComplete();
			FacesContext.getCurrentInstance().addMessage(
					null,
					new FacesMessage(FacesMessage.SEVERITY_INFO,
							"Sucesso ao emitir arquivo.", ""));

			FacesContext.getCurrentInstance().responseComplete();

		} catch (ControleException e) {
			e.printStackTrace();
			FacesContext.getCurrentInstance().addMessage(
					null,
					new FacesMessage(FacesMessage.SEVERITY_FATAL, e
							.getMessage(), ""));

		} catch (Exception e) {
			e.printStackTrace();
			FacesContext.getCurrentInstance().addMessage(
					null,
					new FacesMessage(FacesMessage.SEVERITY_FATAL,
							"Problemas ao emitir arquivo.", ""));

		}

		return listarFaturas();
	}

	public void selecionarListaFaturar(ActionEvent actionEvent) {

		for (ClienteFaturarDTO dto : listaClienteFaturarDTO) {
			dto.setSelecionado(statusCheckBox);
		}

	}

	public String faturarSelecionados() {
		try {
			for (ClienteFaturarDTO fatura : listaClienteFaturarDTO) {
				if (fatura.getSelecionado()) {
					Fatura faturaGerada = faturaService
							.gerarFaturaCliente(fatura.getIdCliente());
					this.gerarRelatorioFatura(faturaGerada, true, false);
				}
			}

			FacesContext.getCurrentInstance().addMessage(
					null,
					new FacesMessage(FacesMessage.SEVERITY_INFO,
							"Sucesso ao gerar faturas.", ""));

		} catch (Exception e) {
			// TODO: handle exception

			FacesContext.getCurrentInstance().addMessage(
					null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR,
							"Erro ao gerar faturas.", e.getMessage()));
		}

		return listarClientesFaturar();
	}

	// Responsável por listar todos os clientes com faturas de casos fixo para
	// serem faturados
	// TODO rever

	// public String listarClientesFaturarFixo(){
	//
	// listaClienteFaturarDTO= faturaService.listarClientesFaturarFixo();
	//
	//
	// return "/pages/faturamento/para_faturar_fixo";
	// }
	// TODO rever
	// public String configurarFaturaFixo(){
	//
	// //TODO obter todos os casos e popular a DTO correta para aparecer na
	// tela.
	//
	// listCasoFixoDTO = faturaService.configurarFaturaFixo(clienteFaturarDto);
	//
	// return "/pages/faturamento/configurar_fatura_fixo";
	// }
	// //TODO rever
	// public String gerarFaturaFixa(){
	//
	// gerarRelatorioFaturaFixo(faturaService.gerarFaturaFixo(),true);
	//
	//
	// return listarClientesFaturarFixo();
	// }
	//
	// //TODO rever
	// public void gerarRelatorioFaturaFixo(Fatura faturaFixo, Boolean
	// faturamentoFinal){
	// try {
	// String relatorioFaturamento =
	// faturaService.obterStringRelatorioFaturaFixo(faturaFixo,
	// faturamentoFinal);
	//
	// FacesContext facesContext = FacesContext.getCurrentInstance();
	//
	// HttpServletResponse response = (HttpServletResponse)
	// facesContext.getExternalContext().getResponse();
	//
	// response.setContentType("application/pdf");
	// response.addHeader("Content-disposition","attachment; filename=\" "+faturaFixo.getCliente().getNome()+"_"+faturaFixo.getMesReferencia()
	// + ".pdf\"");
	//
	// OutputStream os = null;
	//
	// os = response.getOutputStream();
	//
	// Teste.convert(relatorioFaturamento, os);
	//
	// os.flush();
	// os.close();
	//
	// FacesContext.getCurrentInstance().responseComplete();
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	//
	// }

	// //TODO rever
	// public void listenerAlterarQuantidadeParcelasFixas(ActionEvent
	// actionEvent){
	//
	// for (CasoFixoDTO casoFixoDTO : listCasoFixoDTO) {
	// if(casoFixoDTO.getQuantidadeParcelasFatura() >
	// casoFixoDTO.getCaso().getParcelasPagas()){
	// casoFixoDTO.setQuantidadeParcelasFatura(casoFixoDTO.getCaso().getQuantidadeParcelas()
	// - casoFixoDTO.getCaso().getParcelasPagas());
	// }
	// }
	// }

	public void setListFatura(List<Fatura> listFatura) {
		this.listFatura = listFatura;
	}

	public List<ClienteFaturarDTO> getListaClienteFaturarDTO() {
		return listaClienteFaturarDTO;
	}

	public void setListaClienteFaturarDTO(
			List<ClienteFaturarDTO> listaClienteFaturarDTO) {
		this.listaClienteFaturarDTO = listaClienteFaturarDTO;
	}

	public Fatura getFatura() {
		return fatura;
	}

	public void setFatura(Fatura fatura) {
		this.fatura = fatura;
	}

	public Date getDataInicioPesquisa() {
		return dataInicioPesquisa;
	}

	public void setDataInicioPesquisa(Date dataInicioPesquisa) {
		this.dataInicioPesquisa = dataInicioPesquisa;
	}

	public Date getDataFimPesquisa() {
		return dataFimPesquisa;
	}

	public void setDataFimPesquisa(Date dataFimPesquisa) {
		this.dataFimPesquisa = dataFimPesquisa;
	}

	public List<StatusTrabalho> getListaStatusFaturaPesquisa() {
		return listaStatusFaturaPesquisa;
	}

	public void setListaStatusFaturaPesquisa(
			List<StatusTrabalho> listaStatusFaturaPesquisa) {
		this.listaStatusFaturaPesquisa = listaStatusFaturaPesquisa;
	}

	public DetalhesParticipacaoAdvogadoDTO getDetalhesParticipacaoAdvogadoDTO() {
		return detalhesParticipacaoAdvogadoDTO;
	}

	public void setDetalhesParticipacaoAdvogadoDTO(
			DetalhesParticipacaoAdvogadoDTO detalhesParticipacaoAdvogadoDTO) {
		this.detalhesParticipacaoAdvogadoDTO = detalhesParticipacaoAdvogadoDTO;
	}

	public Date getDataInicioDetalhesPesquisa() {
		return dataInicioDetalhesPesquisa;
	}

	public void setDataInicioDetalhesPesquisa(Date dataInicioDetalhesPesquisa) {
		this.dataInicioDetalhesPesquisa = dataInicioDetalhesPesquisa;
	}

	public Date getDataFimDetalhesPesquisa() {
		return dataFimDetalhesPesquisa;
	}

	public void setDataFimDetalhesPesquisa(Date dataFimDetalhesPesquisa) {
		this.dataFimDetalhesPesquisa = dataFimDetalhesPesquisa;
	}

	public Boolean getStatusCheckBox() {
		return statusCheckBox;
	}

	public void setStatusCheckBox(Boolean statusCheckBox) {
		this.statusCheckBox = statusCheckBox;
	}

	public Date getCurrentDate() {
		return currentDate;
	}

	public void setCurrentDate(Date currentDate) {
		this.currentDate = currentDate;
	}

	public Integer getIdFatura() {
		return idFatura;
	}

	public void setIdFatura(Integer idFatura) {
		this.idFatura = idFatura;
	}

	public void setValorFaturaFixo(Float valorFaturaFixo) {
		this.valorFaturaFixo = valorFaturaFixo;
	}

	public ClienteFaturarDTO getClienteFaturarDto() {
		return clienteFaturarDto;
	}

	public void setClienteFaturarDto(ClienteFaturarDTO clienteFaturarDto) {
		this.clienteFaturarDto = clienteFaturarDto;
	}

	public Boolean getPesquisarComPendencias() {
		return pesquisarComPendencias;
	}

	public void setPesquisarComPendencias(Boolean pesquisarComPendencias) {
		this.pesquisarComPendencias = pesquisarComPendencias;
	}

}
