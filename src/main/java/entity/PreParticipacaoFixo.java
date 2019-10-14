package entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import util.NumeroUtil;

//classe para definir qual adovgado faz parte da pre particiação
@Entity
@Table(name="preParticipacaoFixo",schema="controle")
public class PreParticipacaoFixo implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2366090951886384590L;

	@Id
	@SequenceGenerator(name = "PRE_PARTICIPACAO_ID", sequenceName = "id_pre_participacao_seq", schema="controle",allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "PRE_PARTICIPACAO_ID")
	@Column(name = "id_pre_participacao")
	private Integer idPreParticipacao;
	
	@Column(precision=10, scale=2)
	private Float valorParticipacao;
	
	@ManyToOne(fetch = FetchType.LAZY)
	private Prolabore prolabore;
	
	@ManyToOne(fetch = FetchType.LAZY)
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

	public Prolabore getProlabore() {
		return prolabore;
	}

	public void setProlabore(Prolabore prolabore) {
		this.prolabore = prolabore;
	}

	public Usuario getAdvogado() {
		return advogado;
	}

	public void setAdvogado(Usuario advogado) {
		this.advogado = advogado;
	}


	
}
