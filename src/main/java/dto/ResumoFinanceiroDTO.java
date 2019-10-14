package dto;

public class ResumoFinanceiroDTO {
	private Float saldoEmConta;
	private Float saldoReservadoAdvogado;
	private Float saldoReservadoImposto;
	private Float saldoDescontado;
	
	public ResumoFinanceiroDTO(){
		saldoEmConta = 0f;
		saldoReservadoAdvogado= 0f;
		saldoReservadoImposto= 0f;
		saldoDescontado= 0f;
	}
	
	public Float getSaldoEmConta() {
		return saldoEmConta;
	}
	public void setSaldoEmConta(Float saldoEmConta) {
		this.saldoEmConta = saldoEmConta;
	}
	public Float getSaldoReservadoAdvogado() {
		return saldoReservadoAdvogado;
	}
	public void setSaldoReservadoAdvogado(Float saldoReservadoAdvogado) {
		this.saldoReservadoAdvogado = saldoReservadoAdvogado;
	}
	public Float getSaldoReservadoImposto() {
		return saldoReservadoImposto;
	}
	public void setSaldoReservadoImposto(Float saldoReservadoImposto) {
		this.saldoReservadoImposto = saldoReservadoImposto;
	}
	public Float getSaldoDescontado() {
		return saldoDescontado;
	}
	public void setSaldoDescontado(Float saldoDescontado) {
		this.saldoDescontado = saldoDescontado;
	}
}
