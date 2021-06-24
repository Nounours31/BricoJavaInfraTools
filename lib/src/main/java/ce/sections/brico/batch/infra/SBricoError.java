package ce.sections.brico.batch.infra;

public class SBricoError 
{
	private int _err = 0;
	private String _msg = "OK";
	
	public SBricoError (int err, String msg)
	{
		_err = err;
		_msg = msg;
	}

	public SBricoError (String msg)
	{
		_err = 55;
		_msg = msg;
	}
	
	public SBricoError ()
	{
		_err = 55;
		_msg = "Undef";
	}

	static public SBricoError sOK = new SBricoError(0, "no error");
	static public SBricoError eFAIL = new SBricoError(1, "unknow error");
	
	
	public boolean SUCCEEDED ()
	{
		if (this._err == 0)
			return true;
		return false;
	}

	public boolean FAILED ()
	{
		return !this.SUCCEEDED();
	}

	public String getMsg() {
		return _msg;
	}

	public void setMsg(String _msg) {
		this._msg = _msg;
	}
	
	@Override
	public String toString() {
        return ("ErrCode: [" + Integer.toString(_err) + "] Msg: ["+_msg+"]");
    }

}
