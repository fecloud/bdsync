package com.yuncore.bdsync.task;

import java.util.LinkedList;
import java.util.Queue;

public class TaskContainer {

	private Queue<Task> stacks;

	protected ContainerListener listener;

	public TaskContainer() {
		stacks = new LinkedList<Task>();
	}

	public synchronized Task getTask() {
		if (stacks.isEmpty()) {
			return null;
		}
		return stacks.poll();
	}

	public Task addTask(Task task) {
		synchronized (this) {
			stacks.add(task);
		}
		if (null != listener) {
			listener.onTaskAdd(stacks.peek());
		}
		return task;
	}

	public synchronized ContainerListener getListener() {
		return listener;
	}

	public synchronized void setListener(ContainerListener listener) {
		this.listener = listener;
	}

	public interface ContainerListener {

		void onTaskAdd(Task dir);

	}

}
