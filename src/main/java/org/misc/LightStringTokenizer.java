package org.misc;

/**
 * This is a replacement of the Sun Java StringTokenizer because
 * the original Sun class does not return empty tokens.
 * 
 * It is not a full replacement! This code is from 2002!
 *
 * @author  Rene Schwietzke
 * @version 1.2.0.0.12 2002/07/07
 */
public class LightStringTokenizer
{
    /**
     * The source to parse.
     */
    private String source;

    /**
     * The length of the source string
     */
    private int sourceLength;

    /**
     * The delimiter of the source parts.
     */
    private String delimiter;

    /**
     * The length of the delimiter
     */
    private int delimiterLength;

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
    public LightStringTokenizer(String source, String delimiter)
    {
        this.source          = source;
        this.delimiter       = delimiter;

        if (source != null)
        {
            sourceLength = source.length();
        }
        else
        {
            sourceLength = 0;
        }

        if (delimiter != null)
        {
            this.delimiterLength = delimiter.length();
        }
        else
        {
            this.delimiterLength = 0;
        }
    }

    /**
     * Reset the tokenizer. Use a new string and a new delimiter.
     * Function was introduced to avoid expensive object creation.
     *
     * @param source the new source string
     * @param delimiter the new delimiter
     */
    public void reset(String source, String delimiter)
    {
        this.source          = source;
        this.delimiter       = delimiter;

        if (source != null)
        {
            sourceLength = source.length();
        }
        else
        {
            sourceLength = 0;
        }

        if (delimiter != null)
        {
            this.delimiterLength = delimiter.length();
        }
        else
        {
            this.delimiterLength = 0;
        }

        lastPos = -1;
    }

    /**
     * Reset the tokenizer. Use same string and same delimiter.
     */
    public void reset()
    {
        lastPos = -1;
    }

    /**
     * Get the delimiter
     *
     * @return the used delimiter
     */
    public String getDelimiter()
    {
        return delimiter;
    }

    /**
     * Get the source
     *
     * @return the used source
     */
    public String getSource()
    {
        return source;
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

        if (delimiterLength == 0)
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
            nextPos = lastPos + delimiterLength;
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

        // no delimiter return full string
        if (delimiterLength == 0)
        {
            // set source null because we only can return it once
            String temp = source;
            source = null;
            return temp;
        }

        // ok, determine the next position
        // just search the next delimiter
        int nextPos = -1;
        if (lastPos != -1)
        {
            nextPos = lastPos + delimiterLength;
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