package testing;


import java.awt.Robot;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javafx.animation.Animation.Status;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.SequentialTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Screen;
import javafx.util.Duration;

public class WarPane extends StackPane
{
    private static final double BG_DURATION = 10000;
    
    private War war;
    
    private WarTimer timer;
    
    private SequentialTransition bgTransition;
    
    private ImageView bgView1, bgView2, me;
    
    private Image meImage, bombImage;
    
    private Pane battlefield;
    
    private ControlPane controlPane;
    
    private Label lbScore;
    
    private int[][] mePixels;
    
    private double width, height, meWidth, meHeight;
    
    private Robot robot;
    
    private List<Enemy> enemyList;
    
    private List<Bullet> bulletList;
    
    private int totalScore;

    public WarPane(War war) throws Exception
    {
        super();
        this.war = war;
        this.enemyList = Collections.synchronizedList(new ArrayList<Enemy>());
        this.bulletList = Collections.synchronizedList(new ArrayList<Bullet>());
        this.timer = new WarTimer(this);
        this.robot = new Robot();
        this.meImage = new Image(new URL("file", null, "images/me.png").toExternalForm());
        this.bombImage = new Image(new URL("file", null, "images/bomb.png").toExternalForm());
        getStylesheets().add(new URL("file", null, "css/War.css").toExternalForm());
        init();
        initBgAnimation();
        reset();
    }
    
    private void init()
    {
        try
        {
            ImageView scoreView = new ImageView(new URL("file", null, "images/score.png").toExternalForm());
            this.bgView1 = new ImageView(new URL("file", null, "images/bg1.jpg").toExternalForm());
            this.bgView2 = new ImageView(new URL("file", null, "images/bg2.jpg").toExternalForm());
            this.lbScore = new Label("0", scoreView);
            this.me = new ImageView(meImage);
            Image bgImage = bgView1.getImage();
            Image meImage = me.getImage();
            this.battlefield = new Pane(lbScore, me);
            this.controlPane = new ControlPane(this);
            this.meWidth = meImage.getWidth();
            this.meHeight = meImage.getHeight();
            this.mePixels = new int[(int)meHeight][(int)meWidth];
            Utils.fillPixelArray(mePixels, meImage.getPixelReader());
            calculateSize(bgImage.getWidth(), bgImage.getHeight());
            resetMePixels();
            lbScore.setId("Real_Score_Label");
            lbScore.setVisible(false);
            bgView2.setTranslateY(-height);
            this.setPrefSize(width, height);
            bgView1.setFitWidth(width);
            bgView1.setFitHeight(height);
            bgView1.setPreserveRatio(false);
            bgView2.setFitWidth(width);
            bgView2.setFitHeight(height);
            bgView2.setPreserveRatio(false);
            this.getChildren().addAll(bgView1, bgView2, battlefield, controlPane);
            battlefield.setOnMouseMoved(new EventHandler<MouseEvent>()
            {
                public void handle(MouseEvent e)
                {
                    go(e);
                }
            });
            battlefield.setOnMouseEntered(new EventHandler<MouseEvent>()
            {
                public void handle(MouseEvent e)
                {
                    resetMouseLocation();
                }
            });
        }
        catch(MalformedURLException e)
        {
            e.printStackTrace();
        }
    }
    
    private void initBgAnimation()
    {
        Timeline animation1 = new Timeline();
        Timeline animation2 = new Timeline();
        animation1.getKeyFrames().add(new KeyFrame(Duration.ZERO, new KeyValue(bgView1.translateYProperty(), 0),
                        new KeyValue(bgView2.translateYProperty(), -height)));
        animation1.getKeyFrames().add(new KeyFrame(new Duration(BG_DURATION), new KeyValue(bgView1.translateYProperty(), height),
                        new KeyValue(bgView2.translateYProperty(), 0)));
        animation2.getKeyFrames().add(new KeyFrame(Duration.ZERO, new KeyValue(bgView2.translateYProperty(), 0),
                        new KeyValue(bgView1.translateYProperty(), -height)));
        animation2.getKeyFrames().add(new KeyFrame(new Duration(BG_DURATION), new KeyValue(bgView2.translateYProperty(), height),
                        new KeyValue(bgView1.translateYProperty(), 0)));
        bgTransition = new SequentialTransition(animation1, animation2);
        bgTransition.setCycleCount(Timeline.INDEFINITE);
        bgTransition.setAutoReverse(false);
    }
    
    private void calculateSize(double realWidth, double realHeight)
    {
        this.width = realWidth;
        this.height = realHeight;
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        double screenHeight = screenBounds.getHeight();
        int vPadding = 50;
        
        if(realHeight > screenHeight - vPadding)
        {
            this.height = screenHeight - vPadding;
            this.width = realWidth * (height / realHeight);
        }
    }
    
    private void resetMePixels()
    {
        int width = (int)meWidth - 16;
        int height = (int)meHeight - 10;
        int[][] pixels = new int[height][width];
        
        for(int h = 0; h < height; h++)
        {
            for(int w = 0; w < width; w++)
            {
                pixels[h][w] = mePixels[h][w + 8];
            }
        }
        
        this.mePixels = pixels;
    }
    
    public void reset()
    {
        if(!isWarring())
        {
            me.setImage(meImage);
            me.setX((width - meWidth) / 2);
            me.setY(height - meHeight);
            timer.reset();
            enemyList.clear();
            bulletList.clear();
            battlefield.getChildren().setAll(lbScore, me);
            ArmFactory.reset();
            lbScore.setText(String.valueOf(totalScore = 0));
            lbScore.toFront();
        }
    }
    
    private void go(MouseEvent e)
    {
        if(isWarring())
        {
            double x = e.getX() - meWidth / 2;
            double y = e.getY() - meHeight / 2;
            x = Math.max(x, 0);
            x = Math.min(width - meWidth, x);
            y = Math.max(y, 0);
            y = Math.min(height - meHeight, y);
            me.setX(x);
            me.setY(y);
        }
    }
    
    private void resetMouseLocation()
    {
        int x = (int)(me.getX() + meWidth / 2 + war.getSceneAbsoluteX());
        int y = (int)(me.getY() + meHeight / 2 + war.getSceneAbsoluteY());
        robot.mouseMove(x, y);
    }
    
    private void start(boolean restart)
    {
        controlPane.hide();
        resetMouseLocation();
        this.setCursor(Cursor.NONE);
        
        if(restart)
        {
            bgTransition.playFromStart();
        }
        else
        {
            bgTransition.play();
        }
        
        timer.start();
        
        for(Enemy enemy: enemyList)
        {
            enemy.war(restart);
        }
        
        for(Bullet bullet: bulletList)
        {
            bullet.shoot(restart);
        }
    }
    
    public void start()
    {
        start(false);
    }
    
    public void restart()
    {
        lbScore.setVisible(true);
        reset();
        System.gc();
        start(true);
    }
    
    public void pause()
    {
        this.setCursor(Cursor.DEFAULT);
        bgTransition.pause();
        timer.stop();
        
        for(Enemy enemy: enemyList)
        {
            enemy.pause();
        }
        
        for(Bullet bullet: bulletList)
        {
            bullet.pause();
        }
        
        controlPane.showWhenPause();
    }
    
    public void stop(boolean showControlPane)
    {
        lbScore.setVisible(false);
        this.setCursor(Cursor.DEFAULT);
        bgTransition.stop();
        timer.stop();
        
        for(Enemy enemy: enemyList)
        {
            enemy.stop();
        }
        
        for(Bullet bullet: bulletList)
        {
            bullet.stop();
        }
        
        if(showControlPane)
        {
            controlPane.showWhenStop();
        }
    }
    
    public void startOrPause()
    {
        Status status = bgTransition.getStatus();
        
        if(status == Status.RUNNING)
        {
            pause();
        }
        else if(status == Status.PAUSED)
        {
            start();
        }
        else if(status == Status.STOPPED)
        {
            restart();
        }
    }
    
    public void shoot()
    {
        Bullet bullet = ArmFactory.getBullet(this);
        bulletList.add(bullet);
        battlefield.getChildren().add(bullet.getView());
        bullet.getView().toBack();
        bullet.shoot(true);
    }
    
    public void bulletGo(Bullet bullet)
    {
        bulletList.remove(bullet);
        battlefield.getChildren().remove(bullet.getView());
        ArmFactory.recycleBullet(bullet);
    }
    
    public void enemyCome(Enemy enemy)
    {
        enemyList.add(enemy);
        battlefield.getChildren().add(enemy.getView());
        enemy.getView().toBack();
        enemy.war(true);
    }
    
    public void enemyGo(Enemy enemy, boolean killed)
    {
        enemyList.remove(enemy);
        battlefield.getChildren().remove(enemy.getView());
        ArmFactory.recycleEnemy(enemy);
        
        if(killed)
        {
            this.totalScore += enemy.getScore();
            lbScore.setText(String.valueOf(totalScore));
        }
    }
    
    public void gameOver()
    {
        me.setImage(bombImage);
        me.setFitWidth(meWidth);
        me.setFitHeight(meHeight);
        me.setPreserveRatio(false);
        Task<Object> task = new Task<Object>()
        {
            protected Enemy call() throws Exception
            {
                Thread.sleep(100);
                Platform.runLater(new Runnable()
                {
                    public void run()
                    {
                        stop(true);
                    }
                });
                return null;
            }
        };
        new Thread(task).start();
    }
    
    public boolean isWarring()
    {
        return bgTransition.getStatus() == Status.RUNNING;
    }
    
    public double getBattlefieldWidth()
    {
        return this.width;
    }

    public double getBattlefieldHeight()
    {
        return this.height;
    }

    public Bounds getMeBounds()
    {
        Bounds bounds = me.getBoundsInParent();
        BoundingBox newBounds = new BoundingBox(bounds.getMinX() + 8, bounds.getMinY(), bounds.getWidth() - 16, bounds.getHeight() - 10);
        return newBounds;
    }

    public int[][] getMePixels()
    {
        return this.mePixels;
    }

    public Image getBombImage()
    {
        return this.bombImage;
    }
    
    public int getTotalScore()
    {
        return totalScore;
    }

    public List<Enemy> getEnemys()
    {
        return this.enemyList;
    }
}
