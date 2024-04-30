package view;

import java.net.SocketException;
import java.time.format.DateTimeFormatter;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import net.omu.productions.Client;
import net.omu.productions.Nachricht;

public class MainController {
	private GridPane gridPane;

	@FXML
	private ScrollPane scrollPane;

	@FXML
	private ListView<String> onlineListe;

	@FXML
	private Button sendenButton;

	@FXML
	private TextArea textArea;

	private Client client;

	private int rowIndex = 0;

	private static final DateTimeFormatter formatter = DateTimeFormatter
			.ofPattern("HH:mm:ss - dd.MM.yyyy");

	public static final int ZEICHEN_LIMIT = 500;

	public void setClient(Client client) {
		this.client = client;
		try {
			client.nachrichtenVerarbeiten(onlineListe);
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}

	@FXML
	private void handleSenden() {
		if (!textArea.getText().trim().isEmpty()) {
			client.anAlleSenden(textArea.getText());
			textArea.setText("");
		}
	}

	public void addNachricht(Nachricht n) {
		Label label;
		if (!n.getSender().equals(client.getName())) {
			label = new Label(n.getSender() + ": " + n.getInhalt());
			label.getStyleClass().add("empfaenger");
			gridPane.add(label, 1, rowIndex);
		} else {
			label = new Label(n.getInhalt());
			label.getStyleClass().add("sender");
			gridPane.add(label, 0, rowIndex);
		}
		label.setWrapText(true);
		label.setTooltip(new Tooltip(n.getSendedatum().format(formatter)));

		rowIndex++;
	}

	public void addServerMeldung(Nachricht n) {
		Label label;
		label = new Label(n.getInhalt());
		label.getStyleClass().add("empfaenger");
		Platform.runLater(() -> {
			gridPane.add(label, 1, rowIndex);
		});

		label.setWrapText(true);
		label.setTooltip(new Tooltip(n.getSendedatum().format(formatter)));

		rowIndex++;
	}

	public void onlineListeAktualisieren(String[] fuenfNamenWillI) {
		ObservableList<String> names = FXCollections.observableArrayList();
		names.addAll(fuenfNamenWillI);
		names.remove(client.getName());
		onlineListe.setItems(null);
		onlineListe.setItems(names);
	}

	@FXML
	void initialize() {
		assert onlineListe != null : "fx:id=\"onlineListe\" was not injected: check your FXML file 'MainView.fxml'.";
		assert sendenButton != null : "fx:id=\"sendenButton\" was not injected: check your FXML file 'MainView.fxml'.";
		assert textArea != null : "fx:id=\"textArea\" was not injected: check your FXML file 'MainView.fxml'.";

		onlineListe.setOnMouseClicked((MouseEvent e) -> {
					if (e.getClickCount() == 2
							&& onlineListe.getSelectionModel()
									.getSelectedItem() != null) {
						client.oeffneSingleChatClickHandler(onlineListe
								.getSelectionModel().getSelectedItem());
					}
				});
		gridPane = new GridPane();
		gridPane.setHgap(10);
		gridPane.setVgap(5);
		
		gridPane.heightProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observable, Object oldvalue, Object newValue) {
             scrollPane.setVvalue((Double)newValue );  
            }
        });
		
		ColumnConstraints column1 = new ColumnConstraints();
		column1.setHalignment(HPos.LEFT);
		column1.setPercentWidth(50);
		gridPane.getColumnConstraints().add(column1); 

		ColumnConstraints column2 = new ColumnConstraints();
		column2.setHalignment(HPos.RIGHT);
		column2.setPercentWidth(50);
		gridPane.getColumnConstraints().add(column2); 

		scrollPane.setContent(gridPane);
		scrollPane.setFitToWidth(true);

		textArea.lengthProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable,
					Number oldValue, Number newValue) {
				if (newValue.intValue() > oldValue.intValue()) {
					if (textArea.getText().length() >= ZEICHEN_LIMIT) {
						textArea.setText(textArea.getText().substring(0,
								ZEICHEN_LIMIT));
					}
				}
			}
		});

		textArea.setOnKeyPressed(keyEvent -> {
			if (keyEvent.getCode() == KeyCode.ENTER && keyEvent.isControlDown()) {
				textArea.appendText("\n");
				keyEvent.consume();
			} else if (keyEvent.getCode() == KeyCode.ENTER) {
				handleSenden();
				keyEvent.consume();
			}
		});
	}
}