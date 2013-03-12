package chabernac.portforward;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import junit.framework.TestCase;

import org.apache.log4j.BasicConfigurator;


public class PortForwardTest extends TestCase{
  
  static{
    BasicConfigurator.configure();
  }
  
  public void testPortForward() throws IOException{
    final ServerSocket theSeverSocket = new ServerSocket(8999);
    ExecutorService theService = Executors.newCachedThreadPool();
    theService.execute( new Runnable(){
      public void run(){
        try {
          Socket theSocket  = theSeverSocket.accept();
          InputStream theInputStream = theSocket.getInputStream();
          OutputStream theOutputStream = theSocket.getOutputStream();
          byte[] theBuffer = new byte[128];
          int theBytesRead = 0;
          while((theBytesRead = theInputStream.read(theBuffer)) != -1){
            theOutputStream.write(theBuffer, 0, theBytesRead);
          }
        } catch ( IOException e ) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
    });
    
    PortForward thePortForward = new PortForward( 7999, "localhost", 8999);
    assertTrue( thePortForward.start(theService) );
    
    Socket theSocket = new Socket("localhost", 7999);
    PrintWriter theWriter = new PrintWriter( new OutputStreamWriter( theSocket.getOutputStream() ) );
    BufferedReader theReadear = new BufferedReader( new InputStreamReader( theSocket.getInputStream() ) );
      
    for(int i=0;i<100;i++){
      String theString = "test string " + i;
      theWriter.println( theString );
      theWriter.flush();
      assertEquals( theString, theReadear.readLine() );
    }
  }

}
