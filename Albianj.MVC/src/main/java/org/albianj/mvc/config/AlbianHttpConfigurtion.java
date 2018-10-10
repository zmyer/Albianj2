package org.albianj.mvc.config;

import org.albianj.verify.Validate;

import java.util.Map;

public class AlbianHttpConfigurtion {
	
	private String ContextPath = "/";
	private Map<String,ViewConfigurtion> pages = null;
	private Map<String,ViewConfigurtion> templates = null;
	private FileUploadConfigurtion fileUploadConfigurtion = null;
	private String rootPath;
	private String suffix = ".shtm";
	private Class<?> formatServiceClass;
	private ViewConfigurtion notFoundViewConfigurtion;
	private ViewConfigurtion errorViewConfigurtion;
	private String charset = "UTF-8";
	private HttpMode mode = HttpMode.production;
	private Map<String,ViewConfigurtion> masterView;
	private String welcomePage = "index.shtm";
	private Map<String,Object> items = null;
	private Map<String,CustomTagConfigurtion> customTags;
	private BrushingConfigurtion brushing;

	/**
	 * @return the pages
	 */
	public Map< String, ViewConfigurtion> getPages( ) {
		return pages;
	}


	
	/**
	 * @param pages the pages to set
	 */
	public void setPages( Map< String, ViewConfigurtion> pages ) {
		this.pages = pages;
	}


	/**
	 * @return the contextPath
	 */
	public String getContextPath( ) {
		return ContextPath;
	}

	
	/**
	 * @param contextPath the contextPath to set
	 */
	public void setContextPath( String contextPath ) {
		ContextPath = contextPath;
	}



	public Map<String,ViewConfigurtion> getTemplates( ) {
		return templates;
	}



	public void setTemplates( Map<String,ViewConfigurtion> templates ) {
		this.templates = templates;
	}



	public FileUploadConfigurtion getFileUploadConfigurtion( ) {
		return fileUploadConfigurtion;
	}



	public void setFileUploadConfigurtion( FileUploadConfigurtion fileUploadConfigurtion ) {
		this.fileUploadConfigurtion = fileUploadConfigurtion;
	}

	public String getRootPath(){
		return this.rootPath;
	}

	public void setRootPath(String rootPath){
		this.rootPath = rootPath;
	}

	public String getSuffix(){
		return this.suffix;
	}

	public void setSuffix(String suffix){
		this.suffix = suffix;
	}

	public void setFormatServiceClass(Class<?> formatServiceClass){
		this.formatServiceClass = formatServiceClass;
	}

	public Class<?> getFormatServiceClass(){
		return this.formatServiceClass;
	}

	public void setNotFoundViewConfigurtion(ViewConfigurtion notFoundViewConfigurtion){
		this.notFoundViewConfigurtion = notFoundViewConfigurtion;
	}

	public ViewConfigurtion getNotFoundViewConfigurtion(){
		return this.notFoundViewConfigurtion;
	}

	public void setErrorViewConfigurtion(ViewConfigurtion errorViewConfigurtion){
		this.errorViewConfigurtion = errorViewConfigurtion;
	}

	public ViewConfigurtion getErrorViewConfigurtion(){
		return this.errorViewConfigurtion;
	}

	public void setCharset(String charset){
		this.charset = charset;
	}

	public String getCharset(){
		return this.charset;
	}

	public void setMode(HttpMode mode){
		this.mode = mode;
	}

	public HttpMode getMode(){
		return this.mode;
	}

	public Map<String,ViewConfigurtion> getMasterViews(){
		return this.masterView;
	}

	public void setMasterViews(Map<String,ViewConfigurtion> pc){
		this.masterView = pc;
	}

	public String getWelcomePage(){
		return this.welcomePage;
	}

	public void setWelcomePage(String welcomePage){
		this.welcomePage = welcomePage;
	}

	public void setItems(Map<String,Object> items){
		this.items = items;
	}

	public Map<String, Object> getItems(){
		return this.items;
	}

	public Object getItem(String key){
		if(null == key) return null;
		if(Validate.isNullOrEmpty(items)) return null;
		return items.get(key);
	}

	public Map<String,CustomTagConfigurtion> getCustomTags(){
		return this.customTags;
	}
	public void setCustomTags(Map<String,CustomTagConfigurtion> customTags){
		this.customTags = customTags;
	}

	public BrushingConfigurtion getBrushing(){
		return this.brushing;
	}

	public void setBrushing(BrushingConfigurtion brushing){
		this.brushing = brushing;
	}

}
