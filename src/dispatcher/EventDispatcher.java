package dispatcher;

import com.android.ddmlib.*;
import events.common.Event;
import events.EventMap;
import events.register.BuildingEvents;
import game.GameException;
import game.GameInstance;
import game.GameStatus;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.opencv.core.*;
import org.opencv.core.Point;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import store.MatchPoint;
import util.FilePath;
import util.Global;
import util.Logger;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class EventDispatcher implements IShellOutputReceiver {
    public LocalDateTime lastExecuteTime;
    public List<MatchPoint> matchedPoints;
    private Tesseract tesseract;
    public boolean requirePullFile = false;
    public GameInstance game;

    public EventDispatcher(GameInstance game) {
        this.game = game;
        tesseract = new Tesseract();
        tesseract.setTessVariable("tessedit_char_whitelist", "0123456789KkMm/,.:dD");
        tesseract.setDatapath(FilePath.TRAIN_DATA_PATH);
        resetExecuteTime();
    }


    public static void killServer() {
        EventDispatcher.exec("adb logcat -c", s -> true);
        EventDispatcher.exec("adb kill-server", s -> true);
    }

    public void resetExecuteTime() {
        this.lastExecuteTime = LocalDateTime.now();
    }

    public void stopGame() throws Exception {
        exec("am force-stop com.tap4fun.brutalage_test");
    }

    public void startGame() throws Exception {
        exec("monkey -p com.tap4fun.brutalage_test 1");
    }

    public void changeAccount(String uid, boolean stored) throws Exception {
        //exec("pm clear com.tap4fun.brutalage_test");
        exec("rm /data/data/com.tap4fun.brutalage_test/files/tap4fun/be/Documents/LASTLOGIN_LOCAL_DATA.sav");

        exec("content insert --uri content://settings/secure --bind name:s:android_id --bind value:s:" + uid);

        if(stored) {
            File sav = new File(game.store.accountPath + uid+"/LASTLOGIN_LOCAL_DATA.sav");
            if(sav.exists()) {
                execADBIP(game.store.metadata.getIp(), "push \"" + sav.getAbsolutePath() + "\" /data/data/com.tap4fun.brutalage_test/files/tap4fun/be/Documents", s->false);
            }else{
                requirePullFile = true;
            }
        }
        //  adb shell "pm clear com.tap4fun.brutalage_test && content insert --uri content://settings/secure --bind name:s:android_id --bind value:s:wzz20200320212736
    }

    public void pullAccountData(String uid){
        execADBIP(game.store.metadata.getIp(), "pull /data/data/com.tap4fun.brutalage_test/files/tap4fun/be/Documents/LASTLOGIN_LOCAL_DATA.sav \""+game.store.accountPath + uid+"\"", s->false);
    }

    public void enterText(String str) throws Exception {
        game.dispatch.deleteText();
        exec("input text \"" + str + "\"");
        staticDelay(0.25);
    }

    public void swipeServer(int diff) throws Exception {

        int _diff = diff * ((diff <= 2) ? 30 : 40);

        int swipe = 840 - _diff;
        if (swipe < 300)
            swipe = 300;
        else if (swipe > 1200) {
            swipe = 1200;
        }

        exec("input swipe 350 840 350 " + swipe + " 600");
    }

    public void delay(double i) throws InterruptedException {
        Thread.sleep((int) (game.store.getDelay() * i));
    }

    public void staticDelay(double i) throws InterruptedException {
        Thread.sleep((int) (1000 * i));
    }


    public void delay(Event event) throws InterruptedException {
        if (event.delay > 0.0)
            delay(event.delay);
    }


    public void exec(String cmd) throws Exception{
        if (!game.debug && game.account!=null && lastExecuteTime != null
                && Duration.between(lastExecuteTime, LocalDateTime.now()).toMinutes()
                > GameStatus.getTimeout(game.status.get())) {
            throw new GameException("Not responding, Stuck at " + game.status.get().name());
        }

        Logger.log("* execute " + cmd);
        game.store.device.executeShellCommand(cmd, this);

    }

    private String getInputEventFile(){
        return Global.config.getEventName();
    }

    public void mapzoom() throws Exception {

        for(int i =0; i<5;i++) {
            exec("su 0 cat /mnt/sdcard/baevents/map_out_event > /dev/input/"+getInputEventFile());
            game.dispatch.staticDelay(0.25);
        }
        mapzoomin();
    }

    public void mapzoomin() throws Exception{
        exec("su 0 cat /mnt/sdcard/baevents/map_in_event > /dev/input/"+getInputEventFile());
        game.dispatch.staticDelay(0.25);
    }

    public void deleteText()  throws Exception{
        exec("su 0 cat /mnt/sdcard/baevents/delete_event > /dev/input/"+getInputEventFile());
        game.dispatch.staticDelay(0.25);
    }

    public void cityZoom()  throws Exception{
        exec("su 0 cat /mnt/sdcard/baevents/city_zoom_event > /dev/input/"+getInputEventFile());
        game.dispatch.staticDelay(0.25);
    }
    public void zoomout() throws Exception {
        exec("su 0 cat /mnt/sdcard/baevents/zoomout_event > /dev/input/"+getInputEventFile());
        game.dispatch.staticDelay(0.25);
    }

    public void zoomin() throws Exception {
        exec("su 0 cat /mnt/sdcard/baevents/zoomin_event > /dev/input/"+getInputEventFile());
        game.dispatch.staticDelay(0.25);
    }


    public void selectMonster(int x, int y) throws Exception {
        int[] prevWorld;
        String tapStr = "input tap " + x + " " + y;
        int redo = 10;
        do {
            prevWorld = game.log.world.clone();
            exec(tapStr + "&" + tapStr + "&" + tapStr);
            staticDelay(1.5);
        } while (game.log.btnName.contains("tiles") && Arrays.equals(game.log.world, prevWorld) && redo-- > 0);

        if (redo > 0) {
            game.dispatch.exec(tapStr);
            delay(1.5);
        }
    }

    public void changePosition(int x, int y) throws Exception {
        exec("input tap 341 1168");
        delay(1.5);
        exec("input tap 278 517");
        delay(1);
        enterText(String.valueOf(x));
        delay(1);
        exec("input tap 484 509");
        delay(1);
        enterText(String.valueOf(y));
        delay(1);
        exec("input tap 358 646");
        staticDelay(1.25);
    }


    public boolean sendEvent(String eventName) throws Exception {
        Event event = EventMap.get(eventName);
        if (event == null) {
            System.out.println("*" + eventName + "* does not exit");
            return false;
        }

        boolean isTrue = sendEvent(event);
        if (event.chain != null) {
            sendEventChain(event.chain, true);
        }
        return isTrue;
    }


    public boolean sendEvent(Event event) throws Exception {
        int redo = 0;
        boolean pass = true;
        while (redo <= event.maxRedo) {
            while (game.store.isPause()) {
                resetExecuteTime();
                delay(1);
            }

            execMiddleware(event);

            if (event.isBuilding) {
                pass = locateBuilding(event);
            } else if (event.templateName != null && !event.templateName.equalsIgnoreCase("")) {
                matchedPoints = getMatch(event);
            } else if (event.loc != null) {
                if (event.loc.length == 2) {
                    exec(String.format("input tap %d %d", event.loc[0], event.loc[1]));
                } else if (event.loc.length == 5) {
                    System.out.println("swipe!");
                    exec(String.format("input swipe %d %d %d %d %d", event.loc[0], event.loc[1], event.loc[2], event.loc[3], event.loc[4]));
                }
            }

            delay(event);

            if (event.listener != null && (event = event.listener.check(event, game)) != null) {
                redo++;
            } else {
                return pass;
            }
        }

        if (event.maxRedo <= 0)
            return false;

        throw new GameException(event.name + " failed");
    }

    private void locateBuidlingSwipe(Event event) throws Exception {
        Event tapBuildingEvent = EventMap.get("tap_building");
        int redo = 15;
        int otherRedo = 4;
        double prevX = Double.MAX_VALUE, prevY = Double.MAX_VALUE;
        while (redo > 0) {
            if(game.log.btnName.contains("monster_tip")){
                exec("input swipe 400 1000 200 400 500");
                staticDelay(0.5);
                sendEvent("top_left");
                if(game.log.btnName.contains("profile")){
                    game.dispatch("top_left");
                }
                exec("input swipe 400 1000 200 400 500");
                staticDelay(0.5);
            }
            else if (game.log.city.x == prevX && game.log.city.y == prevY) {
                sendEvent("top_left");
                if(otherRedo-- <= 0){
                    sendEvent("template_close");
                }
            }

            prevX = game.log.city.x;
            prevY = game.log.city.y;

            double diffX = game.log.city.x - event.loc[0];
            double diffY = game.log.city.y - event.loc[1];

            double absDiffX = Math.abs(diffX);
            double absDiffY = Math.abs(diffY);

            int SWIPE_MAX = 500;
            if (absDiffX > SWIPE_MAX) {
                diffX = (diffX / absDiffX) * SWIPE_MAX;
            }

            if (absDiffY > SWIPE_MAX) {
                diffY = (diffY / absDiffY) * SWIPE_MAX;
            }

            Logger.log(String.format("%s(%d, %d)  now(%d, %d)  diff(%d,%d)",
                    event.name, event.loc[0], event.loc[1], (int) game.log.city.x, (int) game.log.city.y, (int) diffX, (int) diffY
            ));

            if(game.log.btnName.contains("scene_tiles")){
                game.dispatch("bottom_left");
            }

            execMiddleware(event);
            if ((absDiffX + absDiffY) >= 22) {
                exec(String.format("input swipe %d %d %d %d 500", tapBuildingEvent.loc[0], tapBuildingEvent.loc[1],
                        tapBuildingEvent.loc[0] - (int) diffX, tapBuildingEvent.loc[1] - (int) diffY));
                staticDelay(0.15);
                redo--;
            } else {
                sendEvent(tapBuildingEvent);

                if(event.isUpgrade){
                    if(!game.log.btnName.contains("loc")){
                        game.dispatch("top_left");
                        if(game.log.btnName.contains("profile")){
                            game.dispatch("top_left");
                        }
                        sendEvent(tapBuildingEvent);
                    }
                }
                if(!game.log.btnName.contains("loc_30")){
                    break;
                }
            }
        }
        if (redo <= 0) {
            throw new GameException("Building " + event.getName() + " not found!!");
        }
    }

    private boolean locateBuilding(Event event) throws Exception {
        locateBuidlingSwipe(event);

        if (event.loc.length > 2) {
            int i = 2;
            int end = (event.isUpgrade || event.isAccess) ? event.loc.length - 2 : event.loc.length;
            for (; i < end; i += 2) {
                exec(String.format("input tap %d %d", event.loc[i], event.loc[i + 1]));
                delay(1);
            }

            String verifyName = "";
            if (event.isAccess) {
                verifyName = "buttons_2:btn_2";
            } else if (event.isUpgrade) {
                if (game.account != null && game.account.getBuildingLvl(event.getName()) > 0) {
                    verifyName = "btn_upgrade";
                } else if (game.posTarget != null && game.posTarget.containsKey(event.getName())) {
                    verifyName = "btn_upgrade";
                }
            }

            if (!verifyName.equalsIgnoreCase("")) {
                int redo = 0;
                int hx = event.loc[i];

                if(game.account != null) {
                    if (event.getName().equalsIgnoreCase("stronghold")) {
                        if (game.account.getBuildingLvl("stronghold") >= 6) {
                            hx = 520;
                        }
                    }
                }

                int hy = event.loc[i+1];
                String execStr = String.format("input tap %d %d", hx, hy);

                exec(execStr);
                delay(1);
                while (!game.log.btnName.contains(verifyName) && !game.log.btnName.contains("buttons_1:btn_1") ) {
                     if(redo > 14){
                         return false;
                     }
                     else if(redo == 5 || redo == 11){
                         game.dispatch("top_left");
                         if(game.log.btnName.contains("profile")){
                             game.dispatch("top_left");
                         }
                         exec("input swipe 337 834 600 1000 500");
                         game.dispatch("login_zoom");
                    }

                    locateBuidlingSwipe(event);
                    exec(execStr);
                    delay(1);
                    execMiddleware(event);
                    redo++;
                }
            }
        }
        return true;
    }

    private void execMiddleware(Event event) throws Exception {
        if(game.store.isForceStop){
            throw new GameException("Force Stopped");
        }else if(game.store.isClose){
            throw new Exception("Closed");
        }
        if(!game.status.is(GameStatus.starting)) {
            if (game.log.levelupDialog) {
                game.log.levelupDialog = false;
                if (game.account != null) {
                    game.account.setLevel(game.account.getLevel() + 1);
                    game.updateAccount();
                    Logger.log("**** Level up to " + game.account.getLevel());
                }
                staticDelay(1.5);
                sendEvent("levelup_dialog");
            }

            if (game.log.btnName.equalsIgnoreCase("main:")) {
                staticDelay(1.5);
                exec("input tap 362 1183");
            }
            else if(game.log.btnName.equalsIgnoreCase("popup:bg") ||game.log.btnName.contains("board:btn_chest") ){
                exec("input tap 324 500");
            }
        }
    }

    public void sendEventChain(Event[] chain, boolean deep) throws Exception {
        for (Event event : chain) {
            sendEvent(event);
            if (deep) {
                if (event.chain != null && event.chain.length > 0) {
                    sendEventChain(event.chain, deep);
                }
            }
        }
    }

    public RawImage getRaw() {
        try {
            return game.store.device.getScreenshot();
        } catch (TimeoutException | IOException | AdbCommandRejectedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<MatchPoint> getMatch(String template) {
        try {
            RawImage rawImage = game.store.device.getScreenshot();
            Mat source = rawimg2Mat(rawImage);
            return getMatch(source, template, 4);
        } catch (AdbCommandRejectedException | TimeoutException | IOException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public List<MatchPoint> getMatch(Event event) {
        try {
            RawImage rawImage = game.store.device.getScreenshot();
            Mat source = rawimg2Mat(rawImage);
            return getMatch(source, event.templateName, event.templateMatches);
        } catch (AdbCommandRejectedException | TimeoutException | IOException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }


    public List<MatchPoint> getMonsterMatch(String templateFile, int maxMatch) {
        Mat rawMat = rawimg2Mat(getRaw());
        return getMatch(100, 100, rawMat.width() - 100, rawMat.height() - 100, 0.25, 0, rawMat, templateFile + ".png", maxMatch);
    }

    public List<MatchPoint> getMonsterMatch(int maxMatch, ArrayList<String> templates){
        Mat rawMat = rawimg2Mat(getRaw());
        List<MatchPoint> matches = new ArrayList<>();
        double ratio;
        for(String template: templates) {
            if(template.contains("wood") || template.contains("meat")){
                ratio = 0.25;
            }else{
                ratio = 0.5;
            }
            matches.addAll(
                getMatch(100, 100, rawMat.width() - 100, rawMat.height() - 100, ratio, 0, rawMat, template + "_rss.png", maxMatch)
            );
            matches.addAll(
                getMatch(100, 100, rawMat.width() - 100, rawMat.height() - 100, ratio, 0, rawMat, template + ".png", maxMatch)
            );
        }

        Iterator<MatchPoint> it = matches.iterator();
        MatchPoint p;
        boolean shouldRemove;
        while (it.hasNext()) {
            shouldRemove = false;
            p = it.next();
            for (MatchPoint tp : matches) {
                if (tp != p && (Math.sqrt((p.x - tp.x) * (p.x - tp.x) + (p.y - tp.y) * (p.y - tp.y))) < 20) {
                    if(p.threshold < tp.threshold) {
                        shouldRemove = true;
                    }
                    break;
                }
            }
            if (shouldRemove) it.remove();
        }
        return matches;

    }



    public List<MatchPoint> getMatch(Mat source, String templateFile, int maxMatch) {
        return getMatch(0, 0, source.width(), source.height(), 0, 0, source, templateFile, maxMatch);
    }

    public List<MatchPoint> getMatch(int x, int y, int x1, int y1, double adjustX, double adjustY, Mat source, String templateFile, int maxMatch) {
        Mat template = Imgcodecs.imread(FilePath.TEMPLATE_PATH + templateFile, CvType.CV_8U);
        Logger.log("**Trying to match " + templateFile);

        return getMatch( x,  y,  x1,  y1,  adjustX,  adjustY,  source, template,  maxMatch);
    }

    public List<MatchPoint> getMatch(int x, int y, int x1, int y1, double adjustX, double adjustY, Mat source, Mat template, int maxMatch) {

        int machMethod = Imgproc.TM_CCOEFF_NORMED;
        Mat sub_source = source.submat(y, y1, x, x1);
        Imgproc.matchTemplate(sub_source, template, sub_source, machMethod);
        double thres = 1.0;
        int count = 0;
        ArrayList<MatchPoint> list = new ArrayList<>();
        while (count < maxMatch) {
            Core.MinMaxLocResult mmr = Core.minMaxLoc(sub_source);
            Point matchLoc = mmr.maxLoc;
            //Draw rectangle on result image
            Imgproc.rectangle(sub_source, matchLoc, new Point(matchLoc.x + template.cols(),
                    matchLoc.y + template.rows()), new Scalar(255, 255, 255));

            Imgproc.rectangle(sub_source, matchLoc,
                    new Point(matchLoc.x + template.cols() + 10, matchLoc.y + template.rows() + 10),
                    new Scalar(0, 100, 0), -5);

            thres = mmr.maxVal;

            if (thres < 0.8) {
                break;
            }

            if (adjustX == 0) adjustX = 0.5;
            if (adjustY == 0) adjustY = 0.5;
            MatchPoint p = new MatchPoint((int)(matchLoc.x + x + (template.width() * adjustX)), (int)(matchLoc.y + y + (template.height() * adjustY)), thres);
           // p.setMat(source.submat((int)matchLoc.y  + y, (int)matchLoc.y+template.height(),(int)matchLoc.x+ x , (int)matchLoc.x+template.width()));
            list.add(p);
            System.out.println(String.format("First match thredhold: %.4f loc: (%d, %d)", thres, p.x, p.y));
            count++;
        }
        Logger.log("**Match " + list.size() + " results");
        return list;
    }

    public String findClosestBuilding(GameInstance game) {
        for (String building : BuildingEvents.AllBuildings) {
            Event event = EventMap.get(building);
            int diffX = (int) game.log.city.x - event.loc[0];
            int diffY = (int) game.log.city.y - event.loc[1];
            if (Math.abs(diffX) + Math.abs(diffY) <= 100) {
                Logger.log(String.format("Closest of (%d, %d) is %s", (int) game.log.city.x, (int) game.log.city.y, building));
                return building;
            }
        }
        Logger.log("Closest building not found!!");
        return "";
    }

    ;

    @Override
    public void addOutput(byte[] bytes, int i, int i1) {
        // System.out.println(new String(bytes));
    }

    @Override
    public void flush() {

    }

    @Override
    public boolean isCancelled() {
        return false;
    }


    public static void execFast(String cmd) {
        try {
            System.out.println("** execute: " + cmd);
            Runtime rt = Runtime.getRuntime();
            Process proc = rt.exec(cmd);
            //  proc.destroy();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void execADBIP(String ip, String cmd, IDispatcherReader reader){
        exec("adb -s " + ip + " " + cmd, reader);
    }

    public static BufferedReader exec(String cmd, IDispatcherReader reader) {
        try {
            System.out.println("** execute: " + cmd);
            Runtime rt = Runtime.getRuntime();
            Process proc = rt.exec(cmd);
            BufferedReader stdInput = new BufferedReader(new
                    InputStreamReader(proc.getInputStream()));
            if(reader != null) {
                String s;
                while (true) {
                    s = stdInput.readLine();
                    if (s == null) break;
                    if (reader.read(s)) break;
                }
                proc.destroy();
            }
            stdInput.close();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Mat rawimg2Mat(RawImage raw) {
        int W = raw.width;
        int H = raw.height;
        Mat mat = new Mat(H, W, CvType.CV_8U);
        byte[] data = new byte[H * W * (int) mat.elemSize()];
        int index = 0;
        int IndexInc = raw.bpp >> 3;
        int r, g, b;
        for (int y = 0; y < H; y++) {
            for (int x = 0; x < W; x++) {
                int value = raw.getARGB(index);
                //   r = (byte) ((value>> 16) & 0xFF);
                g = (byte) ((value >> 8) & 0xFF);
                b = (byte) ((value) & 0xFF);
                data[index / 4] = (byte) ((g) + (0.1 * b));
                index += IndexInc;
            }
        }
        mat.put(0, 0, data);
        return mat;
    }

    public void screenshot(String path) {
        try {
            RawImage rawImage = game.store.device.getScreenshot();
            if (rawImage != null) {
                Mat source = EventDispatcher.rawimg2Mat(rawImage);
                Imgcodecs.imwrite(path, source);
                System.out.println("Take screenshot and put in " + path);
            }
        } catch (AdbCommandRejectedException | TimeoutException | IOException e) {
            e.printStackTrace();
        }
    }


    public String doOSR(int x, int y, int x1, int y1) throws TesseractException, TimeoutException, AdbCommandRejectedException, IOException {
        return doOSR(captureAsBI(), x, y, x1, y1);
    }

    public String doOSR(BufferedImage img, int x, int y, int x1, int y1) throws TesseractException {
        String result = tesseract.doOCR(img, new Rectangle(x, y, x1 - x, y1 - y));
        // Logger.log(String.format("Perform osr in %d %d %d %d result: %s", x, y, x1, y1, result));
        return result;
    }

    public String doOSR(Mat m) throws TesseractException, IOException {
        MatOfByte mob = new MatOfByte();
        Imgcodecs.imencode(".jpg", m, mob);
        byte ba[] = mob.toArray();

        BufferedImage bi = ImageIO.read(new ByteArrayInputStream(ba));

        return doOSR(bi,0, 0,bi.getWidth(),bi.getHeight());
    }

    public BufferedImage captureAsBI() throws AdbCommandRejectedException, IOException, TimeoutException {
        return raw2BufferedImage(game.store.device.getScreenshot());
    }

    public BufferedImage raw2BufferedImage(RawImage rawImage) {
        BufferedImage image = new BufferedImage(rawImage.width, rawImage.height,
                BufferedImage.TYPE_INT_ARGB);
        int index = 0;
        int IndexInc = rawImage.bpp >> 3;
        for (int y = 0; y < rawImage.height; y++) {
            for (int x = 0; x < rawImage.width; x++) {
                int value = rawImage.getARGB(index);
                index += IndexInc;
                image.setRGB(x, y, value);
            }
        }
        return image;
    }

    public void changeHorde(int horde) throws Exception {
        if (horde > 0) {
            sendEvent("top_left");
            exec("input tap 500 1120");
            delay(1.5);
            for (int i = 0; i < horde; i++) {
                exec("input swipe 180 668 390 677 300");
                delay(1.5);
            }
            exec("input tap 365 1006");
            delay(1);
            exec("input tap 356 714");
            delay(1);
            sendEvent("top_left");
            sendEvent("top_left");
        }
    }

}
