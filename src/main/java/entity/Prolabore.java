package entity;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import util.NumeroUtil;

@Entity
@Table(name="prolabore",schema="controle")
public class Prolabore implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5449938340848418454L;
	
	public Prolabore(){
		
	}
	
	public Prolabore(Integer ordem){
		this.ordem = ordem;
	}
	
	@Id
	@SequenceGenerator(name = "PROLABORE_ID", sequenceName = "id_prolabore_seq", schema="controle",allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "PROLABORE_ID")
	@Column(name = "id_prolabore")
	private Integer id;
	
	private Date dataVencimento;
	
	private String evento;
	
	private Float valor;
	
	private Integer ordem;
	
	@Enumerated
	private ValorParticipacaoEnum valorParticipacaoEnum;
	
	@Enumerated
	private ProlaboreEnum prolaboreEnum;
	
	@ManyToOne(fetch = FetchType.LAZY)
	private Fatura fatura;
	
	@ManyToOne(fetch = FetchType.LAZY)
	private Caso caso;
	
	@Transient
	private boolean renderDataVencimento;
	
	@Transient
	private boolean renderEvento;
	
	//distribuição
	
	private Float porcentagemParticipacaoSocio;
	
	private Float porcentagemParticipacaoTrabalho;
	
	private Float porcentagemIndicacao;
	
	private Float porcentagemImposto;
	
	private Float porcentagemFundo;
	
	@Enumerated
	private StatusTrabalho statusProlaboreEnum;
	
	private Boolean preParticipacaoCadastrada;
	
	private Float valorParticipacaoTrabalho;
	
	
	@OneToMany(mappedBy="prolabore", cascade=CascadeType.ALL,  orphanRemoval=true)
	@LazyCollection(LazyCollectionOption.TRUE) //para funcionar eager num mtm
	private Set<PreParticipacaoFixo> listaPreParticipacaoFixos;
	
	@Transient
	private Boolean selecionado;
	
	public Date getDataVencimento() {
		return dataVencimento;
	}

	public void setDataVencimento(Date dataVencimento) {
		this.dataVencimento = dataVencimento;
	}

	public String getEvento() {
		return evento;
	}

	public void setEvento(String evento) {
		this.evento = evento;
	}

	public Float getValor() {
		return valor;
	}

	public void setValor(Float valor) {
		this.valor = valor;
	}

	public Integer getOrdem() {
		return ordem;
	}

	public void setOrdem(Integer ordem) {
		this.ordem = ordem;
	}

	public ProlaboreEnum getProlaboreEnum() {
		return prolaboreEnum;
	}

	public void setProlaboreEnum(ProlaboreEnum prolaboreEnum) {
		this.prolaboreEnum = prolaboreEnum;
	}
	
	public String vencimento(){
		
		if (evento != null && !evento.equals("")){
			return evento;
		}
		else if(dataVencimento != null) {
			
			SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy");
			String retorno = formato.format(dataVencimento);
			
			return retorno;
		}
		
		return null;
	}
	
	public String participacoes(){
		if (listaPreParticipacaoFixos == null || listaPreParticipacaoFixos.size() == 0){
			return "---";
		}
		else {
			Float soma = 0f;
			boolean porcentagem = this.getValorParticipacaoEnum().equals(ValorParticipacaoEnum.PORCENTAGEM);
			float valorComparacao = 0f;
			
			if (valorParticipacaoTrabalho != null){
				valorComparacao = valorParticipacaoTrabalho;
			}
			
			if (porcentagem){
				valorComparacao = 100f;
			}
			
			for (PreParticipacaoFixo p : listaPreParticipacaoFixos) {
				if (p.getValorParticipacao() != null ){
					soma = NumeroUtil.somarDinheiro(soma, p.getValorParticipacao(), 3);
				}
			}
			
			
			if (soma.equals(valorComparacao)){
				return "OK";
			}
			else {
				return "---";
			}
			
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((dataVencimento == null) ? 0 : dataVencimento.hashCode());
		result = prime * result + ((evento == null) ? 0 : evento.hashCode());
		result = prime * result + ((ordem == null) ? 0 : ordem.hashCode());
		result = prime * result
				+ ((prolaboreEnum == null) ? 0 : prolaboreEnum.hashCode());
		result = prime * result + ((valor == null) ? 0 : valor.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Prolabore other = (Prolabore) obj;
		if (dataVencimento == null) {
			if (other.dataVencimento != null)
				return false;
		} else if (!dataVencimento.equals(other.dataVencimento))
			return false;
		if (evento == null) {
			if (other.evento != null)
				return false;
		} else if (!evento.equals(other.evento))
			return false;
		if (ordem == null) {
			if (other.ordem != null)
				return false;
		} else if (!ordem.equals(other.ordem))
			return false;
		if (prolaboreEnum != other.prolaboreEnum)
			return false;
		if (valor == null) {
			if (other.valor != null)
				return false;
		} else if (!valor.equals(other.valor))
			return false;
		return true;
	}

	public boolean isRenderDataVencimento() {
		return renderDataVencimento;
	}

	public void setRenderDataVencimento(boolean renderDataVencimento) {
		this.renderDataVencimento = renderDataVencimento;
	}

	public boolean isRenderEvento() {
		return renderEvento;
	}

	public void setRenderEvento(boolean renderEvento) {
		this.renderEvento = renderEvento;
	}

	public Fatura getFatura() {
		return fatura;
	}

	public void setFatura(Fatura fatura) {
		this.fatura = fatura;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Caso getCaso() {
		return caso;
	}

	public void setCaso(Caso caso) {
		this.caso = caso;
	}

	public ValorParticipacaoEnum getValorParticipacaoEnum() {
		return valorParticipacaoEnum;
	}

	public void setValorParticipacaoEnum(ValorParticipacaoEnum valorParticipacaoEnum) {
		this.valorParticipacaoEnum = valorParticipacaoEnum;
	}

	public Set<PreParticipacaoFixo> getListaPreParticipacaoFixos() {
		return listaPreParticipacaoFixos;
	}

	public void setListaPreParticipacaoFixos(
			Set<PreParticipacaoFixo> listaPreParticipacaoFixos) {
		this.listaPreParticipacaoFixos = listaPreParticipacaoFixos;
	}

	public Float getPorcentagemParticipacaoSocio() {
		return porcentagemParticipacaoSocio;
	}

	public void setPorcentagemParticipacaoSocio(Float porcentagemParticipacaoSocio) {
		this.porcentagemParticipacaoSocio = porcentagemParticipacaoSocio;
	}

	public Float getPorcentagemParticipacaoTrabalho() {
		return porcentagemParticipacaoTrabalho;
	}

	public void setPorcentagemParticipacaoTrabalho(
			Float porcentagemParticipacaoTrabalho) {
		this.porcentagemParticipacaoTrabalho = porcentagemParticipacaoTrabalho;
	}

	public Float getPorcentagemIndicacao() {
		return porcentagemIndicacao;
	}

	public void setPorcentagemIndicacao(Float porcentagemIndicacao) {
		this.porcentagemIndicacao = porcentagemIndicacao;
	}

	public Float getPorcentagemImposto() {
		return porcentagemImposto;
	}

	public void setPorcentagemImposto(Float porcentagemImposto) {
		this.porcentagemImposto = porcentagemImposto;
	}

	public Float getPorcentagemFundo() {
		return porcentagemFundo;
	}

	public void setPorcentagemFundo(Float porcentagemFundo) {
		this.porcentagemFundo = porcentagemFundo;
	}

	public StatusTrabalho getStatusProlaboreEnum() {
		return statusProlaboreEnum;
	}

	public void setStatusProlaboreEnum(StatusTrabalho statusProlaboreEnum) {
		this.statusProlaboreEnum = statusProlaboreEnum;
	}

	public Boolean getPreParticipacaoCadastrada() {
		return preParticipacaoCadastrada;
	}

	public void setPreParticipacaoCadastrada(Boolean preParticipacaoCadastrada) {
		this.preParticipacaoCadastrada = preParticipacaoCadastrada;
	}

	public Float getValorParticipacaoTrabalho() {
		return valorParticipacaoTrabalho;
	}

	public void setValorParticipacaoTrabalho(Float valorParticipacaoTrabalho) {
		this.valorParticipacaoTrabalho = valorParticipacaoTrabalho;
	}

	public Boolean getSelecionado() {
		return selecionado;
	}

	public void setSelecionado(Boolean selecionado) {
		this.selecionado = selecionado;
	}
	
	
}
