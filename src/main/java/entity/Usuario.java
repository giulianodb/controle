package entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.Size;

@Entity
@Table(name="usuario",schema="controle")
@NamedQueries({ @NamedQuery(name = "obterPorLogin", query = "SELECT u FROM Usuario u WHERE u.loginUsuario = :login"),
	@NamedQuery(name = "listarUsuario", query = "SELECT u FROM Usuario u ")
})
public class Usuario implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -3795035789464662511L;
	
	@Id
	@SequenceGenerator(name = "USUARIO_ID", sequenceName = "id_usuario_seq", schema="controle",allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "USUARIO_ID")
	@Column(name = "id_usuario")
	private Integer idUsuario;
	
	@Size(max=50)
	private String nome;
	
	@ManyToOne(fetch=FetchType.EAGER)
	private Papel papel;
	
	@Column(unique=true)
	private String loginUsuario;
	
	private String senha;
	
	private String email;
	
	private Boolean participaExtra;
	
	private Float participacao;
	
	@Enumerated
	@Column(name="status")
	private StatusUsuario statusUsuario;
	
	public Integer getIdUsuario() {
		return idUsuario;
	}

	public void setIdUsuario(Integer idUsuario) {
		this.idUsuario = idUsuario;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public Papel getPapel() {
		return papel;
	}

	public void setPapel(Papel papel) {
		this.papel = papel;
	}

	public String getLoginUsuario() {
		return loginUsuario;
	}

	public void setLoginUsuario(String loginUsuario) {
		this.loginUsuario = loginUsuario;
	}

	public String getSenha() {
		return senha;
	}

	public void setSenha(String senha) {
		this.senha = senha;
	}

	public Boolean getParticipaExtra() {
		return participaExtra;
	}

	public void setParticipaExtra(Boolean participaExtra) {
		this.participaExtra = participaExtra;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public StatusUsuario getStatusUsuario() {
		return statusUsuario;
	}

	public void setStatusUsuario(StatusUsuario statusUsuario) {
		this.statusUsuario = statusUsuario;
	}

	public Float getParticipacao() {
		return participacao;
	}

	public void setParticipacao(Float participacao) {
		this.participacao = participacao;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((idUsuario == null) ? 0 : idUsuario.hashCode());
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
		Usuario other = (Usuario) obj;
		if (idUsuario == null) {
			if (other.idUsuario != null)
				return false;
		} else if (!idUsuario.equals(other.idUsuario))
			return false;
		return true;
	}

	
}
