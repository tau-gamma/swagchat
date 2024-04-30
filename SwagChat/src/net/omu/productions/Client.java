package net.omu.productions;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import view.MainController;
import view.SingleChatController;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;

public class Client {
	private InetAddress serverAddress;
	private int serverPort = 4477;
	private DatagramSocket socket;
	private String name;
	private String serverIp;
	private HashMap<String,SingleChatHelperClass> singleChatFensterListe = new HashMap<>();
	private MainController mainController;
	private String[] onlineBenutzer;
	
	/**
	 * 0 wenn erfolgreich angemeldet
	 * -1 wenn username vergeben
	 * -2 wenn timeout
	 * -3 other errors
	 * @param name
	 * @param serverIp
	 * @param serverPort
	 * @return 
	 */
	public int init(String name,String serverIp, int serverPort,MainController controller) {
		this.mainController = controller;
		if(name == null || name.length()==0)
			return -1;
		this.name = name;
		this.serverPort = serverPort;
		this.serverIp = serverIp;
		Nachricht response = null;
		try {
			serverAddress = InetAddress.getByName(serverIp);
			socket = new DatagramSocket();
			socket.setSoTimeout(2000);
			sendeNachricht(new Nachricht(2, null, this.name, "", LocalDateTime.now()));
			response = empfangeNachricht();
//			pferdeSound();
		} catch (SocketTimeoutException e) {
			//socket.close();
			return -2;
		}catch (ClassNotFoundException | IOException e) {
			e.printStackTrace();
			return -3;
		}
		return response.getStatuscode() == 6 ? -1 : 0;
	}
	
	private void pferdeSound() {
		String musicFile = "Swag.m4a";
		Media sound = new Media(new File(musicFile).toURI().toString());
		MediaPlayer player = new MediaPlayer(sound);
		player.play();
	}

	public InetAddress getServerAddress() {
		return serverAddress;
	}

	public void setServerAddress(InetAddress ServerAddress) {
		this.serverAddress = ServerAddress;
	}

	public int getServerPort() {
		return serverPort;
	}

	public void setServerPort(int serverPort) {
		this.serverPort = serverPort;
	}

	public DatagramSocket getSocket() {
		return socket;
	}

	public void setSocket(DatagramSocket socket) {
		this.socket = socket;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getServerIp() {
		return serverIp;
	}

	public void setServerIp(String serverIp) {
		this.serverIp = serverIp;
	}

	private void sendeNachricht(Nachricht n){
		try {
			byte[] data = Util.convertToBytes(n);
			DatagramPacket packet = new DatagramPacket(data, data.length,serverAddress,serverPort);
			socket.send(packet);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	private Nachricht empfangeNachricht() throws IOException, ClassNotFoundException{
			DatagramPacket packet = new DatagramPacket(new byte[1024], 1024);
			socket.receive(packet);
			byte[] data = packet.getData();
			Nachricht n = (Nachricht) Util.convertFromBytes(data);
//			System.out.println(n.getEmpfaenger()+"   "+n.getSender()+"   "+n.getStatuscode()+"   "+n.getSendedatum());
			return n;
	}

	public void abmelden() {
		if(!socket.isClosed()){
			sendeNachricht(new Nachricht(3, null, this.name, null, LocalDateTime.now()));
			//socket.close();
		}
	}
	
	public void anAlleSenden(String text){
		sendeNachricht(new Nachricht(0, null, name, text, LocalDateTime.now()));
	}
	public void anBestimmtePersonSenden(String text,String empfaenger){
		sendeNachricht(new Nachricht(1, empfaenger, name, text, LocalDateTime.now()));
	}

	public void nachrichtenVerarbeiten(ListView<String> onlineListe) throws SocketException {
		socket.setSoTimeout(0);
		new Thread(() -> {
			while (true) {
				try {
					DatagramPacket packet = new DatagramPacket(new byte[1024],1024);
					socket.receive(packet);
					byte[] data = packet.getData();
					Nachricht n = (Nachricht) Util.convertFromBytes(data);
					switch (n.getStatuscode()) {
					case 0:
						Platform.runLater(()->{
							Client.this.mainController.addNachricht(n); 
						});
						break;
					case 1:// An bestimmte Person
							// Ist fenster schon offen?
						String sender = n.getSender();
						assert sender != null : "Sender ist null, 12345";
						if (singleChatFensterListe.containsKey(sender)) {
							SingleChatHelperClass sin = singleChatFensterListe.get(sender);
							Platform.runLater(()->{
								sin.getController().addNachricht(n);
								sin.getStage().show();
								sin.getStage().toFront();
							});
						} else {
							Task<SingleChatHelperClass> task = new Task<SingleChatHelperClass>() {
								 @Override
								protected SingleChatHelperClass call() throws Exception {
									return neuerSingleChat(Client.this,sender);
								 }
							};
							Platform.runLater(task);
							try {
								SingleChatHelperClass helper = task.get(1,TimeUnit.SECONDS);
								singleChatFensterListe.put(sender,helper);
								Platform.runLater(()->{
									helper.getController().addNachricht(n);
									helper.getStage().show();
									helper.getStage().toFront();
								});
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
						// TODO Nachricht in Privatchat ausgeben
				break;
					case 5:
						Platform.runLater(() -> {
							mainController.onlineListeAktualisieren(n.getUsernameList());
						});
						setOnlineBenutzer(n.getUsernameList());
						break;
					case 6:
						// TODO Username existiert bereits, Alert neuen eingeben
						// und erneut anmelden versuchen
						break;
					case 7:
						// TODO Nachricht vom Server ausgeben zb mit alert da es
						// meldungen wie "Alex jetzt online" sind
//						pferdeSound();
						mainController.addServerMeldung(n);
						break;
					default:
						break;
					}
				} catch (IOException | ClassNotFoundException e) {
					e.printStackTrace();
				}
			}
		}).start();
	}
	private SingleChatHelperClass neuerSingleChat(Client c,String empfaenger){
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/SingleChatView.fxml"));
			Scene scene = new Scene(loader.load());
			Stage stage = new Stage();
			stage.setScene(scene);
			stage.setTitle(name + " -> " + empfaenger);
			SingleChatController localChatController = loader.<SingleChatController>getController();
			localChatController.setClient(c);
			localChatController.setEmpfaenger(empfaenger);
			stage.show();
			return new SingleChatHelperClass(localChatController, stage);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public void oeffneSingleChatClickHandler(String empfaenger){
		if(singleChatFensterListe.containsKey(empfaenger)){
			singleChatFensterListe.get(empfaenger).getStage().show();
			singleChatFensterListe.get(empfaenger).getStage().toFront();
		}else{
			SingleChatHelperClass chatHelperClass= neuerSingleChat(this,empfaenger);
			singleChatFensterListe.put(empfaenger, chatHelperClass);
			chatHelperClass.getStage().show();
			chatHelperClass.getStage().toFront();
		}
	}

	public String[] getOnlineBenutzer() {
		return onlineBenutzer;
	}

	public void setOnlineBenutzer(String[] onlineBenutzer) {
		this.onlineBenutzer = onlineBenutzer;
	}
}
