/**
 * 
 */
package com.yuncore.bdsync.files;

import java.util.Set;

/**
 * @author ouyangfeng
 *
 */
public interface FileExclude {
	
	public void addExclude(String file);
	
	public void addExclude(Set<String> files);

	public Set<String> getExcludes();
	
	public boolean rmExclude(String exclude);
	
}
