import java.io.*;
import java.util.*;
import java.net.*;

public class TCPServer {
    
	/**
	 * @param args
	 */
	public static String folderPath = "/Users/victor/a1/";
    
	// public static String folderPath = "~/home/a1/";
	public static void main(String[] args) throws Exception {
		ServerSocket serverSocket = new ServerSocket(9000);
		while (true) {
			System.out.println("waiting for new connection");
			Socket s = serverSocket.accept();
            
			InputStream is = s.getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
            
			OutputStream os = s.getOutputStream();
			DataOutputStream output = new DataOutputStream(os);
            
			String inputString = br.readLine();
			System.out.println(inputString);
			String fields[] = inputString.split(" ");
            
			if (fields[0].equals("GET")) {
				String fileName = fields[1];
				System.out.println(fileName);
				fileName = folderPath + fileName;
				while (inputString.compareTo("") != 0) {
					inputString = br.readLine();
				}
                
				File f = new File(fileName);
				if (f.canRead()) {
					int size = (int) f.length();
					byte buffer[] = new byte[size];
					FileInputStream fis = new FileInputStream(fileName);
					fis.read(buffer);
                    
					output.writeBytes("HTTP/1.0 200 OK\r\n");
					if (fileName.endsWith("html")) {
						output.writeBytes("Content-Type: text/html\r\n");
						output.writeBytes("\r\n");
						output.write(buffer, 0, size);
					} else if (fileName.endsWith("jpg")) {
						output.writeBytes("Content-Type: image/jpeg\r\n");
						output.writeBytes("\r\n");
						output.write(buffer, 0, size);
					} else if (fileName.endsWith("pl")) {
						Process p = Runtime.getRuntime().exec(
                                                              "/usr/bin/perl " + folderPath + "todo.pl");
						try {
							InputStream lsOut = p.getInputStream();
							InputStreamReader r = new InputStreamReader(lsOut);
							BufferedReader in = new BufferedReader(r);
							String line;
							while ((line = in.readLine()) != null)
								output.writeBytes(line + "\r\n");
						} catch (Exception e) { // exception thrown
                            
							System.out.println("Command failed!");
                            
						}
						output.writeBytes("\r\n");
					}
                    
					// while (true) {
					System.out.println(output.size());
					// }
				} else {
					output.writeBytes("HTTP/1.0 404 Not Found\r\n");
					output.writeBytes("\r\n");
				}
			} else if (fields[0].equals("POST")) {
				
			} else {
				while (inputString.compareTo("") != 0) {
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
