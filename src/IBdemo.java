import com.ib.client.Contract;
import com.ib.client.EClientSocket;
import com.ib.client.EReader;
import com.ib.client.EReaderSignal;

public class IBdemo {

	private class Contracts {
		public static Contract SPY() {
			Contract contract = new Contract();
			contract.symbol("SPY");
			contract.secType("STK");
			contract.currency("USD");
			contract.exchange("SMART");
			contract.primaryExch("ARCA");
			return contract;
		}
	}

    public static void main(String[] args) throws Exception {
        EWrapperImpl wrapper = new EWrapperImpl();
        final EClientSocket m_client = wrapper.getClient();
        final EReaderSignal m_signal = wrapper.getSignal();
        // m_client.setConnectOptions("+PACEAPI");

        m_client.eConnect("127.0.0.1", 7497, 2); //Establish a connection on Local Host, to a paper account, with the client ID of 2
		final EReader reader = new EReader(m_client, m_signal); 
		
		/*
		 *
		 * Port 7496 is typically for live trading, port 7497 is typically for paper trading
		 * The last argument is your client id and how your application will be identified as far as TWS is concerned.
		 * Is it possible to have multiple connections to your TWS client at the same time from multiple applications up to a max of 32 clients.
		 * ID 0 will be able to see order submitted manually through TWS as well as it's own connnections orders
		 * It is possible to desginate a "master" client in TWS that can see every order for every client including api clients.
		 * This can be useful if you want a designated connection running for order management.  
		 * 
		 */

		//The EReader class captures messages to the API client and places them in a queue
         

        // An additional thread is created in this program design to empty the messaging queue
		new Thread(() -> {
		    while (m_client.isConnected()) {
		        m_signal.waitForSignal();
		        try {
		            reader.processMsgs();
		        } catch (Exception e) {
		            System.out.println("Exception: "+e.getMessage());
		        }
		    }
		}).start();
		// A pause to give the application time to establish the connection
		// In a production application, it would be best to wait for callbacks to confirm the connection is complete
		Thread.sleep(1000);

		m_client.reqMktData(1001, Contracts.SPY(), "", false, false, null);

		Thread.sleep(100000);
		m_client.eDisconnect();
    }
}
