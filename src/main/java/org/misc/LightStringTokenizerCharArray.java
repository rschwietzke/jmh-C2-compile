package org.misc;

import java.util.Arrays;

/**
 * This is a replacement of the Sun Java StringTokenizer because
 * the original Sun class does not return empty tokens.
 * It is not a full replacement!
 * 
 * This is a rewrite of the rewrite from 2002 to avoid the use
 * of String methods as much as possible. Hence it is not longer
 * as universal but for the purpose of CSV tokenization or our
 * JDK8/11 demo, it is perfectly fine.
 *
 * @author  Rene Schwietzke
 * @updated 2020/03/21
 */
public class LightStringTokenizerCharArray
{
    /**
     * The source to parse.
     */
    private final char[] source;

    /**
     * The length of the source string
     */
    private final int sourceLength;

    /**
     * The delimiter of the source parts.
     */
    private final char delimiter;

    /**
     * The last position of the delimiter
     */
    private int lastPos = -1;

    /**
     * Constructor
     *
     * Creates a new Tokenizer.
     *
     * @param source the string to tokenize
     * @param delimiter the delimiter
     */
    public LightStringTokenizerCharArray(String source, char delimiter)
    {
        this.source          = source.toCharArray();
        this.delimiter       = delimiter;

        sourceLength = source.length();
    }

    private int next(final int possibleNext)
    {
        for (int i = possibleNext; i < sourceLength; i++)
        {
            if (source[i] == delimiter)
            {
                return i;
            }
        }
        
        return sourceLength;
    }
    
    /**
     * Checks if more tokens are available
     *
     * @return true if more tokens available, false if not
     */
    public boolean hasMoreTokens()
    {
        return !(lastPos + 1 >= sourceLength);
    }

    /**
     * Returns the next available token. This class does not throw an
     * exception in case of reaching the end of tokens.
     *
     * @return the next token or null if no more tokens available
     */
    public String nextToken()
    {
        if (hasMoreTokens() == false)
        {
            return null;
        }
        
        final int possibleNext = lastPos + 1;
        final int next = next(possibleNext);
        lastPos = next; // remember it
        
        // nothing anymore, return the rest
        if (next == sourceLength)
        {
            return String.copyValueOf(source, possibleNext, sourceLength - possibleNext);
        }

        return String.copyValueOf(source, possibleNext, next - possibleNext);
    }

    @Override
    public String toString()
    {
        final int maxLen = 20;
        return "LightStringTokenizerArray [source=" + (source != null ? Arrays.toString(Arrays.copyOf(source, Math.min(source.length, maxLen))) : null) + ", sourceLength="
                        + sourceLength + ", delimiter=" + delimiter + ", lastPos=" + lastPos + "]";
    }
}