package tw.com.orangice.sf.lib.ldap.model;

public class GroupModel extends ADModel{
	String name = "";

	String objectType = "";
	public String getName() {
		return name;
	}
	public void setName(String name) {
		if(name.startsWith("name:")){
			this.name = name.substring("name:".length()).trim();
		}
		else{
			this.name = name;
		}
	}
	public String getObjectType() {
		return objectType;
	}
	public void setObjectType(String objectType) {
		this.objectType = objectType;
	}
	public String getObjectId() {
		return objectId;
	}
	public void setObjectId(String objectId) {
		this.objectId = objectId;
	}
	public String getServerType() {
		return serverType;
	}
	public void setServerType(String serverType) {
		this.serverType = serverType;
	}
	public String getData() {
		return data;
	}
	public void setData(String data) {
		this.data = data;
	}
	public String getAccount() {
		return account;
	}
	public void setAccount(String account) {
		this.account = account;
	}
	public String getsAMAccountName() {
		return sAMAccountName;
	}
	public void setsAMAccountName(String sAMAccountName) {
		this.sAMAccountName = sAMAccountName;
	}
	String objectId = "";
	String serverType = "";
	String data = "";
	String account = "";
	String sAMAccountName = "";
	String sAMAccountType = "";
	String objectGuid = "";
	public String getsAMAccountType() {
		return sAMAccountType;
	}
	public void setsAMAccountType(String sAMAccountType) {
		this.sAMAccountType = sAMAccountType;
	}
	public String getObjectGuid() {
		return objectGuid;
	}
	public void setObjectGuid(String objectGuid) {
		this.objectGuid = objectGuid;
	}
	public String getObjectSid() {
		return objectSid;
	}
	public void setObjectSid(String objectSid) {
		this.objectSid = objectSid;
	}
	String objectSid = "";
	//String distinguishedName = "";

	
}
