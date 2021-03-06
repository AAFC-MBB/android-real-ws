package ca.gc.agr.mbb.seqdb.ws.http;

import java.util.logging.*;

import java.io.IOException;
import java.net.URI;
import javax.json.stream.JsonGenerator;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import ca.gc.agr.mbb.seqdb.ws.mockstate.MockState;



import java.net.UnknownHostException;
import java.net.InetAddress;
import java.util.Enumeration;
import java.net.NetworkInterface;



public class Main {
    private final Logger logger=Logger.getLogger(this.getClass().getPackage().getName());
    // Base URI the Grizzly HTTP server will listen on
    public static String BASE_URI = null;

    static{
	try{
	    BASE_URI = "http://" + getLocalHostLANAddress().getCanonicalHostName() + ":8080/";
	    System.err.println("BASE_URI=" + BASE_URI);
	}catch(Throwable t){
	    t.printStackTrace();
	    System.err.println("Unable to ascertain hostname");
	    System.exit(42);
	}
    }

    public static MockState mockState;

    /**
     * Starts Grizzly HTTP server exposing JAX-RS resources defined in this application.
     * @return Grizzly HTTP server.
     */
    public HttpServer startServer() {
        // create a resource config that scans for JAX-RS resources and providers
        // in com.example package
        final ResourceConfig rc = new ResourceConfig().packages("ca.gc.agr.mbb.seqdb.ws");
	rc.property(JsonGenerator.PRETTY_PRINTING, true);

	mockState = new MockState();
	logger.log(Level.INFO, "Some message");
        // create and start a new instance of grizzly http server
        // exposing the Jersey application at BASE_URI
        return GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), rc);
    }

    /**
     * Main method.
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
	Main main = new Main();
        final HttpServer server = main.startServer();
        System.out.println(String.format("Jersey app started with WADL available at "
                + "%sapplication.wadl\nHit enter to stop it...", BASE_URI));
        System.in.read();
        server.stop();
    }

    // From: http://stackoverflow.com/a/20418809/459050
    private static InetAddress getLocalHostLANAddress() throws UnknownHostException {
	try {
	    InetAddress candidateAddress = null;
	    // Iterate all NICs (network interface cards)...
	    for (Enumeration ifaces = NetworkInterface.getNetworkInterfaces(); ifaces.hasMoreElements();) {
		NetworkInterface iface = (NetworkInterface) ifaces.nextElement();
		// Iterate all IP addresses assigned to each card...
		for (Enumeration inetAddrs = iface.getInetAddresses(); inetAddrs.hasMoreElements();) {
		    InetAddress inetAddr = (InetAddress) inetAddrs.nextElement();
		    if (!inetAddr.isLoopbackAddress()) {

			if (inetAddr.isSiteLocalAddress()) {
			    // Found non-loopback site-local address. Return it immediately...
			    return inetAddr;
			}
			else if (candidateAddress == null) {
			    // Found non-loopback address, but not necessarily site-local.
			    // Store it as a candidate to be returned if site-local address is not subsequently found...
			    candidateAddress = inetAddr;
			    // Note that we don't repeatedly assign non-loopback non-site-local addresses as candidates,
			    // only the first. For subsequent iterations, candidate will be non-null.
			}
		    }
		}
	    }
	    if (candidateAddress != null) {
		// We did not find a site-local address, but we found some other non-loopback address.
		// Server might have a non-site-local address assigned to its NIC (or it might be running
		// IPv6 which deprecates the "site-local" concept).
		// Return this non-loopback candidate address...
		return candidateAddress;
	    }
	    // At this point, we did not find a non-loopback address.
	    // Fall back to returning whatever InetAddress.getLocalHost() returns...
	    InetAddress jdkSuppliedAddress = InetAddress.getLocalHost();
	    if (jdkSuppliedAddress == null) {
		throw new UnknownHostException("The JDK InetAddress.getLocalHost() method unexpectedly returned null.");
	    }
	    return jdkSuppliedAddress;
	}
	catch (Exception e) {
	    UnknownHostException unknownHostException = new UnknownHostException("Failed to determine LAN address: " + e);
	    unknownHostException.initCause(e);
	    throw unknownHostException;
	}
    }

}

