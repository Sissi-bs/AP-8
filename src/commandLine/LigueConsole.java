
package commandLine;

import static commandLineMenus.rendering.examples.util.InOut.getString;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;

import commandLineMenus.List;
import commandLineMenus.Menu;
import commandLineMenus.Option;

import personnel.*;

public class LigueConsole
{
    private GestionPersonnel gestionPersonnel;
    private EmployeConsole employeConsole;

    public LigueConsole(GestionPersonnel gestionPersonnel, EmployeConsole employeConsole)
    {
        this.gestionPersonnel = gestionPersonnel;
        this.employeConsole = employeConsole;
    }

    // ---------- Root : toutes les ligues ----------

    Menu menuLigues()
    {
        Menu menu = new Menu("Gérer les ligues", "l");
        menu.add(afficherLigues());
        menu.add(ajouterLigue());
        menu.add(selectionnerLigue());
        menu.addBack("q");
        return menu;
    }

    // ---------- Administrateur : sa ligue uniquement ----------

    Menu menuAdministrateur(Ligue ligue)
    {
        Menu menu = new Menu("Administrer " + ligue.getNom(), "l");
        menu.add(afficher(ligue));
        menu.add(gererEmployes(ligue));
        menu.addBack("q");
        return menu;
    }

    // ---------- Employé simple : lecture seule ----------

    Menu menuConsultation()
    {
        Menu menu = new Menu("Annuaire des ligues", "l");
        menu.add(afficherLigues());
        menu.add(new List<Ligue>("Consulter une ligue", "e",
                () -> new ArrayList<>(gestionPersonnel.getLigues()),
                (element) -> consulterLigue(element)
        ));
        menu.addBack("q");
        return menu;
    }

    private Menu consulterLigue(final Ligue ligue)
    {
        Menu menu = new Menu("Consulter " + ligue.getNom());
        menu.add(afficher(ligue));
        menu.add(new Option("Afficher les employés", "m",
                () -> {System.out.println(ligue.getEmployes());}));
        menu.addBack("q");
        return menu;
    }

    // ---------- Options communes ----------

    private Option afficherLigues()
    {
        return new Option("Afficher les ligues", "l",
                () -> {System.out.println(gestionPersonnel.getLigues());});
    }

    private Option afficher(final Ligue ligue)
    {
        return new Option("Afficher la ligue", "l",
                () ->
                {
                    System.out.println(ligue);
                    System.out.println("administrée par " + ligue.getAdministrateur());
                }
        );
    }

    private Option afficherEmployes(final Ligue ligue)
    {
        return new Option("Afficher les employes", "l",
                () -> {System.out.println(ligue.getEmployes());});
    }

    private Option ajouterLigue()
    {
        return new Option("Ajouter une ligue", "a", () ->
        {
            try
            {
                gestionPersonnel.addLigue(getString("nom : "));
            }
            catch(SauvegardeImpossible exception)
            {
                System.err.println("Impossible de sauvegarder cette ligue");
            }
        });
    }

    private Menu editerLigue(Ligue ligue)
    {
        Menu menu = new Menu("Editer " + ligue.getNom());
        menu.add(afficher(ligue));
        menu.add(gererEmployes(ligue));
        menu.add(changerAdministrateur(ligue));
        menu.add(changerNom(ligue));
        menu.add(supprimer(ligue));
        menu.addBack("q");
        return menu;
    }

    private Option changerNom(final Ligue ligue)
    {
        return new Option("Renommer", "r",
                () -> {ligue.setNom(getString("Nouveau nom : "));});
    }

    private List<Ligue> selectionnerLigue()
    {
        return new List<Ligue>("Sélectionner une ligue", "e",
                () -> new ArrayList<>(gestionPersonnel.getLigues()),
                (element) -> editerLigue(element)
        );
    }

    // ---------- Ajouter un employé avec ses dates ----------

    private Option ajouterEmploye(final Ligue ligue)
    {
        return new Option("Ajouter un employé", "a",
                () ->
                {
                    String nom = getString("nom : ");
                    String prenom = getString("prenom : ");
                    String mail = getString("mail : ");
                    String password = getString("password : ");

                    LocalDate dateArrivee;
                    LocalDate dateDepart;

                    // On recommence si les dates sont incohérentes.
                    while (true)
                    {
                        dateArrivee = saisirDate("Date d'arrivée (AAAA-MM-JJ, vide si inconnue) : ");
                        dateDepart = saisirDate("Date de départ (AAAA-MM-JJ, vide si inconnue) : ");

                        try
                        {
                            if (dateArrivee != null && dateDepart != null
                                    && dateDepart.isBefore(dateArrivee))
                            {
                                throw new DateIncoherente(
                                    "La date de départ ne peut pas précéder la date d'arrivée.");
                            }

                            break;
                        }
                        catch (DateIncoherente e)
                        {
                            System.err.println("Erreur : " + e.getMessage());
                            System.out.println("Veuillez saisir à nouveau les dates.");
                        }
                    }

                    try
                    {
                        ligue.addEmploye(nom, prenom, mail, password,
                                dateArrivee, dateDepart);
                        System.out.println("Employé ajouté avec succès.");
                    }
                    catch (DateIncoherente e)
                    {
                        System.err.println("Erreur : " + e.getMessage());
                    }
                }
        );
    }

    // ---------- Saisie et vérification d'une date ----------

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
                System.err.println("Veuillez utiliser le format AAAA-MM-JJ (exemple : 2026-09-15).");
            }
        }
    }

    // ---------- Gestion des employés ----------

    private Menu gererEmployes(Ligue ligue)
    {
        Menu menu = new Menu("Gérer les employés de " + ligue.getNom(), "e");
        menu.add(afficherEmployes(ligue));
        menu.add(ajouterEmploye(ligue));
        menu.add(modifierEmploye(ligue));
        menu.add(supprimerEmploye(ligue));
        menu.addBack("q");
        return menu;
    }

    private List<Employe> supprimerEmploye(final Ligue ligue)
    {
        return new List<>("Supprimer un employé", "s",
                () -> new ArrayList<>(ligue.getEmployes()),
                (index, element) -> {element.remove();}
        );
    }

    // ---------- Changer l'administrateur ----------

    private List<Employe> changerAdministrateur(final Ligue ligue)
    {
        return new List<>("Changer l'administrateur", "c",
                () -> new ArrayList<>(ligue.getEmployes()),
                (index, element) ->
                {
                    try
                    {
                        ligue.setAdministrateur(element);
                        System.out.println(element + " est maintenant administrateur.");
                    }
                    catch (DroitsInsuffisants e)
                    {
                        System.err.println("Cet employé ne peut pas administrer cette ligue.");
                    }
                }
        );
    }

    private List<Employe> modifierEmploye(final Ligue ligue)
    {
        return new List<>("Modifier un employé", "e",
                () -> new ArrayList<>(ligue.getEmployes()),
                employeConsole.editerEmploye()
        );
    }

    private Option supprimer(Ligue ligue)
    {
        return new Option("Supprimer", "d", () -> {ligue.remove();});
    }
}
