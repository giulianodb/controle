package dto;

import entity.Usuario;

/**
 * DTO responsavel em definir qual adovogado e qual prolabore
 * @author giuliano
 *
 */
public class AdvogadoValorExito {
	
	private Usuario usuario;
	
	private float valorExito;

	public Usuario getUsuario() {
		return usuario;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}

	public float getValorExito() {
		return valorExito;
	}

	public void setValorExito(float valorExito) {
		this.valorExito = valorExito;
	}
}
