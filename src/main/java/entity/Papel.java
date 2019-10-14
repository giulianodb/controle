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
import javax.validation.constraints.Size;

@Entity
@Table(name="papel",schema="controle")
@NamedQueries({ @NamedQuery(name = "listarPapel", query = "SELECT p FROM Papel p") 	
})
public class Papel implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 8996467323447003284L;
	@Id
	@SequenceGenerator(name = "PAPEL_ID", sequenceName = "id_papel_seq", schema="controle",allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "PAPEL_ID")
	@Column(name = "id_papel")
	private Integer idPapel;
	
	@Size(max=155)
	private String descricaoPapel;
	
	@Column(name="nome_papel")
	@Size(max=50)
	private String nomePapel;
	
	
	public String getNomePapel() {
		return nomePapel;
	}
	public void setNomePapel(String nomePapel) {
		this.nomePapel = nomePapel;
	}
	public Integer getIdPapel() {
		return idPapel;
	}
	public void setIdPapel(Integer idPapel) {
		this.idPapel = idPapel;
	}
	public String getDescricaoPapel() {
		return descricaoPapel;
	}
	public void setDescricaoPapel(String descricaoPapel) {
		this.descricaoPapel = descricaoPapel;
	}
	
	
}
