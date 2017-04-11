package com.github.ryenus.rop;


public class OptParseException extends RuntimeException
{
	public OptParseException(String msg)
	{
		super(msg);
	}

	public OptParseException(String msg, Throwable e)
	{
		super(msg, e);
	}

	public OptParseException(Throwable e)
	{
		super(e);
	}
}
