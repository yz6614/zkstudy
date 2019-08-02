package curator.watcher;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.junit.After;
import org.junit.Before;

import java.util.concurrent.CountDownLatch;

public class BaseWather {
    public CuratorFramework client;
    public final String CLUSTERINFO = "168.2.4.57:2181";
    public final String ROOTPATH = "/zyl_watcher";
    public CountDownLatch countDownLatch;
    public static RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);

    @Before
    public void initial() throws InterruptedException {
        this.client = CuratorFrameworkFactory.builder()
                .connectString(CLUSTERINFO)
                .sessionTimeoutMs(5000)
                .connectionTimeoutMs(5000)
                .retryPolicy(retryPolicy)
                .build();
        this.client.start();
        this.countDownLatch = new CountDownLatch(1);
        System.out.println("\n\n ===========  初始化完成  ===========");
    }

    @After
    public void FinalClose() {
        this.client.close();
    }

    public String getROOTPATH() {
        return ROOTPATH;
    }

    public CountDownLatch getCountDownLatch() {
        return countDownLatch;
    }

    public void setClient(CuratorFramework client) {
        this.client = client;
    }


    public void setCountDownLatch(CountDownLatch countDownLatch) {
        this.countDownLatch = countDownLatch;
    }
}
