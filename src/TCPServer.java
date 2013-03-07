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
			String inputString;
			inputString = br.readLine();
			String fields[] = inputString.split(" ");
			
			System.out.println("hjhggftfzcx");
			if (fields[0].equals("GET")) {
				String fileName = fields[1];
				System.out.println(fileName);
				fileName = folderPath + fileName;
				while (inputString.compareTo("") != 0) {
					inputString = br.readLine();
					System.out.println("["+inputString+"]");
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
				System.out.println("adsasdaseadad");
				 int contentLength = -1;
			        while (true) {
			            final String line = br.readLine();
			            System.out.println(line);

			            final String contentLengthStr = "Content-Length: ";
			            if (line.startsWith(contentLengthStr)) {
			                contentLength = Integer.parseInt(line.substring(contentLengthStr.length()));
			            }

			            if (line.length() == 0) {
			                break;
			            }
			        }

			        // We should actually use InputStream here, but let's assume bytes map
			        // to characters
			        final char[] content = new char[contentLength];
			        br.read(content);
			        String contentString = new String(content);
			} else {
				while (inputString.length() != 0) {
					output.writeBytes(inputString.toUpperCase() + "\r\n");
					inputString = br.readLine();
				}
			}

			System.out.println("connection accepted");
			s.close();
			System.out.println("connection closed");
		}
	}

}
