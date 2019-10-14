package entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import util.NumeroUtil;

/**
 * @author giuliano
 *
 */
@Entity
@Table(name="transacao",schema="controle")
public class Transacao implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -4548591082243018188L;
	
	@Id
	@SequenceGenerator(name = "TRANSACAO_ID", sequenceName = "id_transacao_seq", schema="controle",allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "TRANSACAO_ID")
	@Column(name = "id_transacao")
	private Integer idTranasacao;
	@Column(precision=10, scale=2)
	private Float valorTransacao;
	
	private Date dataTransacao;
	
	private String descricao;
	
	@Enumerated
	@Column(name="tipo_transacao")
	private TipoTransacaoEnum tipoTransacaoEnum;
	
	@ManyToOne
	private Participacao participacao;
	
	@ManyToOne
	private Conta conta;
	
	/**
	 * Define para qual cliente a transacção pertence. As transacções para advogados não possuiem clientes
	 */
	@ManyToOne
	private Fatura fatura;
	
	public Integer getIdTranasacao() {
		return idTranasacao;
	}
	public void setIdTranasacao(Integer idTranasacao) {
		this.idTranasacao = idTranasacao;
	}
	public Float getValorTransacao() {
		return valorTransacao;
	}
	public void setValorTransacao(Float valorTransacao) {
		this.valorTransacao = NumeroUtil.deixarFloatDuasCasas(valorTransacao);
	}
	public Date getDataTransacao() {
		return dataTransacao;
	}
	public void setDataTransacao(Date dataTransacao) {
		this.dataTransacao = dataTransacao;
	}
	public Participacao getParticipacao() {
		return participacao;
	}
	public void setParticipacao(Participacao participacao) {
		this.participacao = participacao;
	}
	public Conta getConta() {
		return conta;
	}
	public void setConta(Conta conta) {
		this.conta = conta;
	}
	public TipoTransacaoEnum getTipoTransacaoEnum() {
		return tipoTransacaoEnum;
	}
	public void setTipoTransacaoEnum(TipoTransacaoEnum tipoTransacaoEnum) {
		this.tipoTransacaoEnum = tipoTransacaoEnum;
	}
	public Fatura getFatura() {
		return fatura;
	}
	public void setFatura(Fatura fatura) {
		this.fatura = fatura;
	}
	
	public String mostrarDescricao(){
		String retorno = "";
		
		if (descricao == null || descricao.equals("")){
			if (tipoTransacaoEnum.equals(TipoTransacaoEnum.PAGAMENTO)){
				retorno = "Pagamento para advogado: " + conta.getAdvogado().getNome();
			}else{
				retorno = "Pagamento de cliente:" + fatura.getCliente().getNome();
			}
		}
		else {
			retorno = descricao;
		}
		
		return retorno;
	}
	
	public String getDescricao() {
		return descricao;
	}
	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}

	
}
