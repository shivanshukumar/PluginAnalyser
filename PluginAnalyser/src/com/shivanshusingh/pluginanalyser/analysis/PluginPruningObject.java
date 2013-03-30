package com.shivanshusingh.pluginanalyser.analysis;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PluginPruningObject {

public	Set<String> invokationsToBeRemoved = new HashSet<String>();	
public Map<String,Set<String>> invokationProxies=new HashMap<String,Set<String>>();
}
