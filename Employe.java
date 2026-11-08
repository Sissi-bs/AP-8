package personnel;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * Employé d'une ligue hébergée par la M2L. Certains peuvent 
 * être administrateurs des employés de leur ligue.
 * Un seul employé, rattaché à aucune ligue, est le root.
 * Il est impossible d'instancier directement un employé, 
 * il faut passer la méthode {@link Ligue#addEmploye addEmploye}.
 */

public class Employe implements Serializable, Comparable<Employe>
{
	private static final long serialVersionUID = 4795721718037994734L;
	private int id = -1;
	private String nom, prenom, password, mail;
	private LocalDate dateArrivee, dateDepart;
	private Ligue ligue;
	private GestionPersonnel gestionPersonnel;
	
	Employe(GestionPersonnel gestionPersonnel, Ligue ligue, String nom, String prenom, String mail, String password)
	{
		this(gestionPersonnel, ligue, nom, prenom, mail, password, null, null);
	}

	Employe(GestionPersonnel gestionPersonnel, Ligue ligue, String nom, String prenom, String mail, String password, 
			LocalDate dateArrivee, LocalDate dateDepart)
	{
		verifierDates(dateArrivee, dateDepart);
		this.gestionPersonnel = gestionPersonnel;
		this.nom = nom;
		this.prenom = prenom;
		this.password = password;
		this.mail = mail;
		this.ligue = ligue;
		this.dateArrivee = dateArrivee;
		this.dateDepart = dateDepart;
	}

	Employe(GestionPersonnel gestionPersonnel, int id, Ligue ligue, String nom, String prenom, String mail, String password)
	{
		this(gestionPersonnel, id, ligue, nom, prenom, mail, password, null, null);
	}

	Employe(GestionPersonnel gestionPersonnel, int id, Ligue ligue, String nom, String prenom, String mail, String password,
			LocalDate dateArrivee, LocalDate dateDepart)
	{
		this(gestionPersonnel, ligue, nom, prenom, mail, password, dateArrivee, dateDepart);
		this.id = id;
	}

	/**
	 * Lève DateIncoherente si la date de départ précède la date d'arrivée.
	 * Une date à null signifie "non renseignée" et n'est jamais incohérente.
	 */

	private static void verifierDates(LocalDate arrivee, LocalDate depart)
	{
		if (arrivee != null && depart != null && depart.isBefore(arrivee))
			throw new DateIncoherente("La date de départ ne peut pas précéder la date d'arrivée.");
	}

	public int getId()
	{
		return id;
	}

	void setId(int id)
	{
		this.id = id;
	}

	public String getPassword()
	{
		return password;
	}

	private void majBase()
	{
		try
		{
			gestionPersonnel.update(this);
		}
		catch (SauvegardeImpossible e)
		{
			e.printStackTrace();
		}
	}
	
	public boolean estAdmin(Ligue ligue)
	{
		return ligue.getAdministrateur() == this;
	}
	
	public boolean estRoot()
	{
		return gestionPersonnel.getRoot() == this;
	}
	
	public String getNom()
	{
		return nom;
	}

	public void setNom(String nom)
	{
		this.nom = nom;
		majBase();
	}

	public String getPrenom()
	{
		return prenom;
	}
	
	public void setPrenom(String prenom)
	{
		this.prenom = prenom;
		majBase();
	}

	public String getMail()
	{
		return mail;
	}
	
	public void setMail(String mail)
	{
		this.mail = mail;
		majBase();
	}

	public boolean checkPassword(String password)
	{
		return this.password.equals(password);
	}

	public void setPassword(String password)
	{
		this.password = password;
		majBase();
	}

	/**
	 * Retourne la date d'arrivée de l'employé (null si non renseignée).
	 */

	public LocalDate getDateArrivee()
	{
		return dateArrivee;
	}

	/**
	 * Change la date d'arrivée. Lève DateIncoherente si elle 
	 * est postérieure à la date de départ.
	 */

	public void setDateArrivee(LocalDate dateArrivee)
	{
		verifierDates(dateArrivee, this.dateDepart);
		this.dateArrivee = dateArrivee;
		majBase();
	}

	/**
	 * Retourne la date de départ de l'employé (null si non renseignée).
	 */

	public LocalDate getDateDepart()
	{
		return dateDepart;
	}

	/**
	 * Change la date de départ. Lève DateIncoherente si elle 
	 * précède la date d'arrivée.
	 */

	public void setDateDepart(LocalDate dateDepart)
	{
		verifierDates(this.dateArrivee, dateDepart);
		this.dateDepart = dateDepart;
		majBase();
	}

	public Ligue getLigue()
	{
		return ligue;
	}

	/**
	 * Supprime l'employé. Si celui-ci est un administrateur, le root
	 * récupère les droits d'administration sur sa ligue.
	 */
	
	public void remove()
	{
		Employe root = gestionPersonnel.getRoot();
		if (this != root)
		{
			if (estAdmin(getLigue()))
				getLigue().setAdministrateur(root);
			try
			{
				gestionPersonnel.delete(this);
			}
			catch (SauvegardeImpossible e)
			{
				e.printStackTrace();
			}
			getLigue().remove(this);
		}
		else
			throw new ImpossibleDeSupprimerRoot();
	}

	@Override
	public int compareTo(Employe autre)
	{
		int cmp = getNom().compareTo(autre.getNom());
		if (cmp != 0)
			return cmp;
		return getPrenom().compareTo(autre.getPrenom());
	}
	
	@Override
	public String toString()
	{
		String res = nom + " " + prenom + " " + mail + " (";
		if (estRoot())
			res += "super-utilisateur";
		else
			res += ligue.toString();
		return res + ")";
	}
}