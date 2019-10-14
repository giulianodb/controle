package control.exception;

import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;


@Interceptor
@ExceptionHandler
public class InterceptionExceptionHandler {
 
	@AroundInvoke
	public Object protege(InvocationContext context) throws Exception
	{
	//	System.out.println("ContaInterceptor.protege(antes) >> " + context.getMethod().getName());
 
		Object object = context.proceed();
 
	//	System.out.println("ContaInterceptor.protege(depois) >> " + context.getMethod().getName());
 
		return object;
	}
}