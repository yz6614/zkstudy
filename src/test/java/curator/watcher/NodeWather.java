package curator.watcher;

import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.zookeeper.CreateMode;
import org.junit.Test;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;


/**
 * 只能监听指定节点的数据改变， 或者是该节点被删除
 */
public class NodeWather extends BaseWather implements Callable<NodeCache> {
    private final String NODEWATCHERPATH = "/nodeDemo";
    private NodeCache nodeCache;

    @Test
    public void nodeWatherTest() throws InterruptedException {
        NodeWather nodeWather = new NodeWather();
        nodeWather.setClient(this.client);
        nodeWather.setCountDownLatch(this.countDownLatch);
        FutureTask<NodeCache> futureTask = new FutureTask(nodeWather);
        Thread nodeWatcherTHread = new Thread(futureTask);
        nodeWatcherTHread.start();
        getCountDownLatch().await();
        try {
            this.nodeCache = futureTask.get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        try {
            this.client.create().withMode(CreateMode.EPHEMERAL)
                    .forPath(new StringBuffer()
                            .append(getROOTPATH()).append(NODEWATCHERPATH).toString());
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            this.client.setData().forPath( new StringBuffer()
                    .append(getROOTPATH()).append(NODEWATCHERPATH).toString(), "01".getBytes());
            Thread.sleep(100);
            this.client.setData().forPath( new StringBuffer()
                    .append(getROOTPATH()).append(NODEWATCHERPATH).toString(), "02".getBytes());
            Thread.sleep(100);
            System.out.println("\n\n ===========  自由时间开始  60s ==============");
            TimeUnit.SECONDS.sleep(60);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            this.client.delete().deletingChildrenIfNeeded().forPath( new StringBuffer()
                    .append(getROOTPATH()).append(NODEWATCHERPATH).toString());
            Thread.sleep(1000 * 2);
            this.nodeCache.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public NodeCache call() throws Exception {
        this.nodeCache = new NodeCache(this.client, new StringBuffer()
                .append(getROOTPATH()).append(NODEWATCHERPATH).toString());

        this.nodeCache.getListenable().addListener(() -> {
            ChildData data = this.nodeCache.getCurrentData();
            if (null != data) {
                System.out.println("UpData 节点数据：" + new String(this.nodeCache.getCurrentData().getData()));
            } else {
                System.out.println("DeleteDate 节点被删除!");
            }
        });
        this.nodeCache.start();
        getCountDownLatch().countDown();
        return this.nodeCache;
    }
}
