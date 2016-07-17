package me.wiefferink.gocraft.tools;

public interface Callback<T> {
	/**
	 * Called when the process is complete
	 * @param result The result of process
	 */
	void execute(T result);
}
