package entity;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import util.NumeroUtil;

@Entity
@Table(name="conta",schema="controle")
public class Conta implements Serializable {
	
	private static final long serialVersionUID = 2598055189421012168L;
	@Id
	@SequenceGenerator(name = "CONTA_ID", sequenceName = "id_conta_seq", schema="controle",allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "CONTA_ID")
	@Column(name = "id_conta")
	private Integer idConta;
	
	@Column(precision=10, scale=2)
	private Float valorDisponivel;
	
	@OneToOne
	private Usuario advogado;
	
	@OneToMany(fetch=FetchType.LAZY,mappedBy="conta")
	private List<Transacao> listTransacao;
	
	@Enumerated
	@Column(name="tipo_conta")
	private TipoContaEnum tipoContaEnum;
	
	public Integer getIdConta() {
		return idConta;
	}

	public void setIdConta(Integer idConta) {
		this.idConta = idConta;
	}

	public Float getValorDisponivel() {
		return valorDisponivel;
	}

	public void setValorDisponivel(Float valorDisponivel) {
		this.valorDisponivel = NumeroUtil.deixarFloatDuasCasas(valorDisponivel);
	}

	public Usuario getAdvogado() {
		return advogado;
	}

	public void setAdvogado(Usuario advogado) {
		this.advogado = advogado;
	}

	public List<Transacao> getListTransacao() {
		return listTransacao;
	}

	public void setListTransacao(List<Transacao> listTransacao) {
		this.listTransacao = listTransacao;
	}

	public TipoContaEnum getTipoContaEnum() {
		return tipoContaEnum;
	}

	public void setTipoContaEnum(TipoContaEnum tipoContaEnum) {
		this.tipoContaEnum = tipoContaEnum;
	}
	
	
	
}
