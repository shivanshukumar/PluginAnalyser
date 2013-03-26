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
 * @deprecated
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

	/**
	 * @deprecated
	 */
	public BundleInformation()  {
		
	}

	public BundleInformation(InputStream is) throws IOException, ParseException {
		initializeBundleInformation(new Manifest(is));
	}

	/**
	 * @deprecated
	 *
	 * @param ma {@link Manifest}
	 * 
	 */
	public BundleInformation(Manifest ma) throws ParseException {
		initializeBundleInformation(ma);
	}
	/**
	 * @deprecated
	 */
	public void addCapability(BundleCapability capability) {
		bundleinfo.addCapability(capability);
	}
	/**
	 * @deprecated
	 */
	private void addClasspathEntries() throws ParseException {
		if (null != manifest) {
			Attributes mainAttributes = manifest.getMainAttributes();
			// Log.outln( mainAttributes.keySet().toString() );

			parseAttribute(mainAttributes, BUNDLE_CLASSPATH);
		}

	}
	/**
	 * @deprecated
	 */
	public void addRequirement(BundleRequirement requirement) {
		bundleinfo.addRequirement(requirement);

	}
	/**
	 * @deprecated
	 */
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
	/**
	 * @deprecated
	 */
	public BundleInfo getBundleInfo() {
		if(null!=bundleinfo)
			return this.bundleinfo;
		else
			return null;
	}
	/**
	 * @deprecated
	 */
	public Set/* <BundleCapability> */getCapabilities() {
		return bundleinfo.getCapabilities();
	}
	/**
	 * @deprecated
	 */
	public Set<String> getClasspathEntries() {
		return classpathEntries;
	}
	/**
	 * @deprecated
	 */
	public String getDescription() {
		return bundleinfo.getDescription();
	}
	/**
	 * @deprecated
	 */
	public String getDocumentation() {
		return bundleinfo.getDocumentation();

	}
	/**
	 * @deprecated
	 */
	public List/* <String> */getExecutionEnvironments() {
		return bundleinfo.getExecutionEnvironments();
	}
	/**
	 * @deprecated
	 */
	public Set/* <ExportPackage> */getExports() {
		Set s = new HashSet();
		if(null!=bundleinfo)
			s=bundleinfo.getExports()    ;
		return s;
	}
	/**
	 * @deprecated
	 */
	public String getId() {
		return bundleinfo.getId();

	}
	/**
	 * @deprecated
	 */
	public Set/* <BundleRequirement> */getImports() {
		Set s = new HashSet();
		if(null!=bundleinfo)
			s=bundleinfo.getImports();
		return s;
	}
	/**
	 * @deprecated
	 */
	public String getLicense() {
		return bundleinfo.getLicense();

	}
	/**
	 * @deprecated
	 */
	public String getPluginXml() {
		return pluginXml;
	}

	/**
	 * @deprecated
	 */
	public String getPresentationName() {
		return bundleinfo.getPresentationName();

	}
	/**
	 * @deprecated
	 */
	public Version getRawVersion() {
		return bundleinfo.getRawVersion();

	}
	/**
	 * @deprecated
	 */
	public Set/* <BundleRequirement> */getRequirements() {
		Set s  =  new HashSet();
		if(null!=bundleinfo)
		  s  = bundleinfo.getRequirements();
		return s;

	}
	/**
	 * @deprecated
	 */
	public Set/* <BundleRequirement> */getRequires() {
		Set s = new HashSet();
		if(null!=bundleinfo)
			s=bundleinfo.getRequires();
		return s;
	}
	/**
	 * @deprecated
	 */
	public Set/* <BundleCapability> */getServices() {
		Set s = new HashSet();
			if(null!=bundleinfo)
				s=bundleinfo.getServices();
		return s;
	}
	/**
	 * @deprecated
	 */
	public Integer getSize() {
		return bundleinfo.getSize();
	}
	/**
	 * @deprecated
	 */
	public String getSymbolicName() {
		if(null!=bundleinfo)
			return bundleinfo.getSymbolicName();
		else return "";

	}
	/**
	 * @deprecated
	 */
	public URI getUri() {
		return bundleinfo.getUri();

	}
	/**
	 * @deprecated
	 */
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
	/**
	 * @deprecated
	 */
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
	 * @deprecated
	 * @param ma {@link Manifest}
	 * @throws ParseException
	 */
	private void initializeBundleInformation(Manifest ma) throws ParseException {
		this.manifest = ma;
		this.bundleinfo = ManifestParser.parseManifest(this.manifest);
		addClasspathEntries();
	}
	/**
	 * @deprecated
	 */
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
	/**
	 * @deprecated
	 */
	public void setDescription(String description) {

		bundleinfo.setDescription(description);

	}
	/**
	 * @deprecated
	 */
	public void setDocumentation(String documentation) {
		bundleinfo.setDocumentation(documentation);
	}
	/**
	 * @deprecated
	 */
	public void setExecutionEnvironments(List/* <String> */executionEnvironment) {
		bundleinfo.setExecutionEnvironments(executionEnvironment);
	}
	/**
	 * @deprecated
	 */
	public void setId(String id) {
		bundleinfo.setId(id);

	}
	/**
	 * @deprecated
	 */
	public void setLicense(String license) {
		bundleinfo.setLicense(license);

	}
	/**
	 * @deprecated
	 */
	public void setPluginXml(String pluginXml) {
		this.pluginXml = pluginXml;
	}
	/**
	 * @deprecated
	 */
	public void setPresentationName(String presentationName) {
		bundleinfo.setPresentationName(presentationName);

	}
	/**
	 * @deprecated
	 */
	public void setSize(Integer size) {
		bundleinfo.setSize(size);
	}
	/**
	 * @deprecated
	 */
	public void setUri(URI uri) {
		bundleinfo.setUri(uri);

	}
	/**
	 * @deprecated
	 */
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
