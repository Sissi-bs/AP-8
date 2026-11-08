package jdbc;

import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

import personnel.*;

public class JDBC implements Passerelle 
{
	Connection connection;

	public JDBC()
	{
		try
		{
			Class.forName(Credentials.getDriverClassName());
			connection = DriverManager.getConnection(Credentials.getUrl(), Credentials.getUser(), Credentials.getPassword());
		}
		catch (ClassNotFoundException e)
		{
			System.out.println("Pilote JDBC non installé.");
		}
		catch (SQLException e)
		{
			System.out.println(e);
		}
	}
	
	@Override
	public GestionPersonnel getGestionPersonnel() 
	{
		GestionPersonnel gestionPersonnel = new GestionPersonnel();
		try 
		{
			Statement instruction = connection.createStatement();
			ResultSet ligues = instruction.executeQuery("select * from ligue");
			while (ligues.next())
			{
				Ligue ligue = gestionPersonnel.addLigue(ligues.getInt("id"), ligues.getString("nom"));
				chargerEmployes(ligue, ligues.getInt("id_administrateur"));
			}
		}
		catch (SQLException e)
		{
			System.out.println(e);
		}
		return gestionPersonnel;
	}

	private void chargerEmployes(Ligue ligue, int idAdmin) throws SQLException
	{
		PreparedStatement ps = connection.prepareStatement("select * from employe where id_ligue = ?");
		ps.setInt(1, ligue.getId());
		ResultSet employes = ps.executeQuery();
		while (employes.next())
		{
			Employe e = ligue.addEmploye(employes.getInt("id"), employes.getString("nom"), 
					employes.getString("prenom"), employes.getString("mail"), employes.getString("password"));
			if (e.getId() == idAdmin)
				ligue.setAdministrateur(e);
		}
	}

	@Override
	public void sauvegarderGestionPersonnel(GestionPersonnel gestionPersonnel) throws SauvegardeImpossible 
	{
		close();
	}
	
	public void close() throws SauvegardeImpossible
	{
		try
		{
			if (connection != null)
				connection.close();
		}
		catch (SQLException e)
		{
			throw new SauvegardeImpossible(e);
		}
	}
	
	@Override
	public int insert(Ligue ligue) throws SauvegardeImpossible 
	{
		try 
		{
			PreparedStatement instruction;
			instruction = connection.prepareStatement("insert into ligue (nom) values(?)", Statement.RETURN_GENERATED_KEYS);
			instruction.setString(1, ligue.getNom());		
			instruction.executeUpdate();
			ResultSet id = instruction.getGeneratedKeys();
			id.next();
			return id.getInt(1);
		} 
		catch (SQLException exception) 
		{
			exception.printStackTrace();
			throw new SauvegardeImpossible(exception);
		}		
	}

	@Override
	public int insert(Employe employe) throws SauvegardeImpossible
	{
		try 
		{
			PreparedStatement ps = connection.prepareStatement(
				"insert into employe (nom, prenom, mail, password, id_ligue) values(?,?,?,?,?)", 
				Statement.RETURN_GENERATED_KEYS);
			ps.setString(1, employe.getNom());
			ps.setString(2, employe.getPrenom());
			ps.setString(3, employe.getMail());
			ps.setString(4, employe.getPassword());
			ps.setInt(5, employe.getLigue().getId());
			ps.executeUpdate();
			ResultSet id = ps.getGeneratedKeys();
			id.next();
			return id.getInt(1);
		} 
		catch (SQLException e) 
		{
			e.printStackTrace();
			throw new SauvegardeImpossible(e);
		}
	}

	@Override
	public void update(Employe employe) throws SauvegardeImpossible
	{
		try 
		{
			PreparedStatement ps = connection.prepareStatement(
				"update employe set nom=?, prenom=?, mail=?, password=? where id=?");
			ps.setString(1, employe.getNom());
			ps.setString(2, employe.getPrenom());
			ps.setString(3, employe.getMail());
			ps.setString(4, employe.getPassword());
			ps.setInt(5, employe.getId());
			ps.executeUpdate();
		} 
		catch (SQLException e) 
		{
			e.printStackTrace();
			throw new SauvegardeImpossible(e);
		}
	}

	@Override
	public void update(Ligue ligue) throws SauvegardeImpossible
	{
		try 
		{
			PreparedStatement ps = connection.prepareStatement(
				"update ligue set nom=?, id_administrateur=? where id=?");
			ps.setString(1, ligue.getNom());
			if (ligue.getAdministrateur().estRoot())
				ps.setNull(2, Types.INTEGER);   // NULL = c'est le root l'administrateur
			else
				ps.setInt(2, ligue.getAdministrateur().getId());
			ps.setInt(3, ligue.getId());
			ps.executeUpdate();
		} 
		catch (SQLException e) 
		{
			e.printStackTrace();
			throw new SauvegardeImpossible(e);
		}
	}

	@Override
	public void delete(Employe employe) throws SauvegardeImpossible
	{
		try 
		{
			PreparedStatement ps = connection.prepareStatement("delete from employe where id=?");
			ps.setInt(1, employe.getId());
			ps.executeUpdate();
		} 
		catch (SQLException e) 
		{
			e.printStackTrace();
			throw new SauvegardeImpossible(e);
		}
	}

	@Override
	public void delete(Ligue ligue) throws SauvegardeImpossible
	{
		try 
		{
			// on retire d'abord l'administrateur pour éviter un conflit de clés étrangères
			PreparedStatement ps = connection.prepareStatement("update ligue set id_administrateur=NULL where id=?");
			ps.setInt(1, ligue.getId());
			ps.executeUpdate();
			// les employés de la ligue sont supprimés automatiquement (ON DELETE CASCADE)
			ps = connection.prepareStatement("delete from ligue where id=?");
			ps.setInt(1, ligue.getId());
			ps.executeUpdate();
		} 
		catch (SQLException e) 
		{
			e.printStackTrace();
			throw new SauvegardeImpossible(e);
		}
	}
}