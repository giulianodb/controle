package control;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.w3c.dom.Document;
import org.w3c.tidy.Tidy;
import org.xhtmlrenderer.pdf.ITextRenderer;

import util.PropertiesLoaderImpl;

import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.BaseFont;


public class Teste {
    public static void convert(String input, OutputStream out) throws DocumentException{  
        convert(new ByteArrayInputStream(input.getBytes()), out);  
    }  
      
    public static void convert(InputStream input, OutputStream out) throws DocumentException{  
        Tidy tidy = new Tidy();     
        tidy.setBreakBeforeBR(false);
        tidy.setMakeClean(false);
        tidy.setMakeBare(false);
        
        tidy.setRawOut(false);
        tidy.setSmartIndent(false);
        tidy.setWord2000(true);
        
        Document doc = tidy.parseDOM(input, null); 
        ITextRenderer renderer = new ITextRenderer();  
        renderer.setDocument(doc, null); 
        
        
        try {
        	//inserindo fontes
			renderer.getFontResolver().addFont("http://"+  
					PropertiesLoaderImpl.getValor("endereco")+"/calibri/CALIBRII.TTF",  
			        BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
			
			renderer.getFontResolver().addFont("http://"+  
					PropertiesLoaderImpl.getValor("endereco")+"/calibri/CALIBRIZ.TTF",  
			        BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
			
			renderer.getFontResolver().addFont("http://"+  
					PropertiesLoaderImpl.getValor("endereco")+"/calibri/CALIBRI.TTF",  
			        BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
          
        renderer.getFontResolver().getFontFamily("arial"); 
        
        
        renderer.layout();         
        renderer.createPDF(out);    
        
        String k = " asdasd " +
        		"";
    } 
    
    
    
    public static OutputStream main(String teste){
    	try {
    		OutputStream os = new FileOutputStream("~/teste.pdf");  
			
    		File file = new File("/teste.pdf");
    		
    		
    		String opa = "\" , ~^'`{[}] += *&%$#@!¨";
    		
    		if (teste == null){
    			teste = 
        				"<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\"> " + 
        		"<html> " + 
        		"<head> " + 
        		"<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"> " + 
        		"<title>Insert title here</title> " + 
        		"</head> " + 
        		"<body> " + 
        				
        				"<center>  </center> " + 
        		   "<center>FATURA - HONOR&Aacute;RIOS ADVOCATÍCIOS</center>"+
        		" <br><br>    "+
        		" <table border=\"0\" align=\"left\" width=\"50%\">  "+
        			"<tr>   "+
        				"<td align=\"right\">Cliente:</td> "+
        				"<td>Nome Cliente </td> "+
        			"</tr>   "+
        			"<tr>    "+
        				"<td  align=\"right\">Mês referencia:</td>   "+
        				"<td>Fevereiro</td>   "+
        			"</tr>   "+
        		"</table>   "+
        		"<br><br><br>  "+
        		
        		"<table border=\"1px\" width=\"100%\">  "+
        			"<tr bgcolor=\"#8B8989\"  >   "+
        			"	<td> <b>Data </b></td>  "+
        			"	<td><b>Tempo</b></td>   "+
        			"	<td><b>Caso "+opa+"</b></td>  "+
        			"	<td><b>Descrição</b></td>  "+
        			"</tr>  "+
        			"<tr> " +
        			"	<td>20/10/1987</td>  "+
        			"	<td>1:00</td>  "+
        			"	<td>Nome do caso</td>  "+
        			"	<td>A descrição do caso</td>  "+
        			"</tr>  "+
        		"</table>  "+
        		"<br><br>  "+
        		
        		"<table border=\"1px\" width=\"45%\"> "+
        			"<tr> "+
        			"	<td colspan=\"2\" bgcolor=\"#8B8989\"  align=\"center\" style=\"FONT-FAMILY: 'Abyssinica SIL';\">Total de horas </td> "+
        				
        			"</tr> "+
        			"<tr>  "+
        			"	<td>Total de horas </td>  "+
        			"	<td> 1:00</td>  "+
        			"</tr> "+
        			"<tr> "+
        			"	<td>R$ por hora</td>  "+
        			"	<td> R$ 350,00</td>  "+
        		"	</tr> "+
        		"	<tr> "+
        		"		<td>Total Honorarios</td>  "+
        		"		<td> R$ 750,00</td>  "+
        		"	</tr> "+
        		"</table> "
        		
        		
        		;
    		}
    		
    		Teste.convert(teste, os);
	    	
			
			os.close();
			
			return os;
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}           
    	
    	return null;
    }
}
