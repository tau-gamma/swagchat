package net.omu.productions;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Set;

public class Nachricht implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 8521357420887412484L;
	/**
	 * Verschiedene statuscode bedeutungen:
	 * 0: Textnachricht an alle
	 * 1: Textnachricht an bestimmte Person
	 * 2: Anmelden
	 * 3: Abmelden
	 * 4: Liste aller User die gerade online sind (Anfrage vom Client)
	 * 5: Liste aller User die gerade online sind (Antwort vom Server)
	 * 6: Username existiert bereits
	 * 7: Nachricht vom Server
	 */
	private int statuscode;
	private String empfaenger;
	private String sender;
	private String inhalt;
	private LocalDateTime sendedatum;
	private String[] usernameList;
	
	public Nachricht(int statuscode, String empfaenger, String sender,
			String inhalt, LocalDateTime sendedatum) {
		super();
		this.statuscode = statuscode;
		this.empfaenger = empfaenger;
		this.sender = sender;
		this.inhalt = inhalt;
		this.sendedatum = sendedatum;
	}
	
	public Nachricht(int statuscode, String empfaenger, String sender, Set<String> names) {
		this(statuscode, empfaenger, sender, sender, LocalDateTime.now());
		this.usernameList = setToArray(names);
	}
	
	private String[] setToArray(Set<String> s){
		String[] ret = new String[s.size()];
		int i = 0;
		for (String string : s) {
			ret[i] = string;
			i++;
		}
		return ret;
	}


	public int getStatuscode() {
		return statuscode;
	}
	public void setStatuscode(int statuscode) {
		this.statuscode = statuscode;
	}
	public String getEmpfaenger() {
		return empfaenger;
	}
	public void setEmpfaenger(String empfaenger) {
		this.empfaenger = empfaenger;
	}
	public String getSender() {
		return sender;
	}
	public void setSender(String sender) {
		this.sender = sender;
	}
	public String getInhalt() {
		return inhalt;
	}
	public void setInhalt(String inhalt) {
		this.inhalt = inhalt;
	}
	public LocalDateTime getSendedatum() {
		return sendedatum;
	}
	public void setSendedatum(LocalDateTime sendedatum) {
		this.sendedatum = sendedatum;
	}
	
	public String[] getUsernameList() {
		return usernameList;
	}

	public void setUsernameList(String[] usernameList) {
		this.usernameList = usernameList;
	}

	@Override
	public String toString() {
		return "Nachricht [statuscode=" + statuscode + ", empfaenger=" + empfaenger + ", sender=" + sender + ", inhalt="
				+ inhalt + ", sendedatum=" + sendedatum + ", usernameList=" + usernameList + "]";
	}	
	
}
