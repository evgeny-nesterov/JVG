package ru.nest.jvg.ani;

public interface AniTask {
	boolean isStart(long time);

	boolean isDoAction(long time);

	boolean isEnd(long time);

	boolean isFinish(long time);

	void doAction(long time, boolean isStart, boolean isEnd);
}
