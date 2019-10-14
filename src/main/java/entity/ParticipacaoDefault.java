package entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import util.NumeroUtil;

@Entity
@Table(name="participacao_default",schema="controle")
public class ParticipacaoDefault implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -5281431147018754892L;

	@Id
	@SequenceGenerator(name = "PARTICIPACAO_DEFAULT_ID", sequenceName = "id_participacao_default_seq", schema="controle",allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "PARTICIPACAO_DEFAULT_ID")
	@Column(name = "id_participacao_default")
	private Integer idParticipacaoDefault;
	
	private Float valor;
	
	private String descricao;
	
	@Enumerated
	private TipoParticipacaoEnum nome;

	public Integer getIdParticipacaoDefault() {
		return idParticipacaoDefault;
	}

	public void setIdParticipacaoDefault(Integer idParticipacaoDefault) {
		this.idParticipacaoDefault = idParticipacaoDefault;
	}

	public Float getValor() {
		return valor;
	}

	public void setValor(Float valor) {
		this.valor = NumeroUtil.deixarFloatDuasCasas(valor);
	}

	public TipoParticipacaoEnum getNome() {
		return nome;
	}

	public void setNome(TipoParticipacaoEnum nome) {
		this.nome = nome;
	}

	public String getDescricao() {
		return descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}
	
}
