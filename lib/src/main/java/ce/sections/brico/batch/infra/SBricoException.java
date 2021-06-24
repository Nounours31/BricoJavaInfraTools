package ce.sections.brico.batch.infra;

public class SBricoException extends Exception 
{
	private static final long serialVersionUID = 1L;
	private String _msg = null;

	public SBricoException() {
		super();
	}

	public SBricoException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public SBricoException(String message, Throwable cause) {
		super(message, cause);
	}

	public SBricoException(Throwable cause) {
		super(cause);
	}

	public SBricoException(String message) {
		_msg = message;
	}

	@Override
	public String getMessage() {
		return super.getMessage() + " Info: " + _msg;
	}

	@Override
	public String toString() {
		return super.toString();
	}



}
