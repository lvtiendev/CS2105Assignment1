import java.io.*;
import java.util.*;
import java.net.*;

public class TCPServer {
	
	public static String folderPath = "E:/WorkSpace/CS2105Assignment1/a1";
	
	public static void main(String[] args) throws Exception{
		ServerSocket serverSocket = new ServerSocket(9000);
		while (true) {
			System.out.println("waiting for new connection");
			Socket s = serverSocket.accept();

			InputStream is = s.getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(is));

			OutputStream os = s.getOutputStream();
			DataOutputStream output = new DataOutputStream(os);

			String inputString = br.readLine();
			String fields[] = inputString.split(" ");
			if (fields[0].equals("GET")) {
				String fileName = fields[1];
				fileName = folderPath + fileName;
				while (inputString.compareTo("") != 0 ) {
					inputString = br.readLine();
				}
				
				File f = new File(fileName);
				if (f.canRead()) {
					int size = (int)f.length();
					byte buffer[] = new byte[size];
					FileInputStream fis = new FileInputStream(fileName);
					fis.read(buffer);

					output.writeBytes("HTTP/1.0 200 OK\r\n");
					if (fileName.endsWith("html")) {
						output.writeBytes("Content-Type: text/html\r\n");
					} else if (fileName.endsWith("jpg")) {
						output.writeBytes("Content-Type: image/jpeg\r\n");
					} else if (fileName.endsWith("pl")) {
						// RUN SCRIPT
					}
						
					output.writeBytes("\r\n");
					output.write(buffer, 0, size);
				} else {
					output.writeBytes("HTTP/1.0 404 Not Found\r\n"); 
					output.writeBytes("\r\n");
				}
			} else {
				while (inputString.compareTo("") != 0 ) {
					output.writeBytes(inputString.toUpperCase() + "\n");
					inputString = br.readLine();
				}
			}

			System.out.println("connection accepted");
			s.close();
			System.out.println("connection closed");
		}
	}

}
