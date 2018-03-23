package testing;


import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import testing.Enemy.EnemyType;

public class ArmFactory
{
    private static final int BULLET_CACHE_SIZE = 5;
    
    private static final int PLANE_CACHE_SIZE = 30;
    
    private static final int BIG_PLANE_CACHE_SIZE = 6;
    
    private static final int AIRSHIP_CACHE_SIZE = 3;
    
    private static Set<Bullet> bulletSet = Collections.synchronizedSet(new HashSet<Bullet>(BULLET_CACHE_SIZE));
    
    private static Set<Enemy> planeSet = Collections.synchronizedSet(new HashSet<Enemy>());
    
    private static Set<Enemy> bigPlaneSet = Collections.synchronizedSet(new HashSet<Enemy>(BIG_PLANE_CACHE_SIZE));
    
    private static Set<Enemy> airshipSet = Collections.synchronizedSet(new HashSet<Enemy>(AIRSHIP_CACHE_SIZE));
    
    private static Map<EnemyType, Set<Enemy>> setMap = new HashMap<>();
    
    private static Map<EnemyType, Integer> cacheSizeMap = new HashMap<>();
    
    static
    {
        setMap.put(EnemyType.PLANE, planeSet);
        setMap.put(EnemyType.BIG_PLANE, bigPlaneSet);
        setMap.put(EnemyType.AIRSHIP, airshipSet);
        cacheSizeMap.put(EnemyType.PLANE, PLANE_CACHE_SIZE);
        cacheSizeMap.put(EnemyType.BIG_PLANE, BIG_PLANE_CACHE_SIZE);
        cacheSizeMap.put(EnemyType.AIRSHIP, AIRSHIP_CACHE_SIZE);
    }
    
    public static void reset()
    {
        for(Bullet bullet: bulletSet)
        {
            bullet.resetY();
        }
        
        for(Set<Enemy> set: setMap.values())
        {
            for(Enemy enemy: set)
            {
                enemy.resetY();
            }
        }
    }
    
    public synchronized static Bullet getBullet(WarPane warPane)
    {
        Bullet bullet = null;
        
        if(bulletSet.isEmpty())
        {
            bullet = new Bullet(warPane);
        }
        else
        {
            bullet = bulletSet.iterator().next();
            bulletSet.remove(bullet);
            bullet.reuse();
        }
        
        return bullet;
    }
    
    public synchronized static void recycleBullet(Bullet bullet)
    {
        if(bulletSet.size() < BULLET_CACHE_SIZE)
        {
            bulletSet.add(bullet);
        }
    }
    
    public synchronized static Enemy getEnemy(WarPane warPane, EnemyType type, int lifecycle)
    {
        Set<Enemy> set = setMap.get(type);
        Enemy enemy = null;
        
        if(set.isEmpty())
        {
            enemy = new Enemy(warPane, type, lifecycle);
        }
        else
        {
            enemy = set.iterator().next();
            set.remove(enemy);
            enemy.reuse(lifecycle);
        }
        
        return enemy;
    }
    
    public synchronized static void recycleEnemy(Enemy enemy)
    {
        EnemyType type = enemy.getType();
        Set<Enemy> set = setMap.get(type);
        int cacheSize = cacheSizeMap.get(type);
        
        if(set.size() < cacheSize)
        {
            set.add(enemy);
        }
    }
}