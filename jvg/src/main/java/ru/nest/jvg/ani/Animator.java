package ru.nest.jvg.ani;

import java.util.ArrayList;
import java.util.List;

public class Animator implements Runnable {
	public Animator() {
	}

	public void addTask(AniTask task) {
		synchronized (this) {
			tasks.add(task);
		}
	}

	public void start() {
		new Thread(this).start();
	}

	private int quantum = 10;

	private List<AniTask> tasks = new ArrayList<AniTask>();

	private List<AniTask> executed = new ArrayList<AniTask>();

	@Override
	public void run() {
		long startTime = System.currentTimeMillis();
		long t = startTime;
		while (!stoped) {
			long time = t - startTime;
			try {
				synchronized (this) {
					if (tasks.size() == 0 && executed.size() == 0) {
						break;
					}

					for (int i = tasks.size() - 1; i >= 0; i--) {
						AniTask task = tasks.get(i);
						if (task.isFinish(time)) {
							tasks.remove(i);
							executed.remove(task);
						}

						if (task.isStart(time)) {
							tasks.remove(i);
							executed.add(task);
						}
					}
				}

				for (int i = executed.size() - 1; i >= 0; i--) {
					AniTask task = executed.get(i);
					boolean isEnd = task.isEnd(time);
					if (isEnd) {
						executed.remove(i);
					}

					if (task.isDoAction(time)) {
						boolean isStart = task.isStart(time);
						task.doAction(time, isStart, isEnd);
					}
				}
			} catch (Exception exc) {
			}

			try {
				while (paused) {
					Thread.currentThread();
					Thread.sleep(quantum);
				}

				long execTime = System.currentTimeMillis() - t;
				Thread.currentThread();
				Thread.sleep(Math.max(quantum - execTime, 1));
			} catch (InterruptedException exc) {
			}

			t = System.currentTimeMillis();
		}
	}

	private boolean stoped = false;

	private boolean paused = false;

	public static void main(String[] args) {
		Animator a = new Animator();
		a.addTask(new AniTask() {
			@Override
			public boolean isStart(long time) {
				return time >= 3000;
			}

			@Override
			public boolean isDoAction(long time) {
				return true;
			}

			@Override
			public boolean isEnd(long time) {
				return time >= 5000;
			}

			@Override
			public boolean isFinish(long time) {
				return time >= 6000;
			}

			@Override
			public void doAction(long time, boolean isStart, boolean isEnd) {
				System.out.println("1 time=" + time + ", start=" + isStart + ", end=" + isEnd);
			}
		});

		a.addTask(new AniTask() {
			@Override
			public boolean isStart(long time) {
				return time >= 3000;
			}

			@Override
			public boolean isDoAction(long time) {
				return true;
			}

			@Override
			public boolean isEnd(long time) {
				return time >= 4000;
			}

			@Override
			public boolean isFinish(long time) {
				return time >= 7000;
			}

			@Override
			public void doAction(long time, boolean isStart, boolean isEnd) {
				System.out.println("2 time=" + time + ", start=" + isStart + ", end=" + isEnd);
			}
		});
		a.start();
	}
}
