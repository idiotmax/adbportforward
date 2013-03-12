package chabernac.adb;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import chabernac.portforward.PortForward;
import chabernac.utils.ArgsInterPreter;

public class AndroidADBClientConfiguration {
  private static Logger LOGGER = Logger.getLogger(AndroidADBClientConfiguration.class);
  private final AndroidUtils myAndroidUtils ;
  private PortForward myPortForward = null;
  private ScheduledExecutorService myScheduledExecutorService = null;
  private final String myRemoteHost; 

  public AndroidADBClientConfiguration( AndroidUtils aAndroidUtils, String aRemoteHost ) {
    super();
    myAndroidUtils = aAndroidUtils;
    myRemoteHost = aRemoteHost;
  }

  public synchronized boolean start(){
    if(myPortForward == null){
      try {
        myAndroidUtils.killServer();
      } catch ( IOException e ) {
        LOGGER.error("Unable to kill local adb server", e);
      }
      myPortForward = new PortForward( 5037, myRemoteHost, 6037 );
      myPortForward.start( Executors.newCachedThreadPool() );
    }
    return myPortForward.isStarted();
  }

  public synchronized boolean isStarted(){
    if(myPortForward == null) return false;
    return myPortForward.isStarted();
  }

  public synchronized void stop(){
    if(isStarted()){
      if(myScheduledExecutorService == null){
        myScheduledExecutorService.shutdownNow();
        myScheduledExecutorService = null;
      }
      if(myPortForward != null){
        myPortForward.stop();
      }
    }
  }

  public static void main(String args[]) throws IOException{
    BasicConfigurator.configure();
    ArgsInterPreter theInterPreter = new ArgsInterPreter( args );
    if(!theInterPreter.containsKey( "adblocation" )){
      System.out.println("You must provide the location of adb width adblocation=[path to adb]");
      System.exit( -1 );
    }
    if(!theInterPreter.containsKey( "remotehost" )){
      System.out.println("You must provide the remote host with remotehost=[remote host ip or dns name]");
      System.exit( -1 );
    }

    String theADBLocation = theInterPreter.getKeyValue( "adblocation" );
    String theRemoteHost = theInterPreter.getKeyValue( "remotehost" );

    final AndroidADBClientConfiguration theConfig = new AndroidADBClientConfiguration( new AndroidUtils( theADBLocation ), theRemoteHost );
    
    Runtime.getRuntime().addShutdownHook( new Thread(){
      public void run(){
        theConfig.stop();
      }
    });
    
    boolean isStarted = theConfig.start();
    System.out.println("Android ADB client started: " + isStarted);
    System.in.read();
  }

}
