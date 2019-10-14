package entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.Size;

@Entity
@Table(name="valor_honorario",schema="controle")
public class ValorHonorario implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 8996467323447003284L;
	@Id
	@SequenceGenerator(name = "VALOR_HONORARIO_ID", sequenceName = "id_valor_honorario_seq", schema="controle",allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "VALOR_HONORARIO_ID")
	@Column(name = "id_valor_honorario")
	private Integer idValorHonorario;
	
	@Size(max=155)
	private String descricao;
	
	private Float valorHora;
	
	private String nome;

	public Integer getIdValorHonorario() {
		return idValorHonorario;
	}

	public void setIdValorHonorario(Integer idValorHonorario) {
		this.idValorHonorario = idValorHonorario;
	}

	public String getDescricao() {
		return descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}

	public Float getValorHora() {
		return valorHora;
	}

	public void setValorHora(Float valorHora) {
		this.valorHora = valorHora;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}
	
	
	
	
}
