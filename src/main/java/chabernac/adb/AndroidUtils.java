package chabernac.adb;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.log4j.Logger;

public class AndroidUtils {
  private static Logger LOGGER = Logger.getLogger( AndroidUtils.class );
  private final String myADBLocation;
  
  public AndroidUtils( String aADBLocation ) {
    super();
    myADBLocation = cleanADBLocation( aADBLocation );
  }
  
  private static String cleanADBLocation(String anADBLocation){
    anADBLocation = anADBLocation.replaceAll( "\\\\", "/" );
    if(!anADBLocation.endsWith( "/" )){
      anADBLocation += "/";
    }
    if(anADBLocation.endsWith( "platform-tools/" )) return anADBLocation;
    if(anADBLocation.endsWith( "android-sdk/" )) return anADBLocation + "platform-tools/";
    if(anADBLocation.endsWith( "android/" )) return anADBLocation + "android-sdk/platform-tools/";
    return anADBLocation;
  }

  public String listDevices() throws IOException{
   return executeAdbCommand( "devices" );
  }
  
  public String killServer() throws IOException{
    return executeAdbCommand( "kill-server" );
  }
  
  public String startServer() throws IOException{
    return executeAdbCommand( "start-server" );
  }
  
  private String executeAdbCommand(String aCommand) throws IOException{
    String theCMD  = myADBLocation  + "adb " + aCommand;
    LOGGER.debug( "Executing '" + theCMD + "'" );
    Process theProcess = Runtime.getRuntime().exec(  theCMD );
    BufferedReader theReader = new BufferedReader( new InputStreamReader( theProcess.getInputStream() ) );
    StringBuilder theBuilder = new StringBuilder();
    String theLine = null;
    while((theLine = theReader.readLine()) != null){
      theBuilder.append(theLine);
    }
    return theBuilder.toString();
  }

}
