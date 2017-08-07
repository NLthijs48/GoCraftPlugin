package me.wiefferink.gocraft.management.distribution.operations;

import me.wiefferink.gocraft.management.distribution.ServerUpdate;

public abstract class UpdateOperation {
	public abstract void execute(ServerUpdate serverUpdate);
}
