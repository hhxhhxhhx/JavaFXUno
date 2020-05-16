package classes;

import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;

/**
 * Launcher for the Client. Run this class to connect to a server that
 * 		has already been launched. 
 * @author Roger
 *
 */
public class UnoClientLauncher {
	
	public static void main(String[] args) {
		
		System.out.println();
		Scanner scanner = new Scanner(System.in);
		String addr, port, name;
		InetAddress ip;
		Socket socket;
		
		System.out.println();
		System.out.println("Uno " + UnoGame.VERSION);
		System.out.println();
		
		while (true) {
			try {
				System.out.print("Input ip (defaulted to 127.0.0.1): ");
				addr = scanner.nextLine();
				addr = (addr.equals("") ? "localhost" : addr);
				ip = InetAddress.getByName(addr);
				System.out.print("Input port (defaulted to 6354): ");
				port = scanner.nextLine();
				port = (port.equals("") ? "6354" : port);
				int port_num = Integer.parseInt(port);
				System.out.print("Input name: ");
				name = scanner.nextLine();
				while (name.length() < 1) {
					System.out.println("Name must be at least 1 character long!\n");
					System.out.print("Input name: ");
					name = scanner.nextLine();
				}
				socket = new Socket(ip, port_num);
				break;
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("\nInvalid IP address and/or port!");
			}
		}
		scanner.close();
		
		new UnoClient(socket, name).run();
	}
}
