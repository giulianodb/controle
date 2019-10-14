package entity;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;
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
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.Size;

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import util.NumeroUtil;

@Entity
@Table(name="trabalho",schema="controle")
@NamedQueries({ @NamedQuery(name = "listarTrabalho", query = "SELECT t FROM Trabalho t") 	
})
public class Trabalho implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 8996467323447003284L;
	@Id
	@SequenceGenerator(name = "TRABALHO_ID", sequenceName = "id_trabalho_seq", schema="controle",allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "TRABALHO_ID")
	@Column(name = "id_trabalho")
	private Integer idTrabalho;
	
	@Size(max=240,message="Descrição trabalho deve ser ter no máximo 240 caracteres")
	private String descricaoTrabalho;
	
	private Date dataTrabalho;
	
	private Float horasTrabalho;
	
	private Float valorHora;
	
	private Float valorTotalTrabalho;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@Inject
	private Usuario advogado;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@Inject
	private Caso caso;
	
	private Boolean cobravel;
	
	@Transient
	private boolean alterarViaAdm;
	
	@Enumerated
	@Column(name="status")
	private StatusTrabalho statusTrabalho;
	
	private Date dataEnviadoFaturar;
	
	//define se existe mais usuarios compartilhando esse trabalho. Normalmente usado para resolver questão de reuniõa com mais
	//advogados
	private Boolean trabalhoCompartilhado;
	
	//lista de usuários que compartilham esse trabalho
	@ManyToMany
	@LazyCollection(LazyCollectionOption.TRUE) //para funcionar eager num mtm
	@JoinTable(name="trabalho_advogado", schema="controle", 
            joinColumns=@JoinColumn(name="id_trabalho"), 
            inverseJoinColumns=@JoinColumn(name="id_usuario"))
	private List<Usuario> usuarios;
		
	public Integer getIdTrabalho() {
		return idTrabalho;
	}

	public void setIdTrabalho(Integer idTrabalho) {
		this.idTrabalho = idTrabalho;
	}

	public String getDescricaoTrabalho() {
		return descricaoTrabalho;
	}

	public void setDescricaoTrabalho(String descricaoTrabalho) {
		this.descricaoTrabalho = descricaoTrabalho;
	}

	public Date getDataTrabalho() {
		return dataTrabalho;
	}

	public void setDataTrabalho(Date dataTrabalho) {
		this.dataTrabalho = dataTrabalho;
	}

	public Float getHorasTrabalho() {
		return horasTrabalho;
	}

	public void setHorasTrabalho(Float horasTrabalho) {
		this.horasTrabalho = horasTrabalho;
	}

	public Usuario getAdvogado() {
		return advogado;
	}

	public void setAdvogado(Usuario advogado) {
		this.advogado = advogado;
	}

	public Caso getCaso() {
		return caso;
	}

	public void setCaso(Caso caso) {
		this.caso = caso;
	}

	public Float getValorTotalTrabalho() {
		//Feito para o ajax funcionar
		valorTotalTrabalho =  this.horasTrabalho * this.valorHora;
		return valorTotalTrabalho;
	}
	
	public void setValorTotalTrabalho(Float valorTotalTrabalho) {
		this.valorTotalTrabalho = NumeroUtil.deixarFloatDuasCasas(valorTotalTrabalho);
	}

	public Boolean getCobravel() {
		return cobravel;
	}

	public void setCobravel(Boolean cobravel) {
		this.cobravel = cobravel;
	}

	public Float getValorHora() {
		return valorHora;
	}

	public void setValorHora(Float valorHora) {
		this.valorHora = NumeroUtil.deixarFloatDuasCasas(valorHora);
	}

	public StatusTrabalho getStatusTrabalho() {
		return statusTrabalho;
	}

	public void setStatusTrabalho(StatusTrabalho statusTrabalho) {
		this.statusTrabalho = statusTrabalho;
	}

	public Boolean getAlterarViaAdm() {
		return alterarViaAdm;
	}

	public void setAlterarViaAdm(Boolean alterarViaAdm) {
		this.alterarViaAdm = alterarViaAdm;
	}

	public Date getDataEnviadoFaturar() {
		return dataEnviadoFaturar;
	}

	public void setDataEnviadoFaturar(Date dataEnviadoFaturar) {
		this.dataEnviadoFaturar = dataEnviadoFaturar;
	}

	public Boolean getTrabalhoCompartilhado() {
		return trabalhoCompartilhado;
	}

	public void setTrabalhoCompartilhado(Boolean trabalhoCompartilhado) {
		this.trabalhoCompartilhado = trabalhoCompartilhado;
	}

	public List<Usuario> getUsuarios() {
		return usuarios;
	}

	public void setUsuarios(List<Usuario> usuarios) {
		this.usuarios = usuarios;
	}

}
