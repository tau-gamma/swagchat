package net.omu.productions;

import java.io.IOException;
import java.util.Optional;

import view.MainController;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Callback;

public class ClientMain extends Application{
	private Stage primaryStage;
	private AnchorPane rootLayout;
	private Client client;
	private int serverPort = 4477;
	@Override
	public void start(Stage primaryStage) {
		try {
			this.primaryStage = primaryStage;
	        this.primaryStage.setTitle("SwagChat");
//	        primaryStage.setFullScreen(true);
//	        primaryStage.setMaximized(true);
	 		primaryStage.getIcons().add(new Image("/net/omu/productions/swagChatLogo.png"));
	        FXMLLoader loader = new FXMLLoader();
	        loader.setLocation(ClientMain.class.getResource("/view/MainView.fxml"));
			rootLayout = (AnchorPane) loader.load();
			Scene scene = new Scene(rootLayout);
			primaryStage.setScene(scene);
			MainController controller = loader.<MainController>getController();
			
            client = new Client();
            String[] loginData = showGetNameDialog("Choose your name and server.");
            int port = serverPort;
            try {				
            	port = Integer.parseInt(loginData[2]);
			} catch (Exception e) {}
            int response = client.init(loginData[0], loginData[1], port,controller);
            while(response == -1){
            	port = serverPort;
                try {				
                	port = Integer.parseInt(loginData[2]);
    			} catch (NumberFormatException e) {}
            	loginData = showGetNameDialog("Choose another username.");
            	response = client.init(loginData[0], loginData[1], port,controller);
            }
            this.primaryStage.setTitle("SwagChat - " + loginData[0] );
            if(response == -2 || response == -3){
            	Alert alert = new Alert(AlertType.ERROR);
            	alert.setTitle("Error");
            	alert.setHeaderText("Server error");
            	alert.setContentText("The Server could not be reached.");
            	alert.showAndWait();
            	primaryStage.close();
            	System.exit(0);
            }else if(response == 0){
//            	Alert alert = new Alert(AlertType.INFORMATION);
//            	alert.setTitle("Success");
//            	alert.setHeaderText("Login successful");
//            	alert.setContentText("Now you are logged in as '"+loginData[0]+"'.");
//            	alert.showAndWait();
            }
            primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
				@Override
				public void handle(WindowEvent event) {
					client.abmelden();
					System.exit(0);
					
				}
			});
            scene.getStylesheets().add(getClass().getResource("Style.css").toExternalForm());
            controller.setClient(client);//SEHR WICHTIG SONST GEHT GAR NICHTS!!!
            primaryStage.show();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	 public static void main(String[] args) {
	        launch(args);
	 }
	 
	 private String[] showGetNameDialog(String text){
		 Dialog<String[]> d = new Dialog<>();
		 d.setTitle("Start");
		 d.setHeaderText(text);
		 
		 Label l1 = new Label("Name:");
		 Label l2 = new Label("Server IP:");
		 Label l3 = new Label("Port:");
		 
		 TextField t1 = new TextField();
		 t1.requestFocus();
		 TextField t2 = new TextField();
		 t2.setPromptText("localhost");
		 TextField t3 = new TextField();
		 //t3.setPromptText("4477");
		 
		 GridPane grid = new GridPane();
		 grid.add(l1, 1, 1);
		 grid.add(t1, 2, 1);
		 grid.add(l2, 1, 2);
		 grid.add(t2, 2, 2);
		 grid.add(l3, 1, 3);
		 grid.add(t3, 2, 3);
		 
		 d.getDialogPane().setContent(grid);
		 ButtonType okButton = new ButtonType("Ok",ButtonData.OK_DONE);
		 
		 d.getDialogPane().getButtonTypes().add(okButton);
		 
		 d.setResultConverter(new Callback<ButtonType, String[]>() {
			@Override
			public String[] call(ButtonType b) {
				if(b == okButton)
					return new String[]{t1.getText(),t2.getText(),t3.getText()};
				return null;
			}
			 
		 });
		 Optional<String[]> swag = d.showAndWait();
		 if(swag.isPresent()){
			return swag.get(); 
		 }
		 return null;
	 }
}
