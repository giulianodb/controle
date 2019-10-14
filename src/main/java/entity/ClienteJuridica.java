package entity;

import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

@Entity
@Table(name = "cliente_juridica", schema = "controle")
@NamedQueries({ @NamedQuery(name = "listarClienteJuridica", query = "SELECT cj FROM ClienteJuridica cj") })
public class ClienteJuridica extends Cliente {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6005955777757181896L;

	private String cnpj;

	private String nomeContato;

	private String telefoneContato;

	private String nomeContatoSecundario;

	private String telefoneContatoSecundario;

	public String getNomeContato() {
		return nomeContato;
	}

	public void setNomeContato(String nomeContato) {
		this.nomeContato = nomeContato;
	}

	public String getTelefoneContato() {
		return telefoneContato;
	}

	public void setTelefoneContato(String telefoneContato) {
		this.telefoneContato = telefoneContato;
	}

	public String getCnpj() {
		return cnpj;
	}

	public void setCnpj(String cnpj) {
		this.cnpj = cnpj;
	}

	public String getNomeContatoSecundario() {
		return nomeContatoSecundario;
	}

	public void setNomeContatoSecundario(String nomeContatoSecundario) {
		this.nomeContatoSecundario = nomeContatoSecundario;
	}

	public String getTelefoneContatoSecundario() {
		return telefoneContatoSecundario;
	}

	public void setTelefoneContatoSecundario(String telefoneContatoSecundario) {
		this.telefoneContatoSecundario = telefoneContatoSecundario;
	}
}
