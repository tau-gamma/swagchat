package net.omu.productions.server;
 
import java.time.LocalDateTime;

import net.omu.productions.Nachricht;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
 
public class ServerGUI extends Application {
    public static void main(String[] args) {
        launch(args);
    }
    private Server server;
    
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("SwagChat - Server");
        primaryStage.getIcons().add(new Image("/net/omu/productions/swagChatLogo.png"));
        TextArea textArea = new TextArea();
        textArea.setEditable(false);
        
        StackPane root = new StackPane();
        root.getChildren().add(textArea);
        primaryStage.setScene(new Scene(root, 500, 500));
        primaryStage.show();
        
    	this.server = new Server(textArea,primaryStage);
        
        
        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			@Override
			public void handle(WindowEvent event) {
				server.sendeNachrichtAnAlle(new Nachricht(7, null, null, "Der Server ist offline gegangen.\nJede Nachricht die Sie jetzt versenden wird im Nirvana/Limbus landen!!!", LocalDateTime.now()));
				ServerGUI.this.server.beenden();
				System.exit(0);
			}
		});
    }
}
