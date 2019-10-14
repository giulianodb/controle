package exception;

import javax.ejb.ApplicationException;

@ApplicationException(rollback=true)
public class AlmiranteException extends RuntimeException {
	private static final long serialVersionUID = 3432378603367557125L;
	
	public AlmiranteException() {}
	public AlmiranteException(String msg) {
		super(msg);
	}
	public AlmiranteException(Throwable cause) {
		super(cause);
	}
	public AlmiranteException(String msg, Throwable cause) {
		super(msg, cause);
	}
	
}
