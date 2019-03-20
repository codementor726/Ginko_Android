package com.ginko.context;

public class ConstValues {
    //	public static final String baseUrl = "http://192.168.0.77:9090/xchangeApi";
//    public static final String baseUrl = "http://192.168.254.102:8080/xchangeApi";

    public static final String preferenceName = "com.ginko";

    //public static final String baseUrl = "http://www.xchangewith.me/api/v2/api";//develop version
    //public static final String baseUrl = "http://dev.xchangewith.me/api";//test version
    public static final String baseUrl = "http://api.ginko.mobi/v3";// product version

    public static final String buildVersion = "0208";

	public static final String DEFAULT_DATA_FORMAT = "yyyy-MM-dd HH:mm:ss";
	//public static final String SENDER_ID = "873341200052";//debug key
    public static final String SENDER_ID = "187384323103";

    public static final String LOG_TAG = "GINKO";
	
	public static final String EXTRA_MESSAGE = "message";
	public static final String PROPERTY_REG_ID = "registration_id";
	public static final String PROPERTY_APP_VERSION = "appVersion";
	public static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    public static final String IM_LOCATION_PREFIX = "!@!#xyz!@#!";

    //The following is for development
    public static final boolean TESTING = true;  // for tester testing
    public static boolean DEBUG = false;
    public static String sessionId = "2ffda7e595b7b0149d0e104de3a3b478";
//    public static String sessionId = "64vo62j0jj7emk7kw185412l0a45ym8y";  //for prod

//    public static int userId = 23;
    public static int userId = 507; // for prod

    public static int connectionTimeOut = 10000;

    public static String PROFILE_FIELD_TYPE_NAME = "name";
    public static String PROFILE_FIELD_TYPE_COMPANY = "company";
    public static String PROFILE_FIELD_TYPE_TITLE = "title";
    public static String PROFILE_FIELD_TYPE_PHONE = "phone";
    public static String PROFILE_FIELD_TYPE_MOBILE = "mobile";
    public static String PROFILE_FIELD_TYPE_EMAIL = "email";
    public static String PROFILE_FIELD_TYPE_ADDRESS = "address";
    public static String PROFILE_FIELD_TYPE_FAX = "fax";
    public static String PROFILE_FIELD_TYPE_HOURS = "hours";
    public static String PROFILE_FIELD_TYPE_DATE = "date";
    public static String PROFILE_FIELD_TYPE_FACEBOOK = "facebook";
    public static String PROFILE_FIELD_TYPE_TWITTER = "twitter";
    public static String PROFILE_FIELD_TYPE_LINKEDIN = "linkedin";
    public static String PROFILE_FIELD_TYPE_WEBSITE = "url";
    public static String PROFILE_FIELD_TYPE_CUSTOM = "custom";

    public static final int ENTITY_PHOTO_EDITOR = 1;
    public static final int HOME_PHOTO_EDITOR = 2;
    public static final int WORK_PHOTO_EDITOR = 3;


    public static final int ENTITY_VIDEO_EDITOR = 1;
    public static final int HOME_VIDEO_EDITOR = 2;
    public static final int WORK_VIDEO_EDITOR = 3;

    public static final int SHARE_NONE = 0;
    public static final int SHARE_HOME = 1;
    public static final int SHARE_WORK = 2;
    public static final int SHARE_BOTH = 3;
    public static final int SHARE_CHAT_ONLY = 4;


    public static final int GREY_TYPE_NONE = -1;
    public static final int GREY_TYPE_WORK = 2;
    public static final int GREY_TYPE_HOME = 1;
    public static final int GREY_TYPE_ENTITY = 0;

    public static final int SHOW_PRIVACY = 1;
    public static final int SHOW_TERMS = 2;
    public static final int SHOW_ORIGINAL = 3;

    public static final int CLUSTER_GREEN = 100;
    public static final int CLUSTER_GREY = 101;
    public static final int CLUSTER_PURPLE = 102;

    public static final int ICE_NEW = 200;
    public static final int ICE_CONNECTED = 201;
    public static final int ICE_FAILED = 202;
    public static final int ICE_CLOSED = 203;

    public static final String[] fontNamesArray = {
            "Arial",
            "Arial Black",
            "Arial Narrow",
            "AvantGrade Bk BT",
            "BankGothic Md BT",
            "Bazooka",
            "Book Antiqua",
            "Calibri",
            "Calligrapher",
            "Century Gothic",
            "Charlesworth",
            "CloisterBlack BT",
            "Comic Sans MS",
            "Courier New",
            "Lithograph",
            "Lithograph Light",
            "Lucida Handwriting",
            "Signboard",
            "Storybook",
            "Subway",
            "Tahoma",
            "Times New Roman",
    };

    public static final String[] fontStyleArray = {
            "Normal",
            "Bold",
            "Italic",
            "Bold Italic"
    };
    public static final String[] fontSizeArray = {
            "10",
            "11",
            "12",
            "13",
            "14",
            "15",
            "16",
            "17",
            "18",
            "19",
            "20",
            "25",
            "30"
    };
    /*
    if (phoneNo.matches("\\d{10}")) return true;
    //validating phone number with -, . or spaces
    else if(phoneNo.matches("\\d{3}[-\\.\\s]\\d{3}[-\\.\\s]\\d{4}")) return true;
    //validating phone number with extension length from 3 to 5
    else if(phoneNo.matches("\\d{3}-\\d{3}-\\d{4}\\s(x|(ext))\\d{3,5}")) return true;
    //validating phone number where area code is in braces ()
    else if(phoneNo.matches("\\(\\d{3}\\)-\\d{3}-\\d{4}")) return true;*/
    //return false if nothing matches the input
    public static final String[] validPhoneNumberFormats = {
            "\\d{3}[-\\.\\s]d{3}[-\\.\\s]\\d{4}(x)\\s?(x)\\d{3}",//"123-456-7890x123", "123.456.7890x123 , "123 456 7890 x123"
            "\\(\\d{3}\\)\\s\\d{3}-\\d{4}\\s(x)\\d{3}" , //"(123) 456-7890 x123",
            "\\d{3}\\.\\d{3}\\.\\d{4}x\\.\\d{3}" , //"123.456.7890x.123",
            "\\d{3}\\.\\d{3}\\.\\d{4}\\s(ext)\\.\\s\\d{3}" ,// "123.456.7890 ext. 123",
            "\\d{3}\\.\\d{3}\\.\\d{4}\\s(extension)\\sd{6}",// "123.456.7890 extension 123456",
            "\\d{3}\\s\\d{3}\\s\\d{4}",//"123 456 7890",
            "\\d{3}-\\d{3}-\\d{4}(ex)\\d{3}",//"123-456-7890ex123",
            "\\d{3}[-\\.\\s]\\d{3}[-\\.\\s]\\d{4}\\s?((ex)|(ext))\\d{3}",//"123.456.7890 ex123", "123 456 7890 ext123",
            "\\d{3}[-\\s]\\d{4}",//"456-7890", "456 7890"
            "\\d{3}\\s\\d{4}\\sx\\d{3}",//"456 7890 x123",
            "\\d{10}",//"1234567890",
            "^\\(\\) d{3} d{4}$",//"() 456 7890",
            "\\d{3}\\s\\d{3}\\s\\d{4}\\s,\\s\\d{8}[*]\\d{5}",//"800 555 5555 , 12345678*12345",
            "\\d{2}\\s\\d{3}-\\d{3}-\\d{5}",//"86 180-086-45836",
            "\\d{1}\\s\\d{3}-\\d{3}-\\d{4}",//"1 180-086-4586",
            "\\d{2}[\\s-]-?\\d{3}[\\s-(\\s-\\s)]\\d{6}", //"49 -123 456708", "49 123 456708" , "49 123 - 456708", ,"49 123-456708", "49-123-456708",
            "\\d{3}-\\d{3}-\\d{4},,\\d{6}[*]\\d{4}",//"800-367-2943,,124585*2012",
            "\\d{2}\\s\\d{3}\\s\\d{6}",//"49 234 223455",
            "\\d{2}\\s\\d{9}",//"49 456493993",
            "[+]?\\d{2}\\s\\d{3}-\\d{7},\\s\\d{1}(\\*|#)?",//"49 123-4567942, 3", "49 123-4567942, 3*", "+49 123-4567942, 3*","+49 123-4567942, 3#",
            "[+]\\d{2}\\s\\d{9}",//"+49 123456789",
    };

    public static final String[] validDateFormats = {
            "^(?:Jan(?:uary)?|Feb(?:ruary)?|Ma(?:ch)?|Apr(?:il)?|May|Jun(?:e)?|Jul(?:y)?|Aug(?:ust)?|Sep(?:tember)?|Oct(?:ober)?|Nov(?:ember)?|Dec(?:ember)?) (0?[1-9]|[12][0-9]|3[01]), ((19|20)\\\\d\\\\d)$",//"January 1, 2015","January 1","Jan 1, 2015","Jan 1",
            "^(0?[1-9]|[12][0-9]|3[01])/(0?[1-9]|1[012])/((19|20)\\\\d\\\\d)$",//"01-01-2015", "1-1-2015", "1/1/2015", "01/01/2015",
    };
}
