package org.misc;


/**
 * This is a replacement of the Sun Java StringTokenizer because
 * the original Sun class does not return empty tokens.
 * It is not a full replacement!
 * 
 * Just a twist to the original LightStringTokenizer where the delimiter is just a char
 * and not longer a string. 
 *
 * @author  Rene Schwietzke
 * @version 2020/03/08
 */
public class LightStringTokenizerCharDelimiter
{
    /**
     * The source to parse.
     */
    private final String source;

    /**
     * The length of the source string
     */
    private final int sourceLength;

    /**
     * The delimiter of the source parts.
     */
    private final char delimiter;

    /**
     * The length of the delimiter
     */
    private static final int DELIMITERLENGTH = 1;

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
    public LightStringTokenizerCharDelimiter(final String source, final char delimiter)
    {
        this.source          = source;
        this.delimiter       = delimiter;

        sourceLength = source != null ? source.length() : 0;
    }

    /**
     * Checks if more tokens are available
     *
     * @return true if more tokens available, false if not
     */
    public boolean hasMoreTokens()
    {
        // check for errors
        if (sourceLength == 0)
        {
            return false;
        }

        // just search the next delimiter
        int nextPos = -1;
        if (lastPos == -1)
        {
            nextPos = 0;
        }
        else
        {
            nextPos = lastPos + DELIMITERLENGTH;
        }

        int pos = source.indexOf(delimiter, lastPos + 1);

        // no more delimiters
        if (pos == -1)
        {
            // do we have a rest?
            if (nextPos <= sourceLength - 1)
            {
                return true;
            }
            else
            {
                return false;
            }
        }
        else
        {
            return true;
        }
    }

    /**
     * Returns the next available token. This class does not throw an
     * exception in case of reaching the end of tokens.
     *
     * @return the next token or null if no more tokens available
     */
    public String nextToken()
    {
        // check for errors
        if (sourceLength == 0)
        {
            return null;
        }

        // ok, determine the next position
        // just search the next delimiter
        int nextPos = -1;
        if (lastPos != -1)
        {
            nextPos = lastPos + DELIMITERLENGTH;
        }

        int newPos = source.indexOf(delimiter, nextPos);

        // no more delimiters
        String token = null;
        if (newPos == -1)
        {
            if (nextPos <= sourceLength - 1)
            {
                // deliver the rest
                if (nextPos == -1)
                {
                    // there has not been any token before
                    token = source;
                }
                else
                {
                    token = source.substring(nextPos);
                }
                lastPos = sourceLength - 1;
            }
            else
            {
                return null;
                //throw new NoSuchElementException("No next token.");
            }
        }
        else
        {
            // get string
            if (nextPos == -1)
            {
                nextPos = 0;
            }

            token = source.substring(nextPos, newPos);
            lastPos = newPos;
        }

        return token;
    }
}