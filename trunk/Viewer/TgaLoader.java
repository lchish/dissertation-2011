import java.awt.image.BufferedImage;
import java.io.*;

// http://paulbourke.net/dataformats/tga/
// little endian multi-byte integers: "low-order byte,high-order byte"
//          00,04 -> 04,00 -> 1024
class TargaReader {
        public static BufferedImage getImage(String fileName) throws IOException {
                File f = new File(fileName);
                byte[] buf = new byte[(int)f.length()];
                BufferedInputStream bis = new BufferedInputStream(new FileInputStream(f));
                bis.read(buf);
                bis.close();
                return decode(buf);
        }

        public static int [] getImagePixels(String fileName) throws IOException {
            File f = new File(fileName);
            byte[] buf = new byte[(int)f.length()];
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(f));
            bis.read(buf);
            bis.close();
            return decodePixels(buf);
    }

        private static int offset;

        private static int btoi(byte b) {
                int a = b;
                return (a<0?256+a:a);
        }

        private static int read(byte[] buf) {
                return btoi(buf[offset++]);
        }

        public static BufferedImage decode(byte[] buf) throws IOException {
                offset = 0;

                for (int i=0;i<12;i++)
                        read(buf);
                int width = read(buf)+(read(buf)<<8);   // 00,04=1024
                int height = read(buf)+(read(buf)<<8);  // 40,02=576
                read(buf);
                read(buf);

                int n = width*height;
                int[] pixels = new int[n];
                int idx=0;

                if (buf[2]==0x02 && buf[16]==0x20) { // uncompressed BGRA
                    while(n>0) {
                        int b = read(buf);
                        int g = read(buf);
                        int r = read(buf);
                        int a = read(buf);
                        int v = (a<<24) | (r<<16) | (g<<8) | b;
                        pixels[idx++] = v;
                        n-=1;
                    }
                } else if (buf[2]==0x02 && buf[16]==0x18) {  // uncompressed BGR
                    while(n>0) {
                        int b = read(buf);
                        int g = read(buf);
                        int r = read(buf);
                        int a = 255; // opaque pixel
                        int v = (a<<24) | (r<<16) | (g<<8) | b;
                        pixels[idx++] = v;
                        n-=1;
                    }
                } else {
                    // RLE compressed
                    while (n>0) {
                        int nb = read(buf); // num of pixels
                        if ((nb&0x80)==0) { // 0x80=dec 128, bits 10000000
                            for (int i=0;i<=nb;i++) {
                                int b = read(buf);
                                int g = read(buf);
                                int r = read(buf);
                                pixels[idx++] = 0xff000000 | (r<<16) | (g<<8) | b;
                            }
                        } else {
                            nb &= 0x7f;
                            int b = read(buf);
                            int g = read(buf);
                            int r = read(buf);
                            int v = 0xff000000 | (r<<16) | (g<<8) | b;
                            for (int i=0;i<=nb;i++)
                                pixels[idx++] = v;
                        }
                        n-=nb+1;
                    }
                }

                BufferedImage bimg = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                bimg.setRGB(0, 0, width,height, pixels, 0,width);
                return bimg;
        }
        public static int [] decodePixels(byte[] buf) throws IOException {
            offset = 0;

            for (int i=0;i<12;i++)
                    read(buf);
            int width = read(buf)+(read(buf)<<8);   // 00,04=1024
            int height = read(buf)+(read(buf)<<8);  // 40,02=576
            read(buf);
            read(buf);

            int n = width*height;
            int[] pixels = new int[n*4];
            int idx=0;

            if (buf[2]==0x02 && buf[16]==0x20) { // uncompressed BGRA
                while(n>0) {
                    int b = read(buf);
                    pixels[idx++] = b;
                    int g = read(buf);
                    pixels[idx++] = g;
                    int r = read(buf);
                    pixels[idx++] = r;
                    int a = read(buf);
                    pixels[idx++] = a;
                    n-=1;
                }
            } else if (buf[2]==0x02 && buf[16]==0x18) {  // uncompressed BGR
                while(n>0) {
                    int b = read(buf);
                    pixels[idx++] = b;
                    int g = read(buf);
                    pixels[idx++] = g;
                    int r = read(buf);
                    pixels[idx++] = r;
                    int a = 255; // opaque pixel
                    pixels[idx++] = a;
                    n-=1;
                }
            } else {
                // RLE compressed
                while (n>0) {
                    int nb = read(buf); // num of pixels
                    if ((nb&0x80)==0) { // 0x80=dec 128, bits 10000000
                        for (int i=0;i<=nb;i++) {
                            int b = read(buf);
                            pixels[idx++] = b;
                            int g = read(buf);
                            pixels[idx++] = g;
                            int r = read(buf);
                            pixels[idx++] = r;
                            pixels[idx++] = 0xff;
                        }
                    } else {
                        nb &= 0x7f;
                        int b = read(buf);
                        pixels[idx++] = b;
                        int g = read(buf);
                        pixels[idx++] = g;
                        int r = read(buf);
                        pixels[idx++] = r;
                        pixels[idx++] = 0xff;
                        for (int i=0;i<=nb;i++){
                        	pixels[idx++] = b;
                        	pixels[idx++] = g;
                        	pixels[idx++] = r;
                        	pixels[idx++] = 0xff;
                        }
                    }
                    n-=nb+1;
                }
            }

            
            return pixels;
    }
}
