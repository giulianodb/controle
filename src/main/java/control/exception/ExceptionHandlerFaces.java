package control.exception;
 
import java.util.Iterator;
import java.util.Map;

import javax.faces.FacesException;
import javax.faces.application.NavigationHandler;
import javax.faces.application.ViewExpiredException;
import javax.faces.context.ExceptionHandler;
import javax.faces.context.ExceptionHandlerWrapper;
import javax.faces.context.FacesContext;
import javax.faces.event.ExceptionQueuedEvent;
import javax.faces.event.ExceptionQueuedEventContext;

/**
 * Classe customizada para tratar a exceção ViewExpiredException
 * empilhada pelo JSF.
 *
 */
public class ExceptionHandlerFaces extends ExceptionHandlerWrapper {
 
	private ExceptionHandler wrapped;
	 
	public ExceptionHandlerFaces(ExceptionHandler wrapped) {
		this.wrapped = wrapped;
	}
	 
	@Override
	public ExceptionHandler getWrapped() {
		return this.wrapped;
	}
 
	/**
	 * Manipula as exceções empilhadas pelo JSF. Se a exceção empinhada for do
	 * tipo ViewExpiredException redireciona o fluxo para uma tela customizada
	 * de erro. A tela de erro pode ser alterada setando o parâmetro
	 * pageViewExpiredException no web.xml da aplicação.
	 */
	@Override
	public void handle() throws FacesException {
		for (Iterator<ExceptionQueuedEvent> i = getUnhandledExceptionQueuedEvents().iterator(); i.hasNext();) {
			ExceptionQueuedEvent event = i.next();
			ExceptionQueuedEventContext context = (ExceptionQueuedEventContext) event.getSource();
			Throwable t = context.getException();

			// Realiza tratamento customizado para exceção do tipo ViewExpiredException.
			if (t instanceof ViewExpiredException) {
				ViewExpiredException vee = (ViewExpiredException) t;
				FacesContext fc = FacesContext.getCurrentInstance();
				Map<String, Object> requestMap = fc.getExternalContext().getRequestMap();
				NavigationHandler nav = fc.getApplication().getNavigationHandler();
				try {
					// Push some useful stuff to the request scope for
					// use in the page
					requestMap.put("currentViewId", vee.getViewId());

					// Obtendo parametro do Web.xml
					String paginaErro = fc.getExternalContext().getInitParameter("pageViewExpiredException");

					if (paginaErro != null && !paginaErro.equals("")) {
						nav.handleNavigation(fc, null, paginaErro);
					} else {
						nav.handleNavigation(fc, null, "/pages/templates/error_viewExpired");
					}
					fc.renderResponse();

				} finally {
					i.remove();
				}
			}
		}
		// At this point, the queue will not contain any ViewExpiredEvents.
		// Therefore, let the parent handle them.
		getWrapped().handle();
	}
}