package testing;


import java.net.URL;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class War extends Application
{
    private Stage stage;
    
    private Scene scene;
    
    @Override
    public void start(Stage primaryStage) throws Exception
    {
        this.stage = primaryStage;
        WarPane warPane = new WarPane(this);
        scene = new Scene(warPane);
        primaryStage.setScene(scene);
        primaryStage.sizeToScene();
        primaryStage.centerOnScreen(); 
        primaryStage.setTitle("SpaceJammer");
        primaryStage.setResizable(false);
        primaryStage.getIcons().add(new Image(new URL("file", null, "images/icon.png").toExternalForm()));
        
        initListener(warPane);
        primaryStage.show();
    }

    private void initListener(final WarPane warPane)
    {
        stage.setOnCloseRequest(new EventHandler<WindowEvent>()
        {
            public void handle(WindowEvent e)
            {
                warPane.stop(false);
            }
        });
        scene.setOnKeyPressed(new EventHandler<KeyEvent>()
        {
            public void handle(KeyEvent e)
            {
                if(e.getCode() == KeyCode.SPACE)
                {
                    warPane.startOrPause();
                }
            }
        });
        scene.setOnMouseReleased(new EventHandler<MouseEvent>()
        {
            public void handle(MouseEvent e)
            {
                if(e.getButton() == MouseButton.PRIMARY)
                {
                    warPane.startOrPause();
                }
            }
        });
        stage.focusedProperty().addListener(new ChangeListener<Boolean>()
        {
            public void changed(ObservableValue<? extends Boolean> focused, Boolean oldValue, Boolean newValue)
            {
                if(!newValue.booleanValue() && warPane.isWarring())
                {
                    warPane.pause();
                }
            }
        });
    }

    public double getSceneAbsoluteX()
    {
        return stage.getX() + scene.getX();
    }
    
    public double getSceneAbsoluteY()
    {
        return stage.getY() + scene.getY();
    }
    
    public static void main(String[] args)
    {
        launch(args);
    }
}
