package ce.sections.brico.tools;


public class SBricoLogger {
	private static SBricoLogger _singleton = null;	

	private eSBricoLoggerLevel _level = eSBricoLoggerLevel.FATAL;
	private SBricoLogger() {}
	
	public synchronized static SBricoLogger getLogger()
	{
		if (_singleton == null)
			_singleton = new SBricoLogger();
		return _singleton;
	}
	


	public void setLogLevel (eSBricoLoggerLevel level)  {
        this._level = level;
	}
	public eSBricoLoggerLevel getLogLevel ()  {
        return this._level;
	}

	public boolean isDebug ()  {
        return (this._level.getInt() >= eSBricoLoggerLevel.DEBUG.getInt());
	}
	
	public void fatal (String msg) { this.log(eSBricoLoggerLevel.FATAL, msg);}
	public void error (String msg) {this.log(eSBricoLoggerLevel.ERROR, msg);}
	public void info (String msg) {this.log(eSBricoLoggerLevel.INFO, msg);}
	public void debug (String msg) {this.log(eSBricoLoggerLevel.DEBUG, msg);}
	public void console (String msg) {this.log(eSBricoLoggerLevel.CONSOLE, msg);}

	public void fatal(Exception e) {
		this.fatal(e.getMessage());
		e.printStackTrace(System.err);
	}
	
	
	private void log(eSBricoLoggerLevel level, String msg) 
	{
        if (this._level.getInt() <= level.getInt()) {
            System.err.println(msg);
        }
	}
}
