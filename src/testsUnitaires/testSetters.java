package testsUnitaires;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import personnel.*;

class testSetters 
{
	GestionPersonnel gestionPersonnel = GestionPersonnel.getGestionPersonnel();
	Ligue ligue;
	Employe employe;

	@BeforeEach
	void init() throws SauvegardeImpossible
	{
		// nom unique pour ne pas mélanger les ligues entre deux tests
		ligue = gestionPersonnel.addLigue("Ligue" + System.nanoTime());
		employe = ligue.addEmploye("Bouchard", "Gérard", "g.bouchard@gmail.com", "azerty");
	}

	// ---------- Setters de Employe ----------

	@Test
	void setNom()
	{
		employe.setNom("Dupont");
		assertEquals("Dupont", employe.getNom());
	}

	@Test
	void setPrenom()
	{
		employe.setPrenom("Paul");
		assertEquals("Paul", employe.getPrenom());
	}

	@Test
	void setMail()
	{
		employe.setMail("paul@mail.fr");
		assertEquals("paul@mail.fr", employe.getMail());
	}

	@Test
	void setPassword()
	{
		employe.setPassword("nouveau");
		assertTrue(employe.checkPassword("nouveau"));
		assertFalse(employe.checkPassword("azerty"));
	}

	// ---------- Setters de Ligue ----------

	@Test
	void setNomLigue()
	{
		ligue.setNom("Billard" + System.nanoTime());
		assertTrue(ligue.getNom().startsWith("Billard"));
	}

	@Test
	void setAdministrateur()
	{
		ligue.setAdministrateur(employe);
		assertSame(employe, ligue.getAdministrateur());
		assertTrue(employe.estAdmin(ligue));
	}

	// ---------- Changement d'administrateur ----------

	@Test
	void ancienAdministrateurPerdSesDroits()
	{
		Employe autre = ligue.addEmploye("Martin", "Luc", "luc@mail.fr", "pwd");
		ligue.setAdministrateur(employe);
		ligue.setAdministrateur(autre);
		assertFalse(employe.estAdmin(ligue));
		assertTrue(autre.estAdmin(ligue));
	}

	@Test
	void administrateurDUneAutreLigueRefuse() throws SauvegardeImpossible
	{
		Ligue autreLigue = gestionPersonnel.addLigue("Autre" + System.nanoTime());
		Employe etranger = autreLigue.addEmploye("Durand", "Marie", "marie@mail.fr", "x");
		assertThrows(DroitsInsuffisants.class, () -> ligue.setAdministrateur(etranger));
	}

	@Test
	void rootPeutEtreAdministrateur()
	{
		ligue.setAdministrateur(gestionPersonnel.getRoot());
		assertTrue(ligue.getAdministrateur().estRoot());
	}

	// ---------- Suppressions ----------

	@Test
	void supprimerEmploye()
	{
		employe.remove();
		assertFalse(ligue.getEmployes().contains(employe));
	}

	@Test
	void supprimerAdministrateurRedonneLeRoot()
	{
		ligue.setAdministrateur(employe);
		employe.remove();
		assertTrue(ligue.getAdministrateur().estRoot());
		assertFalse(ligue.getEmployes().contains(employe));
	}

	@Test
	void supprimerLeRootImpossible()
	{
		assertThrows(ImpossibleDeSupprimerRoot.class, () -> gestionPersonnel.getRoot().remove());
	}

	@Test
	void supprimerLigue()
	{
		ligue.remove();
		assertFalse(gestionPersonnel.getLigues().contains(ligue));
	}
}