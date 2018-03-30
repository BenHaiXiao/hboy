package com.github.hboy.web.bean;




public class UserBean {
	private  boolean isLeaf = false ;  // 是否为叶子节点，即是否为最底层用户 
	
	private String ParentUid  = null ; //父级节点的uid 
	
	private boolean isRoot = false ;  //是否为root用户

	private Long uid;

	public Long getUid() {
		return uid;
	}

	public void setUid(Long uid) {
		this.uid = uid;
	}

	public UserBean(){}

	public boolean isRoot() {
		return isRoot;
	}

	public void setRoot(boolean isRoot) {
		this.isRoot = isRoot;
	}



	public boolean isLeaf() {
		return isLeaf;
	}

	public void setLeaf(boolean isLeaf) {
		this.isLeaf = isLeaf;
	}

	public String getParentUid() {
		return ParentUid;
	}

	public void setParentUid(String parentUid) {
		ParentUid = parentUid;
	}
	
}
