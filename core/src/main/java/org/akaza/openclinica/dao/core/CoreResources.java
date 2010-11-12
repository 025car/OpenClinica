package org.akaza.openclinica.dao.core;

import org.akaza.openclinica.bean.extract.ExtractPropertyBean;
import org.akaza.openclinica.bean.service.PdfProcessingFunction;
import org.akaza.openclinica.bean.service.SasProcessingFunction;
import org.akaza.openclinica.bean.service.SqlProcessingFunction;
import org.akaza.openclinica.exception.OpenClinicaSystemException;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Properties;

public class CoreResources implements ResourceLoaderAware {

    private ResourceLoader resourceLoader;
    public static String PROPERTIES_DIR;
    private static String DB_NAME;
    private static Properties DATAINFO;
    private static Properties EXTRACTINFO;

    private Properties dataInfo;
    private Properties extractInfo;
    // private MessageSource messageSource;
    private static ArrayList<ExtractPropertyBean> extractProperties;

    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
        try {
            // setPROPERTIES_DIR();
            String dbName = dataInfo.getProperty("dataBase");
            DATAINFO = dataInfo;
            EXTRACTINFO = extractInfo;
            extractProperties = findExtractProperties();
            DB_NAME = dbName;
            SQLFactory factory = SQLFactory.getInstance();
            factory.run(dbName, resourceLoader);
        } catch (OpenClinicaSystemException e) {
            // throw new OpenClinicaSystemException(e.getMessage(), e.fillInStackTrace());
        }
    }

    public ResourceLoader getResourceLoader() {
        return resourceLoader;
    }

    // public MessageSource getMessageSource() {
    // return messageSource;
    // }
    //
    // public void setMessageSource(MessageSource messageSource) {
    // this.messageSource = messageSource;
    // }

    public static ArrayList<ExtractPropertyBean> getExtractProperties() {
        return extractProperties;
    }

    public void setExtractProperties(ArrayList extractProperties) {
        this.extractProperties = extractProperties;
    }

    private ArrayList<ExtractPropertyBean> findExtractProperties() {
        ArrayList<ExtractPropertyBean> ret = new ArrayList<ExtractPropertyBean>();

        // ExtractPropertyBean epbean = new ExtractPropertyBean();
        int i = 1;
        while (!getExtractField("extract." + i+".file").equals("")) {
            ExtractPropertyBean epbean = new ExtractPropertyBean();
            epbean.setId(i);
            // we will implement a find by id function in the front end
            epbean.setFileName(getExtractFields("extract." + i+".file"));
            // file name of the xslt stylesheet
            epbean.setFiledescription(getExtractField("extract." + i+".fileDescription"));
            // description of the choice of format
            epbean.setHelpText(getExtractField("extract." + i+".helptext"));
            // help text, currently in the alt-text of the link
            epbean.setLinkText(getExtractField("extract." + i+".linkText"));
            // link text of the choice of format
            // epbean.setRolesAllowed(getExtractField("xsl.allowed." + i).split(","));
            // which roles are allowed to see the choice?
            epbean.setFileLocation(getExtractField("extract." + i+".location"));
            // destination of the copied files
            // epbean.setFormat(getExtractField("xsl.format." + i));
            // if (("").equals(epbean.getFormat())) {
            
            epbean.setFormat("oc1.3");
            // }
            // formatting choice. currently permenantly set at oc1.3
            epbean.setExportFileName(getExtractFields("extract." + i+".exportname"));
            // destination file name of the copied files
            String whichFunction = getExtractField("extract."+i+".post").toLowerCase();
            // post-processing event after the creation
            // System.out.println("found post function: " + whichFunction);
   
            //added by JN: Zipformat comes from extract properties returns true by default
            epbean.setZipFormat(getExtractFieldBoolean("extract."+i+".zip"));
            epbean.setDeleteOld(getExtractFieldBoolean("extract."+i+".deleteOld"));
            epbean.setSuccessMessage(getExtractField("extract."+i+".success"));
            epbean.setFailureMessage(getExtractField("extract."+i+".failure"));
            
            if ("sql".equals(whichFunction)) {
                // set the bean within, so that we can access the file locations etc
                SqlProcessingFunction function = new SqlProcessingFunction(epbean);
                String whichSettings = getExtractField("xsl.post." + i + ".sql");
                if (!"".equals(whichSettings)) {
                    function.setDatabaseType(getExtractField(whichSettings + ".dataBase").toLowerCase());
                    function.setDatabaseUrl(getExtractField(whichSettings + ".url"));
                    function.setDatabaseUsername(getExtractField(whichSettings + ".username"));
                    function.setDatabasePassword(getExtractField(whichSettings + ".password"));
                } else {
                    // set default db settings here
                    function.setDatabaseType(getField("dataBase"));
                    function.setDatabaseUrl(getField("url"));
                    function.setDatabaseUsername(getField("username"));
                    function.setDatabasePassword(getField("password"));
                }
                // also pre-set the database connection stuff
                epbean.setPostProcessing(function);
                // System.out.println("found db password: " + function.getDatabasePassword());
            } else if ("pdf".equals(whichFunction)) {
                // TODO add other functions here
                epbean.setPostProcessing(new PdfProcessingFunction());
            } else if ("sas".equals(whichFunction)) {
                epbean.setPostProcessing(new SasProcessingFunction());
            }else if(!whichFunction.isEmpty()){
            	String postProcessorName = getExtractField(whichFunction+".postProcessor");
            	if(postProcessorName.equals("pdf")){
            		epbean.setPostProcessing(new PdfProcessingFunction());
            		epbean.setPostProcDeleteOld(getExtractFieldBoolean(whichFunction+".deleteOld"));
            		epbean.setPostProcZip(getExtractFieldBoolean(whichFunction+".zip"));
            		epbean.setPostProcLocation(getExtractField(whichFunction+".location"));
            		epbean.setPostProcExportName(getExtractField(whichFunction+".exportName"));
            	}
            	else if(postProcessorName.equals("sql")){
            		
            	}
            	 
            }
            else {
                // add a null here
                epbean.setPostProcessing(null);
            }
            ret.add(epbean);
            i++;
        }

        System.out.println("found " + ret.size() + " records in extract.properties");
        return ret;
    }

    public InputStream getInputStream(String fileName) throws IOException {
        return resourceLoader.getResource("classpath:properties/" + fileName).getInputStream();
    }

    public URL getURL(String fileName) throws IOException {
        return resourceLoader.getResource("classpath:properties/" + fileName).getURL();
    }

    public File getFile(String fileName) {
        try {
            InputStream inputStream = getInputStream(fileName);
            File f = new File(fileName);
            OutputStream outputStream = new FileOutputStream(f);
            byte buf[] = new byte[1024];
            int len;
            try {
                while ((len = inputStream.read(buf)) > 0)
                    outputStream.write(buf, 0, len);
            } finally {
                outputStream.close();
                inputStream.close();
            }
            return f;

        } catch (IOException e) {
            throw new OpenClinicaSystemException(e.getMessage(), e.fillInStackTrace());
        }
    }

    public void setPROPERTIES_DIR() {
        String resource = "classpath:properties/placeholder.properties";
        System.out.println("Resource" + resource);
        Resource scr = resourceLoader.getResource(resource);
        String absolutePath = null;
        try {
            // System.out.println("Resource" + resource);
            absolutePath = scr.getFile().getAbsolutePath();
            // System.out.println("Resource" + ((ClassPathResource) scr).getPath());
            // System.out.println("Resource" + resource);
            PROPERTIES_DIR = absolutePath.replaceAll("placeholder.properties", "");
            System.out.println("Resource" + PROPERTIES_DIR);
        } catch (IOException e) {
            throw new OpenClinicaSystemException(e.getMessage(), e.fillInStackTrace());
        }

    }

    public static String getDBName() {
        return DB_NAME;
    }

    public static String getField(String key) {
        String value = DATAINFO.getProperty(key);
        if (value != null) {
            value = value.trim();
        }
        return value == null ? "" : value;

    }

    // TODO internationalize
    public static String getExtractField(String key) {
        String value = EXTRACTINFO.getProperty(key);
        if (value != null) {
            value = value.trim();
        }
        return value == null ? "" : value;
    }

    //JN:The following method returns default of true when converting from string
    public static boolean getExtractFieldBoolean(String key) {
        String value = EXTRACTINFO.getProperty(key);
        if (value != null) {
            value = value.trim();
        }
        if(value==null)
        	return true;//Defaulting to true
        if(value.equalsIgnoreCase("false"))
        return false;
        else
        	return true;//defaulting to true
        
    }
    public static String[] getExtractFields(String key) {
        String value = EXTRACTINFO.getProperty(key);
        if (value != null) {
            value = value.trim();
        }
        return value.split(",");
    }
    public static ExtractPropertyBean findExtractPropertyBeanById(int id) {
        for (ExtractPropertyBean epbean : extractProperties) {
            if (epbean.getId() == id) {
                return epbean;
            }
        }
        return null;
    }

    public Properties getDataInfo() {
        return dataInfo;
    }

    public void setDataInfo(Properties dataInfo) {
        this.dataInfo = dataInfo;
    }

    public Properties getExtractInfo() {
        return extractInfo;
    }

    public void setExtractInfo(Properties extractInfo) {
        this.extractInfo = extractInfo;
    }

}
