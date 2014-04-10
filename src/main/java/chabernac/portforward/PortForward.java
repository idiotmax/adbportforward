package chabernac.portforward;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;

import org.apache.log4j.Logger;

public class PortForward {
  private static Logger LOGGER = Logger.getLogger(PortForward.class);

  private final int myLocalPort;
  private final int myDestinationPort;
  private final String myDestinationHost;
  private final Object LOCK = new Object();

  private ServerSocket myServerSocket = null;

  public PortForward( int aLocalPort, String aDestinationHost, int aDestinationPort ) {
    super();
    myLocalPort = aLocalPort;
    myDestinationPort = aDestinationPort;
    myDestinationHost = aDestinationHost;
  }

  public synchronized boolean start(ExecutorService anExecutorService) {
    if(!isStarted()){
      anExecutorService.execute( new ServerSocketHandler(anExecutorService) );
      synchronized ( LOCK ) {
        try {
          LOCK.wait( 5000 );
        } catch ( InterruptedException e ) {
          e.printStackTrace();
        }
      }
    }
    return isStarted();
  }

  public synchronized void stop(){
    if(isStarted()){
      try {
        myServerSocket.close();
      } catch ( IOException e ) {
        LOGGER.error( "Error occured while stopping portforward", e );
      } finally {
        myServerSocket = null;
      }
    }
  }
  
  public synchronized boolean isStarted(){
    return myServerSocket != null;
  }

  private class ServerSocketHandler implements Runnable{
    private final ExecutorService myExecutorService;
    
    public ServerSocketHandler( ExecutorService aExecutorService ) {
      super();
      myExecutorService = aExecutorService;
    }

    public void run(){
      try{
        myServerSocket = new ServerSocket( myLocalPort );
        
        synchronized(LOCK){
          LOCK.notifyAll();
        }

        while(true){
          Socket theSocket = myServerSocket.accept();
          LOGGER.debug("Socket accepted " + PortForward.this.toString());
          myExecutorService.execute( new SocketHandler( theSocket, myExecutorService ) );
        }
      }catch(Exception e){
        LOGGER.error("could not start portfward " + toString(), e);
      }
    }
  }

  private class SocketHandler implements Runnable{
    private final Socket mySocket;
    private final ExecutorService myExecutorService;
    
    public SocketHandler(Socket aSocket, ExecutorService anExecutorService){
      mySocket = aSocket;
      myExecutorService = anExecutorService;
    }

    public void run() {
      //create socket to remote host
      Socket theRemoteHost = null;
      try{
        theRemoteHost = new Socket(myDestinationHost, myDestinationPort);
        ArrayBlockingQueue<Boolean> theQueue = new ArrayBlockingQueue<Boolean>( 1 );
        myExecutorService.execute( new StreamCopier( mySocket.getInputStream(), theRemoteHost.getOutputStream(), theQueue ) );
        myExecutorService.execute( new StreamCopier( theRemoteHost.getInputStream(), mySocket.getOutputStream(), theQueue ) );
        theQueue.take();
      }catch(Throwable e){
        LOGGER.error( "An error occured in setting up connection to " + myDestinationHost + ":" + myDestinationPort, e );
      }

      try{
        mySocket.close();
      }catch(Throwable e){
        e.printStackTrace();        
      }
      try{
        if(theRemoteHost != null){
          theRemoteHost.close();
        }
      }catch(Exception e){
        e.printStackTrace();
      }
    }

  }

  public String toString(){
    return myLocalPort + "-->" + myDestinationHost + ":" + myDestinationPort;
  }

  private class StreamCopier implements Runnable{
    private final InputStream myInputStream;
    private final OutputStream myOutputStream;
    private final ArrayBlockingQueue<Boolean> myQueue;

    public StreamCopier(InputStream anInputStream, OutputStream anOutputStream, ArrayBlockingQueue<Boolean> aQueue){
      myInputStream = anInputStream;
      myOutputStream = anOutputStream;
      myQueue = aQueue;
    }

    public void run() {
      byte[] theBuffer = new byte[1024];
      try{
        while(true){
//          LOGGER.debug( "Stream copier started for " + PortForward.this.toString() );
          int theBytesRead = myInputStream.read( theBuffer );
//          LOGGER.debug( "Bytes read: " + theBytesRead );
          if(theBytesRead == -1) break;
          myOutputStream.write(theBuffer, 0, theBytesRead);
          myOutputStream.flush();
        }
      }catch(Throwable e){
//        LOGGER.error( "Error occured while copying stream " + PortForward.this.toString(), e );
      }
      try {
        myQueue.put( Boolean.TRUE );
      } catch ( InterruptedException e ) {
        e.printStackTrace();
      }
    }

  }
}
