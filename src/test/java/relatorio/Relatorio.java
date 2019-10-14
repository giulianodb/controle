package relatorio;

import java.io.OutputStream;

import control.Teste;

public class Relatorio {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		
//		OutputStream os = null;
//		
//		os = response.getOutputStream();

//		Teste.convert("TESTE", os);
		
		StringBuilder sb = new StringBuilder("<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\"> ");
		sb.append("<html>");
		sb.append("<head>");
		
		sb.append(" <link rel=\"stylesheet\" href=\"~/body.css\"> ");
//		sb.append("  <style type=\"text/css\"> ");
//		sb.append(" body { ");
//		sb.append(" color: purple; ");
//		sb.append("  background-color: #d8da3d }");
//		sb.append("  </style>");
		sb.append("</head>");
		
		
		sb.append("<body>");
		sb.append("teste teste testeteste teste testeteste teste testeteste teste testeteste tteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeeste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste testeteste teste teste");
		sb.append("</body>");
		sb.append("</html>");
		sb.append("");
		

        

		
		OutputStream os = Teste.main(sb.toString());
		/*
		
		String inputFile = "samples/firstdoc.xhtml";
        String url;
		try {
			url = new File(inputFile).toURI().toURL().toString();
		    String outputFile = "firstdoc.pdf";
	        OutputStream os = new FileOutputStream(outputFile);
	        
	        ITextRenderer renderer = new ITextRenderer();
	        renderer.setDocument(url);
	        renderer.layout();
	        renderer.createPDF(os);
	        
	        os.close();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    
		*/
		
		

		
		
		
	}

}
