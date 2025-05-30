//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.grapeshot.halfnes.video;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.awt.image.WritableRaster;

public abstract class Renderer {
    int frame_width;
    int clip = 8;
    int height;
    BufferedImage[] imgs;
    int imgctr;

    public Renderer() {
        this.height = 240 - 2 * this.clip;
        this.imgs = new BufferedImage[]{null, null, null, null};
        this.imgctr = 0;
    }

    protected final void init_images() {
        for(int var1 = 0; var1 < this.imgs.length; ++var1) {
            this.imgs[var1] = new BufferedImage(this.frame_width, this.height, 3);
        }

    }

    public abstract BufferedImage render(int[] var1, int[] var2, boolean var3);

    public void setClip(int var1) {
        this.clip = var1;
        this.height = 240 - 2 * this.clip;
    }

    public BufferedImage getBufferedImage(int[] var1) {
        BufferedImage var2 = this.imgs[++this.imgctr % this.imgs.length];
        WritableRaster var3 = var2.getRaster();
        int[] var4 = ((DataBufferInt)var3.getDataBuffer()).getData();
        System.arraycopy(var1, this.frame_width * this.clip, var4, 0, this.frame_width * this.height);
        return var2;
    }
}
