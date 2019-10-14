package entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

//classe para definir qual adovgado faz parte da pre particiação
@Entity
@Table(name="preParticipacaoExito",schema="controle")
public class PreParticipacaoExito implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2366090951886384590L;

	@Id
	@SequenceGenerator(name = "PRE_PARTICIPACAOEXITO_ID", sequenceName = "id_pre_participacaoexito_seq", schema="controle",allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "PRE_PARTICIPACAOEXITO_ID")
	private Integer idPreParticipacao;
	
	@Column(precision=10, scale=2)
	private Float valorParticipacao;
	
	@ManyToOne
	private Caso caso;
	
	@ManyToOne
	private Usuario advogado;
	
	public Integer getIdPreParticipacao() {
		return idPreParticipacao;
	}

	public void setIdPreParticipacao(Integer idPreParticipacao) {
		this.idPreParticipacao = idPreParticipacao;
	}

	public Float getValorParticipacao() {
		return valorParticipacao;
	}

	public void setValorParticipacao(Float valorParticipacao) {
		this.valorParticipacao = valorParticipacao;
	}

	public Caso getCaso() {
		return caso;
	}

	public void setCaso(Caso caso) {
		this.caso = caso;
	}

	public Usuario getAdvogado() {
		return advogado;
	}

	public void setAdvogado(Usuario advogado) {
		this.advogado = advogado;
	}


	
}
