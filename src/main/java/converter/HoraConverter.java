package converter;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

@FacesConverter("converter.HoraConverter")
public class HoraConverter implements Converter{

	@Override
	public Object getAsObject(FacesContext arg0, UIComponent arg1, String tempo) {
		Float retorno = new Float(0);
		if (tempo != null){
			
			String[] tempoSlipt = tempo.split(":");
			
			Float tempoFloat = Float.parseFloat(tempoSlipt[1])/60;
			
			retorno = tempoFloat + Float.parseFloat(tempoSlipt[0]);
			
		}
		return retorno;
	}

	@Override
	public String getAsString(FacesContext arg0, UIComponent arg1, Object arg2) {
		String horaRetorno = "";
		/**/
		if (arg2 != null){
			String tempoString = arg2.toString().replace(".", ",");;
			String[] tempoSplit = tempoString.split(",");
			
			if (tempoSplit.length > 1){
				if (tempoSplit[1].length() == 1){
					tempoSplit[1]= tempoSplit[1] + "0";
				}
				Integer minuto = (int) (Float.parseFloat(tempoSplit[1])*60*0.01f);
				String minutoString = minuto.toString();
				if (minutoString.length() == 1){
					minutoString = minutoString + "0";
				}
					
				
				horaRetorno = tempoSplit[0] +":"+ minutoString;
			}
			else {
				horaRetorno = arg2.toString();
			}
		}
		
		
		return horaRetorno;
	}

}
