/**
 * 
 */
package com.shivanshusingh.PluginAnalyser.Analysis;

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
 * this contains the data from the MANIFEST.MF file of the plugin.
 * @author singhsk
 * 
 */
public class BundleInformation {

	
	private String pluginXml;
	
	public String getPluginXml() {
		return pluginXml;
	}

	public void setPluginXml(String pluginXml) {
		this.pluginXml = pluginXml;
	}

	private BundleInfo bundleinfo;

	private Manifest manifest;

	private final String BUNDLE_CLASSPATH = "Bundle-ClassPath";

	private Set<String> classpathEntries = new LinkedHashSet<String>();

	public BundleInformation()  {
		
	}
	
	/**
	 * @param ma
	 * 
	 */
	public BundleInformation(Manifest ma) throws ParseException {
		initializeBundleInformation(ma);
	}

	public BundleInformation(InputStream is) throws IOException, ParseException {
		initializeBundleInformation(new Manifest(is));
	}

	/**
	 * @param ma
	 * @throws ParseException
	 */
	private void initializeBundleInformation(Manifest ma) throws ParseException {
		this.manifest = ma;
		this.bundleinfo = ManifestParser.parseManifest(this.manifest);
		addClasspathEntries();
	}

	private void addClasspathEntries() throws ParseException {
		if (null != manifest) {
			Attributes mainAttributes = manifest.getMainAttributes();
			// System.out.println( mainAttributes.keySet().toString() );

			parseAttribute(mainAttributes, BUNDLE_CLASSPATH);
		}

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
				// System.out.println(name);
			}
		}
	}

	public BundleInfo getBundleInfo() {
		if(null!=bundleinfo)
			return this.bundleinfo;
		else
			return null;
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

	public Set<String> getClasspathEntries() {
		return classpathEntries;
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

	public Set/* <BundleRequirement> */getRequires() {
		Set s = new HashSet();
		if(null!=bundleinfo)
			s=bundleinfo.getRequires();
		return s;
	}

	public Set/* <BundleRequirement> */getImports() {
		Set s = new HashSet();
		if(null!=bundleinfo)
			s=bundleinfo.getImports();
		return s;
	}

	public Set/* <ExportPackage> */getExports() {
		Set s = new HashSet();
		if(null!=bundleinfo)
			s=bundleinfo.getExports()    ;
		return s;
	}

	public Set/* <BundleCapability> */getServices() {
		Set s = new HashSet();
			if(null!=bundleinfo)
				s=bundleinfo.getServices();
		return s;
	}

	public String getSymbolicName() {
		if(null!=bundleinfo)
			return bundleinfo.getSymbolicName();
		else return "";

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


	public Version getRawVersion() {
		return bundleinfo.getRawVersion();

	}

	public void setUri(URI uri) {
		bundleinfo.setUri(uri);

	}

	public URI getUri() {
		return bundleinfo.getUri();

	}

	public void setId(String id) {
		bundleinfo.setId(id);

	}

	public String getId() {
		return bundleinfo.getId();

	}

	public void setPresentationName(String presentationName) {
		bundleinfo.setPresentationName(presentationName);

	}

	public String getPresentationName() {
		return bundleinfo.getPresentationName();

	}

	public void setDescription(String description) {

		bundleinfo.setDescription(description);

	}

	public String getDescription() {
		return bundleinfo.getDescription();
	}

	public void setDocumentation(String documentation) {
		bundleinfo.setDocumentation(documentation);
	}

	public String getDocumentation() {
		return bundleinfo.getDocumentation();

	}

	public void setLicense(String license) {
		bundleinfo.setLicense(license);

	}

	public String getLicense() {
		return bundleinfo.getLicense();

	}

	public void setSize(Integer size) {
		bundleinfo.setSize(size);
	}

	public Integer getSize() {
		return bundleinfo.getSize();
	}

	public void addRequirement(BundleRequirement requirement) {
		bundleinfo.addRequirement(requirement);

	}

	public Set/* <BundleRequirement> */getRequirements() {
		Set s  =  new HashSet();
		if(null!=bundleinfo)
		  s  = bundleinfo.getRequirements();
		return s;

	}

	public void addCapability(BundleCapability capability) {
		bundleinfo.addCapability(capability);
	}

	public Set/* <BundleCapability> */getCapabilities() {
		return bundleinfo.getCapabilities();
	}

	public List/* <String> */getExecutionEnvironments() {
		return bundleinfo.getExecutionEnvironments();
	}

	public void setExecutionEnvironments(List/* <String> */executionEnvironment) {
		bundleinfo.setExecutionEnvironments(executionEnvironment);
	}

	public int hashCode() {
		final int prime = 31;
		int result = 1;

		result = prime * result + bundleinfo.hashCode();
		result = prime
				* result
				+ ((classpathEntries == null) ? 0 : classpathEntries.hashCode());
		return result;
	}

}
