package gui;

import agents.Profiler;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
 * Created by victoraxelsson on 2016-11-13.
 */
public class ProfilerApp extends Application {
    Label lbl;
    int msgCounter;
    TextField userTextField;

    OnInput onInput;

    private static ProfilerApp instance;

    public ProfilerApp(){
        instance = this;
        msgCounter = 0;
    }

    public static ProfilerApp getInstance(){
        return instance;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));


        Text scenetitle = new Text("Welcome");
        scenetitle.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
        grid.add(scenetitle, 0, 0, 2, 1);

        Label userCommand = new Label("Command:");
        grid.add(userCommand, 0, 1);

        userTextField = new TextField();
        grid.add(userTextField, 1, 1);

        Button btn = new Button("Execute");
        btn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if(onInput != null){
                    onInput.onCommand(userTextField.getText());
                    userTextField.setText("");
                }else{
                    System.out.println("There is no listener for the command");
                }
            }
        });

        HBox hbBtn = new HBox(10);
        hbBtn.setAlignment(Pos.BOTTOM_RIGHT);
        hbBtn.getChildren().add(btn);
        grid.add(hbBtn, 1, 3);

        lbl = new Label();
        ScrollPane sp = new ScrollPane();
        sp.setContent(lbl);
        grid.add(sp, 0, 4, 2, 4);

        Scene scene = new Scene(grid, 1024, 600);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Smart Museum - Profiler");
        primaryStage.show();
    }

    public void setLabel(String msg){
        Platform.runLater(new Runnable(){
            @Override
            public void run() {
                msgCounter++;
                lbl.setText("[" + msgCounter +"] " + msg + "\n" + lbl.getText());
            }
        });
    }


    public void setOnInput(OnInput onInput) {
        this.onInput = onInput;
    }
}
