package org.xceptance;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.zip.GZIPInputStream;

import com.xceptance.common.lang.XltCharBuffer;
import com.xceptance.common.util.CsvLineDecoder3;
import com.xceptance.common.util.CsvUtilsDecode;
import com.xceptance.common.util.SimpleArrayList;

public class B99_FullFileTest
{
    public static void main(String[] args)
    {
        BufferedReader reader = null;
        try
        {
            long count = 0;
            if (args[0].endsWith("gz"))
            {
                reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(args[0]))));
            }
            else
            {
                reader = new BufferedReader(new InputStreamReader(new FileInputStream(args[0])));
            }

            String line;
            var result = new SimpleArrayList<XltCharBuffer>(50);

            var start = System.currentTimeMillis();
            while ((line = reader.readLine()) != null)
            {
                count++;
                parseV6(result, line);
            }
            var end = System.currentTimeMillis();
            var runtime = end - start;
            var linesPerMS = Math.round((double)count / (double)runtime);
            long linesPerS = Math.round( (double)count / ((double)runtime / 1000.0));

            System.out.printf("Read %d lines in %d ms%n", count, runtime);
            System.out.printf("Average %d lines/msec%n", linesPerMS);
            System.out.printf("Average %d lines/s%n", linesPerS);
        }
        catch (FileNotFoundException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        finally
        {
            try
            {
                reader.close();
            }
            catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public static int parseV1(SimpleArrayList result, String line)
    {
        result.clear();
        var r = CsvUtilsDecode.parse(result, XltCharBuffer.valueOf(line), ',');

        return result.size();
    }

    public static int parseV6(SimpleArrayList result, String line)
    {
        result.clear();
        var r = CsvLineDecoder3.parse(result, XltCharBuffer.valueOf(line));

        return result.size();
    }
}
