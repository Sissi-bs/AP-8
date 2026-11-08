package personnel;

public class DateIncoherente extends RuntimeException
{
	private static final long serialVersionUID = 1L;

	public DateIncoherente(String message)
	{
		super(message);
	}
}