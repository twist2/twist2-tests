package common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

class StreamGobbler extends Thread
{
    InputStream is;
    StringBuilder output;

    StreamGobbler(InputStream is)
    {
        this.is = is;
        output = new StringBuilder();
    }

    public void run()
    {
        try
        {
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line;
            while ( (line = br.readLine()) != null)
                output.append(line + System.getProperty("line.separator"));
        } catch (IOException ioe)
        {
            ioe.printStackTrace();
        }
    }

    public String getOutput()
    {
        return output.toString();
    }
}