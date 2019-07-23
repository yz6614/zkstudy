import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.leader.LeaderSelector;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListenerAdapter;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class LeaderSelectorAdapter extends LeaderSelectorListenerAdapter implements Closeable {
	private final String name;
	private final LeaderSelector leaderSelector;
	private final AtomicInteger leaderCount = new AtomicInteger();

	public LeaderSelectorAdapter(CuratorFramework client, String path, String name) {
		this.name = name;
		leaderSelector = new LeaderSelector(client, path, this);
		/**
		 * 自动重新排队
		 * 该方法的调用可以确保此实例在释放领导权后还可能获得领导权
		 */
		leaderSelector.autoRequeue();
	}
	/**
	 * 启动  调用leaderSelector.start()
	 * @throws IOException
	 * 一旦启动，如果获取了leadership的话，takeLeadership()会被调用，只有当leader释放了leadership的时候，takeLeadership()才会返回。
	 */
	public void start() throws IOException {
		leaderSelector.start();
	}

	@Override
	public void close() throws IOException {
		leaderSelector.close();
	}

	/**
	 * 获取领导权之后执行的业务逻辑，执行完自动放弃领导权
	 * @param client
	 * @throws Exception
	 * 可以在takeLeadership进行任务的分配等等，并且不要返回，如果你想要要此实例一直是leader的话可以加一个死循环。
	 * 在这里我们使用AtomicInteger来记录此client获得领导权的次数， 它是”fair”， 每个client有平等的机会获得领导权。
	 */
	@Override
	public void takeLeadership(CuratorFramework client) throws Exception {
		final int waitSeconds = (int) (5 * Math.random()) + 1;
		System.out.println(name + " is now the leader. Waiting " + waitSeconds + " seconds...");
		System.out.println(name + " has been leader " + leaderCount.getAndIncrement() + " time(s) before.");
		try {
			Thread.sleep(TimeUnit.SECONDS.toMillis(waitSeconds));
		} catch (InterruptedException e) {
			System.err.println(name + " was interrupted.");
			Thread.currentThread().interrupt();
		} finally {
			System.out.println(name + " relinquishing leadership.\n");
		}
	}
}
