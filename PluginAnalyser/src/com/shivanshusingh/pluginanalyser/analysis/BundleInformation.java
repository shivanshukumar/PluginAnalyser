package com.shivanshusingh.pluginanalyser.analysis;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.text.ParseException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.apache.ivy.osgi.core.BundleCapability;
import org.apache.ivy.osgi.core.BundleInfo;
import org.apache.ivy.osgi.core.BundleRequirement;
import org.apache.ivy.osgi.core.ManifestHeaderElement;
import org.apache.ivy.osgi.core.ManifestHeaderValue;
import org.apache.ivy.osgi.core.ManifestParser;
import org.apache.ivy.osgi.util.Version;

/**
 * this contains the data from the MANIFEST.MF file of the plugin and  the plugin.xml data.
 * @author Shivanshu Singh
 * 
 */
public class BundleInformation {

	
	private String pluginXml;
	
	private BundleInfo bundleinfo;

	private Manifest manifest;

	private final String BUNDLE_CLASSPATH = "Bundle-ClassPath";

	private Set<String> classpathEntries = new LinkedHashSet<String>();

	public BundleInformation()  {
		
	}

	public BundleInformation(InputStream is) throws IOException, ParseException {
		initializeBundleInformation(new Manifest(is));
	}

	/**
	 * @param ma {@link Manifest}
	 * 
	 */
	public BundleInformation(Manifest ma) throws ParseException {
		initializeBundleInformation(ma);
	}
	
	public void addCapability(BundleCapability capability) {
		bundleinfo.addCapability(capability);
	}

	private void addClasspathEntries() throws ParseException {
		if (null != manifest) {
			Attributes mainAttributes = manifest.getMainAttributes();
			// Log.outln( mainAttributes.keySet().toString() );

			parseAttribute(mainAttributes, BUNDLE_CLASSPATH);
		}

	}

	public void addRequirement(BundleRequirement requirement) {
		bundleinfo.addRequirement(requirement);

	}

	public boolean equals(Object obj) {

		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof BundleInformation)) {
			return false;
		}
		BundleInformation other = (BundleInformation) obj;

		if (bundleinfo == null) {
			if (other.bundleinfo != null) {
				return false;
			}
		} else if (!bundleinfo.equals(other.bundleinfo)) {
			return false;
		}

		if (classpathEntries == null) {
			if (other.classpathEntries != null) {
				return false;
			}
		} else if (!classpathEntries.equals(other.classpathEntries)) {
			return false;
		}

		return true;
	}

	public BundleInfo getBundleInfo() {
		if(null!=bundleinfo)
			return this.bundleinfo;
		else
			return null;
	}

	public Set/* <BundleCapability> */getCapabilities() {
		return bundleinfo.getCapabilities();
	}

	public Set<String> getClasspathEntries() {
		return classpathEntries;
	}

	public String getDescription() {
		return bundleinfo.getDescription();
	}

	public String getDocumentation() {
		return bundleinfo.getDocumentation();

	}

	public List/* <String> */getExecutionEnvironments() {
		return bundleinfo.getExecutionEnvironments();
	}

	public Set/* <ExportPackage> */getExports() {
		Set s = new HashSet();
		if(null!=bundleinfo)
			s=bundleinfo.getExports()    ;
		return s;
	}

	public String getId() {
		return bundleinfo.getId();

	}

	public Set/* <BundleRequirement> */getImports() {
		Set s = new HashSet();
		if(null!=bundleinfo)
			s=bundleinfo.getImports();
		return s;
	}

	public String getLicense() {
		return bundleinfo.getLicense();

	}

	public String getPluginXml() {
		return pluginXml;
	}


	public String getPresentationName() {
		return bundleinfo.getPresentationName();

	}

	public Version getRawVersion() {
		return bundleinfo.getRawVersion();

	}

	public Set/* <BundleRequirement> */getRequirements() {
		Set s  =  new HashSet();
		if(null!=bundleinfo)
		  s  = bundleinfo.getRequirements();
		return s;

	}

	public Set/* <BundleRequirement> */getRequires() {
		Set s = new HashSet();
		if(null!=bundleinfo)
			s=bundleinfo.getRequires();
		return s;
	}

	public Set/* <BundleCapability> */getServices() {
		Set s = new HashSet();
			if(null!=bundleinfo)
				s=bundleinfo.getServices();
		return s;
	}

	public Integer getSize() {
		return bundleinfo.getSize();
	}

	public String getSymbolicName() {
		if(null!=bundleinfo)
			return bundleinfo.getSymbolicName();
		else return "";

	}

	public URI getUri() {
		return bundleinfo.getUri();

	}

	public Version getVersion() {
		
		Version v = null;
		if(null!=bundleinfo)
			v=bundleinfo.getVersion();
		return v;
		
		

	}
	/*public String getVersionToString() {
		
		String s="";
		if(null!=bundleinfo)
			s=bundleinfo.getVersion().toString();
		return s;
		
	}*/

	public int hashCode() {
		final int prime = 31;
		int result = 1;

		result = prime * result + bundleinfo.hashCode();
		result = prime
				* result
				+ ((classpathEntries == null) ? 0 : classpathEntries.hashCode());
		return result;
	}

	/**
	 * @param ma {@link Manifest}
	 * @throws ParseException
	 */
	private void initializeBundleInformation(Manifest ma) throws ParseException {
		this.manifest = ma;
		this.bundleinfo = ManifestParser.parseManifest(this.manifest);
		addClasspathEntries();
	}

	private void parseAttribute(Attributes mainAttributes, String headerName)
			throws ParseException {
		ManifestHeaderValue elements = new ManifestHeaderValue(
				mainAttributes.getValue(headerName));
		Iterator itElement = elements.getElements().iterator();
		while (itElement.hasNext()) {
			ManifestHeaderElement element = (ManifestHeaderElement) itElement
					.next();

			Iterator itNames = element.getValues().iterator();
			while (itNames.hasNext()) {
				String name = (String) itNames.next();
				this.classpathEntries.add(name);
				// Log.outln(name);
			}
		}
	}

	public void setDescription(String description) {

		bundleinfo.setDescription(description);

	}

	public void setDocumentation(String documentation) {
		bundleinfo.setDocumentation(documentation);
	}

	public void setExecutionEnvironments(List/* <String> */executionEnvironment) {
		bundleinfo.setExecutionEnvironments(executionEnvironment);
	}

	public void setId(String id) {
		bundleinfo.setId(id);

	}

	public void setLicense(String license) {
		bundleinfo.setLicense(license);

	}

	public void setPluginXml(String pluginXml) {
		this.pluginXml = pluginXml;
	}

	public void setPresentationName(String presentationName) {
		bundleinfo.setPresentationName(presentationName);

	}

	public void setSize(Integer size) {
		bundleinfo.setSize(size);
	}

	public void setUri(URI uri) {
		bundleinfo.setUri(uri);

	}

	public String toString() {
		StringBuffer builder = new StringBuffer();
		builder.append("BundleInfo [executionEnvironments=");
		builder.append(bundleinfo.getExecutionEnvironments());
		builder.append(", capabilities=");
		builder.append(bundleinfo.getCapabilities());
		builder.append(", requirements=");
		builder.append(bundleinfo.getRequirements());
		builder.append(", symbolicName=");
		builder.append(bundleinfo.getSymbolicName());
		builder.append(", version=");
		builder.append(bundleinfo.getVersion());
		builder.append(", classpathEntries=");
		builder.append(classpathEntries);
		builder.append("]");
		return builder.toString();
	}

}
