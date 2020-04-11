package util;

import store.Account;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;

public class Logger {


    public static void log(String string) {
        System.out.println(string);
        addToFile(
                FilePath.LOG_FILE_PATH,
                string
        );
    }

    public static void recordRound(Account acc, String str) {
        log( "------------- ******** "+acc.getId()+ "#"+acc.getRound()+ " round ended using: " +str + " ******** ------------- " );
        addToFile(
                FilePath.TIME_FILE_PATH,
                acc.getId()+ "#"+acc.getRound()+":   " +str
        );
    }


    public static synchronized void addToFile(String fileName, String str) {
        try {
            BufferedWriter writer = new BufferedWriter(
                    new FileWriter(fileName, true)  //Set true for append mode
            );
            writer.newLine();   //Add new line
            writer.write(str);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
