package eu.pollux28.imggen.data;


import ar.com.hjg.pngj.*;
import eu.pollux28.imggen.ImgGen;
import org.apache.logging.log4j.Level;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

public class ImageManipulator {
    private final String path;
    private PngReader reader;
    private final int maxWidth;
    private final int maxHeight;
    private final int width;
    private final int height;
    private final int wCount;
    private final int hCount;
    private final String imageType;


    private final int[][] loadedImgPosCache;

    private final BufferedImage[] loadedImagesCache;

    public ImageManipulator(String path,String imageType) {
        this.path = path;
        Path configDir = Paths.get("", "imggen", "image", path);
        this.reader = new PngReader(configDir.toFile());
        this.width = reader.imgInfo.cols;
        this.height = reader.imgInfo.rows;
        this.imageType = imageType;
        //int[] combi = getBestCombi(width,height, Integer.MAX_VALUE/8);
        this.maxWidth =ImgGen.CONFIG.segmentImageWidth;
        this.maxHeight =ImgGen.CONFIG.segmentImageHeight;
        this.wCount = (width - 1) / maxWidth + 1;
        this.hCount = (height - 1) / maxHeight + 1;
        this.loadedImgPosCache = new int[ImgGen.CONFIG.imageCacheSize][2];
        this.loadedImagesCache = new BufferedImage[ImgGen.CONFIG.imageCacheSize];
        Arrays.fill(loadedImgPosCache,new int[]{-1,-1});
        segmentImage(imageType);
    }
    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void segmentImage(String imageType){
        ImgGen.logger.log(Level.INFO,"Starting image segmentation for {} - This may take a while !",imageType);
        int hProgress = 0;
        int channels = reader.imgInfo.channels;
        for(int i = 0; i< hCount; i++){
            int wProgress = 0;

            int hSpan = height - hProgress;

            int minHeight = Math.min(maxHeight, hSpan);
            IImageLineSet<?> lines = reader.readRows(minHeight,hProgress,1);
            for(int j = 0; j< wCount; j++){
                int wSpan = width - wProgress;
                int minWidth = Math.min(maxWidth, wSpan);
                IImageLine lint;
                BufferedImage image = new BufferedImage(minWidth, minHeight,BufferedImage.TYPE_INT_ARGB);
                int r =hProgress;
                while (r< hProgress+minHeight) {
                    lint = lines.getImageLine(r);
                    int[] scanLine = ((ImageLineInt)lint).getScanline();
                    for (int ii = wProgress; ii < wProgress+minWidth; ii++) {
                        int nextPixel = 0;
                        if(channels ==4){
                            nextPixel = ImageLineHelper.getPixelARGB8(lint,ii);
                        }else if(channels==3){
                            nextPixel = ImageLineHelper.getPixelRGB8(lint,ii);
                        }else if(channels==1){
                            nextPixel = (scanLine[ii] << 16) |(scanLine[ii] << 8)| (scanLine[ii]) | 0xFF000000;
                        }
                        image.setRGB(ii-wProgress,r-hProgress,nextPixel);
                    }
                    r+=1;
                }
                File img = Paths.get("", "imggen", "image","gen","imagegen_"+imageType+"_"+j+"_"+i+".png").toFile();
                try {
                    ImageIO.write(image,"png",img);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                wProgress += maxWidth;
            }
            hProgress += maxHeight;
        }
    }

//    private int[] getBestCombi(int w, int h, int a){
//        int[] bestCombi = {1,1,1,w*h};//p,n,a,nb
//        long nbT = (long) w *h;
//        long rect_aT=1;
//        for(int p =1;p<=w;p++){
//            if(nbT<p) break;
//            int rect_w = (int) Math.ceil(w/p);
//            if(a<rect_w)continue;
//            for(int n = 1; n<=h;n++){
//                int nb = p*n;
//                if(nbT<nb) continue;
//                int rect_h = (int) Math.ceil(h/n);
//                long rect_a;
//                rect_a =(long)rect_w*rect_h;
//                if(a<rect_a)continue;
//                if(nb<nbT||(nb == nbT && rect_a<rect_aT)){
//                    bestCombi[0]=p;
//                    bestCombi[1]=n;
//                    nbT = nb;
//                    rect_aT = rect_a;
//                }
//            }
//        }
//        return bestCombi;
//    }

    public boolean isInLoadedImages(int[] pos){
        for (int[] ints : this.loadedImgPosCache) {
            if (Arrays.equals(pos, ints)) {
                return true;
            }
        }
        return false;
    }

    public BufferedImage getImage(int x, int y){
        int wNumber = x/maxWidth;
        int hNumber = y/maxHeight;
        BufferedImage img = null;
        try {
            img = ImageIO.read(Paths.get("", "imggen", "image","gen","imagegen_"+imageType+"_"+wNumber+"_"+hNumber+".png").toFile());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return img;
    }

    private void putInCache(int[] pos, BufferedImage image){
        for(int i =0; i<this.loadedImgPosCache.length;i++){
            this.loadedImgPosCache[i] = this.loadedImgPosCache[i+1];
            this.loadedImagesCache[i] = this.loadedImagesCache[i+1];
        }
        this.loadedImgPosCache[this.loadedImgPosCache.length-1] = pos;
        this.loadedImagesCache[this.loadedImagesCache.length-1] = image;
    }

    public int getRGB(int x,int y){
        final int[] pos = {x/maxWidth, y/maxHeight};
        if(!isInLoadedImages(pos)){
            putInCache(pos,getImage(x,y));
        }
        for(int i = 0; i<this.loadedImgPosCache.length;i++){
            if(Arrays.equals(pos,this.loadedImgPosCache[i])){
                int localX = x%maxWidth;
                int localY = y%maxHeight;
                return loadedImagesCache[i].getRGB(localX,localY);
            }
        }
        throw new IllegalStateException("Could not retrieve image for "+x+" , "+y+"");
    }
}
