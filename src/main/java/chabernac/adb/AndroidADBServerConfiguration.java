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
    private static Logger            LOGGER                     = Logger.getLogger( AndroidADBServerConfiguration.class );
    private ScheduledExecutorService myScheduledExecutorService = null;
    private PortForward              myPortForward              = null;
    private final int                serverPort;

    private final AndroidUtils       myAndroidUtils;

    public AndroidADBServerConfiguration( AndroidUtils aAndroidUtils, int aServerPort ) {
        super();
        myAndroidUtils = aAndroidUtils;
        serverPort = aServerPort;
    }

    public synchronized boolean start() {
        if ( myPortForward == null ) {
            myScheduledExecutorService = Executors.newScheduledThreadPool( 1 );
            myScheduledExecutorService.scheduleAtFixedRate( new Runnable() {
                public void run() {
                    try {
                        myAndroidUtils.startServer();
                    } catch ( IOException e ) {
                        LOGGER.error( "Unable to start adb server", e );
                    }
                }
            }, 0, 30, TimeUnit.SECONDS );

            myPortForward = new PortForward( serverPort, "127.0.0.1", 5037 );
            myPortForward.start( Executors.newCachedThreadPool() );
        }
        return myPortForward.isStarted();
    }

    public synchronized boolean isStarted() {
        if ( myPortForward == null ) return false;
        return myPortForward.isStarted();
    }

    public synchronized void stop() {
        if ( isStarted() ) {
            if ( myScheduledExecutorService == null ) {
                myScheduledExecutorService.shutdownNow();
                myScheduledExecutorService = null;
            }
            if ( myPortForward != null ) {
                myPortForward.stop();
            }
        }
    }

	private static String getAdbLocation() {
		return System.getenv("ANDROID_HOME");
	}

    public static void main( String args[] ) throws IOException {
        BasicConfigurator.configure();
		ArgsInterPreter theInterPreter = new ArgsInterPreter(args);
		String adbLocation = null;
		if (theInterPreter.containsKey("adblocation")) {
			adbLocation = theInterPreter.getKeyValue("adblocation");
		}
		if (adbLocation == null || adbLocation.length() <= 0) {
			adbLocation = getAdbLocation();
		}
		if (adbLocation == null || adbLocation.length() <= 0) {
			System.out.println("You must provide the location of adb width adblocation=[path to adb]");
			System.exit(-1);
		}

        String theADBLocation = adbLocation;
        int thePort = Integer.parseInt( theInterPreter.getKeyValue( "port", "6037" ) );

        final AndroidADBServerConfiguration theConfig = new AndroidADBServerConfiguration(
                new AndroidUtils( theADBLocation ),
                thePort );

        Runtime.getRuntime().addShutdownHook( new Thread() {
            @Override
            public void run() {
                theConfig.stop();
            }
        } );

        boolean isStarted = theConfig.start();
        System.out.println( "Android ADB server started: " + isStarted );
        System.in.read();
    }
}
