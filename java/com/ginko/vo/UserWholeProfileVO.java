package com.ginko.vo;

import com.sz.util.json.Alias;

import org.json.JSONObject;


public class UserWholeProfileVO  extends BaseUserVO{

	private UserUpdateVO profile = new UserUpdateVO();

	private UserUpdateVO home = new UserUpdateVO();

	private UserUpdateVO work = new UserUpdateVO();
	
	//It works, it's a JSONObject
	@Alias( "share")  //Can be a instance of ContactVO or ExchangeRequestVO
	private SharedInfoVO share;

	@Alias( "profile")
	public UserUpdateVO getProfile() {
		return profile;
	}

	public void setProfile(UserUpdateVO profile) {
		this.profile = profile;
	}

	@Alias( "home")
	public UserUpdateVO getHome() {
		return home;
	}

	@Alias("contact_info")
	private ContactUserInfoVo contactUserInfo;

	public void setContactUserInfo(ContactUserInfoVo info){this.contactUserInfo = info;}
	public ContactUserInfoVo getContactUserInfo(){return this.contactUserInfo;}

    @Alias(ignoreGet = true , ignoreSet = true)
    public UserUpdateVO getGroupInfoByGroupType(int groupType)
    {
        switch(groupType)
        {
            case 1://home
                return home;
            case 2:
                return work;
        }
        return null;
    }

	public void setHome(UserUpdateVO home) {
		this.home = home;
        home.setGroupName("home");
	}
	
	@Alias( "work")
	public UserUpdateVO getWork() {
		return work;
	}

	public void setWork(UserUpdateVO work) {
		this.work = work;
        work.setGroupName("work");
	}

	public SharedInfoVO getShare() {
		return share;
	}

	public void setShare(SharedInfoVO share) {
		this.share = share;
	}
	


}
