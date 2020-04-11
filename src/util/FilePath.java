package util;

import store.Store;

import java.io.File;
import java.io.IOException;

public class FilePath {
    public static final String RootPath = Store.class.getResource("")
            .getPath().substring(1).replace("%20"," ")+"../res";

    public static final String EVENTS_PATH = RootPath + "/baevents";
    public final static String LOG_FILE_PATH = RootPath + "/files/log";
    public final static String ACC_DATA_PATH = RootPath + "/files/accdata";
    public final static String METADATA_PATH = RootPath + "/files/metadata";
    public final static String TASK_LIST_PATH = RootPath + "/files/taskList";
    public final static String USER_LOG_PATH = RootPath + "/files/userLog";
    public final static String ACCOUNT_GROUP_PATH = RootPath + "/files/account_group";
    public final static String ACCOUNT_PATH = RootPath + "/account/";
    public final static String TEMPLATE_PATH = RootPath + "/images/template/";
    public final static String ERROR_PATH  = RootPath+"/errors/";

    public final static String TRAIN_DATA_PATH  = RootPath+"/tessdata/";

    public static final String TIME_FILE_PATH = RootPath + "/files/time" ;
}
