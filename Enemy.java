package testing;


import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;

public class Enemy
{
    private static Random random = new Random();
    
    private static Map<EnemyType, Image> imageMap;
    
    private static Map<EnemyType, int[][]> pixelMap;
    
    private static Map<EnemyType, Integer> shotMap, scoreMap;
    
    private ImageView view;
    
    private EnemyType type;
    
    private TranslateTransition transition;
    
    private double width, height;
    
    private int[][] pixels;
    
    private int shotCount, shotLimit, score;
    
    private boolean killed;
    
    private WarPane warPane;
    
    static
    {
        try
        {
            imageMap = new HashMap<>();
            pixelMap = new HashMap<>();
            shotMap = new HashMap<>();
            scoreMap = new HashMap<>();
            Image planeImage = new Image(new URL("file", null, "images/plane.png").toExternalForm());
            Image bigPlaneImage = new Image(new URL("file", null, "images/big_plane.png").toExternalForm());
            Image airshipImage = new Image(new URL("file", null, "images/airship.png").toExternalForm());
            int[][] planePixels = new int[(int)planeImage.getHeight()][(int)planeImage.getWidth()];
            int[][] bigPlanePixels = new int[(int)bigPlaneImage.getHeight()][(int)bigPlaneImage.getWidth()];
            int[][] airshipPixels = new int[(int)airshipImage.getHeight()][(int)airshipImage.getWidth()];
            Utils.fillPixelArray(planePixels, planeImage.getPixelReader());
            Utils.fillPixelArray(bigPlanePixels, bigPlaneImage.getPixelReader());
            Utils.fillPixelArray(airshipPixels, airshipImage.getPixelReader());
            imageMap.put(EnemyType.PLANE, planeImage);
            imageMap.put(EnemyType.BIG_PLANE, bigPlaneImage);
            imageMap.put(EnemyType.AIRSHIP, airshipImage);
            pixelMap.put(EnemyType.PLANE, planePixels);
            pixelMap.put(EnemyType.BIG_PLANE, bigPlanePixels);
            pixelMap.put(EnemyType.AIRSHIP, airshipPixels);
            shotMap.put(EnemyType.PLANE, 1);
            shotMap.put(EnemyType.BIG_PLANE, 9);
            shotMap.put(EnemyType.AIRSHIP, 21);
            scoreMap.put(EnemyType.PLANE, 1000);
            scoreMap.put(EnemyType.BIG_PLANE, 6000);
            scoreMap.put(EnemyType.AIRSHIP, 30000);
        }
        catch(MalformedURLException e)
        {
            e.printStackTrace();
        }
    }
    
    public enum EnemyType
    {
        PLANE, BIG_PLANE, AIRSHIP
    }
    
    public Enemy(final WarPane warPane, EnemyType type, int lifecycle)
    {
        this.warPane = warPane;
        this.type = type;
        this.shotLimit = shotMap.get(type);
        this.score = scoreMap.get(type);
        Image image = imageMap.get(type);
        this.pixels = pixelMap.get(type);
        this.view = new ImageView(image);
        this.transition = new TranslateTransition(Duration.millis(lifecycle), view);
        this.width = image.getWidth();
        this.height = image.getHeight();
        
        reuse(lifecycle);
        transition.setCycleCount(1);
        transition.setAutoReverse(false);
        transition.setToY(warPane.getBattlefieldHeight() + height);
        transition.setOnFinished(new EventHandler<ActionEvent>()
        {
            public void handle(ActionEvent e)
            {
                warPane.enemyGo(Enemy.this, false);
            }
        });
        view.translateYProperty().addListener(new ChangeListener<Number>()
        {
            public void changed(ObservableValue<? extends Number> translateY, Number oldValue, Number newValue)
            {
                if(oldValue.intValue() != newValue.intValue() && !killed
                                && Utils.hitDetecton(warPane.getMeBounds(), view.getBoundsInParent(), warPane.getMePixels(), pixels))
                {
                    warPane.gameOver();
                }
            }
        });
    }
    
    public void reuse(int lifecycle)
    {
        this.shotCount = 0;
        this.killed = false;
        int x = 0;
        
        if(type == EnemyType.PLANE)
        {
            int padding = 18;
            x = random.nextInt((int)(warPane.getBattlefieldWidth() - padding * 2 - this.width)) + padding;
        }
        else
        {
            x = random.nextInt((int)(warPane.getBattlefieldWidth() - this.width));
        }
        
        view.setImage(imageMap.get(type));
        view.setX(x);
        view.setY(-height);
        transition.setFromY(0);
        transition.setDuration(Duration.millis(lifecycle));
    }
    
    public void war(boolean fromStart)
    {
        if(fromStart)
        {
            transition.playFromStart();
        }
        else
        {
            transition.play();
        }
    }
    
    public void pause()
    {
        transition.pause();
    }
    
    public void stop()
    {
        transition.stop();
    }
    
    public void shot()
    {
        shotCount++;
        
        if(shotCount >= shotLimit)
        {
            bomb();
        }
    }
    
    private void bomb()
    {
        killed = true;
        transition.stop();
        view.setImage(warPane.getBombImage());
        view.setFitWidth(width);
        view.setFitHeight(height);
        view.setPreserveRatio(false);
        Task<Enemy> task = new Task<Enemy>()
        {
            protected Enemy call() throws Exception
            {
                Thread.sleep(100);
                Platform.runLater(new Runnable()
                {
                    public void run()
                    {
                        warPane.enemyGo(Enemy.this, true);
                        resetY();
                    }
                });
                return Enemy.this;
            }
        };
        new Thread(task).start();
    }
    
    public void resetY()
    {
        view.setTranslateY(transition.getToY());
    }
    
    public ImageView getView()
    {
        return this.view;
    }
    
    public int[][] getPixels()
    {
        return this.pixels;
    }
    
    public Bounds getBounds()
    {
        return view.getBoundsInParent();
    }

    public int getScore()
    {
        return this.score;
    }

    public EnemyType getType()
    {
        return this.type;
    }
}