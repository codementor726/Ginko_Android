package com.ginko.vo;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.sz.util.json.Alias;

public class GreyContactVO {

	@Alias("contact_id")
	private Integer greyContactId;

	@Alias("type")
	private Integer contactType;

	@Alias("first_name")
	private String firstName;

	@Alias("middle_name")
	private String middleName;

	@Alias("last_name")
	private String lastName;
	private String email;

	// means already send the exchange request, but wasn't accepted.
	@Alias("is_pending")
	private boolean pending;

	@Alias("photo_url")
	private String photoUrl;

	@Alias("photo_name")
	private String photoName;

	private String notes = "";

	@Alias("is_read")
	private boolean read;

	private List<String> phones;

	private List<String> emails;

	@Alias("contact_type")
	private int greyContactType;

	@Alias("fields")
	private Set<GreyContactFieldVO> fields = new HashSet<GreyContactFieldVO>();

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public Integer getGreyContactId() {
		return this.greyContactId;
	}

	public void setGreyContactId(Integer greyContactId) {
		this.greyContactId = greyContactId;
	}

	public Integer getContactType() {
		return this.contactType;
	}

	public void setContactType(Integer contactType) {
		this.contactType = contactType;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public Set<GreyContactFieldVO> getOriginalFields() {
		return this.fields;
	}

	public String getPhotoUrl() {
		return photoUrl;
	}

	public void setPhotoUrl(String photoUrl) {
		this.photoUrl = photoUrl;
	}

	public void setFields(Set<GreyContactFieldVO> fields) {
		this.fields = fields;
	}

	public String getPhotoName() {
		return photoName;
	}

	public void setPhotoName(String photoName) {
		this.photoName = photoName;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public String getMiddleName() {
		return middleName;
	}

	public void setMiddleName(String middleName) {
		this.middleName = middleName;
	}

	public boolean isPending() {
		return pending;
	}

	public void setPending(boolean pending) {
		this.pending = pending;
	}

	public boolean isRead() {
		return read;
	}

	public void setRead(boolean read) {
		this.read = read;
	}

	public List<String> getPhones() {
		return phones;
	}

	public void setPhones(List<String> phones) {
		this.phones = phones;
	}

	public List<String> getEmails() {
		return emails;
	}

	public int getGreyContactType() {
		return greyContactType;
	}

	public void setGreyContactType(int greyContactType) {
		this.greyContactType = greyContactType;
	}

	public Set<GreyContactFieldVO> getFields() {
		return fields;
	}

	public void setEmails(List<String> emails) {
		this.emails = emails;
	}

}
