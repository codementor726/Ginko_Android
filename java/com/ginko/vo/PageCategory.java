package com.ginko.vo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.sz.util.json.Alias;


public class PageCategory implements Serializable {
	private static final long serialVersionUID = 1L;
	private int id;
	private String name;
	private Integer parentId;
	
	private List<PageCategory> children;

    public PageCategory()
    {}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Alias("parent_id")
	public Integer getParentId() {
		return parentId;
	}

	public void setParentId(Integer parentId) {
		this.parentId = parentId;
	}


	public List<PageCategory> getChildren() {
		if (children == null){
			children=new ArrayList<PageCategory>();
		}
		return children;
	}


	public void setChildren(List<PageCategory> children) {
		this.children = children;
	}


	public boolean isRoot() {
		return this.getParentId()==null;
	}
	
	@Override
	public String toString(){
		return this.name;
	}
}
