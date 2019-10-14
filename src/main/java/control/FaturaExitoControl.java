package control;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.servlet.http.HttpServletResponse;

import service.ClienteService;
import service.FaturaService;
import service.ParcelaExitoService;
import service.ParticipacaoDefaultService;
import service.ParticipacaoService;
import service.UsuarioService;
import entity.Cliente;
import entity.Fatura;
import entity.ParcelaExito;
import exception.ControleException;

@Named
@SessionScoped
public class FaturaExitoControl implements Serializable {

	private static final long serialVersionUID = 441317418357476411L;
	
	private List<ParcelaExito> listParcelaExito;
	
	private Date dataInicioPesquisa;
	
	private Date dataFimPesquisa;
	
	private List<Cliente> listaClienteCombo;
	
	private ParcelaExito parcelaExitoPesquisa;
	
	private ParcelaExito parcelaExito;
	
	private Integer idClienteComboPesquisa;

	@EJB
	private FaturaService faturaService;
	
	@EJB
	private ParcelaExitoService parcelaExitoService;
	
	@EJB
	private ClienteService clienteService;
	
	@EJB
	private UsuarioService usuarioService;
	
	@EJB
	private ParticipacaoDefaultService participacaoDefaultService;
	
	@EJB
	private ParticipacaoService participacaoService;
	
	private Boolean statusCheckBox;
	
	
	public void carregarComboCliente(){
		if (listaClienteCombo == null || listaClienteCombo.size() == 0){
			listaClienteCombo = clienteService.pesquisarCliente(new Cliente());
		}
		
	}
	
	
	public void gerarFaturaExito(Fatura fatura) throws Exception{
			//Fatura fatura = faturaService.gerarFaturaExito(parcelaExito);
			this.gerarRelatorioFaturaExito(fatura, true, true);
	}
	
	public void gerarRelatorioFaturaExito(Fatura fatura, Boolean faturamentoFinal, boolean download) throws Exception{
		try {
			
			ByteArrayOutputStream byteArrayOutputStream  = faturaService.gerarRelatorioFaturaExito(fatura, faturamentoFinal);
			
			FacesContext facesContext = FacesContext.getCurrentInstance();
			
			if (download){
				HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();
				response.setContentType("application/pdf");
				response.addHeader("Content-disposition","attachment; filename=\" "+fatura.getCliente().getNome()+"_"+fatura.getMesReferencia() + ".pdf\"");
				
				OutputStream os = null;
				os = response.getOutputStream();
				
				byteArrayOutputStream.writeTo(os);  
				os.flush();  
				os.close();  
				byteArrayOutputStream.close();  
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso ao gerar fatura.", ""));
				FacesContext.getCurrentInstance().responseComplete();
				
			}
			else {
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso ao gerar fatura.", ""));
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		
	}

	public Boolean mostrarListaFaturamentoExito(){
		
		if(listParcelaExito != null && listParcelaExito.size() > 0){
			return true;
		}
		else {
			return false;
		}
		
	}
	
	public List<ParcelaExito> getListParcelaExito() {
		return listParcelaExito;
	}

	public void setListParcelaExito(List<ParcelaExito> listParcelaExito) {
		this.listParcelaExito = listParcelaExito;
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

	public List<Cliente> getListaClienteCombo() {
		return listaClienteCombo;
	}

	public void setListaClienteCombo(List<Cliente> listaClienteCombo) {
		this.listaClienteCombo = listaClienteCombo;
	}

	public ParcelaExito getParcelaExitoPesquisa() {
		return parcelaExitoPesquisa;
	}

	public void setParcelaExitoPesquisa(ParcelaExito parcelaExitoPesquisa) {
		this.parcelaExitoPesquisa = parcelaExitoPesquisa;
	}

	public ParcelaExito getParcelaExito() {
		return parcelaExito;
	}

	public void setParcelaExito(ParcelaExito parcelaExito) {
		this.parcelaExito = parcelaExito;
	}

	public Integer getIdClienteComboPesquisa() {
		return idClienteComboPesquisa;
	}

	public void setIdClienteComboPesquisa(Integer idClienteComboPesquisa) {
		this.idClienteComboPesquisa = idClienteComboPesquisa;
	}

	public Boolean getStatusCheckBox() {
		return statusCheckBox;
	}

	public void setStatusCheckBox(Boolean statusCheckBox) {
		this.statusCheckBox = statusCheckBox;
	}

	
}
