package entity;

import java.io.Serializable;
import java.util.ArrayList;
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
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

@Entity
@Table(name = "caso", schema = "controle")
@NamedQueries({ @NamedQuery(name = "listarCaso", query = "SELECT c FROM Caso c") })
public class Caso implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1193809041154202061L;

	@Id
	@SequenceGenerator(name = "CASO_ID", sequenceName = "id_caso_seq", schema = "controle", allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "CASO_ID")
	@Column(name = "id_caso")
	private Integer idCaso;

	private Date data;

	private Float tempo;

	private Float valorHora;

	private Boolean cobravel;

	private String nomeCaso;

	private String descricao;

	// atributos para caso fixo
	private Float valorFixo;

	private Integer quantidadeParcelas;

	private Integer parcelasPagas;

	// fim atributos caso fixo
	// TODO fazer get valor tatal
	// TODO fazer desconto
	// /private Float valorTotal;

	@ManyToOne(fetch = FetchType.LAZY)
	@NotNull
	private Cliente cliente;

	@ManyToOne(fetch = FetchType.LAZY)
	@NotNull
	private Usuario advogado;

	@ManyToOne(fetch = FetchType.LAZY)
	private TipoCaso tipoCaso;

	@ManyToMany
	@LazyCollection(LazyCollectionOption.FALSE)
	// para funcionar eager num mtm
	@JoinTable(name = "caso_indicacao", schema = "controle", joinColumns = @JoinColumn(name = "id_caso"), inverseJoinColumns = @JoinColumn(name = "id_usuario"))
	private List<Usuario> listaUsuarioIndicacao;

	@OneToMany(mappedBy = "caso", cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy("dataTrabalho")
	@LazyCollection(LazyCollectionOption.TRUE)
	// para funcionar eager num mtm
	private List<Trabalho> listaTrabalho;

	@Enumerated
	@Column(name = "statusCaso")
	private StatusCaso statusCaso;

	@Enumerated
	@Column(name = "tipoFaturaCaso")
	private TipoFaturaCasoEnum tipoFaturaCaso;

	@Transient
	private Boolean casoSelecionado;

	@LazyCollection(LazyCollectionOption.FALSE)
	@OneToMany(mappedBy = "caso", cascade = CascadeType.ALL)
	@OrderBy("ordem")
	private List<Prolabore> listaProlabole;
	
	
	public List<Prolabore> getListaProlaboleList() {
			this.listaProlaboleList = new ArrayList<Prolabore>();
			this.listaProlaboleList.addAll(this.listaProlabole);
		return listaProlaboleList;
	}

	public void setListaProlaboleList(List<Prolabore> listaProlaboleList) {
		this.listaProlaboleList = listaProlaboleList;
	}

	//Gambeta para obter lista..
	@Transient
	private List<Prolabore> listaProlaboleList;

	@LazyCollection(LazyCollectionOption.FALSE)
	@OneToMany(mappedBy = "caso", cascade = CascadeType.ALL)
	private List<PreParticipacaoExito> listaPreParticipacaoExito;

	@LazyCollection(LazyCollectionOption.TRUE)
	@OneToOne(cascade = CascadeType.ALL)
	private ParcelaExito parcelaExito;

	@Enumerated
	private ValorParticipacaoEnum tipoExito;

	private Float valorSucumbencia;
	
	/**
	 * Valor do êxito. se o tipo for valor será inserido nesse atributo. Caso
	 * for porcentagem o valor da porcentagem estará nesse atributo
	 */
	private Float valorExito;

	/**
	 * Caso existir exito com porcetagem a porcetagem estará nesse atributo
	 */
	private Float valorPorcetagemExito;
	
	private Float basePorcetegemExito;

	private Date dataExito;

	private Float valorDistribuicaoExitoSocio;

	private Float valorDistribuicaoExitoTrabalho;

	private Float valorDistribuicaoExitoIndicacao;

	private Float valorDistribuicaoExitoImposto;

	private Float valorDistribuicaoExitoFundo;
	
	private Boolean previsaoSucumbencia;
	

	public Float getValorTotal() {
		if (tempo != null && valorHora != null) {
			return tempo * valorHora;
		}

		else {
			return 0F;
		}

	}

	public Integer getIdCaso() {
		return idCaso;
	}

	public void setIdCaso(Integer idCaso) {
		this.idCaso = idCaso;
	}

	public Date getData() {
		return data;
	}

	public void setData(Date data) {
		this.data = data;
	}

	public Float getTempo() {
		return tempo;
	}

	public void setTempo(Float tempo) {
		this.tempo = tempo;
	}

	public Float getValorHora() {
		return valorHora;
	}

	public void setValorHora(Float valorHora) {
		this.valorHora = valorHora;
	}

	public Boolean getCobravel() {
		return cobravel;
	}

	public void setCobravel(Boolean cobravel) {
		this.cobravel = cobravel;
	}

	public String getDescricao() {
		return descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}

	public Cliente getCliente() {
		return cliente;
	}

	public void setCliente(Cliente cliente) {
		this.cliente = cliente;
	}

	public Usuario getAdvogado() {
		return advogado;
	}

	public void setAdvogado(Usuario advogado) {
		this.advogado = advogado;
	}

	public TipoCaso getTipoCaso() {
		return tipoCaso;
	}

	public void setTipoCaso(TipoCaso tipoCaso) {
		this.tipoCaso = tipoCaso;
	}

	public String getNomeCaso() {
		return nomeCaso;
	}

	public void setNomeCaso(String nomeCaso) {
		this.nomeCaso = nomeCaso;
	}

	public List<Trabalho> getListaTrabalho() {
		return listaTrabalho;
	}

	public void setListaTrabalho(List<Trabalho> listaTrabalho) {
		this.listaTrabalho = listaTrabalho;
	}

	public StatusCaso getStatusCaso() {
		return statusCaso;
	}

	public void setStatusCaso(StatusCaso statusCaso) {
		this.statusCaso = statusCaso;
	}

	public TipoFaturaCasoEnum getTipoFaturaCaso() {
		return tipoFaturaCaso;
	}

	public void setTipoFaturaCaso(TipoFaturaCasoEnum tipoFaturaCaso) {
		this.tipoFaturaCaso = tipoFaturaCaso;
	}

	public List<Usuario> getListaUsuarioIndicacao() {
		return listaUsuarioIndicacao;
	}

	public void setListaUsuarioIndicacao(List<Usuario> listaUsuarioIndicacao) {
		this.listaUsuarioIndicacao = listaUsuarioIndicacao;
	}

	public String obterNomesIndicacoes() {
		String retorno = "";
		if (listaUsuarioIndicacao != null) {
			for (Usuario usuario : listaUsuarioIndicacao) {
				if (!retorno.equals("")) {
					retorno = retorno + " - ";
				}
				retorno = retorno + usuario.getNome();
			}
		}

		return retorno;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((idCaso == null) ? 0 : idCaso.hashCode());
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
		Caso other = (Caso) obj;
		if (idCaso == null) {
			if (other.idCaso != null)
				return false;
		} else if (!idCaso.equals(other.idCaso))
			return false;
		return true;
	}

	public Float getValorFixo() {
		return valorFixo;
	}

	public void setValorFixo(Float valorFixo) {
		this.valorFixo = valorFixo;
	}

	public Integer getQuantidadeParcelas() {
		return quantidadeParcelas;
	}

	public void setQuantidadeParcelas(Integer quantidadeParcelas) {
		this.quantidadeParcelas = quantidadeParcelas;
	}

	public Integer getParcelasPagas() {
		return parcelasPagas;
	}

	public void setParcelasPagas(Integer parcelasPagas) {
		this.parcelasPagas = parcelasPagas;
	}

	public Boolean getCasoSelecionado() {
		return casoSelecionado;
	}

	public void setCasoSelecionado(Boolean casoSelecionado) {
		this.casoSelecionado = casoSelecionado;
	}

	public List <Prolabore> getListaProlabole() {
		return listaProlabole;
	}

	public void setListaProlabole(List<Prolabore> listaProlabole) {
		this.listaProlabole = listaProlabole;
	}

	public ValorParticipacaoEnum getTipoExito() {
		return tipoExito;
	}

	public void setTipoExito(ValorParticipacaoEnum tipoExito) {
		this.tipoExito = tipoExito;
	}

	public Date getDataExito() {
		return dataExito;
	}

	public void setDataExito(Date dataExito) {
		this.dataExito = dataExito;
	}

	public Float getValorExito() {
		return valorExito;
	}

	public void setValorExito(Float valorExito) {
		this.valorExito = valorExito;
	}

	public Float getValorSucumbencia() {
		return valorSucumbencia;
	}

	public void setValorSucumbencia(Float valorSucumbencia) {
		this.valorSucumbencia = valorSucumbencia;
	}

	public Float getValorDistribuicaoExitoSocio() {
		return valorDistribuicaoExitoSocio;
	}

	public void setValorDistribuicaoExitoSocio(Float valorDistribuicaoExitoSocio) {
		this.valorDistribuicaoExitoSocio = valorDistribuicaoExitoSocio;
	}

	public Float getValorDistribuicaoExitoTrabalho() {
		return valorDistribuicaoExitoTrabalho;
	}

	public void setValorDistribuicaoExitoTrabalho(
			Float valorDistribuicaoExitoTrabalho) {
		this.valorDistribuicaoExitoTrabalho = valorDistribuicaoExitoTrabalho;
	}

	public Float getValorDistribuicaoExitoIndicacao() {
		return valorDistribuicaoExitoIndicacao;
	}

	public void setValorDistribuicaoExitoIndicacao(
			Float valorDistribuicaoExitoIndicacao) {
		this.valorDistribuicaoExitoIndicacao = valorDistribuicaoExitoIndicacao;
	}

	public Float getValorDistribuicaoExitoImposto() {
		return valorDistribuicaoExitoImposto;
	}

	public void setValorDistribuicaoExitoImposto(
			Float valorDistribuicaoExitoImposto) {
		this.valorDistribuicaoExitoImposto = valorDistribuicaoExitoImposto;
	}

	public Float getValorDistribuicaoExitoFundo() {
		return valorDistribuicaoExitoFundo;
	}

	public void setValorDistribuicaoExitoFundo(Float valorDistribuicaoExitoFundo) {
		this.valorDistribuicaoExitoFundo = valorDistribuicaoExitoFundo;
	}

	public List<PreParticipacaoExito> getListaPreParticipacaoExito() {
		return listaPreParticipacaoExito;
	}

	public void setListaPreParticipacaoExito(
			List<PreParticipacaoExito> listaPreParticipacaoExito) {
		this.listaPreParticipacaoExito = listaPreParticipacaoExito;
	}

	public Float getValorPorcetagemExito() {
		return valorPorcetagemExito;
	}

	public void setValorPorcetagemExito(Float valorPorcetagemExito) {
		this.valorPorcetagemExito = valorPorcetagemExito;
	}

	public Float getBasePorcetegemExito() {
		return basePorcetegemExito;
	}

	public void setBasePorcetegemExito(Float basePorcetegemExito) {
		this.basePorcetegemExito = basePorcetegemExito;
	}

	public ParcelaExito getParcelaExito() {
		return parcelaExito;
	}

	public void setParcelaExito(ParcelaExito parcelaExito) {
		this.parcelaExito = parcelaExito;
	}

	public Boolean getPrevisaoSucumbencia() {
		return previsaoSucumbencia;
	}

	public void setPrevisaoSucumbencia(Boolean previsaoSucumbencia) {
		this.previsaoSucumbencia = previsaoSucumbencia;
	}

}
