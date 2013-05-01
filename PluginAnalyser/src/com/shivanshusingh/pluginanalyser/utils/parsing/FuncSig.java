/**
 * 
 */
package com.shivanshusingh.pluginanalyser.utils.parsing;

/**
 * the func signature handler for parsing purposes.
 * separates the function class from function name. e.g. if the input is:
 * org.s.G com.x.A.foo () returned: classAndFuncName[0]=org.s.G
 * classAndFuncName[1]=com.x.A classAndFuncName[3]=foo ()
 * 
 * functions to manipulate and use this data provided.
 * 
 * NOTE: this is a mutable object. to copy use the {@link FuncSig({@link FuncSig})} constructor.
 * function signature
 * @author Shivanshu Singh
 *
 */
public class FuncSig {

	private String returnType="";
	public String getReturnType() {
		return returnType;
	}
	public void setReturnType(String returnType) {
		this.returnType = returnType;
	}
	public String getClassName() {
		return className;
	}
	public void setClassName(String className) {
		this.className = className;
	}
	public String getFunction() {
		return function;
	}
	public void setFunction(String function) {
		this.function = function;
	}

	private String  className="";
	private String function="";
	
	public FuncSig(FuncSig funcSig)
	{
		this.className=funcSig.getClassName();
		this.function=funcSig.getFunction();
		this.returnType=funcSig.getReturnType();
	}
	
	public FuncSig(String funcSignature)
	{
		if(null!=funcSignature  &&  0<funcSignature.length() )
		{
//			System.err.println("Now splitting funcSig:"+funcSignature);
		String[] spaceSplits = funcSignature.split(" ");
		String[] dotSplits;
		String paramsPart="";
		// returnType
		if(null!=spaceSplits && spaceSplits.length>=2)
		{
			if(spaceSplits.length>2)
			{
				this.returnType= spaceSplits[0].trim();
				String toDotSplit =spaceSplits[1].trim();
				dotSplits = toDotSplit.split("\\.");
				paramsPart=spaceSplits[2].trim();
	
			}
			else
			{
				String toDotSplit = spaceSplits[0].trim();
				dotSplits = toDotSplit.split("\\.");
				paramsPart=spaceSplits[1].trim();
			}
		
	
			// function name and parameters
			this.function= dotSplits[dotSplits.length - 1].trim() + " " + paramsPart;
		
			// class name
			if(2<=dotSplits.length)
				this.className = dotSplits[0].trim();
			else
				this.className=    "";
			
			for (int x = 1; x < dotSplits.length - 1; x++)
				this.className += "." + dotSplits[x].trim();
		}
		spaceSplits=null;
		dotSplits=null;
		}
	}
	
	public String getSignature() {
		String signature= ( this.returnType.trim().length()>0?this.returnType.trim()+" ":"") +  this.className + "."+ this.function;
		return  signature;
	}
}
