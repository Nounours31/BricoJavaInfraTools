package ce.sections.brico.tools;

public enum eSBricoLoggerLevel 
{
	DEBUG (0), INFO(2), ERROR (1), CONSOLE(5), FATAL(500);
	
	
	private int _info;
	private eSBricoLoggerLevel (int i) {_info = i;};
	
	public int getInfo() {return _info;}
	public int getInt() {return _info;}
}
