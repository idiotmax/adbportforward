package chabernac.utils;

public class ArgsInterPreter {
  private final String[] myArgs;
  
  public ArgsInterPreter(String[] anArgs){
    myArgs = anArgs;
  }
  
  public boolean containsKey(String aKey){
    for(String theArg : myArgs){
      String[] theParts = theArg.split( "=" );
      if(theParts[0].equalsIgnoreCase( aKey )){
        return true;
      }
    }
    return false;
  }
  
  public String getKeyValue(String aKey, String aDefault){
    String theValue = getKeyValue(aKey);
    if(theValue == null || "".equals(theValue)) return aDefault;
    return theValue;
  }
  
  public String getKeyValue(String aKey){
    for(String theArg : myArgs){
      String[] theParts = theArg.split( "=" );
      if(theParts[0].equalsIgnoreCase( aKey ) && theParts.length >= 2){
        return theParts[1];
      }
    }
    return null;
  }

}

