package entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.Size;
import javax.inject.Named;


@Entity
@Table(name="cliente",schema="controle")
//@Inheritance(strategy=InheritanceType.TABLE_PER_CLASS) //TODO verificar o que devo por 
@Inheritance(strategy = InheritanceType.JOINED)
@NamedQueries({ @NamedQuery(name = "listarCliente", query = "SELECT c FROM Cliente c") 	
})
public class Cliente implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -3795035789464662511L;
	
	@Id
	@SequenceGenerator(name = "CLIENTE_ID", sequenceName = "id_cliente_seq", schema="controle",allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "CLIENTE_ID")
	@Column(name = "id_cliente")
	private Integer idCliente;
	
	@Size(max=50)
	private String nome;

	private Date dataCadastro;
	
	private String email;
	
	private String telefone;
	
	private float valorHoraEstagiario;
	
	private float valorHoraJunior;
	
	private float valorHoraPleno;
	
	private float valorHoraSenior;
	
	public Integer getIdCliente() {
		return idCliente;
	}

	public void setIdCliente(Integer idCliente) {
		this.idCliente = idCliente;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public Date getDataCadastro() {
		return dataCadastro;
	}

	public void setDataCadastro(Date dataCadastro) {
		this.dataCadastro = dataCadastro;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getTelefone() {
		return telefone;
	}

	public void setTelefone(String telefone) {
		this.telefone = telefone;
	}

	public float getValorHoraEstagiario() {
		return valorHoraEstagiario;
	}

	public void setValorHoraEstagiario(float valorHoraEstagiario) {
		this.valorHoraEstagiario = valorHoraEstagiario;
	}

	public float getValorHoraJunior() {
		return valorHoraJunior;
	}

	public void setValorHoraJunior(float valorHoraJunior) {
		this.valorHoraJunior = valorHoraJunior;
	}

	public float getValorHoraPleno() {
		return valorHoraPleno;
	}

	public void setValorHoraPleno(float valorHoraPleno) {
		this.valorHoraPleno = valorHoraPleno;
	}

	public float getValorHoraSenior() {
		return valorHoraSenior;
	}

	public void setValorHoraSenior(float valorHoraSenior) {
		this.valorHoraSenior = valorHoraSenior;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((idCliente == null) ? 0 : idCliente.hashCode());
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
		Cliente other = (Cliente) obj;
		if (idCliente == null) {
			if (other.idCliente != null)
				return false;
		} else if (!idCliente.equals(other.idCliente))
			return false;
		return true;
	}

	
}
