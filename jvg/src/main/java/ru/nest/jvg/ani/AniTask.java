package ru.nest.jvg.ani;

public interface AniTask {
	public boolean isStart(long time);

	public boolean isDoAction(long time);

	public boolean isEnd(long time);

	public boolean isFinish(long time);

	public void doAction(long time, boolean isStart, boolean isEnd);
}
