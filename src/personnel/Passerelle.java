package personnel;

public interface Passerelle 
{
	public GestionPersonnel getGestionPersonnel();
	public void sauvegarderGestionPersonnel(GestionPersonnel gestionPersonnel) throws SauvegardeImpossible;
	public int insert(Ligue ligue) throws SauvegardeImpossible;

	// "default" : la classe Serialization n'a pas besoin d'être modifiée
	public default int insert(Employe employe) throws SauvegardeImpossible { return -1; }
	public default void update(Ligue ligue) throws SauvegardeImpossible {}
	public default void update(Employe employe) throws SauvegardeImpossible {}
	public default void delete(Ligue ligue) throws SauvegardeImpossible {}
	public default void delete(Employe employe) throws SauvegardeImpossible {}
}