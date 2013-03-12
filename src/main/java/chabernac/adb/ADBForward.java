package chabernac.adb;

import java.io.IOException;

public class ADBForward {

  public static void main( String[] args ) throws IOException {
    if(args.length == 0 || !args[0].equals( "server" ) && !args[0].equals( "client" )){ 
      System.out.println("You must indicate whether the adb forward is running as client or server [server|client]");
      System.exit(-1);
    }
    
    if(args[0].equals( "server" )){
      AndroidADBServerConfiguration.main( args );
    } else if(args[0].equals( "client" )){
      AndroidADBClientConfiguration.main( args );
    }
  }

}
