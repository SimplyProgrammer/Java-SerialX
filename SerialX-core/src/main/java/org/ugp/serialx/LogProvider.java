package org.ugp.serialx;

/**
 * Log provider used for logging in SerialX.
 * You can use to to easily implement log4j or other logging utilities.
 * 
 * @author PETO
 *
 * @since 1.3.5
 */
public class LogProvider 
{
	protected boolean reThrowException;
	
	public static LogProvider instance = new LogProvider();
	
	/**
	 * @param obj | Object to log in normal mode!
	 * 
	 * @since 1.3.5
	 */
	public void logOut(Object obj)
	{
		System.out.println(obj);
	}
	
	/**
	 * @param obj | Object to log in error mode!
	 * @param ex | Exception that cause the error!
	 * 
	 * @throws RuntimeException | Of "ex" if exception re-throwing is enabled!
	 * 
	 * @since 1.3.5
	 */
	public void logErr(Object obj, Throwable ex)
	{
		if (reThrowException)
		{
			if (ex == null)
				throw new RuntimeException(obj.toString());
			throw new RuntimeException(ex);
		}
		System.err.println(obj);
	}

	/**
	 * @return If true, exception or error object will be rethrown as {@link RuntimeException} by {@link LogProvider#logErr(Object, Throwable)}!
	 * 
	 * @since 1.3.5
	 */
	public boolean isReThrowException() 
	{
		return reThrowException;
	}

	/**
	 * @param reThrowException | If true, exception or error object will be rethrown as {@link RuntimeException} by {@link LogProvider#logErr(Object, Throwable)}!
	 * 
	 * @since 1.3.5
	 */
	public void setReThrowException(boolean reThrowException) 
	{
		this.reThrowException = reThrowException;
	}
}
