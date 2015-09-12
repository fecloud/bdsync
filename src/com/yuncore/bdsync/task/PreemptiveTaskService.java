package com.yuncore.bdsync.task;

import java.util.ArrayList;

import com.yuncore.bdsync.task.TaskContainer.ContainerListener;
import com.yuncore.bdsync.task.TaskStatus.OnStatusChange;

public abstract class PreemptiveTaskService implements ContainerListener, OnStatusChange {

	protected TaskContainer taskContainer;

	protected TaskStatus taskStatus;

	protected int threads = 1;

	protected ArrayList<TaskExecute> taskExecutes;

	public PreemptiveTaskService() {
		taskContainer = new TaskContainer();
		taskContainer.setListener(this);
		taskStatus = new TaskStatus();
		taskStatus.setOnStatusChange(this);

		taskExecutes = new ArrayList<TaskExecute>(threads);
	}

	protected synchronized void waitTaskFinish() {
		try {
			wait();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public synchronized void onTaskAdd(Task dir) {

		// 线程还没有满
		if (taskExecutes.isEmpty() || taskExecutes.size() < threads) {
			final TaskExecute e = newTaskExecute();
			e.setId(taskExecutes.size());
			e.setStatus(1);
			final Thread thread = new Thread(e);
			thread.setName(getTaskExecuteName() + e.getId());
			taskExecutes.add(e);
			thread.start();
		} else {
			// 查找空闲的线程

			for (TaskExecute e : taskExecutes) {
				if (e.getStatus() != 1) {
					e.setStatus(1);
					final Thread thread = new Thread(e);
					thread.setName(getTaskExecuteName() + e.getId());
					thread.start();
				}
			}
		}

	}

	protected abstract TaskExecute newTaskExecute();

	protected abstract String getTaskExecuteName();

	@Override
	public synchronized void onSame(int taskSize, int status) {
		if (taskSize <= threads && status == 0) {
			notifyAll();
		}
	}

}
