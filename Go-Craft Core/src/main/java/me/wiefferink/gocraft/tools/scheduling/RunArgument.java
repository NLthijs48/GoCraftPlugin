package me.wiefferink.gocraft.tools.scheduling;

public interface RunArgument<T> {
    void run(T argument);
}
