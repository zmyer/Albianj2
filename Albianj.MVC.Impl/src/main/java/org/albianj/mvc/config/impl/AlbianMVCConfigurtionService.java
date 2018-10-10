package org.albianj.mvc.config.impl;


import org.albianj.io.Path;
import org.albianj.loader.AlbianClassLoader;
import org.albianj.logger.AlbianLoggerLevel;
import org.albianj.logger.IAlbianLoggerService2;
import org.albianj.mvc.*;
import org.albianj.mvc.config.*;
import org.albianj.mvc.internal.impl.ErrorView;
import org.albianj.mvc.internal.impl.NotFoundView;
import org.albianj.mvc.service.IAlbianMVCConfigurtionService;
import org.albianj.mvc.service.impl.AlbianFileUploadService;
import org.albianj.mvc.service.impl.AlbianFormatService;
import org.albianj.runtime.AlbianModuleType;
import org.albianj.service.AlbianServiceRouter;
import org.albianj.service.IAlbianServiceAttribute;
import org.albianj.service.parser.AlbianParserException;
import org.albianj.service.parser.FreeAlbianParserService;
import org.albianj.text.StringHelper;
import org.albianj.verify.Validate;
import org.albianj.xml.XmlParser;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.dom4j.Element;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

public class AlbianMVCConfigurtionService extends FreeAlbianParserService implements IAlbianMVCConfigurtionService {

    public String getServiceName(){
        return Name;
    }


    private String configFilename = "mvf.xml";
    protected AlbianHttpConfigurtion c = null;



    @Override
    public void init() throws AlbianParserException {
        c = new AlbianHttpConfigurtion();

        Map<String, IAlbianServiceAttribute> map = new LinkedHashMap<>();
        try {
            parserFile(c,configFilename);
        } catch (Exception e) {
            AlbianServiceRouter.getLogger2().logAndThrow(IAlbianLoggerService2.AlbianRunningLoggerName,
                    IAlbianLoggerService2.InnerThreadName, AlbianLoggerLevel.Error,null,
                    AlbianModuleType.AlbianMvf,AlbianModuleType.AlbianMvf.getThrowInfo(),
                    "loading the service.xml is error.");
        }

        return;
    }

    public void parserFile(AlbianHttpConfigurtion c, String filename){
        Document doc = null;
        try {
            String realFilename = confirmConfigFile(filename);
            doc = XmlParser.load(realFilename);
        } catch (Exception e) {
            AlbianServiceRouter.getLogger2().logAndThrow(IAlbianLoggerService2.AlbianRunningLoggerName,
                    IAlbianLoggerService2.InnerThreadName, AlbianLoggerLevel.Error,e,
                    AlbianModuleType.AlbianMvf,AlbianModuleType.AlbianMvf.getThrowInfo(),
                    "loading the mvc.xml is error.");
        }
        if (null == doc) {
            AlbianServiceRouter.getLogger2().logAndThrow(IAlbianLoggerService2.AlbianRunningLoggerName,
                    IAlbianLoggerService2.InnerThreadName, AlbianLoggerLevel.Error,null,
                    AlbianModuleType.AlbianMvf,AlbianModuleType.AlbianMvf.getThrowInfo(),
                    "loading the mvc.xml is error. the file is null.");
        }

        String rootPath = XmlParser.getAttributeValue(doc,"Mvf/RootPath","Value");
        if(Validate.isNullOrEmptyOrAllSpace(rootPath)){
            AlbianServiceRouter.getLogger2().logAndThrow(IAlbianLoggerService2.AlbianRunningLoggerName,
                    IAlbianLoggerService2.InnerThreadName, AlbianLoggerLevel.Error,null,
                    AlbianModuleType.AlbianMvf,AlbianModuleType.AlbianMvf.getThrowInfo(),
                    "RootPath for mvf is empry or null.");

        }
        c.setRootPath(rootPath);

        String suffix = XmlParser.getAttributeValue(doc,"Mvf/Suffix","Value");
        if(Validate.isNullOrEmptyOrAllSpace(suffix)){
            suffix = ".shtm";
        }
        c.setSuffix(suffix);

        String welcomePath = XmlParser.getAttributeValue(doc,"Mvf/WelcomePage","Path");
        if(Validate.isNullOrEmptyOrAllSpace(welcomePath)){
            welcomePath = "/index.shtm";
        }
        c.setWelcomePage(welcomePath);


        String context = XmlParser.getAttributeValue(doc,"Mvf/Context","Value");
        if(!Validate.isNullOrEmptyOrAllSpace(context)){
            c.setContextPath(context);
        }

        String charset = XmlParser.getAttributeValue(doc,"Mvf/Charset","Value");
        if(!Validate.isNullOrEmptyOrAllSpace(charset)){
            c.setCharset(charset);
        }

        String mode = XmlParser.getAttributeValue(doc,"Mvf/Mode","Value");
        if(!Validate.isNullOrEmptyOrAllSpace(mode)){
            HttpMode m = Enum.valueOf(HttpMode.class,mode);
            c.setMode(m);
        }

        String formatClassname = XmlParser.getAttributeValue(doc,"Mvf/FormatService","ClassName");
        if(!Validate.isNullOrEmptyOrAllSpace(formatClassname)){
            try {
                Class<?> format = AlbianClassLoader.getSystemClassLoader().loadClass(formatClassname);
                c.setFormatServiceClass(format);
            } catch (ClassNotFoundException e) {
                AlbianServiceRouter.getLogger2().logAndThrow(IAlbianLoggerService2.AlbianRunningLoggerName,
                        IAlbianLoggerService2.InnerThreadName, AlbianLoggerLevel.Error,e,
                        AlbianModuleType.AlbianMvf,AlbianModuleType.AlbianMvf.getThrowInfo(),
                        "cannot found formatservice class:%s.",formatClassname);
            }
        } else {
            c.setFormatServiceClass(AlbianFormatService.class);
        }

//        String templateClassname = XmlParser.getAttributeValue(doc,"Mvf/TemplateService","ClassName");
//        if(!Validate.isNullOrEmptyOrAllSpace(templateClassname)){
//            try {
//                Class<?> template = AlbianClassLoader.getSystemClassLoader().loadClass(templateClassname);
//                c.setFormatServiceClass(template);
//            } catch (ClassNotFoundException e) {
//                AlbianServiceRouter.getLogger2().logAndThrow(IAlbianLoggerService2.AlbianRunningLoggerName,
//                        IAlbianLoggerService2.InnerThreadName, AlbianLoggerLevel.Error,e,
//                        AlbianModuleType.AlbianMvf,AlbianModuleType.AlbianMvf.getThrowInfo(),
//                        "cannot found template service class:%s.",formatClassname);
//            }
//        } else {
//            c.setFormatServiceClass(AlbianVelocityTemplateService.class);
//        }

        Element elt = XmlParser.analyzeSingle(doc,"Mvf/FileUploadService");
        FileUploadConfigurtion fuc = null;
        if(null == elt){
             fuc = new FileUploadConfigurtion();
            fuc.setFolder("/tmp/albianj-mvf");
            fuc.setFileUploadServiceClass(AlbianFileUploadService.class);
        } else {
            fuc = parserFileUploadService(elt);
        }
        c.setFileUploadConfigurtion(fuc);
//
        List mvNodes = XmlParser.analyze(doc,"Mvf/MasterViews/MasterView");
        if(!Validate.isNullOrEmpty(mvNodes)){
            Map<String,ViewConfigurtion> mvs = parserMasterViews(mvNodes);
            if(!Validate.isNullOrEmpty(mvs)){
                c.setMasterViews(mvs);
            }
        }

        Element eNotFoundPage = XmlParser.analyzeSingle(doc,"Mvf/NotFoundView");
        ViewConfigurtion notFoundViewConfigurtion = parserNotFoundPage(eNotFoundPage);
        c.setNotFoundViewConfigurtion(notFoundViewConfigurtion);

        Element eErrorPage = XmlParser.analyzeSingle(doc,"Mvf/ErrorView");
        ViewConfigurtion errorViewConfigurtion = parserErrorPage(eErrorPage);
        c.setErrorViewConfigurtion(errorViewConfigurtion);


        List pagesNodes = XmlParser.analyze(doc,"Mvf/Views");
        if(!Validate.isNullOrEmpty(pagesNodes)) {
            parserPages(c, pagesNodes);
        }

        List itemNodes = XmlParser.analyze(doc,"Mvf/Items/Item");
        if(!Validate.isNullOrEmpty(itemNodes)) {
            Map<String,Object> items = parserItems(itemNodes);
            c.setItems(items);
        }

        Map<String,CustomTagConfigurtion> ctcs = new HashedMap();
        List tagsFileNodes = XmlParser.analyze(doc,"Mvf/Tags/Include");
        if(!Validate.isNullOrEmpty(tagsFileNodes)){
            for(Object e : tagsFileNodes){
                Element eFile = (Element) e;
                String fname = XmlParser.getAttributeValue(eFile,"File");
                parserTagsFile(ctcs,fname);

            }
        }

        List tagsNodes = XmlParser.analyze(doc,"Mvf/Tags/Tag");
        if(!Validate.isNullOrEmpty(tagsNodes)) {
            for(Object e : tagsNodes){
                CustomTagConfigurtion ctc = parserTags((Element) e);
                if(null != ctc){
                    ctcs.put(ctc.getName(),ctc);
                }
            }
        }

        if(!Validate.isNullOrEmpty(ctcs)){
            c.setCustomTags(ctcs);
        }

        Element eltBrushing = XmlParser.analyzeSingle(doc,"Mvf/Brushing");
        if(null != eltBrushing){
            BrushingConfigurtion brushing = new BrushingConfigurtion();
            String sUnitTime = XmlParser.getSingleChildNodeValue(eltBrushing,"UnitTime");
            if(!Validate.isNullOrEmptyOrAllSpace(sUnitTime)){
                brushing.setUnitTime(Long.parseLong(sUnitTime));
            }
            String sRequestCount = XmlParser.getSingleChildNodeValue(eltBrushing,"RequestCount");
            if(!Validate.isNullOrEmptyOrAllSpace(sRequestCount)){
                brushing.setRequestCount(Long.parseLong(sRequestCount));
            }
            c.setBrushing(brushing);
        }

    }

    private void parserTagsFile(Map<String,CustomTagConfigurtion> map, String filename){
        Document doc = null;
        try {
            String realFilename = confirmConfigFile(filename);
            doc = XmlParser.load(realFilename);
        } catch (Exception e) {
            AlbianServiceRouter.getLogger2().logAndThrow(IAlbianLoggerService2.AlbianRunningLoggerName,
                    IAlbianLoggerService2.InnerThreadName, AlbianLoggerLevel.Error,null,
                    AlbianModuleType.AlbianMvf,AlbianModuleType.AlbianMvf.getThrowInfo(),
                    "loading the mvc.xml is fail.");
        }
        if (null == doc) {
            AlbianServiceRouter.getLogger2().logAndThrow(IAlbianLoggerService2.AlbianRunningLoggerName,
                    IAlbianLoggerService2.InnerThreadName, AlbianLoggerLevel.Error,null,
                    AlbianModuleType.AlbianMvf,AlbianModuleType.AlbianMvf.getThrowInfo(),
                    "loading the mvc.xml is fail.");
        }


        List tagsNodes = XmlParser.analyze(doc,"Tags/Tag");
        for(Object e : tagsNodes){
            CustomTagConfigurtion ctc = parserTags((Element) e);
            if(null != ctc){
                map.put(ctc.getName(),ctc);
            }
        }
        return;
    }

    private CustomTagConfigurtion parserTags(Element elt){
        String name = XmlParser.getAttributeValue(elt,"Name");
        String classname = XmlParser.getAttributeValue(elt,"ClassName");
        if(Validate.isNullOrEmptyOrAllSpace(name) || Validate.isNullOrEmptyOrAllSpace(classname)){
            AlbianServiceRouter.getLogger2().logAndThrow(IAlbianLoggerService2.AlbianRunningLoggerName,
                    IAlbianLoggerService2.InnerThreadName, AlbianLoggerLevel.Error,null,
                    AlbianModuleType.AlbianMvf,AlbianModuleType.AlbianMvf.getThrowInfo(),
                    "custom tag is fail.it's name or classname is null or empty or space.");
        }
        CustomTagConfigurtion ctc = new CustomTagConfigurtion(name,classname);
        return ctc;
    }


    private Map<String,Object> parserItems(List itemNodes){
        Map<String,Object> items = new HashedMap();
        for (Object node : itemNodes) {
            Element elt = XmlParser.toElement(node);
            String key = XmlParser.getAttributeValue(elt,"Key");
            String value = XmlParser.getAttributeValue(elt,"Value");
            if(Validate.isNullOrEmptyOrAllSpace(key)){
                items.put(key,value);
            }
        }
        return items;
    }



    public void parserPages(AlbianHttpConfigurtion c, List pagesNodes){
        for (Object node : pagesNodes) {
            Element elt = XmlParser.toElement(node);

            List includes = XmlParser.getChildNodes(elt,"Include");

            if(!Validate.isNullOrEmpty(includes)) {
                for(Object iNode : includes){
                    Element eNode = (Element)  iNode;
                    String filename = XmlParser.getAttributeValue(eNode,"FileName");
                    parserFile(c,filename);
                }
            } else {

                String packageName = XmlParser.getAttributeValue(elt, "Package");
                if(Validate.isNullOrEmptyOrAllSpace(packageName)) {

                }

                String path = XmlParser.getAttributeValue(elt,"Path");
                if(Validate.isNullOrEmptyOrAllSpace(path)) path = c.getRootPath();

                String sBinding = XmlParser.getAttributeValue(elt, "AutoBinding");
                boolean isBinding = true;
                if(!Validate.isNullOrEmptyOrAllSpace(sBinding)){
                    isBinding = Boolean.parseBoolean(sBinding);
                }

                Map<String,ViewConfigurtion> pageConfigurtionMap = c.getPages();
                if(null == pageConfigurtionMap){
                    pageConfigurtionMap = new HashMap<>();
                    c.setPages(pageConfigurtionMap);
                }

                Map<String,ViewConfigurtion> templateConfigurtionMap = c.getTemplates();
                if(null == templateConfigurtionMap){
                    templateConfigurtionMap = new HashMap<>();
                    c.setTemplates(templateConfigurtionMap);
                }


                String fpath = Path.join(c.getRootPath(),path);
                List<String> templates = findAllTemplates(c,fpath);
                for(String t : templates) {
                    ViewConfigurtion pc =  parserAutoMappingPage(packageName, path, isBinding, t);
                    if(null == pc) continue;
                    pageConfigurtionMap.put(pc.getFullClassName(),pc);
                    templateConfigurtionMap.put(pc.getTemplate(),pc);
                }

                List pages = XmlParser.getChildNodes(elt, "View");
                if(!Validate.isNullOrEmpty(pages)){
                    for(Object ePageNode : pages) {
                        ViewConfigurtion pc = parserCustomPage(packageName, path, isBinding, (Element) ePageNode);
                        if(null == pc) continue;
                        pageConfigurtionMap.put(pc.getFullClassName(),pc);
                        templateConfigurtionMap.put(pc.getTemplate(),pc);
                    }
                }

            }
        }
    }


    private ViewConfigurtion parserCustomPage(String packageName, String fatherPath, boolean fatherBinding, Element elt){

        String template = XmlParser.getAttributeValue(elt,"Template");
        if(Validate.isNullOrEmptyOrAllSpace(template)){

        }
        String classname = XmlParser.getAttributeValue(elt, "ClassName");
        String sBinding = XmlParser.getAttributeValue(elt, "AutoBinding");
        boolean isBinding = fatherBinding;
        if(!Validate.isNullOrEmptyOrAllSpace(sBinding)){
            isBinding = Boolean.parseBoolean(sBinding);
        }
        return parserPage(false,packageName,fatherPath,template,classname,isBinding);
    }

    private ViewConfigurtion parserPage(boolean isAutoBinding, String packageName, String fatherPath,
                                        String template, String classname, boolean isBinding){
        ViewConfigurtion pc = new ViewConfigurtion();
        if(Validate.isNullOrEmptyOrAllSpace(classname)) {
            if(template.startsWith(fatherPath)){
                template = template.substring(fatherPath.length());
            }
            String childClassname = template.replace(File.separator,".");
            //sub the template suffix
            childClassname = childClassname.substring(0,childClassname.lastIndexOf("."));
            int pos = childClassname.lastIndexOf(".");// have child package?
            String cpkg = null;
            String simpleClassname = null;
            if(0 <= pos) { // have child package
                simpleClassname = childClassname.substring(pos + 1);
                cpkg =  childClassname.substring(0,pos + 1);
            }else {
                simpleClassname = childClassname;
            }

            if(simpleClassname.contains("_")) {
                String[] allClassname = simpleClassname.split("_");
                StringBuffer sb = new StringBuffer();
                for(String s : allClassname) {
                    sb.append(StringHelper.captureName(s));
                }
                simpleClassname = sb.toString();
            } else {
                simpleClassname = StringHelper.captureName(simpleClassname);
            }
            if(-1 != pos) { //have child package
                simpleClassname  = cpkg + simpleClassname;
            }
            if(simpleClassname.startsWith(".")) {
                classname = packageName +  simpleClassname;
            } else {
                classname = packageName + "." + simpleClassname;
            }
        }
        template = Path.joinWithFilename(template,fatherPath);
        pc.setAutoBinding(isBinding);
        Class<? extends View> cla = null;
        boolean isload = true;
        int trytime = 3;
        do {
            try {
                cla = (Class<? extends View>) AlbianClassLoader.getInstance().loadClass(classname);
                isload = true;
            } catch (Exception | NoClassDefFoundError e) {
                int idx = classname.lastIndexOf(".") + 1;
                String simpleClassname = classname.substring(idx);
                classname = classname.substring(0, idx) + StringHelper.captureName(simpleClassname);
                isload = false;
            }
        }while(!isload && (0 < (trytime--)));
        if(!isload){
//            if(!isAutoBinding) {
            AlbianServiceRouter.getLogger2().log(IAlbianLoggerService2.AlbianRunningLoggerName,
                    IAlbianLoggerService2.InnerThreadName,AlbianLoggerLevel.Warn,
                    "template -> %s is not finding behind class -> %s.", template,classname);
//            }
            return null;
        }
        pc.setRealClass(cla);
        pc.setFullClassName(classname);
        pc.setTemplate(template);
        reflectPage(cla,pc);
        return pc;
    }


    private ViewConfigurtion parserAutoMappingPage(String packageName, String fatherPath, boolean fatherBinding, String template){
        return parserPage(true,packageName,fatherPath,template,null,fatherBinding);
    }


    public static void reflectPage(Class<? extends View> cla, ViewConfigurtion pc){
        Field[] fields = cla.getDeclaredFields();
        Map<String,ViewFieldConfigurtion> fieldConfigurtionMap = new HashMap<>();
        for(Field f : fields){
            if(f.isAnnotationPresent(NotHttpFieldAttribute.class)) continue;;
            ViewFieldConfigurtion pfc = new ViewFieldConfigurtion();
            f.setAccessible(true);
            Class t = f.getType();
            String bindingName = null;
            String name = f.getName();
            if(f.isAnnotationPresent(HttpFieldAttribute.class)){
                HttpFieldAttribute hfa = f.getAnnotation(HttpFieldAttribute.class);
                bindingName = hfa.Name();
            } else {
                bindingName = name;
            }
            pfc.setBindingName(bindingName);
            pfc.setField(f);
            pfc.setName(name);
            pfc.setType(t);
            fieldConfigurtionMap.put(bindingName,pfc);
        }

        if(!Validate.isNullOrEmpty(fieldConfigurtionMap)) {
            pc.setFields(fieldConfigurtionMap);
        }

        Map<String,ViewActionConfigurtion> pageActionConfigurtionMap = new HashMap<>();
        Method[] methods = cla.getMethods();
        for(Method m : methods){
            if(m.isAnnotationPresent(NotHttpActionAttribute.class)) continue;;
            if(!ActionResult.class.isAssignableFrom(m.getReturnType())) continue;;
            ViewActionConfigurtion pac = new ViewActionConfigurtion();
            String bindingName = null;
            String name = m.getName();
            if(m.isAnnotationPresent(HttpActionAttribute.class)){
                HttpActionAttribute haa = m.getAnnotation(HttpActionAttribute.class);
                if(Validate.isNullOrEmptyOrAllSpace(haa.Name())){
                    bindingName = name;
                } else {
                    bindingName = haa.Name();
                }
                pac.setHttpActionMethod(haa.Method());
            } else {
                bindingName = name;
            }

            if(bindingName.equals(name) && "load".equals(bindingName)){
                pac.setHttpActionMethod(HttpActionMethod.Get);
            }
            if(bindingName.equals(name) && "execute".equals(bindingName)){
                pac.setHttpActionMethod(HttpActionMethod.Post);
            }
            m.setAccessible(true);
            pac.setName(name);
            pac.setBindingName(bindingName);
            pac.setMethod(m);
            pageActionConfigurtionMap.put(bindingName,pac);
        }
        if(!Validate.isNullOrEmpty(pageActionConfigurtionMap)){
            pc.setActions(pageActionConfigurtionMap);
        }
    }

    private List< String > findAllTemplates(AlbianHttpConfigurtion c, String path ) {
        String rpath = StringUtils.isBlank( path ) ? c.getRootPath( ) : path;
        List< String > fs = new ArrayList<>( );
        String suffix = c.getSuffix();
        suffix = suffix.startsWith(".") ? suffix.substring(1) : suffix;
        Collection<File> files = FileUtils.listFiles(new File(rpath),new String[]{suffix},true);
        int idx = c.getRootPath( ).length();
        for(File f : files){
            fs.add(f.getAbsolutePath().substring(idx - 1));
        }
        return fs;
    }

    private FileUploadConfigurtion parserFileUploadService(Element elt){
        FileUploadConfigurtion fuc = new FileUploadConfigurtion();
        String classname = XmlParser.getAttributeValue(elt,"ClassName");
        if(Validate.isNullOrEmptyOrAllSpace(classname)){
            fuc.setFileUploadServiceClass(AlbianFileUploadService.class);
        } else{
            try {
                Class<?> cla = AlbianClassLoader.getSystemClassLoader().loadClass(classname);
                fuc.setFileUploadServiceClass(cla);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

        }

        String sMaxFileSize = XmlParser.getAttributeValue(elt,"FileSizeMax");
        if(Validate.isNullOrEmptyOrAllSpace(sMaxFileSize)){
            fuc.setMaxFileSize(4 * 1024 * 1024);
        } else {
            fuc.setMaxFileSize(Long.parseLong(sMaxFileSize));
        }

        String sTempPath = XmlParser.getAttributeValue(elt,"TempPath");
        if(Validate.isNullOrEmptyOrAllSpace(sTempPath)){
            fuc.setFolder("/tmp/albianj-mvf");
        } else {
            fuc.setFolder(sTempPath);
        }

        String sRequestSizeMax = XmlParser.getAttributeValue(elt,"RequestSizeMax");
        if(Validate.isNullOrEmptyOrAllSpace(sRequestSizeMax)){
            fuc.setMaxRequestSize(10 * 1024 * 1024);
        } else {
            fuc.setMaxRequestSize(Long.parseLong(sRequestSizeMax));
        }

        return fuc;
    }

    private ViewConfigurtion parserNotFoundPage(Element elt){
        ViewConfigurtion pc = new ViewConfigurtion();
        if(null == elt){
            pc.setAutoBinding(true);
            pc.setFullClassName(NotFoundView.class.getName());
            pc.setTemplate("/WEB-INF/internal/NotFoundView.shtm");
            pc.setRealClass(NotFoundView.class);
        } else {
            String template = XmlParser.getAttributeValue(elt,"Template");
            if(Validate.isNullOrEmptyOrAllSpace(template)){
                pc.setTemplate("/WEB-INF/internal/NotFoundView.shtm");
            } else {
                pc.setTemplate(template);
            }
            String classname = XmlParser.getAttributeValue(elt,"ClassName");
            if(Validate.isNullOrEmptyOrAllSpace(classname)){
                pc.setFullClassName(NotFoundView.class.getName());
                pc.setRealClass(NotFoundView.class);
            } else {
                try {
                    Class<? extends View> cla = (Class<? extends View>) AlbianClassLoader.getInstance().loadClass(classname);
                    pc.setFullClassName(classname);
                    pc.setRealClass(cla);
                } catch (ClassNotFoundException e) {
                    AlbianServiceRouter.getLogger2().log(IAlbianLoggerService2.AlbianRunningLoggerName,
                            IAlbianLoggerService2.InnerThreadName,AlbianLoggerLevel.Warn,e,
                            "notfound page class:%s is not found.", classname);
                }
            }

            boolean isBinding = true;
            String sBinding = XmlParser.getAttributeValue(elt,"AutoBinding");
            if(!Validate.isNullOrEmptyOrAllSpace(sBinding)){
                isBinding = Boolean.parseBoolean(sBinding);
            }
            pc.setAutoBinding(isBinding);
        }

        reflectPage(pc.getRealClass(),pc);
        return pc;
    }

    private ViewConfigurtion parserErrorPage(Element elt){
        ViewConfigurtion pc = new ViewConfigurtion();
        if(null == elt){
            pc.setAutoBinding(true);
            pc.setFullClassName(ErrorView.class.getName());
            pc.setTemplate("/WEB-INF/internal/ErrorView.shtm");
            pc.setRealClass(ErrorView.class);
        } else {
            String template = XmlParser.getAttributeValue(elt,"Template");
            if(Validate.isNullOrEmptyOrAllSpace(template)){
                pc.setTemplate("/WEB-INF/internal/ErrorView.shtm");
            } else {
                pc.setTemplate(template);
            }
            String classname = XmlParser.getAttributeValue(elt,"ClassName");
            if(Validate.isNullOrEmptyOrAllSpace(classname)){
                pc.setFullClassName(ErrorView.class.getName());
                pc.setRealClass(ErrorView.class);
            } else {
                try {
                    Class<? extends View> cla = (Class<? extends View>) AlbianClassLoader.getInstance().loadClass(classname);
                    pc.setFullClassName(classname);
                    pc.setRealClass(cla);
                } catch (ClassNotFoundException e) {
                    AlbianServiceRouter.getLogger2().log(IAlbianLoggerService2.AlbianRunningLoggerName,
                            IAlbianLoggerService2.InnerThreadName,AlbianLoggerLevel.Warn,e,
                            "error page class:%s is not found.", classname);
                }
            }

            boolean isBinding = true;
            String sBinding = XmlParser.getAttributeValue(elt,"AutoBinding");
            if(!Validate.isNullOrEmptyOrAllSpace(sBinding)){
                isBinding = Boolean.parseBoolean(sBinding);
            }
            pc.setAutoBinding(isBinding);
        }

        reflectPage(pc.getRealClass(),pc);
        return pc;
    }

    private Map<String,ViewConfigurtion> parserMasterViews(List mvNodes){
        Map<String,ViewConfigurtion> map = new HashMap<>();
        for (Object node : mvNodes) {
            Element elt = XmlParser.toElement(node);
            ViewConfigurtion vc = parserMasterView(elt);
            if(null != vc) map.put(vc.getName(),vc);
        }
        if(Validate.isNullOrEmpty(map)) return null;
        return map;
    }

    private ViewConfigurtion parserMasterView(Element elt){
        String classname = XmlParser.getAttributeValue(elt,"ClassName");
        String template = XmlParser.getAttributeValue(elt,"Template");
        String sBinding = XmlParser.getAttributeValue(elt, "AutoBinding");
        String sName = XmlParser.getAttributeValue(elt, "Name");
        boolean isBinding = true;
        if(!Validate.isNullOrEmptyOrAllSpace(sBinding)){
            isBinding = Boolean.parseBoolean(sBinding);
        }
        if(Validate.isNullOrEmptyOrAllSpace(classname)
                || Validate.isNullOrEmptyOrAllSpace(template)
                || Validate.isNullOrEmptyOrAllSpace(sName))
            return null;
        template = Path.joinWithFilename(template,c.getRootPath());
        try {
            ViewConfigurtion pc = new ViewConfigurtion();
            Class<? extends MView> cla = (Class<? extends MView>) AlbianClassLoader.getSystemClassLoader().loadClass(classname);
            pc.setAutoBinding(isBinding);
            pc.setRealClass(cla);
            pc.setFullClassName(classname);
            pc.setTemplate(template);
            pc.setName(sName);
            reflectPage(cla,pc);
            return pc;
        } catch (ClassNotFoundException e) {
            AlbianServiceRouter.getLogger2().log(IAlbianLoggerService2.AlbianRunningLoggerName,
                    IAlbianLoggerService2.InnerThreadName,AlbianLoggerLevel.Warn,e,
                    "masterview:%s find class:%s is fail.", sName, classname);
        }
        return null;
    }
    @Override
    public AlbianHttpConfigurtion getHttpConfigurtion() {
        return this.c;
    }
}
