package testing;

import testing.Enemy.EnemyType;
import javafx.animation.AnimationTimer;

public class WarTimer extends AnimationTimer
{
    private WarPane warPane;
    
    private long startMillis, breakDuration, stopMillis, lastEnemyComeMillis, lastShotMillis;
    
    private int planeCount, maxPlaneInterval, maxPlaneLifecycle, maxBigPlaneLifecycle, maxAirshipLifecycle;
    
    private int minPlaneInterval, minPlaneLifecycle, minBigPlaneLifecycle, minAirshipLifecycle;
    
    private boolean bigCame, airshipCame;
    
    public WarTimer(WarPane warPane)
    {
        this.warPane = warPane;
        this.maxPlaneInterval = 600;
        this.minPlaneInterval = 100;
        this.maxPlaneLifecycle = 5000;
        this.minPlaneLifecycle = maxPlaneLifecycle / 3;
        this.maxBigPlaneLifecycle = 6500;
        this.minBigPlaneLifecycle = maxBigPlaneLifecycle / 3;
        this.maxAirshipLifecycle = 10000;
        this.minAirshipLifecycle = maxAirshipLifecycle / 3;
    }
    
    public void handle(long nanoTime)
    {
        int durationSeconds = (int)((System.currentTimeMillis() - startMillis - breakDuration) / 1000);
        int planeInterval = Math.max(maxPlaneInterval - durationSeconds, minPlaneInterval);
        int planeLifecycle = Math.max(maxPlaneLifecycle - durationSeconds, minPlaneLifecycle);
        int bigPlaneLifecycle = Math.max(maxBigPlaneLifecycle - durationSeconds, minBigPlaneLifecycle);
        int airshipLifecycle = Math.max(maxAirshipLifecycle - durationSeconds, minAirshipLifecycle);
        long currentMillis = System.currentTimeMillis();
        Enemy enemy;
        
        if(lastShotMillis == 0 || currentMillis - lastShotMillis >= 100)
        {
            warPane.shoot();
            lastShotMillis = currentMillis;
        }
        
        if(lastEnemyComeMillis == 0)
        {
            enemy = ArmFactory.getEnemy(warPane, EnemyType.PLANE, planeLifecycle);
            warPane.enemyCome(enemy);
            planeCount++;
            lastEnemyComeMillis = currentMillis;
        }
        else if(currentMillis - lastEnemyComeMillis >= 100)
        {
            if(!airshipCame && planeCount % 21 == 0)
            {
                enemy = ArmFactory.getEnemy(warPane, EnemyType.AIRSHIP, airshipLifecycle);
                warPane.enemyCome(enemy);
                airshipCame = true;
            }
            else if(!bigCame && planeCount % 9 == 0)
            {
                enemy = ArmFactory.getEnemy(warPane, EnemyType.BIG_PLANE, bigPlaneLifecycle);
                warPane.enemyCome(enemy);
                bigCame = true;
            }
            else if(currentMillis - lastEnemyComeMillis >= planeInterval)
            {
                enemy = ArmFactory.getEnemy(warPane, EnemyType.PLANE, planeLifecycle);
                warPane.enemyCome(enemy);
                planeCount++;
                lastEnemyComeMillis = currentMillis;
                bigCame = false;
                airshipCame = false;
            }
        }
    }
    
    public void start()
    {
        if(startMillis == 0)
        {
            startMillis = System.currentTimeMillis();
        }
        else
        {
            long deltaMillis = System.currentTimeMillis() - stopMillis;
            breakDuration += deltaMillis;
            lastEnemyComeMillis += deltaMillis;
            lastShotMillis += deltaMillis;
            stopMillis = 0;
        }
        
        super.start();
    }
    
    public void stop()
    {
        super.stop();
        stopMillis = System.currentTimeMillis();
    }
    
    public void reset()
    {
        this.startMillis = 0;
        this.breakDuration = 0;
        this.stopMillis = 0;
        this.lastEnemyComeMillis = 0;
        this.lastShotMillis = 0;
    }
}
