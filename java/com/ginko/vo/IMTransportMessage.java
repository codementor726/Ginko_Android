package com.ginko.vo;

import java.util.Date;

import com.sz.util.json.Alias;

public class IMTransportMessage {
	@Alias( "board_id")
	private Integer boardId;

	private String content;

	@Alias( "msg_id")
	private int msgId;

	@Alias( "send_from")
	private Integer from;

	@Alias( "send_time")
	private Date sendTime;

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public int getMsgId() {
		return msgId;
	}

	public void setMsgId(int msgId) {
		this.msgId = msgId;
	}

	public Integer getFrom() {
		return from;
	}

	public void setFrom(Integer from) {
		this.from = from;
	}

	public Date getSendTime() {
		return sendTime;
	}

	public void setSendTime(Date sendTime) {
		this.sendTime = sendTime;
	}

	public Integer getBoardId() {
		return boardId;
	}

	public void setBoardId(Integer boardId) {
		this.boardId = boardId;
	}

}
