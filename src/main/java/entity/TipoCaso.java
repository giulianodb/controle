package entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name="tipo_caso",schema="controle")
@NamedQueries({ @NamedQuery(name = "listarTipoCaso", query = "SELECT tc FROM TipoCaso tc") 	
})
public class TipoCaso implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@SequenceGenerator(name = "TIPO_CASO_ID", sequenceName = "id_tipo_caso_seq", schema="controle",allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "TIPO_CASO_ID")
	@Column(name = "id_tipo_caso")
	private Integer idTipoCaso;
	
	private String nome;
	
	private String descricao;
	
	public Integer getIdTipoCaso() {
		return idTipoCaso;
	}

	public void setIdTipoCaso(Integer idTipoCaso) {
		this.idTipoCaso = idTipoCaso;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public String getDescricao() {
		return descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}
	
	

	
}
