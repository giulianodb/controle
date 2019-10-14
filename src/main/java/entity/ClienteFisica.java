package entity;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

@Entity
@Table(name="cliente_fisica",schema="controle")
@NamedQueries({ @NamedQuery(name = "listarClienteFisica", query = "SELECT cf FROM ClienteFisica cf") 	
})
public class ClienteFisica extends Cliente {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2398116529934356509L;
	
	private String cpf;
	
	private Date dataNascimento;
	
	private String celular;

	public Date getDataNascimento() {
		return dataNascimento;
	}
	public void setDataNascimento(Date dataNascimento) {
		this.dataNascimento = dataNascimento;
	}
	public String getCelular() {
		return celular;
	}
	public void setCelular(String celular) {
		this.celular = celular;
	}
	public String getCpf() {
		return cpf;
	}
	public void setCpf(String cpf) {
		this.cpf = cpf;
	}
}
