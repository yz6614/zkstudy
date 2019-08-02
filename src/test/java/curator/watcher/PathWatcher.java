package curator.watcher;


import org.apache.curator.framework.recipes.cache.*;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent.Type;
import org.apache.zookeeper.CreateMode;
import org.junit.Test;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

/**
 * 只能监听指定的目录下是否有子节点创建或删除
 */

public class PathWatcher extends BaseWather implements Callable<PathChildrenCache> {

    private final String PATHWATCHERPATH = "/pathDemo";
    private PathChildrenCache pathChildrenCache;

    @Test
    public void watchPathWatcher() throws Exception {
        PathWatcher pathWatcher = new PathWatcher();
        pathWatcher.setClient(this.client);
        pathWatcher.setCountDownLatch(this.countDownLatch);

        FutureTask<PathChildrenCache> watcherFuture = new FutureTask(pathWatcher);
        Thread watchThread = new Thread(watcherFuture);
        watchThread.start();
        countDownLatch.await();
        this.pathChildrenCache = watcherFuture.get();


        this.client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(new StringBuffer(ROOTPATH).append(PATHWATCHERPATH).append("/path_demo1").toString(), "01".getBytes());
        TimeUnit.SECONDS.sleep(2);
        this.client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(new StringBuffer().append(ROOTPATH).append(PATHWATCHERPATH).append("/path_demo2").toString(), "02".getBytes());
        TimeUnit.SECONDS.sleep(2);
        this.client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(new StringBuffer().append(ROOTPATH).append(PATHWATCHERPATH).append("/path_demo3/miemie").toString(), "使劲啊".getBytes());
        TimeUnit.SECONDS.sleep(2);

        for (ChildData data : pathChildrenCache.getCurrentData()) {
            System.out.println("\n\n =====  getCurrentData:" + data.getPath() + " = " + new String(data.getData()));
        }

        TimeUnit.SECONDS.sleep(60);
        this.client.delete().forPath(new StringBuffer().append(ROOTPATH).append(PATHWATCHERPATH).append("/path_demo1").toString());
        TimeUnit.SECONDS.sleep(2);
        this.client.delete().forPath(new StringBuffer().append(ROOTPATH).append(PATHWATCHERPATH).append("/path_demo2").toString());
        TimeUnit.SECONDS.sleep(2);
        System.out.println("\n\n ===========  自由时间开始  60s ==============");
        pathChildrenCache.close();
        System.out.println("\n\n ===========  监听结束  ==============");

    }

    @Override
    public PathChildrenCache call() throws Exception {
        this.pathChildrenCache = new PathChildrenCache(this.client, new StringBuffer().append(ROOTPATH).append(PATHWATCHERPATH).toString(), true);
        this.pathChildrenCache.getListenable().addListener((client, event) -> {
            String eventType = "";
            boolean eventTriger = true;
            if (Type.CHILD_ADDED.equals(event.getType())) {
                eventType = Type.CHILD_ADDED.toString();
            } else if (Type.CHILD_REMOVED.equals(event.getType())) {
                eventType = Type.CHILD_REMOVED.toString();
            } else {
                eventTriger = false;
            }
            if (eventTriger) {
                System.out.println(String.format("\n\n ======== [%s] ========", eventType));
                if (null != event.getData()) {
                    System.out.println(String.format("节点数据：[%s] = [%s]  "
                            , event.getData().getPath()
                            , new String(event.getData().getData())));
                }
            }
        });
        try {
            System.out.println("\n\n ===========  建立监听  ==============");
            this.pathChildrenCache.start();
            getCountDownLatch().countDown();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return this.pathChildrenCache;
    }


    public String getPATHWATCHERPATH() {
        return PATHWATCHERPATH;
    }
}
