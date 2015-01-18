package tw.com.orangice.sf.lib.ldap;

import java.util.Hashtable;
import java.util.List;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.OperationNotSupportedException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.Control;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import javax.naming.ldap.StartTlsRequest;
import javax.naming.ldap.StartTlsResponse;
import javax.naming.ldap.PagedResultsControl;
import javax.naming.ldap.PagedResultsResponseControl;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import tw.com.orangice.sf.lib.ldap.model.GroupModel;
import tw.com.orangice.sf.lib.ldap.model.OUModel;
import tw.com.orangice.sf.lib.ldap.model.UserModel;
import static javax.naming.directory.SearchControls.SUBTREE_SCOPE;

public class ActiveDirectory {

	private static String[] userAttributes = { "distinguishedName", "cn",
			"name", "uid", "sn", "givenname", "memberOf", "samaccountname",
			"userPrincipalName" };

	public ActiveDirectory() {
	}

	public UserModel authentication(String ldapHost, String username,
			String password) throws Exception {
		System.out.println("LDAP:authentication:" + ldapHost + ":" + username
				+ ":" + password);
		LdapContext ldapContext = ActiveDirectory.getConnection(username,
				password, ldapHost);
		System.out.println("LDAP:authentication2:" + ldapContext.toString());
		UserModel user = ActiveDirectory.getUser(username, ldapContext);
		System.out.println("LDAP:authentication3:" + user.toString());
		return user;
	}

	// **************************************************************************
	// ** getConnection
	// *************************************************************************/
	/**
	 * Used to authenticate a user given a username/password. Domain name is
	 * derived from the fully qualified domain name of the host machine.
	 */
	public static LdapContext getConnection(String username, String password)
			throws NamingException {
		return getConnection(username, password, null, null);
	}

	// **************************************************************************
	// ** getConnection
	// *************************************************************************/
	/**
	 * Used to authenticate a user given a username/password and domain name.
	 */
	public static LdapContext getConnection(String username, String password,
			String domainName) throws NamingException {
		return getConnection(username, password, domainName, null);
	}

	// **************************************************************************
	// ** getConnection
	// *************************************************************************/
	/**
	 * Used to authenticate a user given a username/password and domain name.
	 * Provides an option to identify a specific a Active Directory server.
	 */
	public static LdapContext getConnection(String username, String password,
			String domainName, String serverName) throws NamingException {

		if (domainName == null) {
			//如果domain為null,用本機端
			try {
				String fqdn = java.net.InetAddress.getLocalHost()
						.getCanonicalHostName();
				if (fqdn.split("\\.").length > 1)
					domainName = fqdn.substring(fqdn.indexOf(".") + 1);
			} catch (java.net.UnknownHostException e) {
				e.printStackTrace();
			}
		}

		// System.out.println("Authenticating " + username + "@" + domainName +
		// " through " + serverName);

		if (password != null) {
			password = password.trim();
			if (password.length() == 0)
				password = null;
		}

		// bind by using the specified username/password
		Hashtable props = new Hashtable();
		// String principalName = username + "@" + domainName;
		if (username.contains("@")) {

			System.out.println("contain @");
			username = username.substring(0, username.indexOf("@"));
			domainName = username
					.substring(username.indexOf("@") + 1);

			System.out.println("getUser username:" + username);
			System.out.println("getUser domainName:" + domainName);

		} else if (username.contains("\\")) {
			System.out.println("contain \\");
			domainName = username.substring(0,
					username.indexOf("\\"));
			username = username
					.substring(username.indexOf("\\") + 1);
			System.out.println("getUser username:" + username);
			System.out.println("getUser domainName:" + domainName);
		} 
		String principalName = username;
		props.put(Context.SECURITY_PRINCIPAL, principalName);
		if (password != null)
			props.put(Context.SECURITY_CREDENTIALS, password);

		String ldapURL = "ldap://"
				+ ((serverName == null) ? domainName : serverName + "."
						+ domainName) + '/';
		props.put(Context.INITIAL_CONTEXT_FACTORY,
				"com.sun.jndi.ldap.LdapCtxFactory");
		props.put(Context.PROVIDER_URL, ldapURL);
		try {
			return new InitialLdapContext(props, null);
		} catch (javax.naming.CommunicationException e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
			throw new NamingException("Failed to connect to " + domainName
					+ ((serverName == null) ? "" : " through " + serverName));
		} catch (NamingException e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
			throw new NamingException("Failed to authenticate " + username
					+ "@" + domainName
					+ ((serverName == null) ? "" : " through " + serverName));
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
			throw new NamingException("Failed to authenticate "
					+ username
					+ "@"
					+ domainName
					+ ((serverName == null) ? "" : " through " + serverName
							+ " unknown"));
		}
	}

	// **************************************************************************
	// ** getUser
	// *************************************************************************/
	/**
	 * Used to check whether a username is valid.
	 * 
	 * @param username
	 *            A username to validate (e.g. "peter", "peter@acme.com", or
	 *            "ACME\peter").
	 */
	public static UserModel getUser(String fullUsername, LdapContext context) {

		try {
			String domainName = null;
			String username = null;
			if (fullUsername.contains("@")) {

				System.out.println("contain @");
				username = fullUsername.substring(0, fullUsername.indexOf("@"));
				domainName = fullUsername
						.substring(fullUsername.indexOf("@") + 1);

				System.out.println("getUser username:" + username);
				System.out.println("getUser domainName:" + domainName);

			} else if (fullUsername.contains("\\")) {
				System.out.println("contain \\");
				domainName = fullUsername.substring(0,
						fullUsername.indexOf("\\"));
				username = fullUsername
						.substring(fullUsername.indexOf("\\") + 1);
				System.out.println("getUser username:" + username);
				System.out.println("getUser domainName:" + domainName);
			} else {

				String authenticatedUser = (String) context.getEnvironment()
						.get(Context.SECURITY_PRINCIPAL);
				System.out.println("contain nothing:" + authenticatedUser);
				if (authenticatedUser.contains("@")) {
					username = authenticatedUser.substring(0,
							authenticatedUser.indexOf("@"));
					domainName = authenticatedUser.substring(authenticatedUser
							.indexOf("@") + 1);
				}

			}
			System.out.println("username:" + username);
			System.out.println("domainName:" + domainName);
			if (domainName != null) {
				String principalName = username + "@" + domainName;
				System.out.println("getUser:" + principalName);
				SearchControls controls = new SearchControls();
				controls.setSearchScope(SUBTREE_SCOPE);
				// controls.setReturningAttributes(userAttributes);
				NamingEnumeration<SearchResult> answer = context.search(
						toDC(domainName), "(& (userPrincipalName="
								+ principalName + ")(objectClass=user))",
						controls);
				if (answer.hasMore()) {
					Attributes attr = answer.next().getAttributes();
					System.out.println(attr);
					Attribute user = attr.get("userPrincipalName");
					if (user != null)
						return new UserModel(attr);
				}
			} else {
				String principalName = username;
				SearchControls controls = new SearchControls();
				controls.setSearchScope(SUBTREE_SCOPE);
				// controls.setReturningAttributes(userAttributes);
				NamingEnumeration<SearchResult> answer = context.search(
						toDC(domainName), "(& (userPrincipalName="
								+ principalName + ")(objectClass=user))",
						controls);
				if (answer.hasMore()) {
					Attributes attr = answer.next().getAttributes();
					System.out.println(attr);
					Attribute user = attr.get("userPrincipalName");
					if (user != null)
						return new UserModel(attr);
				}
			}
		} catch (NamingException e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
		}
		return null;
	}

	public SearchResult findAccountByAccountName(DirContext ctx,
			String ldapSearchBase, String accountName) throws NamingException {

		String searchFilter = "(&(objectClass=user)(sAMAccountName="
				+ accountName + "))";

		SearchControls searchControls = new SearchControls();
		searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);

		NamingEnumeration<SearchResult> results = ctx.search(ldapSearchBase,
				searchFilter, searchControls);

		SearchResult searchResult = null;
		if (results.hasMoreElements()) {
			searchResult = (SearchResult) results.nextElement();

			// make sure there is not another item available, there should be
			// only 1 match
			if (results.hasMoreElements()) {
				System.err
						.println("Matched multiple users for the accountName: "
								+ accountName);
				return null;
			}
		}

		return searchResult;
	}

	public String findGroupBySID(DirContext ctx, String ldapSearchBase,
			String sid) throws NamingException {

		String searchFilter = "(&(objectClass=group)(objectSid=" + sid + "))";

		SearchControls searchControls = new SearchControls();
		searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);

		NamingEnumeration<SearchResult> results = ctx.search(ldapSearchBase,
				searchFilter, searchControls);

		if (results.hasMoreElements()) {
			SearchResult searchResult = (SearchResult) results.nextElement();

			// make sure there is not another item available, there should be
			// only 1 match
			if (results.hasMoreElements()) {
				System.err
						.println("Matched multiple groups for the group with SID: "
								+ sid);
				return null;
			} else {
				return (String) searchResult.getAttributes()
						.get("sAMAccountName").get();
			}
		}
		return null;
	}

	public String getPrimaryGroupSID(SearchResult srLdapUser)
			throws NamingException {
		byte[] objectSID = (byte[]) srLdapUser.getAttributes().get("objectSid")
				.get();
		String strPrimaryGroupID = (String) srLdapUser.getAttributes()
				.get("primaryGroupID").get();

		String strObjectSid = decodeSID(objectSID);

		return strObjectSid.substring(0, strObjectSid.lastIndexOf('-') + 1)
				+ strPrimaryGroupID;
	}

	/**
	 * The binary data is in the form: byte[0] - revision level byte[1] - count
	 * of sub-authorities byte[2-7] - 48 bit authority (big-endian) and then
	 * count x 32 bit sub authorities (little-endian)
	 * 
	 * The String value is: S-Revision-Authority-SubAuthority[n]...
	 * 
	 * Based on code from here -
	 * http://forums.oracle.com/forums/thread.jspa?threadID=1155740&tstart=0
	 */
	public static String decodeSID(byte[] sid) {

		final StringBuilder strSid = new StringBuilder("S-");

		// get version
		final int revision = sid[0];
		strSid.append(Integer.toString(revision));

		// next byte is the count of sub-authorities
		final int countSubAuths = sid[1] & 0xFF;

		// get the authority
		long authority = 0;
		// String rid = "";
		for (int i = 2; i <= 7; i++) {
			authority |= ((long) sid[i]) << (8 * (5 - (i - 2)));
		}
		strSid.append("-");
		strSid.append(Long.toHexString(authority));

		// iterate all the sub-auths
		int offset = 8;
		int size = 4; // 4 bytes for each sub auth
		for (int j = 0; j < countSubAuths; j++) {
			long subAuthority = 0;
			for (int k = 0; k < size; k++) {
				subAuthority |= (long) (sid[offset + k] & 0xFF) << (8 * k);
			}

			strSid.append("-");
			strSid.append(subAuthority);

			offset += size;
		}

		return strSid.toString();
	}

	public static byte[] searchLDAP(long companyId, LdapContext ldapContext,
			byte[] cookie, int maxResults, String baseDN, String filter,
			String[] attributeIds, List<SearchResult> searchResults)
			throws Exception {

		SearchControls searchControls = new SearchControls(
				SearchControls.SUBTREE_SCOPE, maxResults, 0, attributeIds,
				false, false);

		NamingEnumeration<SearchResult> enu = null;

		try {
			if (cookie != null) {
				if (cookie.length == 0) {
					ldapContext
							.setRequestControls(new Control[] { new PagedResultsControl(
									100, Control.CRITICAL) });
				} else {
					// PropsValues.LDAP_PAGE_SIZE
					ldapContext
							.setRequestControls(new Control[] { new PagedResultsControl(
									100, cookie, Control.CRITICAL) });
				}

				enu = ldapContext.search(baseDN, filter, searchControls);

				while (enu.hasMoreElements()) {
					searchResults.add(enu.nextElement());
				}

				return _getCookie(ldapContext.getResponseControls());
			}
		} catch (OperationNotSupportedException onse) {
			if (enu != null) {
				enu.close();
			}

			ldapContext.setRequestControls(null);

			enu = ldapContext.search(baseDN, filter, searchControls);

			while (enu.hasMoreElements()) {
				searchResults.add(enu.nextElement());
			}
		} finally {
			if (enu != null) {
				enu.close();
			}

			ldapContext.setRequestControls(null);
		}

		return null;
	}

	private static byte[] _getCookie(Control[] controls) {
		if (controls == null) {
			return null;
		}

		for (Control control : controls) {
			if (control instanceof PagedResultsResponseControl) {
				PagedResultsResponseControl pagedResultsResponseControl = (PagedResultsResponseControl) control;

				return pagedResultsResponseControl.getCookie();
			}
		}

		return null;
	}

	// **************************************************************************
	// ** getUsers
	// *************************************************************************/
	/**
	 * Returns a list of users in the domain.
	 */
	public static UserModel[] getUsers(LdapContext context)
			throws NamingException {

		java.util.ArrayList<UserModel> users = new java.util.ArrayList<UserModel>();
		String authenticatedUser = (String) context.getEnvironment().get(
				Context.SECURITY_PRINCIPAL);
		System.out.println(authenticatedUser);
		if (authenticatedUser.contains("@")) {
			String domainName = authenticatedUser.substring(authenticatedUser
					.indexOf("@") + 1);
			SearchControls controls = new SearchControls();
			controls.setSearchScope(SUBTREE_SCOPE);
			// controls.setReturningAttributes(userAttributes);
			NamingEnumeration answer = context.search(toDC(domainName),
					"(objectClass=user)", controls);
			try {
				while (answer.hasMore()) {
					Attributes attr = ((SearchResult) answer.next())
							.getAttributes();
					Attribute user = attr.get("userPrincipalName");
					System.out.println(attr);
					if (user != null) {
						users.add(new UserModel(attr));
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return users.toArray(new UserModel[users.size()]);
	}

	public static OUModel[] getOUs(LdapContext context) throws NamingException {
		java.util.ArrayList<OUModel> ous = new java.util.ArrayList<OUModel>();
		String authenticatedUser = (String) context.getEnvironment().get(
				Context.SECURITY_PRINCIPAL);
		System.out.println(authenticatedUser);
		if (authenticatedUser.contains("@")) {
			String domainName = authenticatedUser.substring(authenticatedUser
					.indexOf("@") + 1);
			SearchControls controls = new SearchControls();
			controls.setSearchScope(SUBTREE_SCOPE);
			// controls.setReturningAttributes(groupAttributes);
			NamingEnumeration answer = context.search(toDC(domainName),
					"(objectClass=organizationalUnit)", controls);
			try {
				while (answer.hasMore()) {
					Attributes attr = ((SearchResult) answer.next())
							.getAttributes();
					OUModel ou = new OUModel();

					// Attribute user = attr.get("userPrincipalName");
					System.out.println(attr);
					System.out.println(attr.get("samaccounttype").get());
					// if (user != null) {
					// users.add(new UserModel(attr));
					// }
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return ous.toArray(new OUModel[ous.size()]);
	}

	public static GroupModel[] getGroups(LdapContext context)
			throws NamingException {

		java.util.ArrayList<GroupModel> groups = new java.util.ArrayList<GroupModel>();
		String authenticatedUser = (String) context.getEnvironment().get(
				Context.SECURITY_PRINCIPAL);
		System.out.println(authenticatedUser);
		if (authenticatedUser.contains("@")) {
			String domainName = authenticatedUser.substring(authenticatedUser
					.indexOf("@") + 1);
			SearchControls controls = new SearchControls();
			controls.setSearchScope(SUBTREE_SCOPE);
			// controls.setReturningAttributes(groupAttributes);
			NamingEnumeration answer = context.search(toDC(domainName),
					"(objectClass=group)", controls);
			try {
				while (answer.hasMore()) {
					Attributes attr = ((SearchResult) answer.next())
							.getAttributes();
					GroupModel group = new GroupModel();
					group.setsAMAccountName((String) attr.get("samaccountname")
							.get());
					group.setsAMAccountType((String) attr.get("samaccounttype")
							.get());
					group.setObjectGuid((String) attr.get("objectguid").get());
					group.setName((String) attr.get("name").get());
					group.setObjectSid((String) attr.get("objectsid").get());
					group.setDn((String) attr.get("distinguishedname").get());

					groups.add(group);
					// Attribute user = attr.get("userPrincipalName");
					System.out.println(attr);
					System.out.println(attr.get("samaccounttype").get());
					// if (user != null) {
					// users.add(new UserModel(attr));
					// }
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return groups.toArray(new GroupModel[groups.size()]);
	}

	private static String toDC(String domainName) {
		StringBuilder buf = new StringBuilder();
		for (String token : domainName.split("\\.")) {
			if (token.length() == 0)
				continue; // defensive check
			if (buf.length() > 0)
				buf.append(",");
			buf.append("DC=").append(token);
		}
		return buf.toString();
	}

	// **************************************************************************
	// ** User Class
	// *************************************************************************/

}