package net.omu.productions.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.time.LocalDateTime;
import java.util.HashMap;

import javafx.application.Platform;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import net.omu.productions.Nachricht;
import net.omu.productions.Util;

public class Server {
	/**
	 * Beinhaltet alle namen/adressen der User die gerade online sind
	 */
	private HashMap<String, InetSocketAddress> adressen = new HashMap<>();
	private DatagramSocket socket;
	private String name;
	private TextArea textArea;
	private boolean aktiv = true;
	private Stage primaryStage;
	private String serverTitel;
	
	public Server(TextArea textArea,Stage primaryStage) {
		this.textArea = textArea;
		this.primaryStage = primaryStage;
		new Thread(()->{
			start();
		}).start();
	}
	private void start() {
		try {
			socket = new DatagramSocket();
			textArea.appendText("Server gestartet auf: " + InetAddress.getLocalHost().getHostAddress()+ " : " + socket.getLocalPort()  +"\n");
			serverTitel = "SwagChat - Server - " + InetAddress.getLocalHost().getHostAddress()+ " : " + socket.getLocalPort()+ " - Benutzer online: ";
			titelAktualisieren();
		} catch (Exception e) {
			textArea.appendText("Exception 6241321 \n " + e.getMessage());
		}
		while(aktiv) {
		try {
			while(aktiv){
				//Nachricht nachricht = empfangenNachricht();
				
				/**
				 * Lesen der Nachricht
				 * ===============================================================================================
				 */
				DatagramPacket packet = new DatagramPacket(new byte[1024], 1024);
				socket.receive(packet);
				byte[] data = packet.getData();
				final Nachricht nachricht = (Nachricht) Util.convertFromBytes(data);
				/**
				 * ===============================================================================================
				 */
				
				switch (nachricht.getStatuscode()) {
				case 0:
					textArea.appendText("User: '"+nachricht.getSender()+"' sendet Textnachricht an Alle"+"\n");
					sendeNachrichtAnAlle(nachricht);
					break;
				case 1:
					textArea.appendText("User: '"+nachricht.getSender()+"' sendet Textnachricht an: "+nachricht.getEmpfaenger()+"\n");
					sendeNachricht(nachricht);
					break;
				case 2:
					textArea.appendText("User '"+nachricht.getSender()+"' hat sich angemeldet\n");
					if(!adressen.keySet().contains(nachricht.getSender())){
						sendeNachrichtAnAlle(new Nachricht(7, null, this.name, "User '"+nachricht.getSender()+"' is online!\n", LocalDateTime.now()));
						adressen.put(nachricht.getSender(), (InetSocketAddress) packet.getSocketAddress());
						titelAktualisieren();
						sendeNachricht(new Nachricht(7, nachricht.getSender(), this.name, "Angemeldet", LocalDateTime.now()));
						sendeNachrichtAnAlle(new Nachricht(5, nachricht.getSender(), this.name, adressen.keySet()));
					}else{
						sendeNachrichtManuell(new Nachricht(6, nachricht.getSender(), this.name, null, LocalDateTime.now()), (InetSocketAddress) packet.getSocketAddress());
						textArea.appendText("Error gesendet"+"\n");
					}
					break;
				case 3:
					textArea.appendText("User '"+nachricht.getSender()+"' meldet sich ab"+"\n");
					adressen.remove(nachricht.getSender());
					titelAktualisieren();
					sendeNachrichtAnAlle(new Nachricht(7, null, this.name, "User '"+nachricht.getSender()+"' went offline!\n", LocalDateTime.now()));
					sendeNachrichtAnAlle(new Nachricht(5, nachricht.getSender(), this.name, adressen.keySet()));
					break;
				case 4:
					textArea.appendText("User '"+nachricht.getSender()+"' möchte Liste aller User"+"\n");
					sendeNachricht(new Nachricht(5, nachricht.getSender(), this.name, adressen.keySet()));
					break;
				default:
					break;
				}
			}
		} catch (IOException | ClassNotFoundException e) {
			textArea.appendText("Exception 56123413 \n");
		}
		}
	}
	private void titelAktualisieren(){
		Platform.runLater(()->{
			primaryStage.setTitle(serverTitel + adressen.size());
		});
	}
	
	private void sendeNachrichtManuell(Nachricht n, InetSocketAddress inetSocketAddress) {
		byte[] data = null;
		try {
			data = Util.convertToBytes(n);
			DatagramPacket packet = new DatagramPacket(data, data.length, inetSocketAddress);
			socket.send(packet);
		} catch (IOException e) {
			textArea.appendText("Exception 12344151 \n");
			
		}	
	}
	
	public void sendeNachrichtAnAlle(Nachricht n){
		byte[] data = null;
		try {
			data = Util.convertToBytes(n);
		} catch (IOException e1) {
			textArea.appendText("Exception 514121 \n");
		}
		for(String user : adressen.keySet()){
			try {
				DatagramPacket packet = new DatagramPacket(data, data.length, adressen.get(user));
				socket.send(packet);
			} catch (IOException e) {
				textArea.appendText("Exception 123312 \n");
			}
		}
	}
	
	private void sendeNachricht(Nachricht n){
		if(n.getEmpfaenger() == null){
			Platform.runLater(()->{
				textArea.appendText("Empfänger nicht gesetzt."+"\n");
			});
			return;
		}
		byte[] data = null;
		try {
			data = Util.convertToBytes(n);
		} catch (IOException e1) {
			textArea.appendText("Exception 34234 \n");
		}
		try {
			if(adressen.get(n.getEmpfaenger()) != null) {
				DatagramPacket packet = new DatagramPacket(data, data.length, adressen.get(n.getEmpfaenger()));
				socket.send(packet);
			}else {
				Platform.runLater(()->{
					textArea.appendText("Exception 5123123 \n");
				});
				//Empfaenger ist offline
			}
		} catch (Exception e) {
			textArea.appendText("Exception 5123123 \n");
		}
	}
	
	public void beenden() {
		aktiv = false;
	}
}

