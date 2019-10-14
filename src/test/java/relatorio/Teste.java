package relatorio;


public class Teste {

	/**
	 * @param args
	 */

	public static void main(String[] args) {
		// TODO Auto-generated method stub/*
		/*
		final InputStream in = Teste.class.getResourceAsStream("/invoiceTemplate.odt");
		try {
			final IXDocReport report = XDocReportRegistry.getRegistry().loadReport(
					in, TemplateEngineKind.Velocity);
			
			final IContext context = report.createContext();
					
			// Register developers list
			List<Developer> developers = new ArrayList<Developer>();
			developers.add(new Developer("Leclercq", "Pascal", "pascal.leclercq@gmail.com"));
			developers.add(new Developer("Leclercq2", "Pascal2", "pascal2.leclercq2@gmail.com"));
			context.put("developers", developers);
			
			FieldsMetadata metadata = new FieldsMetadata();
			metadata.addFieldAsList("developers.Name");
			metadata.addFieldAsList("developers.LastName");
			metadata.addFieldAsList("developers.Mail");
			report.setFieldsMetadata(metadata);

			// 3) Generate report by merging Java model with the ODT,
			 // and then converting to PDF
			 final OutputStream out =
			 new FileOutputStream(new File("mergedInvoice.pdf"));
			 
			 final Options options = Options.getTo(ConverterTypeTo.PDF)
			 .via(ConverterTypeVia.ODFDOM);
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
