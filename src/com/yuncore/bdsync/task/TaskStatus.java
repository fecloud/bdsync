package com.yuncore.bdsync.task;

import java.util.Hashtable;
import java.util.Set;
import java.util.Map.Entry;

public class TaskStatus {

	private Hashtable<TaskExecute, Integer> statusSet = new Hashtable<TaskExecute, Integer>();

	private OnStatusChange onStatusChange;

	public synchronized void setStatus(TaskExecute t, int status) {
		statusSet.put(t, status);
		if (onStatusChange != null) {
			checkAllStatus(status);
		}
	}

	/**
	 * 检查所有任务状态是否都一致
	 * 
	 * @param status
	 */
	private synchronized void checkAllStatus(int status) {
		boolean allfinish = true;
		Set<Entry<TaskExecute, Integer>> entrySet = statusSet.entrySet();
		for (Entry<TaskExecute, Integer> entry : entrySet) {
			if (entry.getValue() != status) {
				allfinish = false;
				break;
			}
		}
		if (allfinish) {
			onStatusChange.onSame(statusSet.size(), status);
		}
	}

	public synchronized Integer getStatus(TaskExecute taskExecute) {
		return statusSet.get(taskExecute);
	}

	public synchronized Hashtable<TaskExecute, Integer> getStatusSet() {
		return statusSet;
	}

	public synchronized void setStatusSet(
			Hashtable<TaskExecute, Integer> statusSet) {
		this.statusSet = statusSet;
	}

	public synchronized OnStatusChange getOnStatusChange() {
		return onStatusChange;
	}

	public synchronized void setOnStatusChange(OnStatusChange onStatusChange) {
		this.onStatusChange = onStatusChange;
	}

	/**
	 * 任务状态监听
	 * 
	 * @author ouyangfeng
	 *
	 */
	public interface OnStatusChange {

		/**
		 * 当所有任务状态都一致时
		 * 
		 * @param status
		 */
		void onSame(int taskSize, int status);

	}

}
