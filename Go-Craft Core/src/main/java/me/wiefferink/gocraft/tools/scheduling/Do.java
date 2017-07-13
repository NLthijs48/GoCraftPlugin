package me.wiefferink.gocraft.tools.scheduling;

import me.wiefferink.gocraft.GoCraft;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.Collection;

public class Do {

	// No access
	private Do() {}

	/**
	 * Run a task on the main server thread.
	 * @param runnable The BukkitRunnable to run
	 * @return BukkitTask which can be used to cancel the operation
	 */
	public static BukkitTask sync(Run runnable) {
		return new BukkitRunnable() {
			@Override
			public void run() {
				runnable.run();
			}
		}.runTask(GoCraft.getInstance());
	}

	/**
	 * Run a task on the main server thread.
	 * @param runnable The BukkitRunnable to run
	 * @param delay Ticks to wait before running the task
	 * @return BukkitTask which can be used to cancel the operation
	 */
	public static BukkitTask syncLater(long delay, Run runnable) {
		return new BukkitRunnable() {
			@Override
			public void run() {
				runnable.run();
			}
		}.runTaskLater(GoCraft.getInstance(), delay);
	}

	/**
	 * Run a task on an asynchronous thread.
	 * @param runnable The BukkitRunnable to run

	 * @return BukkitTask which can be used to cancel the operation*/
	public static BukkitTask async(Run runnable) {
		return new BukkitRunnable() {
			@Override
			public void run() {
				runnable.run();
			}
		}.runTaskAsynchronously(GoCraft.getInstance());
	}

	/**
	 * Run a task on an asynchronous thread.
	 * @param runnable The BukkitRunnable to run
	 * @param delay    Ticks to wait before running the task
	 * @return BukkitTask which can be used to cancel the operation
	 */
	public static BukkitTask asyncLater(long delay, Run runnable) {
		return new BukkitRunnable() {
			@Override
			public void run() {
				runnable.run();
			}
		}.runTaskLaterAsynchronously(GoCraft.getInstance(), delay);
	}

	/**
	 * Run a timer task on the main server thread.
	 * @param runnable The BukkitRunnable to run
	 * @param period Time between task runs
	 * @return BukkitTask which can be used to cancel the operation
	 */
	public static BukkitTask syncTimer(long period, Run runnable) {
		return new BukkitRunnable() {
			@Override
			public void run() {
				runnable.run();
			}
		}.runTaskTimer(GoCraft.getInstance(), 0, period);
	}

	/**
	 * Run a timer task on the main server thread.
	 * @param runnable The BukkitRunnable to run
	 * @param period Time between task runs
	 * @return BukkitTask which can be used to cancel the operation
	 */
	public static BukkitTask syncTimer(long period, RunResult<Boolean> runnable) {
		return new BukkitRunnable() {
			@Override
			public void run() {
				if(!runnable.run()) {
					this.cancel();
				}
			}
		}.runTaskTimer(GoCraft.getInstance(), 0, period);
	}

	/**
	 * Run a timer task on an asynchronous thread.
	 * @param runnable The BukkitRunnable to run
	 * @param period Time between task runs
	 * @return BukkitTask which can be used to cancel the operation
	 */
	public static BukkitTask asyncTimer(long period, Run runnable) {
		return new BukkitRunnable() {
			@Override
			public void run() {
				runnable.run();
			}
		}.runTaskTimerAsynchronously(GoCraft.getInstance(), 0, period);
	}

	/**
	 * Run a timer task on an asynchronous thread.
	 * @param runnable The BukkitRunnable to run
	 * @param period Time between task runs
	 * @return BukkitTask which can be used to cancel the operation
	 */
	public static BukkitTask asyncTimer(long period, RunResult<Boolean> runnable) {
		return new BukkitRunnable() {
			@Override
			public void run() {
				if(!runnable.run()) {
					this.cancel();
				}
			}
		}.runTaskTimerAsynchronously(GoCraft.getInstance(), 0, period);
	}

	/**
	 * Perform an action for each given object in a separate tick
	 * @param objects     Objects to process
	 * @param runArgument Function to execute for each object
	 * @param <T>         Type of object to process
	 * @return BukkitTask which can be used to cancel the operation
	 */
	public static <T> BukkitTask forAll(Collection<T> objects, RunArgument<T> runArgument) {
		return forAll(1, objects, runArgument);
	}

	/**
	 * Perform an action for each given object spread over time
	 * @param perTick Number of objects to process per tick
	 * @param objects Objects to process
	 * @param runArgument Function to execute for each object
	 * @param <T> Type of object to process
	 * @return BukkitTask which can be used to cancel the operation
	 */
	public static <T> BukkitTask forAll(int perTick, Collection<T> objects, RunArgument<T> runArgument) {
		final ArrayList<T> finalObjects = new ArrayList<>(objects);
		return new BukkitRunnable() {
			private int current = 0;

			@Override
			public void run() {
				for(int i = 0; i < perTick; i++) {
					if(current < finalObjects.size()) {
						runArgument.run(finalObjects.get(current));
						current++;
					}
				}
				if(current >= finalObjects.size()) {
					this.cancel();
				}
			}
		}.runTaskTimer(GoCraft.getInstance(), 1, 1);
	}

}
