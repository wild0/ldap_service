package tw.com.orangice.sf.lib.ldap;

import javax.naming.NamingException;
import javax.naming.ldap.LdapContext;

import tw.com.orangice.sf.lib.ldap.model.ActiveDirectoryModel;

public class ActiveDirectoryInitiate {
	
	static ActiveDirectoryInitiate instance = null;
	
	ActiveDirectoryModel activeDirectory = null;
	public ActiveDirectoryInitiate(String username, String password, String domain) throws NamingException{
		instance = this;
		LdapContext ldapContext = ActiveDirectory.getConnection(username, password, domain);
		//ActiveDirectory activeDirectory = new ActiveDirectory(username, password, domain);
		//ActiveDirectory.getUsers(ldapContext);
		//ActiveDirectory.getGroups(ldapContext);
		//ActiveDirectory.getOUs(ldapContext);
		
		System.out.println("launcher: connection done:"+ldapContext.toString());
		ActiveDirectory.getUser(username,  ldapContext);
		System.out.println("launcher: query done");
	}
	
	public static ActiveDirectoryInitiate getInstance(){
		return instance;
	}
	
}
