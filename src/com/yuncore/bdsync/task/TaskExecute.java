package com.yuncore.bdsync.task;

public abstract class TaskExecute implements Runnable {

	protected TaskContainer taskContainer;

	protected TaskStatus taskStatus;

	protected int id;

	public TaskExecute(TaskStatus taskStatus,
			TaskContainer taskContainer) {
		super();
		this.taskStatus = taskStatus;
		this.taskContainer = taskContainer;
	}

	@Override
	public void run() {
		if (taskContainer != null) {
			Task t = null;
			while ((t = taskContainer.getTask()) != null) {
				doTask(t);
			}
		}
		setStatus(0);
	}

	protected abstract void doTask(Task task);

	public synchronized int getId() {
		return id;
	}

	public synchronized void setId(int id) {
		this.id = id;
	}

	public int getStatus() {
		return taskStatus.getStatus(this);
	}

	public void setStatus(int status) {
		taskStatus.setStatus(this, status);
	}

}
