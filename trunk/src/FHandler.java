import java.io.*;
import java.util.zip.*;
public final class FHandler {
 public class NonStatic {

 }
 public static DataInputStream getDataStream(String fname) throws IOException {
     int dotPos = fname.lastIndexOf(".");
     String fext = fname.substring(dotPos);
   if ( !fext.equals(".zip") ) {


   FileInputStream fistream = new FileInputStream(fname);
   BufferedInputStream bistream = new BufferedInputStream(fistream);



   DataInputStream distream = new DataInputStream(bistream);
   return distream;
  }
  else {


   FileInputStream fistream = new FileInputStream(fname);
   ZipInputStream zistream = new ZipInputStream(fistream);


   ZipEntry entry = zistream.getNextEntry();

   BufferedInputStream bistream = new BufferedInputStream(zistream);
   DataInputStream distream = new DataInputStream(bistream);

   return distream;



  }
 }
}
