package util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class NumeroUtil {
	
	public static final String MULTI = "*";
	public static final String SOMA = "+";
	public static final String SUB = "-";
	public static final String DIV = "/";
	public static final String ABRE = "(";
	public static final String FECHA = ")";
	
	
	public static Float deixarFloatDuasCasas(Float numero){
		if (numero != null){
			String numeroEmString = numero.toString();
			numeroEmString = numeroEmString.replace(".", ",");
			
			String numeroSplit[] = numeroEmString.split(",");
			String numeroFloat = "";
						
			if (numeroSplit.length >= 2) {
				if(numeroSplit[1].length() == 1){
					numeroSplit[1] = numeroSplit[1]+"0";
				}
				if ( numeroSplit[1].length() >= 2){
					numeroFloat = numeroSplit[0] +"."+ numeroSplit[1].substring(0, 2); 
				}
			}
			
			return Float.valueOf(numeroFloat);
		}
		else {
			return numero;
		}
	}
	
	
	public static Float multiplicarDinheiro(Float x, Float y, Integer casas){
		if (x == null){
			x = 0f;
		}
		
		if (y == null){
			y = 0f;
		}
		BigDecimal a = new BigDecimal(x.toString());
		BigDecimal b = new BigDecimal(y.toString());
		
		a.setScale(casas, RoundingMode.DOWN);
		b.setScale(casas, RoundingMode.DOWN);
		
		Float retorno = a.multiply(b).floatValue(); 
		
		return (retorno);
	}
	
	
	public static Float DividirDinheiro(Float x, Float y, Integer casas){
		if (x == null){
			x = 0f;
		}
		
		if (y == null){
			y = 0f;
		}
		BigDecimal a = new BigDecimal(x.toString());
		BigDecimal b = new BigDecimal(y.toString());
		
		Float retorno = a.divide(b,casas,RoundingMode.DOWN).floatValue(); 
		
		return retorno;
	}
	
	
	public static Float somarDinheiro(Float x, Float y, Integer casas){
		if (x == null){
			x = 0f;
		}
		
		if (y == null){
			y = 0f;
		}
		
		BigDecimal a = new BigDecimal(x.toString());
		BigDecimal b = new BigDecimal(y.toString());
		
		a.setScale(casas, RoundingMode.DOWN);
		b.setScale(casas, RoundingMode.DOWN);
		
		Float retorno = a.add(b).floatValue();
		
		return retorno;
	}
	
	
	public static Float diminuirDinheiro(Float x, Float y, Integer casas){
		if (x == null){
			x = 0f;
		}
		
		if (y == null){
			y = 0f;
		}
		BigDecimal a = new BigDecimal(x.toString());
		BigDecimal b = new BigDecimal(y.toString());
		
		a.setScale(casas, RoundingMode.DOWN);
		b.setScale(casas, RoundingMode.DOWN);
		
		Float retorno = a.subtract(b).floatValue();
		
		return retorno;
	}
	
    /**
     * da o valor em porcento de uma valor
     */
	
	public static Float porcentagem(Float valor, Float porcentagem){
//		BigDecimal a = new BigDecimal(valor.toString());
//		BigDecimal b = new BigDecimal(porcentagem.toString());
		
		Float a = multiplicarDinheiro(valor, porcentagem, 3);
		Float b = DividirDinheiro(a, 100f, 3);
		
		return b;
		
	}
	
	/**
	 * 
	 * @param valor
	 * @param porcentagem
	 * @return
	 */
	
	
	public static Float valorDaPorcentagem(Float valor, Float porcentagem){
//		BigDecimal a = new BigDecimal(valor.toString());
//		BigDecimal b = new BigDecimal(porcentagem.toString());
		
		Float a = DividirDinheiro(porcentagem, 100f, 3);
		Float b = multiplicarDinheiro(a, valor, 3);
		
		return b;
		
	}
	
	
	
	public static Float expressao(List<String> expressao){
		
		
		return null;
	}
	
}
