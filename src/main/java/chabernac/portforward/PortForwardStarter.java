package chabernac.portforward;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.BasicConfigurator;

public class PortForwardStarter {

  /**
   * @param args
   * @throws IOException 
   */
  public static void main( String[] args ) throws IOException {
    System.out.println("Starting portforward");
    BasicConfigurator.configure();
    
    final List<PortForward> thePortForwards = PortForwardConfiguration.read( args[0] );
    System.out.println("porforwards read " + thePortForwards.size());
    Runtime.getRuntime().addShutdownHook( new Thread(){
      public void run(){
       for(PortForward theForward : thePortForwards){
         theForward.stop();
       }
      }
    });
    
    ExecutorService theExecutorService = Executors.newCachedThreadPool();
    
    for(PortForward theForward : thePortForwards){
      boolean isStarted = theForward.start( theExecutorService );
      System.out.println(theForward.toString() +  " " + isStarted);
    }
    
    System.in.read();
  }
}
