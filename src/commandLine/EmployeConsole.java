
package commandLine;

import static commandLineMenus.rendering.examples.util.InOut.getString;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import commandLineMenus.ListOption;
import commandLineMenus.Menu;
import commandLineMenus.Option;

import personnel.Employe;
import personnel.DateIncoherente;

public class EmployeConsole
{
    private Option afficher(final Employe employe)
    {
        return new Option("Afficher l'employé", "l",
                () ->
                {
                    System.out.println(employe);
                    System.out.println("Date d'arrivée : " + employe.getDateArrivee());
                    System.out.println("Date de départ : " + employe.getDateDepart());
                });
    }

    ListOption<Employe> editerEmploye()
    {
        return (employe) -> editerEmploye(employe);
    }

    Option editerEmploye(Employe employe)
    {
        Menu menu = new Menu("Gérer le compte " + employe.getNom(), "c");
        menu.add(afficher(employe));
        menu.add(changerNom(employe));
        menu.add(changerPrenom(employe));
        menu.add(changerMail(employe));
        menu.add(changerPassword(employe));
        menu.add(changerDateArrivee(employe));
        menu.add(changerDateDepart(employe));
        menu.addBack("q");
        return menu;
    }

    private Option changerNom(final Employe employe)
    {
        return new Option("Changer le nom", "n",
                () -> {employe.setNom(getString("Nouveau nom : "));});
    }

    private Option changerPrenom(final Employe employe)
    {
        return new Option("Changer le prénom", "p",
                () -> {employe.setPrenom(getString("Nouveau prénom : "));});
    }

    private Option changerMail(final Employe employe)
    {
        return new Option("Changer le mail", "e",
                () -> {employe.setMail(getString("Nouveau mail : "));});
    }

    private Option changerPassword(final Employe employe)
    {
        return new Option("Changer le password", "x",
                () -> {employe.setPassword(getString("Nouveau password : "));});
    }

    // ---------- Modifier la date d'arrivée ----------

    private Option changerDateArrivee(final Employe employe)
    {
        return new Option("Changer la date d'arrivée", "a",
                () ->
                {
                    while (true)
                    {
                        LocalDate nouvelleDate = saisirDate(
                                "Nouvelle date d'arrivée (AAAA-MM-JJ, vide pour effacer) : ");

                        try
                        {
                            employe.setDateArrivee(nouvelleDate);
                            System.out.println("Date d'arrivée modifiée.");
                            break;
                        }
                        catch (DateIncoherente e)
                        {
                            System.err.println("Erreur : " + e.getMessage());
                            System.out.println("Veuillez réessayer.");
                        }
                    }
                });
    }

    // ---------- Modifier la date de départ ----------

    private Option changerDateDepart(final Employe employe)
    {
        return new Option("Changer la date de départ", "d",
                () ->
                {
                    while (true)
                    {
                        LocalDate nouvelleDate = saisirDate(
                                "Nouvelle date de départ (AAAA-MM-JJ, vide pour effacer) : ");

                        try
                        {
                            employe.setDateDepart(nouvelleDate);
                            System.out.println("Date de départ modifiée.");
                            break;
                        }
                        catch (DateIncoherente e)
                        {
                            System.err.println("Erreur : " + e.getMessage());
                            System.out.println("Veuillez réessayer.");
                        }
                    }
                });
    }

    // ---------- Saisie et vérification du format ----------

    private LocalDate saisirDate(String message)
    {
        while (true)
        {
            String saisie = getString(message);

            if (saisie == null || saisie.trim().isEmpty())
                return null;

            try
            {
                return LocalDate.parse(saisie.trim());
            }
            catch (DateTimeParseException e)
            {
                System.err.println("Format de date invalide.");
                System.err.println("Utilisez le format AAAA-MM-JJ (exemple : 2026-09-15).");
            }
        }
    }
}
