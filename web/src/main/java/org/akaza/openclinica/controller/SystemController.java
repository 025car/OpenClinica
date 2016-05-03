package org.akaza.openclinica.controller;

import net.sf.saxon.functions.IndexOf;

import org.akaza.openclinica.bean.extract.ExtractPropertyBean;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.odmbeans.UserBean;
import org.akaza.openclinica.controller.healthcheck.DatabaseHealthCheck;
import org.akaza.openclinica.core.EmailEngine;
import org.akaza.openclinica.core.OpenClinicaMailSender;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.exception.OpenClinicaSystemException;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import org.akaza.openclinica.service.JobTriggerService;
import org.akaza.openclinica.service.pmanage.ParticipantPortalRegistrar;
import org.akaza.openclinica.service.pmanage.RandomizationRegistrar;
import org.akaza.openclinica.service.pmanage.SeRandomizationDTO;
import org.akaza.openclinica.service.rule.RuleSetService;
import org.akaza.openclinica.service.rule.expression.ExpressionService;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.io.FileSystemUtils;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.Meter;
import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheck;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.codahale.metrics.jvm.BufferPoolMetricSet;
import com.codahale.metrics.jvm.FileDescriptorRatioGauge;
import com.codahale.metrics.jvm.GarbageCollectorMetricSet;
import com.codahale.metrics.jvm.MemoryUsageGaugeSet;
import com.codahale.metrics.jvm.ThreadStatesGaugeSet;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.management.ObjectName;
import javax.servlet.ServletContext;

import java.io.File;
import java.io.FilePermission;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileOwnerAttributeView;
import java.nio.file.attribute.UserPrincipal;
import java.security.AccessController;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Map.Entry;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

@Controller
@RequestMapping(value = "/system")
@ResponseStatus(value = org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR)
public class SystemController {

    // Add in Spring Cor files /healthcheck path to avoid firewall
    @Autowired
    @Qualifier("dataSource")
    private BasicDataSource dataSource;
    @Autowired
    private JavaMailSenderImpl mailSender;

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    @RequestMapping(value = "/systemstatus", method = RequestMethod.POST)
    public ResponseEntity<HashMap> getSystemStatus() throws Exception {
        ResourceBundleProvider.updateLocale(new Locale("en_US"));
        HashMap<String, String> map = new HashMap<>();

        map.put("OpenClinica Version", CoreResources.getField("OpenClinica.version"));
        map.put("Java Version", System.getProperty("java.version"));
        map.put("Java Class Path", System.getProperty("java.class.path"));
        map.put("Java Home", System.getProperty("java.home"));
        map.put("OS Name", System.getProperty("os.name"));
        map.put("OS Version", System.getProperty("os.version"));
        map.put("OS Architecture", System.getProperty("os.arch"));
        map.put("File Separator", System.getProperty("file.separator"));
        map.put("Path Separator", System.getProperty("path.separator"));
        map.put("Line Separator", System.getProperty("line.separator"));
        map.put("User Home", System.getProperty("user.home"));
        map.put("User Directory", System.getProperty("user.dir"));
        map.put("User Name", System.getProperty("user.name"));

        Map<String, String> env = System.getenv();
        for (String envName : env.keySet()) {
            System.out.format("%s=%s%n", envName, env.get(envName));
        }

        DatabaseMetaData metaData = dataSource.getConnection().getMetaData();
        System.out.println(metaData.getSchemas());
        System.out.println(metaData.getUserName());
        System.out.println(metaData.getCatalogs());

        // metaData.getTablePrivileges("pg_catalog", schemaPattern, tableNamePattern)

        // metaData.getTablePrivileges();
        // metaData.getColumnPrivileges();

        try {
            UserAccountDAO udao = new UserAccountDAO(dataSource);
            UserAccountBean uBean = (UserAccountBean) udao.findByPK(1);

            if (uBean.getFirstName().equals("Root") && uBean.getLastName().equals("User")) {
                map.put("Root User Account First And Last Name", uBean.getFirstName() + " " + uBean.getLastName());
                map.put("Database Connection", "PASS");
            } else {
                map.put("Database Connection", "FAIL");
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return new ResponseEntity<HashMap>(map, org.springframework.http.HttpStatus.OK);

    }

    @RequestMapping(value = "/config", method = RequestMethod.GET)
    public ResponseEntity<HashMap> getConfig() throws Exception {
        ResourceBundleProvider.updateLocale(new Locale("en_US"));
        HashMap<String, Object> map = new HashMap<>();
        HashMap<String, Object> ocVersion = new HashMap<>();

        ResourceBundle resLicense = ResourceBundleProvider.getLicensingBundle();
        String versionRelease = resLicense.getString("Version_release");

        ocVersion.put("OC Edition", resLicense.getString("footer.edition.2"));
        ocVersion.put("OC Version number", versionRelease.substring(versionRelease.indexOf("Version") + 9, versionRelease.lastIndexOf('-') - 1));
        ocVersion.put("OC Version Changeset", versionRelease.substring(versionRelease.indexOf("- Changeset") + 13));

        map.put("OC.Version", ocVersion);

        HashMap<String, Object> datainfo = new HashMap<>();

        HashMap<String, String> databaseConfiguration = new HashMap<>();
        databaseConfiguration.put("dbType", CoreResources.getField("dbType"));
        databaseConfiguration.put("db", CoreResources.getField("db"));

        HashMap<String, String> emailSystem = new HashMap<>();
        emailSystem.put("mailHost", CoreResources.getField("mailHost"));
        emailSystem.put("mailPort", CoreResources.getField("mailPort"));
        emailSystem.put("mailProtocol", CoreResources.getField("mailProtocol"));
        emailSystem.put("mailSmtpAuth", CoreResources.getField("mailSmtpAuth"));
        emailSystem.put("mailSmtpStarttls.enable", CoreResources.getField("mailSmtpStarttls.enable"));
        emailSystem.put("mailSmtpsAuth", CoreResources.getField("mailSmtpsAuth"));
        emailSystem.put("mailSmtpsStarttls.enable", CoreResources.getField("mailSmtpsStarttls.enable"));
        emailSystem.put("mailSmtpConnectionTimeout", CoreResources.getField("mailSmtpConnectionTimeout"));
        emailSystem.put("mailErrorMsg", CoreResources.getField("mailErrorMsg"));

        HashMap<String, String> loggingConfiguration = new HashMap<>();
        loggingConfiguration.put("log.dir", CoreResources.getField("log.dir"));
        loggingConfiguration.put("logLocation", CoreResources.getField("logLocation"));
        loggingConfiguration.put("logLevel", CoreResources.getField("logLevel"));
        loggingConfiguration.put("syslog.host", CoreResources.getField("syslog.host"));
        loggingConfiguration.put("syslog.port", CoreResources.getField("syslog.port"));

        HashMap<String, String> news = new HashMap<>();
        news.put("rssUrl", CoreResources.getField("rssUrl"));
        news.put("rssMore", CoreResources.getField("rssMore"));
        news.put("about.text1", CoreResources.getField("about.text1"));
        news.put("about.text2", CoreResources.getField("about.text2"));

        HashMap<String, String> crfFileUploadConfiguration = new HashMap<>();
        crfFileUploadConfiguration.put("crfFileExtensions", CoreResources.getField("crfFileExtensions"));
        crfFileUploadConfiguration.put("crfFileExtensionSettings", CoreResources.getField("crfFileExtensionSettings"));

        HashMap<String, String> quartzSchedulerConfiguration = new HashMap<>();
        quartzSchedulerConfiguration.put("org.quartz.jobStore.misfireThreshold", CoreResources.getField("org.quartz.jobStore.misfireThreshold"));
        quartzSchedulerConfiguration.put("org.quartz.threadPool.threadCount", CoreResources.getField("org.quartz.threadPool.threadCount"));
        quartzSchedulerConfiguration.put("org.quartz.threadPool.threadPriority", CoreResources.getField("org.quartz.threadPool.threadPriority"));

        HashMap<String, String> facilityInformation = new HashMap<>();
        facilityInformation.put("FacCity", CoreResources.getField("FacCity"));
        facilityInformation.put("FacState", CoreResources.getField("FacState"));
        facilityInformation.put("FacZIP", CoreResources.getField("FacZIP"));
        facilityInformation.put("FacCountry", CoreResources.getField("FacCountry"));
        facilityInformation.put("FacContactName", CoreResources.getField("FacContactName"));
        facilityInformation.put("FacContactDegree", CoreResources.getField("FacContactDegree"));
        facilityInformation.put("FacContactPhone", CoreResources.getField("FacContactPhone"));
        facilityInformation.put("FacContactEmail", CoreResources.getField("FacContactEmail"));

        HashMap<String, String> usageStatisticsConfiguration = new HashMap<>();
        usageStatisticsConfiguration.put("collectStats", CoreResources.getField("collectStats"));
        usageStatisticsConfiguration.put("usage.stats.host", CoreResources.getField("usage.stats.host"));
        usageStatisticsConfiguration.put("usage.stats.port", CoreResources.getField("usage.stats.port"));
        usageStatisticsConfiguration.put("OpenClinica.version", CoreResources.getField("OpenClinica.version"));

        HashMap<String, String> ldapConfiguration = new HashMap<>();
        ldapConfiguration.put("ldap.enabled", CoreResources.getField("ldap.enabled"));
        ldapConfiguration.put("ldap.host", CoreResources.getField("ldap.host"));
        ldapConfiguration.put("ldap.loginQuery", CoreResources.getField("ldap.loginQuery"));
        ldapConfiguration.put("ldap.passwordRecoveryURL", CoreResources.getField("ldap.passwordRecoveryURL"));
        ldapConfiguration.put("ldap.userSearch.baseDn", CoreResources.getField("ldap.userSearch.baseDn"));
        ldapConfiguration.put("ldap.userSearch.query", CoreResources.getField("ldap.userSearch.query"));
        ldapConfiguration.put("ldap.userData.distinguishedName", CoreResources.getField("ldap.userData.distinguishedName"));
        ldapConfiguration.put("ldap.userData.username", CoreResources.getField("ldap.userData.username"));
        ldapConfiguration.put("ldap.userData.firstName", CoreResources.getField("ldap.userData.firstName"));
        ldapConfiguration.put("ldap.userData.lastName", CoreResources.getField("ldap.userData.lastName"));
        ldapConfiguration.put("ldap.userData.email", CoreResources.getField("ldap.userData.email"));
        ldapConfiguration.put("ldap.userData.organization", CoreResources.getField("ldap.userData.organization"));

        datainfo.put("database configuration", databaseConfiguration);
        datainfo.put("filePath", CoreResources.getField("filePath"));
        datainfo.put("attachedFileLocation", CoreResources.getField("attachedFileLocation"));
        datainfo.put("userAccountNotification", CoreResources.getField("userAccountNotification"));
        datainfo.put("adminEmail", CoreResources.getField("adminEmail"));
        datainfo.put("mail protocol", emailSystem);
        datainfo.put("sysURL", CoreResources.getField("sysURL"));
        datainfo.put("maxInactiveInterval", CoreResources.getField("maxInactiveInterval"));
        datainfo.put("logging configuration", loggingConfiguration);
        datainfo.put("news", news);
        datainfo.put("crf file upload configuration", crfFileUploadConfiguration);
        datainfo.put("supportURL", CoreResources.getField("supportURL"));
        datainfo.put("quartz scheduler configuration", quartzSchedulerConfiguration);
        datainfo.put("ccts.waitBeforeCommit", CoreResources.getField("ccts.waitBeforeCommit"));
        datainfo.put("facility information", facilityInformation);
        datainfo.put("exportFilePath", CoreResources.getField("exportFilePath"));
        datainfo.put("extract.number", CoreResources.getField("extract.number"));
        datainfo.put("usage statistics configuration", usageStatisticsConfiguration);
        datainfo.put("designerURL", CoreResources.getField("designerURL"));
        datainfo.put("ldap Configuration", ldapConfiguration);
        datainfo.put("portalURL", CoreResources.getField("portalURL"));
        datainfo.put("moduleManager", CoreResources.getField("moduleManager"));
        datainfo.put("xform.enabled", CoreResources.getField("xform.enabled"));

        map.put("datainfo.properties", datainfo);

        return new ResponseEntity<HashMap>(map, org.springframework.http.HttpStatus.OK);

    }

    @RequestMapping(value = "/modules/extract", method = RequestMethod.GET)
    public ResponseEntity<HashMap> getExtractModule() throws Exception {
        ResourceBundleProvider.updateLocale(new Locale("en_US"));
        HashMap<String, Object> map = new HashMap<>();

        ResourceBundle resLicense = ResourceBundleProvider.getLicensingBundle();

        HashMap<String, Object> extractMap = new HashMap<>();
        ArrayList<ExtractPropertyBean> extracts = CoreResources.getExtractProperties();
        int n = 0;
        for (ExtractPropertyBean extract : extracts) {
            n++;
            HashMap<String, String> extractmap = new HashMap<>();
            extractmap.put("odmType", extract.getOdmType());
            extractmap.put("file", Arrays.toString(extract.getFileName()));
            extractmap.put("fileDescription", extract.getFiledescription());
            extractmap.put("linkText", extract.getLinkText());
            extractmap.put("helpText", extract.getHelpText());
            extractmap.put("location", extract.getFileLocation());
            extractmap.put("exportname", Arrays.toString(extract.getExportFileName()));
            extractmap.put("zip", String.valueOf(extract.getZipFormat()));
            extractmap.put("deleteOld", String.valueOf(extract.getDeleteOld()));
            extractmap.put("success", extract.getSuccessMessage());
            extractmap.put("failure", extract.getFailureMessage());

            extractMap.put("extract." + n, extractmap);
        }

        HashMap<String, String> extractDatamart = new HashMap<>();
        HashMap<String, String> datamartRole = new HashMap<>();
        String username = CoreResources.getExtractField("db1.username");
        String password = CoreResources.getExtractField("db1.password");
        String url = CoreResources.getExtractField("db1.url");

        extractDatamart.put("db1.username", username);
        extractDatamart.put("db1.url", url);
        extractDatamart.put("db1.dataBase", CoreResources.getExtractField("db1.dataBase"));

        HashMap<String, String> extractNumber = new HashMap<>();
        extractNumber.put("extract.number", CoreResources.getExtractField("extract.number"));

        extractMap.put("extract.number", extractNumber);
        extractMap.put("DataMart", extractDatamart);

        HashMap<String, String> datamartMap = new HashMap();

        try (Connection conn = DriverManager.getConnection(url, username, password)) {
            datamartRole = getDbRoleProperties(conn, datamartRole, username);
            datamartMap.put("connection", "Open");
        } catch (Exception e) {
            datamartMap.put("connection", "Close");
        }
        map.put("Datamart Facts", datamartMap);
        map.put("extract.properties", extractMap);
        map.put("Role Properties", datamartRole);

        return new ResponseEntity<HashMap>(map, org.springframework.http.HttpStatus.OK);

    }

    @RequestMapping(value = "/modules/participate", method = RequestMethod.GET)
    public ResponseEntity<HashMap> getParticipateModule() throws Exception {
        ResourceBundleProvider.updateLocale(new Locale("en_US"));
        HashMap<String, Object> map = new HashMap<>();
        HashMap<String, String> mapParticipate = new HashMap<>();

        ParticipantPortalRegistrar ppr = new ParticipantPortalRegistrar();
        String result = ppr.getHostNameAvailability("123");
        if (!result.equals(ParticipantPortalRegistrar.UNKNOWN)) {
            mapParticipate.put("Participate", "Active");
            mapParticipate.put("Participate Version", "Comming Soon");

        } else {
            mapParticipate.put("Participate", "In Active");
        }

        map.put("Participate Facts", mapParticipate);

        return new ResponseEntity<HashMap>(map, org.springframework.http.HttpStatus.OK);
    }

    @RequestMapping(value = "/modules/randomize", method = RequestMethod.GET)
    public ResponseEntity<HashMap> getRandomizeModule() throws Exception {
        ResourceBundleProvider.updateLocale(new Locale("en_US"));
        HashMap<String, Object> map = new HashMap<>();

        map.put("Randomize Module", "Comming Soon");

        return new ResponseEntity<HashMap>(map, org.springframework.http.HttpStatus.OK);
    }

    @RequestMapping(value = "/modules/webservices", method = RequestMethod.GET)
    public ResponseEntity<HashMap> getWebServicesModule() throws Exception {
        ResourceBundleProvider.updateLocale(new Locale("en_US"));
        HashMap<String, Object> map = new HashMap<>();

        map.put("Web Services ", "Comming Soon");

        return new ResponseEntity<HashMap>(map, org.springframework.http.HttpStatus.OK);
    }

    @RequestMapping(value = "/modules/ruledesigner", method = RequestMethod.GET)
    public ResponseEntity<HashMap> getRuleDesignerModule() throws Exception {
        ResourceBundleProvider.updateLocale(new Locale("en_US"));
        HashMap<String, Object> map = new HashMap<>();

        map.put("Rule Designer ", "Comming Soon");

        return new ResponseEntity<HashMap>(map, org.springframework.http.HttpStatus.OK);
    }

    @RequestMapping(value = "/modules/auth", method = RequestMethod.GET)
    public ResponseEntity<HashMap> getLdapModule() throws Exception {
        ResourceBundleProvider.updateLocale(new Locale("en_US"));
        HashMap<String, Object> map = new HashMap<>();
        map.put("LDAP ", "Comming Soon");

        return new ResponseEntity<HashMap>(map, org.springframework.http.HttpStatus.OK);
    }

    @RequestMapping(value = "/modules/messaging", method = RequestMethod.GET)
    public ResponseEntity<HashMap> getMessagingModule() throws Exception {
        ResourceBundleProvider.updateLocale(new Locale("en_US"));
        HashMap<String, Object> map = new HashMap<>();

        String result = sendEmail(mailSender, "This is the Subject Of a Rest Call for Health Check", "This is the Body Of a Rest Call for Health Check");
        map.put("Mail connection", result);

        return new ResponseEntity<HashMap>(map, org.springframework.http.HttpStatus.OK);
    }

    @RequestMapping(value = "/filesystem", method = RequestMethod.GET)
    public ResponseEntity<HashMap> getFileSystem() throws Exception {
        ResourceBundleProvider.updateLocale(new Locale("en_US"));
        HashMap<String, Object> map = new HashMap<>();

        String filePath = CoreResources.getField("filePath");
        File file = new File(filePath);

        String tomcatPath = filePath.substring(0, filePath.indexOf("/openclinica.data"));
        File tomcatFile = new File(tomcatPath);

        float freeSpace = new File("/").getFreeSpace();

        map.put("Tomcat Directory Ownership", displayOwnerShipForTomcatDirectory(tomcatFile));
        map.put("Available Disk Space on Drive", freeSpace + " Byte   " + freeSpace / 1024 + " KB   " + freeSpace / 1024 / 1024 + " MB   " + freeSpace / 1024
                / 1024 / 1024 + " GB");
        map.put("openClinica.data Directory & File Count & Size", displayOCDataDirectoryCountAndSize(file));
        // map.put("List Of Directory and File Names in OpenClinica.data Directory", displayDirectoryContents(file, new
        // ArrayList()));

        return new ResponseEntity<HashMap>(map, org.springframework.http.HttpStatus.OK);

    }

    @RequestMapping(value = "/database", method = RequestMethod.GET)
    public ResponseEntity<HashMap> getDatabaseHealthCheck() throws Exception {
        ResourceBundleProvider.updateLocale(new Locale("en_US"));
        HashMap<String, Object> map = new HashMap<>();
        HashMap<String, String> mapRole = new HashMap<>();

        String username = CoreResources.getField("dbUser");
        String password = CoreResources.getField("dbPass");
        String url = CoreResources.getField("url");

        try (Connection conn = DriverManager.getConnection(url, username, password)) {
            map.put("Database Connection", "Open");
            map.put("Version", String.valueOf(conn.getMetaData().getDatabaseProductVersion()));

            mapRole = getDbRoleProperties(conn, mapRole, username);

        } catch (Exception e) {
            map.put("connection", "Close");
        }

        /*
         * HealthCheckRegistry healthChecks = new HealthCheckRegistry();
         * healthChecks.register("postgres Connection", new DatabaseHealthCheck(dataSource));
         * 
         * final Map<String, HealthCheck.Result> results = healthChecks.runHealthChecks();
         * 
         * for (Entry<String, HealthCheck.Result> entry : results.entrySet()) {
         * if (entry.getValue().isHealthy()) {
         * map.put(entry.getKey(), " is healthy");
         * } else {
         * map.put(entry.getKey(), " is UNHEALTHY: " + entry.getValue().getMessage());
         * final Throwable e = entry.getValue().getError();
         * if (e != null) {
         * e.printStackTrace();
         * }
         * }
         * }
         */

        map.put("Role Properties", mapRole);
        return new ResponseEntity<HashMap>(map, org.springframework.http.HttpStatus.OK);

    }

    public ArrayList<String> displayDirectoryContents(File dir, ArrayList<String> list) {
        try {
            File[] files = dir.listFiles();
            for (File file : files) {
                if (file.isDirectory()) {
                    list.add(file.getCanonicalPath());
                    // System.out.println(file.getCanonicalPath());
                    list = displayDirectoryContents(file, list);
                } else {
                    list.add(file.getCanonicalPath());
                    // System.out.println(file.getCanonicalPath());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    public ArrayList<HashMap<String, Object>> displayOCDataDirectoryCountAndSize(File file) throws IOException {
        ArrayList<HashMap<String, Object>> listOfHashMaps = new ArrayList<>();
        HashMap<String, Object> hashMap = null;
        if (file.isDirectory()) {
            hashMap = new HashMap<String, Object>();

            hashMap.put("Read Access", getReadAccess(file));
            hashMap.put("Write Access", getWriteAccess(file));
            int count = 0;
            int fileCount = getFilesCount(file, count);
            int dirCount = getNumberOfSubFolders(file.getCanonicalPath().toString());
            long length = 0;
            float sizeInByte = getFolderSize(file, length);

            hashMap.put("Folder Name", file.getName());
            hashMap.put("Files Count", String.valueOf(fileCount));
            hashMap.put("size", String.valueOf(sizeInByte) + " Byte   " + String.valueOf(sizeInByte / 1024) + " KB");

            listOfHashMaps.add(hashMap);
            if (dirCount != 0) {
                hashMap.put("Number of SubFolders", String.valueOf(dirCount));
                hashMap.put("Sub Folders", displaySubDirectoryCountAndSize(file));
            }
        }
        return listOfHashMaps;
    }

    public ArrayList<HashMap<String, Object>> displaySubDirectoryCountAndSize(File dir) throws IOException {
        ArrayList<HashMap<String, Object>> listOfHashMaps = new ArrayList<>();
        HashMap<String, Object> hashMap = null;
        File[] files = dir.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                hashMap = new HashMap<String, Object>();

                hashMap.put("Read Access", getReadAccess(file));
                hashMap.put("Write Access", getWriteAccess(file));

                int count = 0;
                int fileCount = getFilesCount(file, count);
                int dirCount = getNumberOfSubFolders(file.getCanonicalPath().toString());
                long length = 0;
                float sizeInByte = getFolderSize(file, length);

                hashMap.put("Folder Name", file.getName());
                hashMap.put("Files Count", String.valueOf(fileCount));
                hashMap.put("size", String.valueOf(sizeInByte) + " Byte   " + String.valueOf(sizeInByte / 1024) + " KB");

                listOfHashMaps.add(hashMap);
                if (dirCount != 0) {
                    hashMap.put("Number of SubFolders", String.valueOf(dirCount));
                    hashMap.put("Sub Folders", displaySubDirectoryCountAndSize(file));
                }
            }
        }
        return listOfHashMaps;
    }

    public ArrayList<HashMap<String, Object>> displayOwnerShipForTomcatDirectory(File file) throws IOException {
        ArrayList<HashMap<String, Object>> listOfHashMaps = new ArrayList<>();
        HashMap<String, Object> hashMap = null;
        if (file.isDirectory()) {
            hashMap = new HashMap<String, Object>();

            Path path = Paths.get(file.getCanonicalPath());
            FileOwnerAttributeView ownerAttributeView = Files.getFileAttributeView(path, FileOwnerAttributeView.class);
            UserPrincipal owner = ownerAttributeView.getOwner();

            hashMap.put("owner ship", owner.getName());
            hashMap.put("Folder Name", file.getName());
            listOfHashMaps.add(hashMap);
            int dirCount = getNumberOfSubFolders(file.getCanonicalPath().toString());
            if (dirCount != 0) {
                hashMap.put("Sub Folders", displayOwnerShipForTomcatSubDirectories(file));
            }
        }
        return listOfHashMaps;
    }

    public ArrayList<HashMap<String, Object>> displayOwnerShipForTomcatSubDirectories(File dir) throws IOException {
        ArrayList<HashMap<String, Object>> listOfHashMaps = new ArrayList<>();
        HashMap<String, Object> hashMap = null;
        File[] files = dir.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                hashMap = new HashMap<String, Object>();

                Path path = Paths.get(file.getCanonicalPath());
                FileOwnerAttributeView ownerAttributeView = Files.getFileAttributeView(path, FileOwnerAttributeView.class);
                UserPrincipal owner = ownerAttributeView.getOwner();

                hashMap.put("owner ship", owner.getName());
                hashMap.put("Folder Name", file.getName());
                listOfHashMaps.add(hashMap);
                int dirCount = getNumberOfSubFolders(file.getCanonicalPath().toString());
                if (dirCount != 0) {
                    hashMap.put("Sub Folders", displayOwnerShipForTomcatSubDirectories(file));
                }
            }
        }
        return listOfHashMaps;
    }

    public int getNumberOfSubFolders(String filePath) {
        File file1 = new File(filePath);
        File[] listFiles = file1.listFiles();
        int dirCount = 0;
        for (File f : listFiles) {
            if (f.isDirectory()) {
                dirCount++;
            }
        }
        return dirCount;
    }

    public int getFilesCount(File file, int count) {
        File[] files = file.listFiles();
        // int count = 0;
        for (File f : files)
            if (f.isDirectory()) {
                count = getFilesCount(f, count);
            } else {
                count++;
            }
        return count;
    }

    private long getFolderSize(File folder, long length) {
        File[] files = folder.listFiles();
        int count = files.length;
        for (int i = 0; i < count; i++) {
            if (files[i].isFile()) {
                length += files[i].length();
            } else {
                length = getFolderSize(files[i], length);
            }
        }
        return length;
    }

    public String getWriteAccess(File file) {

        if (file.canWrite()) {
            return "Yes";
        } else {
            return "No";
        }
    }

    public String getReadAccess(File file) {
        if (file.canRead()) {
            return "Yes";
        } else {
            return "No";
        }

    }

    public String sendEmail(JavaMailSenderImpl mailSender, String emailSubject, String message) throws OpenClinicaSystemException {

        logger.info("Sending email...");
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage);
            helper.setFrom(EmailEngine.getAdminEmail());
            helper.setTo("oc123@openclinica.com");
            helper.setSubject(emailSubject);
            helper.setText(message);

            mailSender.send(mimeMessage);
            return "Success";
        } catch (MailException me) {
            return "Failure";
        } catch (MessagingException me) {
            return "Failure";
        }
    }

    public HashMap<String, String> getDbRoleProperties(Connection conn, HashMap<String, String> mapRole, String username) throws SQLException {
        String query = "select * from pg_roles where rolname='" + username + "'";
        ResultSet resultSet = conn.createStatement().executeQuery(query);

        while (resultSet.next()) {
            mapRole.put("RoleName", resultSet.getString("rolname"));
            mapRole.put("SuperUser", resultSet.getString("rolsuper").equals("t") ? "True" : "False");
            mapRole.put("Inherit", resultSet.getString("rolinherit").equals("t") ? "True" : "False");
            mapRole.put("CreateRole", resultSet.getString("rolcreaterole").equals("t") ? "True" : "False");
            mapRole.put("CreateDb", resultSet.getString("rolcreatedb").equals("t") ? "True" : "False");
            mapRole.put("Login", resultSet.getString("rolcanlogin").equals("t") ? "True" : "False");
        }
        return mapRole;
    }
}
