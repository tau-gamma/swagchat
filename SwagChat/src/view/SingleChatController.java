package view;


import net.omu.productions.Client;
import net.omu.productions.Nachricht;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;

public class SingleChatController {
	/**
	 * Enthaelt den Chatverlauf 
	 */
	@FXML 
	private TextArea textArea;
	
	/**
	 * Enthaelt den zu versendenden Text
	 */
	@FXML 
	private TextArea versendenTextArea;
	
	private Client client;
	private String empfaenger;

	
    public String getEmpfaenger() {
		return empfaenger;
	}

	public void setEmpfaenger(String empfaenger) {
		this.empfaenger = empfaenger;
	}

	@FXML
    void onSendenClick() {
    	if(contains(client.getOnlineBenutzer(), empfaenger)){
    		if (versendenTextArea.getText().trim().length() > 0) {
    			client.anBestimmtePersonSenden(versendenTextArea.getText(),empfaenger);
    			textArea.appendText("Me: " + versendenTextArea.getText() + "\n");
    			versendenTextArea.setText("");
    		}
    	}else{
    		textArea.appendText("Der Benutzer '" + empfaenger + "' ist nicht mehr online");
    	}
    }
	private boolean contains(String [] arr,String name){
		for(int i = 0; i < arr.length; i ++){
			if(arr[i].equals(name))
				return true;
		}
		return false;
	}
    
    public Client getClient() {
		return client;
	}

	public void setClient(Client client) {
		this.client = client;
	}
	
	public void addNachricht (Nachricht n){
		Platform.runLater(()->{
			textArea.appendText(n.getSender() +": "+ n.getInhalt() + "\n");
		});
	}

	@FXML
    void initialize() {
        assert textArea != null : "textArea NULL Single Client Chat";
        assert versendenTextArea != null :  "textArea ist NULL";
        
        int LIMIT = MainController.ZEICHEN_LIMIT; // Beschränkt die Anzahl der Zeichen, die man mit einer Nachricht versenden darf 
        versendenTextArea.lengthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable,
                    Number oldValue, Number newValue) {
                if (newValue.intValue() > oldValue.intValue()) {
                    if (versendenTextArea.getText().length() >= LIMIT) {
                    	versendenTextArea.setText(versendenTextArea.getText().substring(0, LIMIT));
                    }
                }
            }
        });
        
        versendenTextArea.setOnKeyPressed(keyEvent -> {
        	if (keyEvent.getCode() == KeyCode.ENTER && keyEvent.isControlDown())  {
        		versendenTextArea.appendText("\n");
            	keyEvent.consume();
            }else if (keyEvent.getCode() == KeyCode.ENTER)  {
            	onSendenClick();
            	keyEvent.consume();
            }
        });
	}
}