package at.fhv.streamprocessing.bfn.producer;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.zip.GZIPInputStream;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class FTPReader {

    @Inject
    Producer producer;

    private final HashMap<String, Integer> fileLines = new HashMap<>();

    public void getFile() {
      String server = "ftp.ncdc.noaa.gov";
      // generally ftp port is 21
       int port = 21;
       String user = "anonymous";
       String pass = "password";

       FTPClient ftpClient = new FTPClient();

       try {

           ftpClient.connect(server, port);
           showServerReply(ftpClient);

           int replyCode = ftpClient.getReplyCode();
           if (!FTPReply.isPositiveCompletion(replyCode)) {
               System.out.println("Connect failed");
               return;
           }

           boolean success = ftpClient.login(user, pass);
           showServerReply(ftpClient);

           if (!success) {
               System.out.println("Could not login to the server");
               return;
           }

           // Changes working directory
           success = ftpClient.changeWorkingDirectory("/pub/data/noaa/2023");
           showServerReply(ftpClient);

           if (success) {
               System.out.println("Successfully changed working directory.");
           } else {
               System.out.println("Failed to change working directory. See server's reply.");
           }

           FTPFile[] files = ftpClient.listFiles();

        //    for (FTPFile file : files) {
        //     String details = file.getName();
        //     if (file.isDirectory()) {
        //         details = "[" + details + "]";
        //     }
        //     details += "\t\t" + file.getSize();
 
        //     System.out.println(details);
        //     }

           ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            // APPROACH #2: using InputStream retrieveFileStream(String)
            String remoteFile2 = "010010-99999-2023.gz";
            InputStream inputStream = ftpClient.retrieveFileStream(remoteFile2);

            BufferedReader is = new BufferedReader(new InputStreamReader(new GZIPInputStream(inputStream)));

            String line;
            int lineCount = 0;
            int skipLines = fileLines.get(remoteFile2) != null ? fileLines.get(remoteFile2) : 0;
            while ((line = is.readLine()) != null) {
                // if (skipLines <= lineCount) {
                    producer.sendToKafka(line);
                // }
                // lineCount++;
            }

            fileLines.put(remoteFile2, lineCount);
            is.close();
   
            success = ftpClient.completePendingCommand();
            if (success) {
                  System.out.println("File #2 has been downloaded successfully.");
            }
            inputStream.close();

           // logs out
           ftpClient.logout();
           ftpClient.disconnect();

       } catch (IOException ex) {
           System.out.println("Oops! Something wrong happened");
           ex.printStackTrace();
       }
   }

   private static void showServerReply(FTPClient ftpClient) {
      String[] replies = ftpClient.getReplyStrings();
      if (replies != null && replies.length > 0) {
          for (String aReply : replies) {
              System.out.println("SERVER: " + aReply);
          }
      }
  }
}
