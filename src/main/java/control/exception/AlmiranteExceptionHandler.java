package control.exception;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Iterator;

import javax.el.ELException;
import javax.faces.FacesException;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExceptionHandler;
import javax.faces.context.ExceptionHandlerWrapper;
import javax.faces.context.FacesContext;
import javax.faces.event.ExceptionQueuedEvent;
import javax.faces.event.ExceptionQueuedEventContext;

import org.apache.log4j.Logger;

import exception.AlmiranteException;

public class AlmiranteExceptionHandler extends ExceptionHandlerWrapper {
	
	private static final Logger log = Logger.getLogger(AlmiranteExceptionHandler.class);
	
	private ExceptionHandler wrapped;
	
	public AlmiranteExceptionHandler(ExceptionHandler wrapped) {
		this.wrapped = wrapped;
	}
	
	@Override
	public ExceptionHandler getWrapped() {
		return wrapped;
	}
	
	@Override
	public void handle() throws FacesException {
		
		Iterator<ExceptionQueuedEvent> i = getUnhandledExceptionQueuedEvents().iterator();
		
		while (i.hasNext()) {
			ExceptionQueuedEvent event = i.next();
			ExceptionQueuedEventContext context = (ExceptionQueuedEventContext) event.getSource();
			Throwable t = context.getException();
			
			FacesContext fc = FacesContext.getCurrentInstance();
			
			try {
				while ((t instanceof FacesException || t instanceof ELException) && t.getCause() != null) {
					t = t.getCause();
				}
				
				if (t instanceof AlmiranteException) {
					fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, t.getMessage(), ""));
					fc.renderResponse();
				} else if (t instanceof Exception) {
					log.error("Erro não tratado:" + t.getMessage(), t);
					
					StringWriter sw = new StringWriter();
					PrintWriter pw = new PrintWriter(sw);
					t.printStackTrace(pw);
					/*while (t.getCause() != null) {
						Throwable cause = t.getCause();
						pw.append("\n");
						cause.printStackTrace(pw);
					}*/
					
					fc.getExternalContext().getRequestMap().put("EXCEPTION_MESSAGE", sw.toString());
					
					fc.getApplication().getNavigationHandler().handleNavigation(fc, null, "/");
					fc.renderResponse();
				}
			} finally {
				i.remove();
			}
		}
		
		getWrapped().handle();
	}

}
