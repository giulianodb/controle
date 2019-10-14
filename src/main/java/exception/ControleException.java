package exception;

import javax.ejb.ApplicationException;

@ApplicationException(rollback=true)
public class ControleException extends RuntimeException {
	private static final long serialVersionUID = 3432378603367557125L;
	
	public ControleException() {}
	public ControleException(String msg) {
		super(msg);
	}
	public ControleException(Throwable cause) {
		super(cause);
	}
	public ControleException(String msg, Throwable cause) {
		super(msg, cause);
	}
	
}
