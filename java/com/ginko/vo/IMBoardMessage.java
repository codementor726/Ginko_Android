package com.ginko.vo;

import java.util.ArrayList;
import java.util.List;


public class IMBoardMessage{
	private int board_id;
	private List<ImMessageVO> messages = new ArrayList<ImMessageVO>();
	private String writing;
	private String users;

	public int getBoard_id() {
		return board_id;
	}

	public void setBoard_id(int board_id) {
		this.board_id = board_id;
	}

	public List<ImMessageVO> getMessages() {
		return messages;
	}

	public void setMessages(List<ImMessageVO> messages) {
		this.messages = messages;
	}

	public String getWriting() {
		return writing;
	}

	public void setWriting(String writing) {
		this.writing = writing;
	}

	public String getUsers() {
		return users;
	}

	public void setUsers(String users) {
		this.users = users;
	}

	@Override
	public boolean equals(Object ibm) {
		if (ibm == null || !(ibm instanceof IMBoardMessage)) {
			return false;
		}
		return this.getBoard_id() == ((IMBoardMessage) ibm).getBoard_id();
	}

}
