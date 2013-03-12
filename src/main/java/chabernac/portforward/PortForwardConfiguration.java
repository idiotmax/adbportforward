package chabernac.portforward;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class PortForwardConfiguration {

    public static List<PortForward> read(String aFile) throws IOException{
      List<PortForward> thePortForwards = new ArrayList<PortForward>();
      BufferedReader theReader = null;
      try{
        theReader = new BufferedReader(  new InputStreamReader( new FileInputStream( new File(aFile) ) ) );
        String theLine = null;
        while((theLine = theReader.readLine()) != null){
          String[] theParts = theLine.split( ";" );
          PortForward thePortForward = new PortForward( Integer.parseInt(theParts[0]), theParts[1], Integer.parseInt(theParts[2]) );
          thePortForwards.add(thePortForward);
        }
        return thePortForwards;
      } finally {
        if(theReader != null){
          try {
            theReader.close();
          } catch ( IOException e ) {
            e.printStackTrace();
          }
        }
      }
    }

}
