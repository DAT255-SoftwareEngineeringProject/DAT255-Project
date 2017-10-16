package absolut.img;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import javax.imageio.ImageIO;

import absolut.can.CanReader;
import com.hopding.jrpicam.RPiCamera;
import com.hopding.jrpicam.exceptions.FailedToRunRaspistillException;
/* Läser av en bild och räknar ut hur mycket bilen ska svänga beroende på var det finns flest röda pixlar */

public class GetPixelColor extends Thread {
    //int y, x, tofind, col;
    /**
     * @param args the command line arguments
     * @throws IOException
     */

    private CanReader can;
    private RPiCamera piCamera;
    private byte steering = 0;
    private byte speed = 20;
    private byte stop = 0;
    public GetPixelColor(){
        try {
            piCamera = new RPiCamera("/home/pi/Pictures");
            piCamera.setTimeout(10);
        } catch (FailedToRunRaspistillException e) {
            e.printStackTrace();
        }
        can = CanReader.getInstance();
        try {
            doFunction();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String args[]) throws IOException, InterruptedException {
        GetPixelColor pixel = new GetPixelColor();
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    CanReader.getInstance().sendMotorSpeed((byte) 0);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }));
    }
    private void doFunction() throws InterruptedException {
        can.sendMotorSpeed((byte) stop);    //testing values
        Thread.sleep(2000);
        can.sendMotorSpeed((byte) speed); //testing values
        while(true){
            scanPicture();
        }
        //can.sendMotorSpeed((byte) stop);    //testing values
    }

    public  String findURL() {
        URL url = null;
        String urlAdress = "ftp://gustaf:absolut@chassit.xyz/home/gustaf/moped/position/Optipos/Connected/";
        try {
            url = new URL(urlAdress);
            //folder = new File (url.toURI());
            URLConnection urlc = url.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(urlc.getInputStream()));

            String inputLine;
            String pictureName;
            while((inputLine = in.readLine()) != null) {
                String str = inputLine;
                String[] parts = str.split(" ");
                pictureName = parts[22];
                //scanPicture(new URL(urlAdress+pictureName));
            }
            in.close();

        }catch (Exception e) {
            e.printStackTrace();
        }return "left.jpg";
    }


    public  void scanPicture() {
        try {
            //read image file
            //File file1 = new File(picture);
            //BufferedImage image = ImageIO.read(file1);
            long time = System.currentTimeMillis();
            BufferedImage image = ImageIO.read(piCamera.takeStill("pi.jpg"));

            System.out.println("Get image: " + (System.currentTimeMillis() - time) + "ms");
            final byte[] pixels = ((DataBufferByte)image.getRaster().getDataBuffer()).getData();
            final int width = image.getWidth();
            final int height = image.getHeight();

            float kvot = 0;
            int redCounterLeft = 0;
            int redCounterRight = 0;
            //int c;
            //System.out.println(image.getWidth() + ":" + image.getHeight());;

            time = System.currentTimeMillis();
            boolean alpha = image.getAlphaRaster() != null;
            int pixelLength = alpha ? 4: 3;
            for (int pixel = 0, row = 0, col = 0; pixel < pixels.length; pixel += pixelLength) {
                int red = ((int)pixels[pixel + (alpha ? 3: 2)] & 0xff);
                if (red > 200 && col < (width / 2))
                    redCounterLeft++;
                else if (red > 200 && col > (width / 2))
                    redCounterRight++;
                col++;
                if (col == width) {
                    col = 0;
                    row++;
                }
            }
            System.out.println("Image process: " + (System.currentTimeMillis() - time) + "ms");
            //System.out.println(" right: " + redCounterRight + " left: " + redCounterLeft);
            //if (redCounterRight > 0) {
            kvot = ((float)redCounterLeft / (float)redCounterRight);
            if (kvot > 0.9 && kvot < 1.1) {
                steering = (byte) 0;
                //System.out.println("Drive straight LeftCounter: " + redCounterLeft + "  Right:   " + redCounterRight + " kvot: " + kvot);
           /* if (Math.abs(redCounterLeft - redCounterRight) < 4000)
                System.out.println("Drive straight " + redCounterLeft + "     " + redCounterRight);
            else if(redCounterLeft > redCounterRight)
                System.out.println("Turn left " + redCounterLeft + " > " + redCounterRight );*/
            }else if (redCounterLeft * 0.6 > redCounterRight) {
                steering = (byte) -70;
            }else if(redCounterLeft * 0.7 > redCounterRight) {
                steering = (byte)-60;
            }else if(redCounterLeft * 0.8 > redCounterRight) {
                steering = (byte)-30;
            }else if(redCounterLeft * 0.9 > redCounterRight) {
                steering = (byte)-10;
            }else if(redCounterRight * 0.6 > redCounterLeft) {
                steering = (byte)70;
            }else if(redCounterRight * 0.7 > redCounterLeft) {
                steering = (byte)60;
            }else if(redCounterRight * 0.8 > redCounterLeft) {
                steering = (byte)30;
            }else if(redCounterRight * 0.9 > redCounterLeft) {
                steering = (byte)10;
            }
            System.out.println("Turn " + (steering < 0 ? "left ": steering == 0 ? "straight ": "right ")  + steering + " " +
                    redCounterRight + (steering < 0 ? " < ": steering == 0 ? " = ": " > ") + redCounterLeft);
            can.sendSteering((byte) steering);

            //else System.out.println("Turn right " + redCounterRight + " > " + redCounterLeft);
        } catch (IOException e) {
            e.printStackTrace();

        } catch (InterruptedException ie){
            ie.printStackTrace();
        }
    }
}