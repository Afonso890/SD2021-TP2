package tp1.impl.srv.soap;


import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;

import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsServer;

import jakarta.xml.ws.Endpoint;
import tp1.impl.discovery.Discovery;
import tp1.impl.srv.Domain;
import tp1.impl.utils.IP;
import tp1.impl.utils.InsecureHostnameVerifier;


public class SpreadsheetsSoapServer {

	public static final int PORT = 14567;
	public static final String SERVICE_NAME = "sheets";
	public static String SERVER_BASE_URI = "https://%s:%s/soap";
	public static final String SOAP_SHEETS_PATH = "/soap/sheets";

	private static Logger Log = Logger.getLogger(SpreadsheetsSoapServer.class.getName());

	public static void main(String[] args) throws Exception {
		Domain.set(args.length > 0 ? args[0] : "?");

		Log.setLevel(Level.FINER);

		String ip = IP.hostAddress();
		String serverURI = String.format(SERVER_BASE_URI, ip, PORT);

		//This allows client code executed by this server to ignore hostname verification
		HttpsURLConnection.setDefaultHostnameVerifier(new InsecureHostnameVerifier());
		
		//Create an https configurator to define the SSL/TLS context
		HttpsConfigurator configurator = new HttpsConfigurator(SSLContext.getDefault());
		
		HttpsServer server = HttpsServer.create(new InetSocketAddress(ip, PORT), 0);
		
		server.setHttpsConfigurator(configurator);
		
		server.setExecutor(Executors.newCachedThreadPool());
		

		Discovery.getInstance().announce(String.format("%s:%s",Domain.get(), SERVICE_NAME), serverURI);

		Endpoint soapSheetsEndpoint = Endpoint.create(new SoapSpreadsheetsWebService());
		
		soapSheetsEndpoint.publish(server.createContext(SOAP_SHEETS_PATH));
		
		server.start();

		Log.info(String.format("%s Soap Server ready @ %s\n", SERVICE_NAME, serverURI));

	}
}
