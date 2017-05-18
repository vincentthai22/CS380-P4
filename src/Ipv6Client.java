import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Created by Vincent on 5/17/2017.
 */
public class Ipv6Client {

    private final static String SERVER_NAME = "codebank.xyz";
    private final static int PORT_NUMBER = 38004;
    private final int MIN_PACKET_SIZE = 40;
    String serverName;
    int portNumber;
    byte[] byteArray;

    public Ipv6Client(String serverName, int portNumber) {
        this.serverName = serverName;
        this.portNumber = portNumber;

        callServer();

    }

    private void callServer() {
        Integer bytes = 2;
        int i = 0;

        try (Socket socket = new Socket(serverName, portNumber)) {

            System.out.println("Connected to server");

            InputStream is = socket.getInputStream();
            InputStreamReader isr = new InputStreamReader(is, "UTF-8");
            BufferedReader br = new BufferedReader(isr);
            OutputStream os = socket.getOutputStream();
            PrintStream out = new PrintStream(os, true, "UTF-8");

            byte[] responseArray = new byte[4];
            while (i < 12) {

                os.write(getIpv6Packet(bytes));
                System.out.println("\ndata length: " + (bytes *= 2));
                System.out.print("Response: 0x");
                for (int j = 0; j < responseArray.length; j++) {
                    responseArray[j] = (byte) is.read();
                    System.out.print(Long.toHexString(responseArray[j]& 0xFF).toUpperCase());
                }
                i++;
            }

        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    private byte[] getIpv6Packet(int additionalData) {
        byte[] ipv6Packet = new byte[MIN_PACKET_SIZE + additionalData];
        int i = 0;

        ipv6Packet[i++] = 0x60;  //version  i = 0

        while (i < 4)
            ipv6Packet[i++] = (byte) 0; // traffic class & flow label (do not implement)

        ipv6Packet[i++] = (byte) (additionalData >>> 8);  //length \ i = 4 , 5
        ipv6Packet[i++] = (byte) (additionalData);


        ipv6Packet[i++] = (byte) 17; //next header i = 6
        ipv6Packet[i++] = (byte) 20; //hop limit i = 7

        while (i < 24) {                    //source addr i = 8 to i = 24

            //i = 17 through 23
            if (i > 17) {
                ipv6Packet[i++] = (byte) 0xFF; // FFFF:FFFF:FFFF  part of the address
                continue;
            }

            //i = 8 through 17
            ipv6Packet[i++] = (byte) 0; // 0:0:0:0:0: beginning of source addr
        }

        while(i < 36) {   //dest address i = 25 - 40

            if(i > 33) {
                ipv6Packet[i++] = (byte) 0xFF;
                continue;
            }

            ipv6Packet[i++] = (byte) 0;

        }

        ipv6Packet[i++] = (byte) 52;  //source address 52.37.88.154
        ipv6Packet[i++] = (byte) 37;
        ipv6Packet[i++] = (byte) 88;
        ipv6Packet[i++] = (byte) 154;

        // data

        while( i < ipv6Packet.length)
            ipv6Packet[i++] = (byte) i;


        return ipv6Packet;
    }


    public static void main(String[] args) {
        new Ipv6Client(SERVER_NAME, PORT_NUMBER);
    }

}



