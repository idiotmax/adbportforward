package chabernac.adb;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import chabernac.portforward.PortForward;
import chabernac.utils.ArgsInterPreter;

public class AndroidADBServerConfiguration {
  private static Logger LOGGER = Logger.getLogger(AndroidADBServerConfiguration.class);
  private ScheduledExecutorService myScheduledExecutorService = null;
  private PortForward myPortForward = null;


  private final AndroidUtils myAndroidUtils;

  public AndroidADBServerConfiguration( AndroidUtils aAndroidUtils ) {
    super();
    myAndroidUtils = aAndroidUtils;
  }


  public synchronized boolean start(){
    if(myPortForward == null){
      myScheduledExecutorService =  Executors.newScheduledThreadPool( 1 );
      myScheduledExecutorService.scheduleAtFixedRate( new Runnable(){
        public void run(){
          try {
            myAndroidUtils.startServer();
          } catch ( IOException e ) {
            LOGGER.error( "Unable to start adb server", e );
          }
        }
      }, 0 , 1 , TimeUnit.MINUTES);


      myPortForward = new PortForward( 6037, "127.0.0.1", 5037 );
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

    String theADBLocation = theInterPreter.getKeyValue( "adblocation" );

    final AndroidADBServerConfiguration theConfig = new AndroidADBServerConfiguration( new AndroidUtils( theADBLocation ) );
    
    Runtime.getRuntime().addShutdownHook( new Thread(){
      public void run(){
       theConfig.stop(); 
      }
    });
    
    boolean isStarted = theConfig.start();
    System.out.println("Android ADB server started: " + isStarted);
    System.in.read();
  }
}
