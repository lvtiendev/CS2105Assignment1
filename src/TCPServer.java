import java.io.*;
import java.util.*;
import java.net.*;

public class TCPServer {

	// public static String folderPath = "/Users/victor/a1";
	public static String folderPath = "/Users/leviettien/a1";
	// public static String folderPath = "/home/a008/a0088447/a1";

	public static final int socketNumber = 2102;

	public static ServerSocket serverSocket;
	public static Socket clientSocket;
	public static InputStream is;
	public static BufferedReader isbr;
	public static OutputStream os;
	public static DataOutputStream output;


	public static void handleStaticFile(String fileName) {
		try {
			String filePath = folderPath + fileName;
			File f = new File(filePath);
			int size = (int) f.length();
			byte buffer[] = new byte[size];
			FileInputStream fis = new FileInputStream(filePath);
			fis.read(buffer);

			output.writeBytes("HTTP/1.0 200 OK\r\n");
			if (fileName.endsWith("html")) {
				output.writeBytes("Content-Type: text/html\r\n");
			} else if (fileName.endsWith("jpg")) {
				output.writeBytes("Content-Type: image/jpeg\r\n");
			} else if (fileName.endsWith("gif")) {
				output.writeBytes("Content-Type: image/gif\r\n");
			} else if (fileName.endsWith("css")) {
				output.writeBytes("Content-Type: text/css\r\n");
			}

			output.writeBytes("\r\n");
			output.write(buffer, 0, size);
			output.writeBytes("\r\n");

			fis.close();
		} catch (IOException e) {
			System.out.println("Error: " + e.getMessage());
		}
	}
	
	public static String getFileNameFromString(String string) {
		// Remove query strings for checking
		String fileName = string;
		int queryStringStartIndex = string.indexOf("?");
		if (queryStringStartIndex > 0) {
			fileName = string.substring(0, queryStringStartIndex);
		}
		return fileName;
	}

	
	public static String getQueryString(String string) {
		String queryString = "";
		int queryStringStartIndex = string.indexOf("?");
		if (queryStringStartIndex > 0) {
			queryString = string.substring(queryStringStartIndex + 1);
		}
		return queryString;
	}

	public static void handleGETRequest(String firstLine) throws Exception {
		String fields[] = firstLine.split(" ");
		String fileName = getFileNameFromString(fields[1]);
		String filePath = folderPath + fileName;
		System.out.println(fileName);
		
		String queryString = "";
		if (getQueryString(fields[1]) != "") {
			queryString = "QUERY_STRING=" + getQueryString(fields[1]);
		}
		String requestMethod = "REQUEST_METHOD=GET";
		
		// Read the rest of header
		String inputString = firstLine;
		while (inputString.compareTo("") != 0) {
			inputString = isbr.readLine();
		}

		File f = new File(filePath);
		if (f.canRead()) {
			if (fileName.endsWith("pl")) {
				String env = requestMethod + " " + queryString;
				Process p = Runtime.getRuntime().exec(
						"/usr/bin/env " + env + " /usr/bin/perl " + filePath);
				try {
					output.writeBytes("HTTP/1.0 200 OK\r\n");
					BufferedReader br = new BufferedReader(
							new InputStreamReader(p.getInputStream()));
					String line;
					while ((line = br.readLine()) != null) {
						output.writeBytes(line + "\r\n");
					}
					output.writeBytes("\r\n");
				} catch (Exception e) {
					System.out.println("Command failed!");
				}
			} else {
				handleStaticFile(fileName);
			}
		} else {
			output.writeBytes("HTTP/1.0 404 Not Found\r\n");
			output.writeBytes("\r\n");
		}
	}

	public static void handlePOSTRequest(String firstLine) throws Exception {
		String fields[] = firstLine.split(" ");
		String fileName = fields[1];
		String filePath = folderPath + fileName;
		
		String contentType = "";

		int contentLength = -1;
		while (true) {
			final String line = isbr.readLine();
			final String contentLengthStr = "Content-Length: ";
			if (line.startsWith(contentLengthStr)) {
				contentLength = Integer.parseInt(line
						.substring(contentLengthStr.length()));
			} else if (line.startsWith("Content-Type:")) {
				String contentLengthFields[] = line.split(" ");
				contentType = "CONTENT_TYPE=" + contentLengthFields[1];
			}

			if (line.length() == 0) {
				break;
			}
		}

		// We should actually use InputStream here, but let's assume bytes map
		// to characters
		final char[] content = new char[contentLength];
		isbr.read(content);
		String contentString = new String(content);

		String requestMethod = "REQUEST_METHOD=POST";
		String contentLengthString = "CONTENT_LENGTH="
				+ contentLength;
		String env = requestMethod + " " + contentType
				+ " " + contentLengthString;

		System.out.println(env);
		Process p = Runtime.getRuntime().exec(
				"/usr/bin/env " + env + " /usr/bin/perl " + filePath);

		DataOutputStream o = new DataOutputStream(p.getOutputStream());
		o.writeBytes(contentString + "\r\n");
		o.close();

		try {
			output.writeBytes("HTTP/1.0 200 OK\r\n");
			BufferedReader br = new BufferedReader(new InputStreamReader(
					p.getInputStream()));
			String line;
			while ((line = br.readLine()) != null) {
				output.writeBytes(line + "\r\n");
				System.out.println(line);
			}
		} catch (Exception e) {
			System.out.println("Command failed!");
		}
		output.writeBytes("\r\n");
	}

	public static String getAction(String inputString) {
		String tokens[] = inputString.split("action=");
		return tokens[tokens.length - 1];
	}

	public static String getBodyRequest() throws Exception {
		int contentLength = -1;
		while (true) {
			final String line = isbr.readLine();
			final String contentLengthStr = "Content-Length: ";
			if (line.startsWith(contentLengthStr)) {
				contentLength = Integer.parseInt(line
						.substring(contentLengthStr.length()));
			}

			if (line.length() == 0) {
				break;
			}
		}

		// We should actually use InputStream here, but let's assume bytes map
		// to characters
		final char[] content = new char[contentLength];
		isbr.read(content);
		String contentString = new String(content);
		return contentString;
	}

	public static void main(String[] args) throws Exception {
		int firstArg;
		if (args.length > 0) {
			try {
				firstArg = Integer.parseInt(args[0]);
				serverSocket = new ServerSocket(firstArg);
				while (true) {
					System.out.println("waiting for new connection");
					clientSocket = serverSocket.accept();
					System.out.println("connection accepted");

					is = clientSocket.getInputStream();
					isbr = new BufferedReader(new InputStreamReader(is));

					os = clientSocket.getOutputStream();
					output = new DataOutputStream(os);

					String inputString;
					inputString = isbr.readLine();
					String fields[] = inputString.split(" ");

					if (fields[0].equals("GET")) {
						handleGETRequest(inputString);
					} else if (fields[0].equals("POST")) {
						handlePOSTRequest(inputString);
					}

					clientSocket.close();
					System.out.println("connection closed");
				}
			} catch (NumberFormatException e) {
				System.err.println("Argument" + " must be an integer");
				System.exit(1);
			}
		}
	}

}
