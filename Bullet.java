package testing;


import java.net.MalformedURLException;
import java.net.URL;

import javafx.animation.TranslateTransition;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;

public class Bullet
{
    private static final double BASE_DURATION_MILLIS = 500;
    
    private static Image bulletImage;
    
    private static int[][] bulletPixels;
    
    private WarPane warPane;
    
    private ImageView view;
    
    private TranslateTransition transition;
    
    static
    {
        try
        {
            bulletImage = new Image(new URL("file", null, "images/bullet.png").toExternalForm());
            bulletPixels = new int[(int)bulletImage.getHeight()][(int)bulletImage.getWidth()];
            Utils.fillPixelArray(bulletPixels, bulletImage.getPixelReader());
        }
        catch(MalformedURLException e)
        {
            e.printStackTrace();
        }
    }
    
    public Bullet(final WarPane warPane)
    {
        this.warPane = warPane;
        this.view = new ImageView(bulletImage);
        Bounds meBounds = warPane.getMeBounds();
        double duration = calculateDuration(warPane, meBounds);
        this.transition = new TranslateTransition(Duration.millis(duration), view);
        transition.setCycleCount(1);
        transition.setAutoReverse(false);
        transition.setToY(-bulletImage.getHeight());
        reuse();
        transition.setOnFinished(new EventHandler<ActionEvent>()
        {
            public void handle(ActionEvent e)
            {
                warPane.bulletGo(Bullet.this);
            }
        });
        view.translateYProperty().addListener(new ChangeListener<Number>()
        {
            public void changed(ObservableValue<? extends Number> translateY, Number oldValue, Number newValue)
            {
                if(oldValue.intValue() != newValue.intValue())
                {
                    for(Enemy enemy: warPane.getEnemys())
                    {
                        if(Utils.hitDetecton(enemy.getBounds(), view.getBoundsInParent(), enemy.getPixels(), bulletPixels))
                        {
                            transition.stop();
                            warPane.bulletGo(Bullet.this);
                            resetY();
                            enemy.shot();
                            break;
                        }
                    }
                }
            }
        });
    }
    
    public void reuse()
    {
        Bounds meBounds = warPane.getMeBounds();
        view.setX(meBounds.getMinX() + (meBounds.getWidth() - bulletImage.getWidth()) / 2);
        transition.setFromY(meBounds.getMinY());
    }
    
    private double calculateDuration(WarPane warPane, Bounds meBounds)
    {
        double baseHeight = warPane.getBattlefieldHeight();
        double nowHeight = meBounds.getMinY() + meBounds.getHeight();
        double duration = BASE_DURATION_MILLIS * nowHeight / baseHeight;
        return duration;
    }
    
    public void shoot(boolean fromStart)
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
    
    public void resetY()
    {
        view.setTranslateY(transition.getToY());
    }
    
    public ImageView getView()
    {
        return this.view;
    }
}