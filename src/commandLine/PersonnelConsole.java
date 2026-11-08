package commandLine;

import personnel.*;
import commandLineMenus.*;
import static commandLineMenus.rendering.examples.util.InOut.*;

public class PersonnelConsole
{
	private GestionPersonnel gestionPersonnel;
	LigueConsole ligueConsole;
	EmployeConsole employeConsole;
	private Employe utilisateur;
	
	public PersonnelConsole(GestionPersonnel gestionPersonnel)
	{
		this.gestionPersonnel = gestionPersonnel;
		this.employeConsole = new EmployeConsole();
		this.ligueConsole = new LigueConsole(gestionPersonnel, employeConsole);
	}
	
	public void start()
	{
		menuPrincipal().start();
	}
	
	/**
	 * Le menu dépend du profil de l'utilisateur connecté.
	 */
	
	private Menu menuPrincipal()
	{
		Menu menu = new Menu("Gestion du personnel des ligues");
		if (utilisateur.estRoot())
		{
			// super-administrateur : tous les droits
			menu.add(employeConsole.editerEmploye(utilisateur));
			menu.add(ligueConsole.menuLigues());
		}
		else if (utilisateur.estAdmin(utilisateur.getLigue()))
		{
			// administrateur : uniquement sa ligue
			menu.add(employeConsole.editerEmploye(utilisateur));
			menu.add(ligueConsole.menuAdministrateur(utilisateur.getLigue()));
		}
		else
		{
			// employé simple : annuaire en lecture seule
			menu.add(ligueConsole.menuConsultation());
		}
		menu.add(menuQuitter());
		return menu;
	}

	private Menu menuQuitter()
	{
		Menu menu = new Menu("Quitter", "q");
		menu.add(quitterEtEnregistrer());
		menu.add(quitterSansEnregistrer());
		menu.addBack("r");
		return menu;
	}
	
	private Option quitterEtEnregistrer()
	{
		return new Option("Quitter et enregistrer", "q", 
				() -> 
				{
					try
					{
						gestionPersonnel.sauvegarder();
						Action.QUIT.optionSelected();
					} 
					catch (SauvegardeImpossible e)
					{
						System.out.println("Impossible d'effectuer la sauvegarde");
					}
				}
			);
	}
	
	private Option quitterSansEnregistrer()
	{
		return new Option("Quitter sans enregistrer", "a", Action.QUIT);
	}
	
	/**
	 * Demande le mail et le password, retrouve l'employé correspondant.
	 * Le mail du super-administrateur est "root".
	 */
	
	private boolean connexion()
	{
		String mail = getString("mail : ");
		String password = getString("password : ");
		Employe employe = gestionPersonnel.getEmploye(mail);
		if (employe == null || !employe.checkPassword(password))
		{
			System.out.println("Mail ou password incorrect.");
			return false;
		}
		utilisateur = employe;
		return true;
	}
	
	public static void main(String[] args)
	{
		PersonnelConsole personnelConsole = 
				new PersonnelConsole(GestionPersonnel.getGestionPersonnel());
		if (personnelConsole.connexion())
			personnelConsole.start();
	}
}