package testing;

import javafx.event.ActionEvent;

import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class ControlPane extends VBox implements EventHandler<ActionEvent>
{
    private static final String BUTTON_ID_START = "Button_Start";
    
    private static final String BUTTON_ID_CONTINUE = "Button_Continue";
    
    private static final String BUTTON_ID_RESTART = "Button_Restart";
    
    private static final String BUTTON_ID_START_WHEN_OVER = "Button_Start_When_Over";
    
    private WarPane warPane;
    
    private BorderPane scorePane;
    
    private Label lbScore;
    
    private Button btnContinue, btnRestart;
    
    public ControlPane(WarPane warPane)
    {
        super();
        this.warPane = warPane;
        init();
    }
    
    private void init()
    {
        Button btnStart = new Button("开始");
        btnStart.setId(BUTTON_ID_START);
        btnStart.setOnAction(this);
        btnContinue = new Button("继续");
        btnContinue.setId(BUTTON_ID_CONTINUE);
        btnContinue.setOnAction(this);
        btnRestart = new Button("重新开始");
        btnRestart.setId(BUTTON_ID_RESTART);
        btnRestart.setOnAction(this);
        createScorePane();
        this.setId("Control_Pane");
        this.getChildren().add(btnStart);
    }
    
    private void createScorePane()
    {
        scorePane = new BorderPane();
        lbScore = new Label();
        Label lbScoreTitle = new Label("飞机大战分数");
        Button btnContinue = new Button("继续");
        HBox buttonParent = new HBox(btnContinue);
        btnContinue.setId(BUTTON_ID_START_WHEN_OVER);
        btnContinue.setOnAction(this);
        buttonParent.setPrefHeight(72);
        buttonParent.setAlignment(Pos.CENTER);
        scorePane.setId("Score_Pane");
        scorePane.setTop(lbScoreTitle);
        scorePane.setCenter(lbScore);
        scorePane.setBottom(buttonParent);
        lbScore.setId("Score_Label");
        lbScoreTitle.setId("Score_Title_Label");
    }
    
    public void showWhenPause()
    {
        this.getChildren().setAll(btnContinue, btnRestart);
        this.setVisible(true);
    }
    
    public void showWhenStop()
    {
        lbScore.setText(String.valueOf(warPane.getTotalScore()));
        this.getChildren().setAll(scorePane);
        this.setVisible(true);
    }
    
    public void hide()
    {
        this.setVisible(false);
    }

    public void handle(ActionEvent e)
    {
        Button button = (Button)e.getSource();
        String id = button.getId();
        
        switch(id)
        {
            case BUTTON_ID_START:
            case BUTTON_ID_RESTART:
            case BUTTON_ID_START_WHEN_OVER:
            {
                warPane.restart();
                break;
            }
            case BUTTON_ID_CONTINUE:
            {
                warPane.start();
                break;
            }
        }
    }
}