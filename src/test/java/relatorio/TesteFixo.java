package relatorio;

import java.io.InputStream;

public class TesteFixo {

	/**
	 * @param args
	 */

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		final InputStream in = TesteFixo.class.getResourceAsStream("/report.odt");
		/*
		try {
			
			final IXDocReport report = XDocReportRegistry.getRegistry().loadReport(
					in, TemplateEngineKind.Velocity);
			
			final IContext context = report.createContext();
					
			context.put("mesanterior", "Junho");
			
			// 3) Generate report by merging Java model with the ODT,
			 // and then converting to PDF
			 final OutputStream out =
			 new FileOutputStream(new File("relatorio.pdf"));
			 
//			 final Options options = Options.getTo(ConverterTypeTo.PDF) .via(ConverterTypeVia.ODFDOM);
			 Options options = Options.getFrom(DocumentKind.DOCX).to(ConverterTypeTo.PDF);
			 
			 report.convert(context, options, out);
			 
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (XDocReportException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
			 */
	}

}
