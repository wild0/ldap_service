package tw.com.orangice.sf.lib.ldap;

import javax.naming.NamingException;

public class launcher {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			System.out.println("launcher");
			//ActiveDirectoryInitiate initiate = new ActiveDirectoryInitiate("adtest@cosa.iii.org.tw", "qwER123!", "140.92.25.120");
			//ActiveDirectoryInitiate initiate = new ActiveDirectoryInitiate("paperless", "P@ssw0rd", "keb200.com.tw");
			ActiveDirectoryInitiate initiate = new ActiveDirectoryInitiate("aaa@paperless.com.tw", "P@ssw0rd", "192.168.2.22");
		} catch (NamingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println(e.getMessage());
		}
		
	}

}
