import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.BackgroundCallback;
import org.apache.curator.framework.api.CuratorEvent;
import org.apache.curator.framework.recipes.cache.*;
import org.apache.curator.framework.recipes.leader.LeaderLatch;
import org.apache.curator.framework.recipes.leader.LeaderLatchListener;

import org.apache.curator.framework.recipes.locks.*;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.shaded.com.google.common.collect.Lists;
import org.apache.curator.utils.CloseableUtils;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;


public class CuratorCUIDTest {

    private static CuratorFramework getClient(){
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);

        String connectionInfo = "168.2.4.56:2181,168.2.4.57:2181,168.2.4.58:2181" ;
        CuratorFramework client =
                CuratorFrameworkFactory.builder()
                        .connectString(connectionInfo)
                        .sessionTimeoutMs(5000)
                        .connectionTimeoutMs(5000)
                        .retryPolicy(retryPolicy)
                        .namespace("wls")
                        .build();
        client.start();
        return client;
    }

    /**
     * 创建数据节点*/
    @Test
    public void createZnodeWithCuratorTest() throws Exception {
        CuratorFramework client = getClient();
        //创建一个空数据内容的节点
//		client.create().forPath("/path_ep");

        //创建一个数据内容为"init_string"的节点
//		client.create().forPath("/path_init","init_string".getBytes());

        //创建一个空数据内容临时节点
//		client.create().withMode(CreateMode.EPHEMERAL).forPath("/path_tmp_ep");
        //创建临时节点需要用下面这句，否则无法看到节点创建
//		Thread.sleep(10000);

//		//创建一个数据内容为"init"的临时节点
//		client.create().withMode(CreateMode.EPHEMERAL).forPath("/path_tmp_init","init".getBytes());
//		Thread.sleep(10000);

//		//创建一个数据内容为"init"的临时节点，并且自动递归创建父节点
//		//这个creatingParentContainersIfNeeded()接口非常有用，
//		//因为一般情况开发人员在创建一个子节点必须判断它的父节点是否存在，如果不存在直接创建会抛出NoNodeException
//		//使用creatingParentContainersIfNeeded()之后Curator能够自动递归创建所有所需的父节点。
        client.create()
                .creatingParentContainersIfNeeded()
                .withMode(CreateMode.EPHEMERAL)
                .forPath("/zyl-test1","init".getBytes());
        Thread.sleep(5000);

        System.out.println("Successfully created a node.");
    }

    /**
     * 删除数据节点*/
    @Test
    public void deleteZnodeWithCuratorTest() throws Exception {
        CuratorFramework client = getClient();
        //删除一个节点,注意，此方法只能删除叶子节点，否则会抛出异常。
		  client.delete().forPath("/path_ep");

//      //删除一个节点，并且递归删除其所有的子节点
//        client.delete().deletingChildrenIfNeeded().forPath("/path_pn");

//      //删除一个节点，强制指定版本进行删除
//        client.delete().withVersion(0).forPath("/path_version");

//      //删除一个节点，强制保证删除
//        client.delete().guaranteed().forPath("/path_g");
//      //guaranteed()接口是一个保障措施，只要客户端会话有效，那么Curator会在后台持续进行删除操作，直到删除节点成功。
//      //注意：上面的多个流式接口是可以自由组合的，例如：
//        client.delete().guaranteed().deletingChildrenIfNeeded().withVersion(10086).forPath("path");

        System.out.println("Successfully deleted a node.");
    }

    /**
     * 读取数据节点数据*/
    @Test
    public void getDataWithCuratorTest() throws Exception {
        CuratorFramework client = getClient();
//        读取一个节点的数据内容,注意，此方法返的返回值是byte[ ];
        byte[] bytes = client.getData().forPath("/path_init");

//        读取一个节点的数据内容，同时获取到该节点的stat
//        Stat stat = new Stat();
//        byte[] bytes = client.getData().storingStatIn(stat).forPath("/path_init");
//        System.out.println("The status of the node: " + stat);
//
        //Convert to String
        String s = new String(bytes);
        System.out.println("Successfully acquired the node's data: " + s);
    }

    /**
     * 更新数据节点数据*/
    @Test
    public void updateDataWithCuratorTest() throws Exception {
        CuratorFramework client = getClient();
//        更新一个节点的数据内容，注意：该接口会返回一个Stat实例
        Stat stat = client.setData().forPath("/path_init","data1".getBytes());

//        更新一个节点的数据内容，强制指定版本进行更新
//      Stat stat = client.setData().withVersion(0).forPath("path","data".getBytes());

        System.out.println("The status of the node: " + stat);
        System.out.println("Successfully updated the node's data.");
    }

    /**
     * 检查数据节点是否存在*/
    @Test
    public void checkZnodeExistsWithCuratorTest() throws Exception {
        CuratorFramework client = getClient();
//        检查数据节点是否存在，注意：该方法返回一个Stat实例，用于检查ZNode是否存在的操作. 可以调用额外的方法(监控或者后台处理)并在最后调用forPath()指定要操作的ZNode
        Stat stat = client.checkExists().forPath("/path_init");
        System.out.println("Check the Znode existed or not. The status of the node: " + stat);
    }

    /**
     * 获取某个节点的所有子节点路径*/
    @Test
    public void getChildrenPathWithCuratorTest() throws Exception {
        CuratorFramework client = getClient();
//        获取某个节点的所有子节点路径，注意：该方法的返回值为List,获得ZNode的子节点Path列表。 可以调用额外的方法(监控、后台处理或者获取状态watch, background or get stat) 并在最后调用forPath()指定要操作的父ZNode
        List list = client.getChildren().forPath("/");
        System.out.println("Get children Znodes's path: " + list);
    }

    /**
     * 事务*/
    @Test
    public void inTransactionWithCuratorTest() throws Exception {
        CuratorFramework client = getClient();
//        CuratorFramework的实例包含inTransaction( )接口方法，调用此方法开启一个ZooKeeper事务.
//        可以复合create, setData, check, and/or delete 等操作然后调用commit()作为一个原子操作提交。一个例子如下：
        client.inTransaction()
                .check().forPath("/path_init")
                .and()
                .create().withMode(CreateMode.EPHEMERAL).forPath("/path_init/path","data".getBytes())
                .and()
                .setData().withVersion(0).forPath("/path_init/path","data2".getBytes())
                .and()
                .commit();
        System.out.println("Start a transaction. ");
        Thread.sleep(10000);
    }

    /**
     * 异步接口*/
    @Test
    public void asynchronousInterfaceWithCuratorTest() throws Exception {
        CuratorFramework client = getClient();
        String path = "/zk-book";
        final CountDownLatch semaphore = new CountDownLatch(2);
        ExecutorService tp = Executors.newFixedThreadPool(2);

        System.out.println("Main thread: " + Thread.currentThread().getName());
        // 此处传入了自定义的Executor
        client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL)
                .inBackground((client1, event) -> {
                    System.out.println("event[code: " + event.getResultCode() + ", type: " + event.getType() + "]");
                    System.out.println("Thread of processResult: " + Thread.currentThread().getName());
                    semaphore.countDown();
                }, tp)
                .forPath(path, "init".getBytes());
        // 此处没有传入自定义的Executor
        client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL)
                .inBackground((client12, event) -> {
                    System.out.println("event[code: " + event.getResultCode() + ", type: " + event.getType() + "]");
                    System.out.println("Thread of processResult: " + Thread.currentThread().getName());
                    semaphore.countDown();
                })
                .forPath(path, "init".getBytes());
        semaphore.await();
        tp.shutdown();
        Thread.sleep(10000);
    }



    /**
     * 缓存 pathCacheTest
     * 注意：如果new PathChildrenCache(client, PATH, true)中的参数cacheData值设置为false，
     * 则示例中的event.getData().getData()、data.getData()将返回null，cache将不会缓存节点数据。
     *
     * 注意：示例中的Thread.sleep(10)可以注释掉，但是注释后事件监听的触发次数会不全，
     * 这可能与PathCache的实现原理有关，不能太过频繁的触发事件！*/
    @Test
    public void pathCacheTest() throws Exception {
        String PATH = "/wls_curator_test/pathCache";
        //设置/更新、移除其实是使用client (CuratorFramework)来操作, 不通过PathChildrenCache操作
		CuratorFramework client = getClient();
		//想使用cache，必须调用它的start方法，使用完后调用close方法。 可以设置StartMode来实现启动的模式
        //StartMode有下面几种：
        //NORMAL：正常初始化。
        //BUILD_INITIAL_CACHE：在调用start()之前会调用rebuild()。
        //POST_INITIALIZED_EVENT： 当Cache初始化数据后发送一个PathChildrenCacheEvent.Type#INITIALIZED事件
        PathChildrenCache cache = new PathChildrenCache(client, PATH, true);
        cache.start();
        PathChildrenCacheListener cacheListener = (client1, event)-> {
            System.out.println("事件类型：" + event.getType());
            if (null != event.getData()) {
                System.out.println("节点数据：" + event.getData().getPath() + " = " + new String(event.getData().getData()));
            }
        };
        //可以增加listener监听缓存的变化
        cache.getListenable().addListener(cacheListener);
        client.create().creatingParentsIfNeeded().forPath("/wls_curator_test/pathCache/test01", "01".getBytes());
        Thread.sleep(10000);
        client.create().creatingParentsIfNeeded().forPath("/wls_curator_test/pathCache/test02", "02".getBytes());
        Thread.sleep(10000);
        client.setData().forPath("/wls_curator_test/pathCache/test01", "01_V2".getBytes());
        Thread.sleep(10000);
        //getCurrentData()方法返回一个List<ChildData>对象，可以遍历所有的子节点
        for (ChildData data : cache.getCurrentData()) {
            System.out.println("getCurrentData:" + data.getPath() + " = " + new String(data.getData()));
        }
        client.delete().forPath("/wls_curator_test/pathCache/test01");
        Thread.sleep(10);
        client.delete().forPath("/wls_curator_test/pathCache/test02");
        Thread.sleep(1000 * 5);
        cache.close();
        client.close();
        System.out.println("OK!");
    }

    /**
     * 缓存 nodeCacheTest
     * Node Cache与Path Cache类似，Node Cache只是监听某一个特定的节点。它涉及到下面的三个类：
     * 1、NodeCache - Node Cache实现类
     * 2、NodeCacheListener - 节点监听器
     * 3、ChildData - 节点数据
     * 注意：NodeCache只能监听一个节点的状态变化*/
    @Test
    public void nodeCacheTest() throws Exception {
        String PATH = "/wls_curator_test/nodeCache";
        CuratorFramework client = getClient();
        client.create().creatingParentsIfNeeded().forPath(PATH);
        final NodeCache cache = new NodeCache(client, PATH);
        NodeCacheListener listener = () -> {
            //getCurrentData()将得到节点当前的状态，通过它的状态可以得到当前的值
            ChildData data = cache.getCurrentData();
            if (null != data) {
                System.out.println("节点数据：" + new String(cache.getCurrentData().getData()));
            } else {
                System.out.println("节点被删除!");
            }
        };
        cache.getListenable().addListener(listener);
        //注意：使用cache，依然要调用它的start()方法，使用完后调用close()方法。
        cache.start();
        client.setData().forPath(PATH, "01".getBytes());
        Thread.sleep(100);
        client.setData().forPath(PATH, "02".getBytes());
        Thread.sleep(100);
        client.delete().deletingChildrenIfNeeded().forPath(PATH);
        Thread.sleep(1000 * 2);
        cache.close();
        client.close();
        System.out.println("OK!");
    }

    /**
     * 缓存 treeCacheTest
     * Tree Cache可以监控整个树上的所有节点，类似于PathCache和NodeCache的组合，主要涉及到下面四个类：
     * 1、TreeCache - Tree Cache实现类
     * 2、TreeCacheListener - 监听器类
     * 3、TreeCacheEvent - 触发的事件类
     * 4、ChildData - 节点数据
     * 注意：在此示例中没有使用Thread.sleep(10)，但是事件触发次数也是正常的.
     * 注意：TreeCache在初始化(调用start()方法)的时候会回调TreeCacheListener实例一个事TreeCacheEvent，
     * 而回调的TreeCacheEvent对象的Type为INITIALIZED，ChildData为null，此时event.getData().getPath()很有可能导致空指针异常，
     * 这里应该主动处理并避免这种情况。*/
    @Test
    public void treeCacheTest() throws Exception {
        String PATH = "/wls_curator_test/treeCache";
        CuratorFramework client = getClient();
        client.create().creatingParentsIfNeeded().forPath(PATH);
        TreeCache cache = new TreeCache(client, PATH);
        TreeCacheListener listener = (client1, event) ->
                System.out.println("事件类型：" + event.getType() +
                        " | 路径：" + (null != event.getData() ? event.getData().getPath() : null));
        cache.getListenable().addListener(listener);
        cache.start();
        client.setData().forPath(PATH, "01".getBytes());
        Thread.sleep(100);
        client.setData().forPath(PATH, "02".getBytes());
        Thread.sleep(100);
        client.delete().deletingChildrenIfNeeded().forPath(PATH);
        Thread.sleep(1000 * 2);
        cache.close();
        client.close();
        System.out.println("OK!");
    }

    /**
     * leader选举
     * Curator 有两种leader选举的recipe,分别是LeaderLatch和LeaderSelector
     * 前者是一旦选举出Leader，除非有客户端挂掉重新触发选举，否则不会交出领导权。
     * 后者是所有存活的客户端不间断的轮流做Leader，大同社会。
     * 对比可知，LeaderLatch必须调用close()方法才会释放领导权，而对于LeaderSelector，通过LeaderSelectorListener可以对领导权进行控制，在适当的时候释放领导权，这样每个节点都有可能获得领导权。
     * 从而，LeaderSelector具有更好的灵活性和可控性，建议有LeaderElection应用场景下优先使用LeaderSelector。
     *
     * leaderLatchTest
     * 异常处理： LeaderLatch实例可以增加ConnectionStateListener来监听网络连接问题。
     *    当 SUSPENDED 或 LOST 时, leader不再认为自己还是leader。当LOST后连接重连后RECONNECTED,LeaderLatch会删除先前的ZNode然后重新创建一个。
     *    LeaderLatch用户必须考虑导致leadership丢失的连接问题。
     *    强烈推荐使用ConnectionStateListener。
     *
     * 首先我们创建了10个LeaderLatch，启动后它们中的一个会被选举为leader。
     * 因为选举会花费一些时间，start后并不能马上就得到leader。
     * 通过hasLeadership查看自己是否是leader， 如果是的话返回true。
     * 可以通过.getLeader().getId()可以得到当前的leader的ID。
     * 只能通过close释放当前的领导权。
     * await是一个阻塞方法， 尝试获取leader地位，但是未必能上位*/
    @Test
    public void leaderLatchTest() throws Exception {
        String PATH = "/wls_curator_test/leaderLatch";
        final int CLIENT_QTY = 10;
        List<CuratorFramework> clients = Lists.newArrayList();
        List<LeaderLatch> examples = Lists.newArrayList();

        RetryPolicy retryPolicy = new ExponentialBackoffRetry(20000, 3);
        String connectionInfo = "168.2.4.56:2181,168.2.4.57:2181,168.2.4.58:2181" ;
        try {
            for (int i = 0; i < CLIENT_QTY; i++) {

                CuratorFramework client =
                        CuratorFrameworkFactory.builder()
                                .connectString(connectionInfo)
                                .sessionTimeoutMs(5000)
                                .connectionTimeoutMs(5000)
                                .retryPolicy(retryPolicy)
                                .namespace("wls_curator_test")
                                .build();
                clients.add(client);
                LeaderLatch latch = new LeaderLatch(client, PATH, "Client #" + i);
                latch.addListener(new LeaderLatchListener() {
                    @Override
                    public void isLeader() {
                        System.out.println("I am Leader");
                    }
                    @Override
                    public void notLeader() {
                        System.out.println("I am not Leader");
                    }
                });
                examples.add(latch);
                client.start();
                //一旦启动，LeaderLatch会和其它使用相同latch path的其它LeaderLatch交涉，然后其中一个最终会被选举为leader，可以通过hasLeadership方法查看LeaderLatch实例是否leader
                latch.start();
            }
            Thread.sleep(10000);
            LeaderLatch currentLeader = null;
            for (LeaderLatch latch : examples) {
                //leaderLatch.hasLeadership( ); //返回true说明当前实例是leader
                //类似JDK的CountDownLatch， LeaderLatch在请求成为leadership会block(阻塞)，一旦不使用LeaderLatch了，必须调用close方法。 如果它是leader,会释放leadership， 其它的参与者将会选举一个leader。
                if (latch.hasLeadership()) {
                    currentLeader = latch;
                }
            }
            System.out.println("current leader is " + currentLeader.getId());
            System.out.println("release the leader " + currentLeader.getId());
            currentLeader.close();

            Thread.sleep(5000);

            for (LeaderLatch latch : examples) {
                if (latch.hasLeadership()) {
                    currentLeader = latch;
                }
            }
            System.out.println("current leader is " + currentLeader.getId());
            System.out.println("release the leader " + currentLeader.getId());
            //currentLeader.close();
        } finally {
            for (LeaderLatch latch : examples) {
                if (null != latch.getState())
                    CloseableUtils.closeQuietly(latch);
            }
            for (CuratorFramework client : clients) {
                CloseableUtils.closeQuietly(client);
            }
        }
    }

    /**
     * leader选举
     * Curator 有两种leader选举的recipe,分别是LeaderSelector和LeaderLatch
     * 前者是所有存活的客户端不间断的轮流做Leader，大同社会。后者是一旦选举出Leader，除非有客户端挂掉重新触发选举，否则不会交出领导权。
     * leaderSelectorTest
     * LeaderSelector使用的时候主要涉及下面几个类：
     * 1、LeaderSelector
     * 2、LeaderSelectorListener
     * 3、LeaderSelectorListenerAdapter
     * 4、CancelLeadershipException
     * 类似LeaderLatch,LeaderSelector必须start: leaderSelector.start();
     * 一旦启动，当实例取得领导权时你的listener的takeLeadership()方法被调用。
     * 而takeLeadership()方法只有领导权被释放时才返回。 当你不再使用LeaderSelector实例时，应该调用它的close方法。
     *
     * 异常处理 LeaderSelectorListener类继承ConnectionStateListener。
     * LeaderSelector必须小心连接状态的改变。如果实例成为leader, 它应该响应SUSPENDED 或 LOST。
     * 当 SUSPENDED 状态出现时， 实例必须假定在重新连接成功之前它可能不再是leader了。
     * 如果LOST状态出现， 实例不再是leader， takeLeadership方法返回。
     *
     * 重要: 推荐处理方式是当收到SUSPENDED 或 LOST时抛出CancelLeadershipException异常。
     * 这会导致LeaderSelector实例中断并取消执行takeLeadership方法的异常。
     * 这非常重要，你必须考虑扩展LeaderSelectorListenerAdapter.LeaderSelectorListenerAdapter提供了推荐的处理逻辑。
     * */
    @Test
    public void leaderSelectorTest() throws Exception {
        String PATH = "/wls_curator_test/leaderSelector";
        final int CLIENT_QTY = 10;
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(20000, 3);
        String connectionInfo = "168.2.4.56:2181,168.2.4.57:2181,168.2.4.58:2181" ;

        List<CuratorFramework> clients = Lists.newArrayList();
        List<LeaderSelectorAdapter> examples = Lists.newArrayList();
        try {
            for (int i = 0; i < CLIENT_QTY; i++) {
                CuratorFramework client =
                        CuratorFrameworkFactory.builder()
                                .connectString(connectionInfo)
                                .sessionTimeoutMs(5000)
                                .connectionTimeoutMs(5000)
                                .retryPolicy(retryPolicy)
                                .namespace("wls_curator_test")
                                .build();
                clients.add(client);
                LeaderSelectorAdapter selectorAdapter = new LeaderSelectorAdapter(client, PATH, "Client #" + i);
                examples.add(selectorAdapter);
                client.start();
                selectorAdapter.start();
            }
            System.out.println("Press enter/return to quit\n");
            new BufferedReader(new InputStreamReader(System.in)).readLine();
        } finally {
            System.out.println("Shutting down...");
            for (LeaderSelectorAdapter exampleClient : examples) {
                CloseableUtils.closeQuietly(exampleClient);
            }
            for (CuratorFramework client : clients) {
                CloseableUtils.closeQuietly(client);
            }
            //CloseableUtils.closeQuietly(client);
        }
    }


    /**
     * 分布式锁 InterProcessMutexTest
     * 提醒：
     * 1.推荐使用ConnectionStateListener监控连接的状态，因为当连接LOST时你不再拥有锁
     * 2.分布式的锁全局同步， 这意味着任何一个时间点不会有两个客户端都拥有相同的锁。
     *
     * 可重入共享锁—Shared Reentrant Lock
     * Shared意味着锁是全局可见的， 客户端都可以请求锁。
     * Reentrant和JDK的ReentrantLock类似，即可重入，意味着同一个客户端在拥有锁的同时，可以多次获取，不会被阻塞。
     * 它是由类InterProcessMutex来实现。
     *
     * 生成5个client， 每个client重复执行10次 请求锁–访问资源–释放锁的过程。每个client都在独立的线程中。 结果可以看到，锁是随机的被每个实例排他性的使用。
     * 既然是可重用的，你可以在一个线程中多次调用acquire(),在线程拥有锁时它总是返回true。
     * 你不应该在多个线程中用同一个InterProcessMutex， 你可以在每个线程中都生成一个新的InterProcessMutex实例，它们的path都一样，这样它们可以共享同一个锁。*/
    @Test
    public void InterProcessMutexTest() throws Exception {
        String PATH = "/wls_curator_test/locks";
        final int QTY = 5;
        final int REPETITIONS = QTY * 10;
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(20000, 3);
        String connectionInfo = "168.2.4.56:2181,168.2.4.57:2181,168.2.4.58:2181" ;

        final FakeLimitedResource resource = new FakeLimitedResource();
        ExecutorService service = Executors.newFixedThreadPool(QTY);

        try {
            for (int i = 0; i < QTY; ++i) {
                final int index = i;
                Callable<Void> task = new Callable<Void>() {
                    @Override
                    public Void call() throws Exception {
                        CuratorFramework client =
                                CuratorFrameworkFactory.builder()
                                        .connectString(connectionInfo)
                                        .sessionTimeoutMs(5000)
                                        .connectionTimeoutMs(5000)
                                        .retryPolicy(retryPolicy)
                                        .namespace("wls_curator_test")
                                        .build();
                        try {
                            client.start();
                            final InterProcessMutexDemo example = new InterProcessMutexDemo(client, PATH, resource, "Client " + index);
                            for (int j = 0; j < REPETITIONS; ++j) {
                                example.doWork(10, TimeUnit.SECONDS);
                                System.out.println("******Thread "+ index +" is working. Repetitions:" + j );
                            }
                            System.out.println("========Thread "+ index +" is done.");
                        } catch (Throwable e) {
                            e.printStackTrace();
                        } finally {
                            CloseableUtils.closeQuietly(client);
                        }
                        return null;
                    }
                };
                service.submit(task);
            }
            service.shutdown();
            service.awaitTermination(10, TimeUnit.MINUTES);
        } finally {
            //CloseableUtils.closeQuietly(server);
        }
    }

    /**
     * 分布式锁 InterProcessSemaphoreMutexTest
     * 不可重入共享锁—Shared Lock
     * 这个锁和上面的InterProcessMutex相比，就是少了Reentrant的功能，也就意味着它不能在同一个线程中重入。
     * 这个类是InterProcessSemaphoreMutex,使用方法和InterProcessMutex类似
     * 运行后发现，有且只有一个client成功获取第一个锁(第一个acquire()方法返回true)，
     * 然后它自己阻塞在第二个acquire()方法，
     * 获取第二个锁超时；其他所有的客户端都阻塞在第一个acquire()方法超时并且抛出异常。
     * 这样也就验证了InterProcessSemaphoreMutex实现的锁是不可重入的。*/
    @Test
    public void InterProcessSemaphoreMutexTest() throws Exception {
        String PATH = "/wls_curator_test/locks";
        final int QTY = 5;
        final int REPETITIONS = QTY * 10;
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(20000, 3);
        String connectionInfo = "168.2.4.56:2181,168.2.4.57:2181,168.2.4.58:2181" ;

        final FakeLimitedResource resource = new FakeLimitedResource();
        ExecutorService service = Executors.newFixedThreadPool(QTY);
        try {
            for (int i = 0; i < QTY; ++i) {
                final int index = i;
                Callable<Void> task = new Callable<Void>() {
                    @Override
                    public Void call() throws Exception {
                        CuratorFramework client =
                                CuratorFrameworkFactory.builder()
                                        .connectString(connectionInfo)
                                        .sessionTimeoutMs(5000)
                                        .connectionTimeoutMs(5000)
                                        .retryPolicy(retryPolicy)
                                        .namespace("wls_curator_test")
                                        .build();
                        try {
                            client.start();
                            final InterProcessSemaphoreMutexDemo example = new InterProcessSemaphoreMutexDemo(client, PATH, resource, "Client " + index);
                            for (int j = 0; j < REPETITIONS; ++j) {
                                example.doWork(10, TimeUnit.SECONDS);
                                System.out.println("******Thread "+ index +" is working. Repetitions:" + j );
                            }
                            System.out.println("=====Thread "+ index +" is done.");
                        } catch (Throwable e) {
                            e.printStackTrace();
                        } finally {
                            CloseableUtils.closeQuietly(client);
                        }
                        return null;
                    }
                };
                service.submit(task);
            }
            service.shutdown();
            service.awaitTermination(10, TimeUnit.MINUTES);
        } finally {
//			CloseableUtils.closeQuietly(server);
        }
        Thread.sleep(Integer.MAX_VALUE);
    }

    /**
     * 分布式锁 ReentrantReadWriteLockTest
     * 可重入读写锁—Shared Reentrant Read Write Lock
     * */
    @Test
    public void ReentrantReadWriteLockTest() throws Exception {
        String PATH = "/wls_curator_test/locks";
        final int QTY = 5;
        final int REPETITIONS = QTY ;
        final FakeLimitedResource resource = new FakeLimitedResource();
        ExecutorService service = Executors.newFixedThreadPool(QTY);
//		final TestingServer server = new TestingServer();
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(20000, 3);
        String connectionInfo = "168.2.4.56:2181,168.2.4.57:2181,168.2.4.58:2181" ;
        try {
            for (int i = 0; i < QTY; ++i) {
                final int index = i;
                Callable<Void> task = new Callable<Void>() {
                    @Override
                    public Void call() throws Exception {
                        CuratorFramework client =
                                CuratorFrameworkFactory.builder()
                                        .connectString(connectionInfo)
                                        .sessionTimeoutMs(5000)
                                        .connectionTimeoutMs(5000)
                                        .retryPolicy(retryPolicy)
                                        .namespace("wls_curator_test")
                                        .build();
                        try {
                            client.start();
                            final ReentrantReadWriteLockDemo example = new ReentrantReadWriteLockDemo(client, PATH, resource, "Client " + index);
                            for (int j = 0; j < REPETITIONS; ++j) {
                                example.doWork(10, TimeUnit.SECONDS);
                                System.out.println("******Thread "+ index +" is working. Repetitions:" + j );
                            }
                            System.out.println("=====Thread "+ index +" is done.");
                        } catch (Throwable e) {
                            e.printStackTrace();
                        } finally {
                            CloseableUtils.closeQuietly(client);
                        }
                        return null;
                    }
                };
                service.submit(task);
            }
            service.shutdown();
            service.awaitTermination(10, TimeUnit.MINUTES);
        } finally {
//			CloseableUtils.closeQuietly(server);
            System.out.println("=*=*=*=*=All done.");
        }
    }

    /**
     * 分布式锁 InterProcessSemaphoreTest
     * 信号量—Shared Semaphore
     * 首先我们先获得了5个租约， 最后我们把它还给了semaphore。 接着请求了一个租约，因为semaphore还有5个租约，所以请求可以满足，返回一个租约，还剩4个租约。 然后再请求5个租约，因为租约不够，阻塞到超时，还是没能满足，返回结果为null(租约不足会阻塞到超时，然后返回null，不会主动抛出异常；如果不设置超时时间，会一致阻塞)。
     * 上面说讲的锁都是公平锁(fair)。 总ZooKeeper的角度看， 每个客户端都按照请求的顺序获得锁，不存在非公平的抢占的情况。
     * */
    @Test
    public void InterProcessSemaphoreTest() throws Exception {
        String PATH = "/wls_curator_test/locks";
        final int MAX_LEASE = 10;

        FakeLimitedResource resource = new FakeLimitedResource();
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(20000, 3);
        String connectionInfo = "168.2.4.56:2181,168.2.4.57:2181,168.2.4.58:2181" ;
        CuratorFramework client =
                CuratorFrameworkFactory.builder()
                        .connectString(connectionInfo)
                        .sessionTimeoutMs(5000)
                        .connectionTimeoutMs(5000)
                        .retryPolicy(retryPolicy)
                        .namespace("wls_curator_test")
                        .build();
        client.start();
        InterProcessSemaphoreV2 semaphore = new InterProcessSemaphoreV2(client, PATH, MAX_LEASE);
        System.out.println("准备请求5个租约");
        Collection<Lease> leases = semaphore.acquire(5);
        System.out.println("get " + leases.size() + " leases," + leases);
        System.out.println("=准备再请求1个租约");
        Lease lease = semaphore.acquire();
        System.out.println("get another lease," + lease);

        resource.use();
        System.out.println("resource is using.");
        System.out.println("=====准备再请求5个租约");
        Collection<Lease> leases2 = semaphore.acquire(5, 10, TimeUnit.SECONDS);
        System.out.println("Should timeout and acquire return " + leases2);

        System.out.println("return one lease");
        semaphore.returnLease(lease);
        System.out.println("return another 5 leases");
        semaphore.returnAll(leases);
    }

    /**
     * 分布式锁 MultiSharedLockTest
     * 多共享锁对象 —Multi Shared Lock
     * Multi Shared Lock是一个锁的容器。 当调用acquire()， 所有的锁都会被acquire()，如果请求失败，所有的锁都会被release。
     * 同样调用release时所有的锁都被release(失败被忽略)。
     * 基本上，它就是组锁的代表，在它上面的请求释放操作都会传递给它包含的所有的锁。
     * 主要涉及两个类：InterProcessMultiLock和InterProcessLock
     * 新建一个InterProcessMultiLock， 包含一个重入锁和一个非重入锁。 调用acquire()后可以看到线程同时拥有了这两个锁。 调用release()看到这两个锁都被释放了。
     * 最后再重申一次， 强烈推荐使用ConnectionStateListener监控连接的状态，当连接状态为LOST，锁将会丢失。
     * */
    @Test
    public void MultiSharedLockTest() throws Exception {
        String PATH1 = "/wls_curator_test/locks1";
        String PATH2 = "/wls_curator_test/locks2";

        FakeLimitedResource resource = new FakeLimitedResource();
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(20000, 3);
        String connectionInfo = "168.2.4.56:2181,168.2.4.57:2181,168.2.4.58:2181" ;
        CuratorFramework client =
                CuratorFrameworkFactory.builder()
                        .connectString(connectionInfo)
                        .sessionTimeoutMs(5000)
                        .connectionTimeoutMs(5000)
                        .retryPolicy(retryPolicy)
                        .namespace("wls_curator_test")
                        .build();
        client.start();
        InterProcessLock lock1 = new InterProcessMutex(client, PATH1);
        InterProcessLock lock2 = new InterProcessSemaphoreMutex(client, PATH2);

        InterProcessMultiLock lock = new InterProcessMultiLock(Arrays.asList(lock1, lock2));

        if (!lock.acquire(10, TimeUnit.SECONDS)) {
            throw new IllegalStateException("could not acquire the lock");
        }
        System.out.println("has got all lock");

        System.out.println("has got lock1: " + lock1.isAcquiredInThisProcess());
        System.out.println("has got lock2: " + lock2.isAcquiredInThisProcess());

        try {
            resource.use(); //access resource exclusively
        } finally {
            System.out.println("releasing the lock");
            lock.release(); // always release the lock in a finally block
        }
        System.out.println("has got lock1: " + lock1.isAcquiredInThisProcess());
        System.out.println("has got lock2: " + lock2.isAcquiredInThisProcess());

    }


}



