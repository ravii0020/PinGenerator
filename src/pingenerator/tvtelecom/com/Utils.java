package pingenerator.tvtelecom.com;

/**
*
* @author Thanwa Ananthaseth
* @version 1.0 - Initial
* @since 02-Feb-2016
*/

public class Utils {
    public static String CharacterEncoding = "UTF-8";
    public static String DbPath = System.getProperty("catalina.base") + "/PinGen";
    public static String ExportFilePath = System.getProperty("catalina.base") + "/Exports";
    public static String appPath = "/PinGenerator/";
    //public static String appPath = "/";
    
    
    public static String csvFileForProvince = "/Single/SingleData/province.csv";
    public static String csvFileForProvinceSplitBy = ",";   //    "\\t";
    
    //public static String SingleIntecIP = "localhost";
    //public static String SingleIntecPort = "8900/SingleIntec";
    public static String SingleIntecIP = "172.31.193.126";
    public static String SingleIntecPort = "8900";
    
    //public static String SingleAppMailIP = "localhost";
    public static String SingleAppMailIP = "172.31.193.74";
    public static String SingleAppMailPort = "9900";
    
    //public static String SingleBatchIP = "localhost";
    public static String SingleBatchIP = "172.31.193.126";
    public static String SingleBatchPort = "9800";
    
    //public static String SingleScheduleIP = "localhost";
    public static String SingleScheduleIP = "172.31.193.126";
    public static String SingleSchedulePort = "9700";
    
    //public static String SingleTimeIP = "localhost";
    public static String SingleTimeIP = "172.31.193.126";
    public static String SingleTimePort = "9600";
    
    public static String OMUsernamePassword = "intecsubmitter:intecsubmitter";
    public static String OMURL = "http://172.24.89.58/totom/services/SyncOrderInterface";
    //public static String OMURL = "http://localhost:8800/HistoryCOrderOMServiceForTest";
}
