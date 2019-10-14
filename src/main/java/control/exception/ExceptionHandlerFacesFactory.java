package control.exception;
 
import javax.faces.context.ExceptionHandler;
import javax.faces.context.ExceptionHandlerFactory;
 
/**
 * Fábrica customizada para tratar exceções do JSF
 */
public class ExceptionHandlerFacesFactory extends ExceptionHandlerFactory {
	 
    private ExceptionHandlerFactory parent;
 
    public ExceptionHandlerFacesFactory(ExceptionHandlerFactory parent) {
        this.parent = parent;
    }
 
    /**
     * Retorna instância da classe que irá manipular as exceções do JSF.
     */
    @Override
    public ExceptionHandler getExceptionHandler() {
        ExceptionHandler result = parent.getExceptionHandler();
        result = new ExceptionHandlerFaces(result);
        return result;
    }
}