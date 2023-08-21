package com.xceptance.common.util;

public class CsvParserException extends RuntimeException
{
    private static final long serialVersionUID = -7622470282311666936L;
    private final String line;

    public CsvParserException(String message, String line)
    {
        super(message);
        this.line = line;
    }

    public String getIncorrectLine()
    {
        return line;
    }
}
