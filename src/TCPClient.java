import java.io.*;
import java.util.*;
import java.net.*;

public class TCPClient {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		Socket s = new Socket("localhost", 9000);


		InputStream is = s.getInputStream();
		BufferedReader br = new BufferedReader(new InputStreamReader(is));

		OutputStream os = s.getOutputStream();
		DataOutputStream output = new DataOutputStream(os);

		output.writeBytes("Hello world!" + "\n");
		String inputString = br.readLine();

		System.out.println(inputString);

		output.writeBytes("\n");

		s.close();
	}
}
