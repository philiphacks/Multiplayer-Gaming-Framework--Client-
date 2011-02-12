package be.tbs.mgf.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.SoapFault;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.kxml2.kdom.Node;
import org.xmlpull.v1.XmlPullParserException;

import be.tbs.mgf.exceptions.JoinException;
import be.tbs.mgf.exceptions.LeaveException;
import be.tbs.mgf.exceptions.StatusException;
import be.tbs.mgf.xml.XMLGregorianCalendar;

/**
 * Client class to execute various operations (Join, Leave, Status)
 * @author philip
 *
 */
public class Client {

	private static String ENDPOINT = "http://mgftanks.appspot.com/cloudserve/GamingService.soap";
	private static String NAMESPACE = "http://www.tbs.be/mgf/schemas";
	private static String JOIN_ACTION = "http://mgftanks.appspot.com/Join";
	private static String LEAVE_ACTION = "http://mgftanks.appspot.com/Leave";
	private static String STATUS_ACTION = "http://mgftanks.appspot.com/Status";
	
	/**
	 * Allows implementing games to change the default host address.
	 * @param endpoint - Endpoint URL
	 * @param joinAction - Join Action URL
	 * @param leaveAction - Leave Action URL
	 * @param statusaction - Status Action URL
	 */
	public static void setEndpoints(String endpoint, String joinAction, String leaveAction, String statusaction) {
		ENDPOINT = endpoint;
		JOIN_ACTION = joinAction;
		LEAVE_ACTION = leaveAction;
		STATUS_ACTION = statusaction;
	}
	
	/**
	 * Represents a join operation
	 * @param name - the player that wants to join the server
	 * @return the player id of the newly joined player, -1 if join failed
	 * @throws JoinException
	 */
	public static int join(String name) throws JoinException {
		SoapObject soapObject = makeRequest("JoinRequest", 1);
		
		SoapObject player = new SoapObject(NAMESPACE, "");
		PropertyInfo pi = newProperty(NAMESPACE, "PlayerName", name, String.class);
		player.addProperty(pi);
		soapObject.addProperty("n0:Player", player);
		
		SoapSerializationEnvelope envelope = makeEnvelope(soapObject);
		SoapObject result = call(envelope, JOIN_ACTION, ENDPOINT);
		
		SoapObject playerObj = null;
		try {
			playerObj = (SoapObject) result.getProperty("Player");
			if (playerObj != null)
				return Integer.valueOf((playerObj.getProperty(0).toString()));
		} catch (RuntimeException e) {
			try {
				playerObj = (SoapObject) result.getProperty("Error");
				if (playerObj != null) {
					SoapPrimitive code = (SoapPrimitive) playerObj.getProperty("Code");
					SoapPrimitive desc = (SoapPrimitive) playerObj.getProperty("Description");
					throw new JoinException("Error " + code.toString() + ": " + desc.toString());
				}
			} catch (RuntimeException e1) {	
				//throw new JoinException(result.toString());
				throw new JoinException("Error 100: Property was not found.");
			}
		}
		return -1;
	}
	
	/**
	 * Represents a leave operation
	 * @param playerID - the player that wants to leave the server
	 * @return whether the leave operation was successful
	 * @throws LeaveException 
	 */
	public static boolean leave(int playerID) throws LeaveException {
		SoapObject soapObject = makeRequest("LeaveRequest", 1);
		
		SoapObject player = new SoapObject(NAMESPACE, "");
		PropertyInfo pi = newProperty(NAMESPACE, "PlayerID", playerID, Integer.class);
		player.addProperty(pi);
		soapObject.addProperty("n0:Player", player);
		
		SoapSerializationEnvelope envelope = makeEnvelope(soapObject);
		SoapObject result = call(envelope, LEAVE_ACTION, ENDPOINT);
		
		SoapPrimitive playerObj = null;
		try {
			playerObj = (SoapPrimitive) result.getProperty("PlayerID");
			if (playerObj != null) {
				if (Integer.valueOf((playerObj.toString())) == playerID)
					return true;
			}
		} catch (RuntimeException e) { 
			try {
				SoapObject errorObj = (SoapObject) result.getProperty("Error");
				if (errorObj != null) {
					SoapPrimitive code = (SoapPrimitive) errorObj.getProperty("Code");
					SoapPrimitive desc = (SoapPrimitive) errorObj.getProperty("Description");
					throw new LeaveException("Error " + code.toString() + ": " + desc.toString());
				}
			} catch (RuntimeException e1) {	
				throw new LeaveException("Error 100: Property was not found.");
			}
		}
		return false;
	}
	
	/**
	 * Represents a status operation
	 * @throws StatusException 
	 */
	public static List<String> status(int playerID, String name, String value) throws StatusException {
		List<String> statusList = new ArrayList<String>();
		SoapObject soapObject = makeRequest("StatusRequest", 1);	
		
		SoapObject player = new SoapObject(NAMESPACE, "");
		PropertyInfo pi = newProperty(NAMESPACE, "PlayerID", playerID, Integer.class);
		player.addProperty(pi);
		soapObject.addProperty("n0:Player", player);
		
		//if (name != "" && value != "") {
			SoapObject property = new SoapObject(NAMESPACE, "Property");
	    	property.addProperty(newProperty(NAMESPACE, "Name", name, String.class));
	    	property.addProperty(newProperty(NAMESPACE, "Value", value, String.class));
	    	property.addProperty(newProperty(NAMESPACE, "Player", playerID, Integer.class));
			soapObject.addProperty("n0:Property", property);			
		//}
		
		SoapSerializationEnvelope envelope = makeEnvelope(soapObject);
		SoapObject result = call(envelope, STATUS_ACTION, ENDPOINT);
		
		try {
			for (int i = 0; i < result.getPropertyCount(); i++) {
				Object o = result.getProperty(i);
				if (o != null) {
					System.out.println("Client received response with a property of type: " + o.getClass().toString());
					if (o instanceof Vector<?>) {
						Vector<Object> vo = (Vector<Object>) o;
						for (Object object : vo) {
							statusList.add(o.toString());
						}
					} else if (o instanceof SoapObject) {
						SoapObject so = (SoapObject) o;
						statusList.add(so.toString());
					} else
						statusList.add(o.toString());
				}
			}
		} catch (RuntimeException e) {
			throw new StatusException(e.getMessage());
		}
		return statusList;
	}
	
	/**
	 * Constructs a request SoapObject with given operation and framework version 
	 * @param operation - the operation to be performed
	 * @param version_number - the framework version number
	 * @return the constructed soapobject request message
	 */
	private static SoapObject makeRequest(String operation, int version_number) {
		SoapObject soapObject = new SoapObject(NAMESPACE, operation);
    	
    	PropertyInfo version = newProperty(NAMESPACE, "Version", version_number, Integer.class);
    	soapObject.addProperty(version);
    	
    	SoapObject ping = new SoapObject(NAMESPACE, "Ping");
    	String time = new XMLGregorianCalendar().toXML();
    	System.out.println("Time: " + time);
    	PropertyInfo timestamp = newProperty(NAMESPACE, "TimeStamp", time, String.class);
    	ping.addProperty(timestamp);
		soapObject.addProperty("n0:Ping", ping);
		
		return soapObject;
	}
	
	/**
	 * Constructs a soap envelope with a given body
	 * @param soapObject - the soapObject to be encapsulated
	 * @return the soap serialization envelope
	 */
	private static SoapSerializationEnvelope makeEnvelope(SoapObject soapObject) {
		SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
		envelope.implicitTypes = true;
		envelope.setAddAdornments(false);
		envelope.setOutputSoapObject(soapObject);
		
		return envelope;
	}
	
	/**
	 * Makes a call to the endpoint and returns the result in a soapobject
	 * @param soapObject - the soap message to be encapsulated in an envelope 
	 * @param action - the action to be performed on the endpoint
	 * @return the result of the call to the endpoint
	 */
	private static SoapObject call(SoapSerializationEnvelope envelope, String action, String my_endpoint) {
		SoapObject result = null;
		try {
			HttpTransportSE httpTransport = new HttpTransportSE(my_endpoint);
			httpTransport.call(action, envelope);
			result = (SoapObject) envelope.bodyIn;
		} catch (IOException e) {
			e.printStackTrace();
			if (e instanceof SoapFault) {
				SoapFault sf = (SoapFault)e;
				String faultString = "SoapFault during call(): Code: " + sf.faultcode + "\nString: " + sf.faultstring;
				System.err.println(faultString);
			}
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		} catch (ClassCastException e) {
			System.err.println("ClassCastException while receiving call result:" + envelope.bodyIn.getClass().getName());
			if (envelope.bodyIn instanceof SoapFault) {
				SoapFault sf = (SoapFault) envelope.bodyIn;
				System.err.println("Caused by SOAPFault, printing info:");
				String faultString = "SoapFault during call(): Code: " + sf.faultcode + "\nString: " + sf.faultstring;		
				System.err.println(faultString);
				Node n = sf.detail;
				for (int i = 0; i < n.getChildCount(); i++) {
					System.err.println(n.getText(i));
				}
			} else {
				e.printStackTrace();
			}
		}
		return result;
	}
	
	/**
	 * Generates a new PropertyInfo SOAP Object
	 * @param namespace - the namespace the property belongs to
	 * @param name - the name of the property
	 * @param value - the value of the property
	 * @param type - the type of the property
	 * @return the newly constructed property
	 */
	private static PropertyInfo newProperty(String namespace, String name, Object value, Object type) {
		PropertyInfo pi = new PropertyInfo();
		pi.namespace = namespace;
		pi.setName(name);
		pi.setValue(value);
		pi.setType(type);
		return pi;
	}
	
}
