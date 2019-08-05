package curator.watcher;


import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.TreeCache;
import com.google.common.base.Preconditions;
import org.apache.zookeeper.CreateMode;
import org.junit.Test;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

public class TreePathWatcher extends BaseWather implements Callable<TreeCache> {
    private final String TREEWATCHERPATH = "/treeDemo";
    private TreeCache treeCache;

    @Test
    public void treeWatcherTest() throws Exception {
        TreePathWatcher treePathWatcher = new TreePathWatcher();
        treePathWatcher.setClient(this.client);
        treePathWatcher.setCountDownLatch(getCountDownLatch());
        FutureTask<TreeCache> futureTask = new FutureTask(treePathWatcher);
        new Thread(futureTask).start();
        getCountDownLatch().await();
        if(null != this.client.checkExists().forPath(new StringBuffer().append(ROOTPATH).append(TREEWATCHERPATH).toString())){
            this.client.delete().forPath(new StringBuffer().append(ROOTPATH).append(TREEWATCHERPATH).toString());
        }

//        this.client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(new StringBuffer().append(ROOTPATH).append(TREEWATCHERPATH).toString());
        this.treeCache = futureTask.get();

        this.client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(new StringBuffer().append(ROOTPATH).append(TREEWATCHERPATH).append("/path_demo1").toString(), "01".getBytes());
        TimeUnit.SECONDS.sleep(2);
        this.client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(new StringBuffer().append(ROOTPATH).append(TREEWATCHERPATH).append("/path_demo2").toString(), "02".getBytes());
        TimeUnit.SECONDS.sleep(2);
        this.client.setData().forPath(new StringBuffer().append(ROOTPATH).append(TREEWATCHERPATH).append("/path_demo2").toString(), "地图炮，乌鸦坐飞机，猫落地".getBytes());
        TimeUnit.SECONDS.sleep(2);
        this.client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(new StringBuffer().append(ROOTPATH).append(TREEWATCHERPATH).append("/path_demo2/zyl/zyl").toString(), "02".getBytes());

        TimeUnit.SECONDS.sleep(10);

        this.client.delete().forPath(new StringBuffer().append(ROOTPATH).append(TREEWATCHERPATH).append("/path_demo1").toString());
        TimeUnit.SECONDS.sleep(2);
        this.client.delete().forPath(new StringBuffer().append(ROOTPATH).append(TREEWATCHERPATH).append("/path_demo2").toString());
        TimeUnit.SECONDS.sleep(2);
        this.client.delete().forPath(new StringBuffer().append(ROOTPATH).append(TREEWATCHERPATH).append("/path_demo2/zyl/zyl").toString());
        TimeUnit.SECONDS.sleep(2);
        System.out.println("\n\n ===========  自由时间开始  60s ==============");
        this.treeCache.close();
        System.out.println("\n\n ===========  监听结束  ==============");
    }

    @Override
    public TreeCache call() throws Exception {
        TreeCache treeCache = new TreeCache(this.client, new StringBuffer().append(ROOTPATH).append(TREEWATCHERPATH).toString());
        treeCache.getListenable().addListener((client, event) -> {
            String eventType = "";
            boolean eventTriger = true;
            if (PathChildrenCacheEvent.Type.CHILD_ADDED.equals(event.getType())) {
                eventType = PathChildrenCacheEvent.Type.CHILD_ADDED.toString();
            } else if (PathChildrenCacheEvent.Type.CHILD_REMOVED.equals(event.getType())) {
                eventType = PathChildrenCacheEvent.Type.CHILD_REMOVED.toString();
            } else if (PathChildrenCacheEvent.Type.CHILD_UPDATED.equals(event.getType())) {
                eventType = PathChildrenCacheEvent.Type.CHILD_REMOVED.toString();
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
            System.out.println("\n\n事件类型：" + event.getType() +
                    " | 路径：" + (null != event.getData() ? event.getData().getPath() : null));
        });
        try {
            treeCache.start();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        getCountDownLatch().countDown();
        return treeCache;
    }
}
