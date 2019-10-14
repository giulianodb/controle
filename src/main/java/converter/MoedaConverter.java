package converter;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

import util.NumeroUtil;

@FacesConverter("converter.MoedaConverter")
public class MoedaConverter implements Converter{

	@Override
	public Object getAsObject(FacesContext arg0, UIComponent arg1, String valor) {
		Float retorno = new Float(0);
		if (valor != null && !valor.equals("")){
			
			valor = valor.replace(",", ".");
			retorno = Float.parseFloat(valor);
			//inserido para deixar como duas casas...
			retorno = NumeroUtil.deixarFloatDuasCasas(retorno);
		}
		
		return retorno;
	}

	@Override
	public String getAsString(FacesContext arg0, UIComponent arg1, Object arg2) {
		String moeda = "";
		if (arg2 != null) {
			moeda = arg2.toString().replace(".", ",");
			
		}
			
		String[] moedaSplit = moeda.split(",");
		
		if (moedaSplit.length >= 2 && moedaSplit[1].length() == 1){
			moeda = moeda + "0";
		}
		
		if (moedaSplit.length >= 2) {
			if ( moedaSplit[1].length() > 2){
				moeda = moedaSplit[0] +","+ moedaSplit[1].substring(0, 2); 
		
			}
		}
		
		return moeda;
	}

}
