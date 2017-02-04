package chabernac.adb;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import chabernac.portforward.PortForward;
import chabernac.utils.ArgsInterPreter;

public class AndroidADBClientConfiguration {
    private static Logger            LOGGER                     = Logger.getLogger( AndroidADBClientConfiguration.class );
    private final AndroidUtils       myAndroidUtils;
    private PortForward              myPortForward              = null;
    private ScheduledExecutorService myScheduledExecutorService = null;
    private final String             myRemoteHost;
    private final int                myRemotePort;

    public AndroidADBClientConfiguration( AndroidUtils aAndroidUtils, String aRemoteHost, int aRemotePort ) {
        super();
        myAndroidUtils = aAndroidUtils;
        myRemoteHost = aRemoteHost;
        myRemotePort = aRemotePort;
    }

    public synchronized boolean start() {
        if ( myPortForward == null ) {
            try {
                myAndroidUtils.killServer();
            } catch ( IOException e ) {
                LOGGER.error( "Unable to kill local adb server", e );
            }
            myPortForward = new PortForward( 5037, myRemoteHost, myRemotePort );
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
            if ( myScheduledExecutorService != null ) {
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
        if ( !theInterPreter.containsKey( "remotehost" ) ) {
            System.out.println( "You must provide the remote host with remotehost=[remote host ip or dns name]" );
            System.exit( -1 );
        }

        String theADBLocation = adbLocation;
        String theRemoteHost = theInterPreter.getKeyValue( "remotehost" );
        int theRemotePort = Integer.parseInt( theInterPreter.getKeyValue( "port", "6037" ) );

        final AndroidADBClientConfiguration theConfig = new AndroidADBClientConfiguration(
                new AndroidUtils( theADBLocation ),
                theRemoteHost,
                theRemotePort );

        Runtime.getRuntime().addShutdownHook( new Thread() {
            @Override
            public void run() {
                theConfig.stop();
            }
        } );

        boolean isStarted = theConfig.start();
        System.out.println( "Android ADB client started: " + isStarted );
        System.in.read();
    }

}
