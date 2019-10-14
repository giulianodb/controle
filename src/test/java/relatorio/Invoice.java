package relatorio;

import java.util.List;

public class Invoice {

    private String formattedTotal;
    private String vatNumber;
    private boolean vatRegistered;
    private List filhos;
    
	public String getFormattedTotal() {
		return formattedTotal;
	}
	public void setFormattedTotal(String formattedTotal) {
		this.formattedTotal = formattedTotal;
	}
	public String getVatNumber() {
		return vatNumber;
	}
	public void setVatNumber(String vatNumber) {
		this.vatNumber = vatNumber;
	}
	public boolean isVatRegistered() {
		return vatRegistered;
	}
	public void setVatRegistered(boolean vatRegistered) {
		this.vatRegistered = vatRegistered;
	}
	public List getFilhos() {
		return filhos;
	}
	public void setFilhos(List filhos) {
		this.filhos = filhos;
	}

    

}