package net.omu.productions;

import javafx.stage.Stage;
import view.SingleChatController;

public class SingleChatHelperClass {
	SingleChatController controller;
	Stage stage;
	public SingleChatHelperClass(SingleChatController controller, Stage stage) {
		super();
		this.controller = controller;
		this.stage = stage;
	}
	public SingleChatController getController() {
		return controller;
	}
	public void setController(SingleChatController controller) {
		this.controller = controller;
	}
	public Stage getStage() {
		return stage;
	}
	public void setStage(Stage stage) {
		this.stage = stage;
	}
	
	

}
