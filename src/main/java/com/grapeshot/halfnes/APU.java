//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.grapeshot.halfnes;

import com.grapeshot.halfnes.audio.AudioOutInterface;
import com.grapeshot.halfnes.audio.ExpansionSoundChip;
import com.grapeshot.halfnes.audio.NoiseTimer;
import com.grapeshot.halfnes.audio.SquareTimer;
import com.grapeshot.halfnes.audio.SwingAudioImpl;
import com.grapeshot.halfnes.audio.Timer;
import com.grapeshot.halfnes.audio.TriangleTimer;
import com.grapeshot.halfnes.mappers.Mapper;
import com.grapeshot.halfnes.ui.Oscilloscope;
import java.util.ArrayList;
import java.util.Iterator;

public class APU {
    private static final int[] TNDLOOKUP = initTndLookup();
    private static final int[] SQUARELOOKUP = initSquareLookup();
    private static final int[] lenctrload = new int[]{10, 254, 20, 2, 40, 4, 80, 6, 160, 8, 60, 10, 14, 12, 26, 14, 12, 16, 24, 18, 48, 20, 96, 22, 192, 24, 72, 26, 16, 28, 32, 30};
    private static final int[][] DUTYLOOKUP = new int[][]{{0, 1, 0, 0, 0, 0, 0, 0}, {0, 1, 1, 0, 0, 0, 0, 0}, {0, 1, 1, 1, 1, 0, 0, 0}, {1, 0, 0, 1, 1, 1, 1, 1}};
    public final NES nes;
    private final Timer[] timers = new Timer[]{new SquareTimer(8, 2), new SquareTimer(8, 2), new TriangleTimer(), new NoiseTimer()};
    private final ArrayList<ExpansionSoundChip> expnSound = new ArrayList();
    private final boolean[] lenCtrEnable = new boolean[]{true, true, true, true};
    private final int[] volume = new int[4];
    private final int[] lengthctr = new int[]{0, 0, 0, 0};
    private final boolean[] lenctrHalt = new boolean[]{true, true, true, true};
    private final int[] envelopeValue = new int[]{15, 15, 15, 15};
    private final int[] envelopeCounter = new int[]{0, 0, 0, 0};
    private final int[] envelopePos = new int[]{0, 0, 0, 0};
    private final boolean[] envConstVolume = new boolean[]{true, true, true, true};
    private final boolean[] envelopeStartFlag = new boolean[]{false, false, false, false};
    private final boolean[] sweepenable = new boolean[]{false, false};
    private final boolean[] sweepnegate = new boolean[]{false, false};
    private final boolean[] sweepsilence = new boolean[]{false, false};
    private final boolean[] sweepreload = new boolean[]{false, false};
    private final int[] sweepperiod = new int[]{15, 15};
    private final int[] sweepshift = new int[]{0, 0};
    private final int[] sweeppos = new int[]{0, 0};
    public int sampleRate = 1;
    public int sprdma_count;
    CPU cpu;
    CPURAM cpuram;
    private double cyclespersample;
    private int apucycle = 0;
    private int remainder = 0;
    private int[] noiseperiod;
    private long accum = 0L;
    private boolean soundFiltering;
    private int framectrreload;
    private int framectrdiv = 7456;
    private int dckiller = 0;
    private int lpaccum = 0;
    private boolean apuintflag = true;
    private boolean statusdmcint = false;
    private boolean statusframeint = false;
    private int framectr = 0;
    private int ctrmode = 4;
    private int[] dmcperiods;
    private int dmcrate = 54;
    private int dmcpos = 0;
    private int dmcshiftregister = 0;
    private int dmcbuffer = 0;
    private int dmcvalue = 0;
    private int dmcsamplelength = 1;
    private int dmcsamplesleft = 0;
    private int dmcstartaddr = 49152;
    private int dmcaddr = 49152;
    private int dmcbitsleft = 8;
    private boolean dmcsilence = true;
    private boolean dmcirq = false;
    private boolean dmcloop = false;
    private boolean dmcBufferEmpty = true;
    private int linearctr = 0;
    private int linctrreload = 0;
    private boolean linctrflag = false;
    private int cyclesPerFrame;
    private AudioOutInterface ai;

    public APU(NES var1, CPU var2, CPURAM var3) {
        this.nes = var1;
        this.cpu = var2;
        this.cpuram = var3;
        this.setParameters();
    }

    private static int[] initTndLookup() {
        int[] var0 = new int[203];

        for(int var1 = 0; var1 < var0.length; ++var1) {
            var0[var1] = (int)(163.67 / (24329.0 / (double)var1 + 100.0) * 49151.0);
        }

        return var0;
    }

    private static int[] initSquareLookup() {
        int[] var0 = new int[31];

        for(int var1 = 0; var1 < var0.length; ++var1) {
            var0[var1] = (int)(95.52 / (8128.0 / (double)var1 + 100.0) * 49151.0);
        }

        return var0;
    }

    public final synchronized void setParameters() {
        Mapper.TVType var1 = this.cpuram.mapper.getTVType();
        this.soundFiltering = PrefsSingleton.get().getBoolean("soundFiltering", false);
        this.sampleRate = PrefsSingleton.get().getInt("sampleRate", 44100);
        if (this.ai != null) {
            this.ai.destroy();
        }

        this.ai = new SwingAudioImpl(this.nes, this.sampleRate, var1);
        if (PrefsSingleton.get().getBoolean("showScope", false)) {
            this.ai = new Oscilloscope(this.ai);
        }

        switch (var1) {
            case NTSC:
            default:
                this.dmcperiods = new int[]{428, 380, 340, 320, 286, 254, 226, 214, 190, 160, 142, 128, 106, 84, 72, 54};
                this.noiseperiod = new int[]{4, 8, 16, 32, 64, 96, 128, 160, 202, 254, 380, 508, 762, 1016, 2034, 4068};
                this.framectrreload = 7456;
                this.cyclespersample = 1789773.0 / (double)this.sampleRate;
                this.cyclesPerFrame = 29781;
                break;
            case DENDY:
                this.dmcperiods = new int[]{428, 380, 340, 320, 286, 254, 226, 214, 190, 160, 142, 128, 106, 84, 72, 54};
                this.noiseperiod = new int[]{4, 8, 16, 32, 64, 96, 128, 160, 202, 254, 380, 508, 762, 1016, 2034, 4068};
                this.framectrreload = 7456;
                this.cyclespersample = 1773448.0 / (double)this.sampleRate;
                this.cyclesPerFrame = 35469;
                break;
            case PAL:
                this.cyclespersample = 1662607.0 / (double)this.sampleRate;
                this.dmcperiods = new int[]{398, 354, 316, 298, 276, 236, 210, 198, 176, 148, 132, 118, 98, 78, 66, 50};
                this.noiseperiod = new int[]{4, 8, 14, 30, 60, 88, 118, 148, 188, 236, 354, 472, 708, 944, 1890, 3778};
                this.framectrreload = 8312;
                this.cyclesPerFrame = 33252;
        }

    }

    public boolean bufferHasLessThan(int var1) {
        return this.ai.bufferHasLessThan(var1);
    }

    public final int read(int var1) {
        this.updateto(this.cpu.clocks);
        switch (var1) {
            case 21:
                int var2 = (this.lengthctr[0] > 0 ? 1 : 0) | (this.lengthctr[1] > 0 ? 2 : 0) | (this.lengthctr[2] > 0 ? 4 : 0) | (this.lengthctr[3] > 0 ? 8 : 0) | (this.dmcsamplesleft > 0 ? 16 : 0) | (this.statusframeint ? 64 : 0) | (this.statusdmcint ? 128 : 0);
                if (this.statusframeint) {
                    --this.cpu.interrupt;
                    this.statusframeint = false;
                }

                return var2;
            case 22:
                this.nes.getcontroller1().strobe();
                return this.nes.getcontroller1().getbyte() | 64;
            case 23:
                this.nes.getcontroller2().strobe();
                return this.nes.getcontroller2().getbyte() | 64;
            default:
                return 64;
        }
    }

    public void addExpnSound(ExpansionSoundChip var1) {
        this.expnSound.add(var1);
    }

    public void destroy() {
        this.ai.destroy();
    }

    public void pause() {
        this.ai.pause();
    }

    public void resume() {
        this.ai.resume();
    }

    public AudioOutInterface getAi() {
        return this.ai;
    }

    public final void write(int var1, int var2) {
        this.updateto(this.cpu.clocks - 1);
        int var3;
        switch (var1) {
            case 0:
                this.lenctrHalt[0] = (var2 & 32) != 0;
                this.timers[0].setduty(DUTYLOOKUP[var2 >> 6]);
                this.envConstVolume[0] = (var2 & 16) != 0;
                this.envelopeValue[0] = var2 & 15;
                break;
            case 1:
                this.sweepenable[0] = (var2 & 128) != 0;
                this.sweepperiod[0] = var2 >> 4 & 7;
                this.sweepnegate[0] = (var2 & 8) != 0;
                this.sweepshift[0] = var2 & 7;
                this.sweepreload[0] = true;
                break;
            case 2:
                this.timers[0].setperiod((this.timers[0].getperiod() & '︀') + (var2 << 1));
                break;
            case 3:
                if (this.lenCtrEnable[0]) {
                    this.lengthctr[0] = lenctrload[var2 >> 3];
                }

                this.timers[0].setperiod((this.timers[0].getperiod() & 511) + ((var2 & 7) << 9));
                this.timers[0].reset();
                this.envelopeStartFlag[0] = true;
                break;
            case 4:
                this.lenctrHalt[1] = (var2 & 32) != 0;
                this.timers[1].setduty(DUTYLOOKUP[var2 >> 6]);
                this.envConstVolume[1] = (var2 & 16) != 0;
                this.envelopeValue[1] = var2 & 15;
                break;
            case 5:
                this.sweepenable[1] = (var2 & 128) != 0;
                this.sweepperiod[1] = var2 >> 4 & 7;
                this.sweepnegate[1] = (var2 & 8) != 0;
                this.sweepshift[1] = var2 & 7;
                this.sweepreload[1] = true;
                break;
            case 6:
                this.timers[1].setperiod((this.timers[1].getperiod() & '︀') + (var2 << 1));
                break;
            case 7:
                if (this.lenCtrEnable[1]) {
                    this.lengthctr[1] = lenctrload[var2 >> 3];
                }

                this.timers[1].setperiod((this.timers[1].getperiod() & 511) + ((var2 & 7) << 9));
                this.timers[1].reset();
                this.envelopeStartFlag[1] = true;
                break;
            case 8:
                this.linctrreload = var2 & 127;
                this.lenctrHalt[2] = (var2 & 128) != 0;
            case 9:
            case 13:
            default:
                break;
            case 10:
                this.timers[2].setperiod((this.timers[2].getperiod() & '\uff00') + var2);
                break;
            case 11:
                if (this.lenCtrEnable[2]) {
                    this.lengthctr[2] = lenctrload[var2 >> 3];
                }

                this.timers[2].setperiod((this.timers[2].getperiod() & 255) + ((var2 & 7) << 8));
                this.linctrflag = true;
                break;
            case 12:
                this.lenctrHalt[3] = (var2 & 32) != 0;
                this.envConstVolume[3] = (var2 & 16) != 0;
                this.envelopeValue[3] = var2 & 15;
                break;
            case 14:
                this.timers[3].setduty((var2 & 128) != 0 ? 6 : 1);
                this.timers[3].setperiod(this.noiseperiod[var2 & 15]);
                break;
            case 15:
                if (this.lenCtrEnable[3]) {
                    this.lengthctr[3] = lenctrload[var2 >> 3];
                }

                this.envelopeStartFlag[3] = true;
                break;
            case 16:
                this.dmcirq = (var2 & 128) != 0;
                this.dmcloop = (var2 & 64) != 0;
                this.dmcrate = this.dmcperiods[var2 & 15];
                if (!this.dmcirq && this.statusdmcint) {
                    --this.cpu.interrupt;
                    this.statusdmcint = false;
                }
                break;
            case 17:
                this.dmcvalue = var2 & 127;
                break;
            case 18:
                this.dmcstartaddr = (var2 << 6) + '쀀';
                break;
            case 19:
                this.dmcsamplelength = (var2 << 4) + 1;
                break;
            case 20:
                for(var3 = 0; var3 < 256; ++var3) {
                    this.cpuram.write(8196, this.cpuram.read((var2 << 8) + var3));
                }

                this.sprdma_count = 2;
                break;
            case 21:
                for(var3 = 0; var3 < 4; ++var3) {
                    this.lenCtrEnable[var3] = (var2 & 1 << var3) != 0;
                    if (!this.lenCtrEnable[var3]) {
                        this.lengthctr[var3] = 0;
                    }
                }

                if ((var2 & 16) != 0) {
                    if (this.dmcsamplesleft == 0) {
                        this.restartdmc();
                    }
                } else {
                    this.dmcsamplesleft = 0;
                    this.dmcsilence = true;
                }

                if (this.statusdmcint) {
                    --this.cpu.interrupt;
                    this.statusdmcint = false;
                }
                break;
            case 22:
                this.nes.getcontroller1().output((var2 & 1) != 0);
                this.nes.getcontroller2().output((var2 & 1) != 0);
                break;
            case 23:
                this.ctrmode = (var2 & 128) != 0 ? 5 : 4;
                this.apuintflag = (var2 & 64) != 0;
                this.framectr = 0;
                this.framectrdiv = this.framectrreload + 8;
                if (this.apuintflag && this.statusframeint) {
                    this.statusframeint = false;
                    --this.cpu.interrupt;
                }

                if (this.ctrmode == 5) {
                    this.setenvelope();
                    this.setlinctr();
                    this.setlength();
                    this.setsweep();
                }
        }

    }

    public final void updateto(int var1) {
        if (this.soundFiltering) {
            for(; this.apucycle < var1; ++this.apucycle) {
                ++this.remainder;
                this.clockdmc();
                if (--this.framectrdiv <= 0) {
                    this.framectrdiv = this.framectrreload;
                    this.clockframecounter();
                }

                this.timers[0].clock();
                this.timers[1].clock();
                if (this.lengthctr[2] > 0 && this.linearctr > 0) {
                    this.timers[2].clock();
                }

                this.timers[3].clock();
                if (!this.expnSound.isEmpty()) {
                    Iterator var5 = this.expnSound.iterator();

                    while(var5.hasNext()) {
                        ExpansionSoundChip var6 = (ExpansionSoundChip)var5.next();
                        var6.clock(1);
                    }
                }

                this.accum += (long)this.getOutputLevel();
                if ((double)this.apucycle % this.cyclespersample < 1.0) {
                    this.ai.outputSample(this.lowpass_filter(this.highpass_filter((int)(this.accum / (long)this.remainder))));
                    this.remainder = 0;
                    this.accum = 0L;
                }
            }
        } else {
            for(; this.apucycle < var1; ++this.apucycle) {
                ++this.remainder;
                this.clockdmc();
                if (--this.framectrdiv <= 0) {
                    this.framectrdiv = this.framectrreload;
                    this.clockframecounter();
                }

                if ((double)this.apucycle % this.cyclespersample < 1.0) {
                    this.timers[0].clock(this.remainder);
                    this.timers[1].clock(this.remainder);
                    if (this.lengthctr[2] > 0 && this.linearctr > 0) {
                        this.timers[2].clock(this.remainder);
                    }

                    this.timers[3].clock(this.remainder);
                    int var2 = this.getOutputLevel();
                    if (!this.expnSound.isEmpty()) {
                        Iterator var3 = this.expnSound.iterator();

                        while(var3.hasNext()) {
                            ExpansionSoundChip var4 = (ExpansionSoundChip)var3.next();
                            var4.clock(this.remainder);
                        }
                    }

                    this.remainder = 0;
                    this.ai.outputSample(this.lowpass_filter(this.highpass_filter(var2)));
                }
            }
        }

    }

    private int getOutputLevel() {
        int var1 = SQUARELOOKUP[this.volume[0] * this.timers[0].getval() + this.volume[1] * this.timers[1].getval()];
        var1 += TNDLOOKUP[3 * this.timers[2].getval() + 2 * this.volume[3] * this.timers[3].getval() + this.dmcvalue];
        if (!this.expnSound.isEmpty()) {
            var1 = (int)((double)var1 * 0.8);

            ExpansionSoundChip var3;
            for(Iterator var2 = this.expnSound.iterator(); var2.hasNext(); var1 += var3.getval()) {
                var3 = (ExpansionSoundChip)var2.next();
            }
        }

        return var1;
    }

    private int highpass_filter(int var1) {
        var1 += this.dckiller;
        this.dckiller -= var1 >> 8;
        this.dckiller += var1 > 0 ? -1 : 1;
        return var1;
    }

    private int lowpass_filter(int var1) {
        var1 += this.lpaccum;
        this.lpaccum = (int)((double)this.lpaccum - (double)var1 * 0.9);
        return this.lpaccum;
    }

    public final void finishframe() {
        this.updateto(this.cyclesPerFrame);
        this.apucycle = 0;
        this.ai.flushFrame(this.nes.isFrameLimiterOn());
    }

    private void clockframecounter() {
        if (this.ctrmode == 4 || this.ctrmode == 5 && this.framectr != 3) {
            this.setenvelope();
            this.setlinctr();
        }

        if (this.ctrmode == 4 && (this.framectr == 1 || this.framectr == 3) || this.ctrmode == 5 && (this.framectr == 1 || this.framectr == 4)) {
            this.setlength();
            this.setsweep();
        }

        if (!this.apuintflag && this.framectr == 3 && this.ctrmode == 4 && !this.statusframeint) {
            ++this.cpu.interrupt;
            this.statusframeint = true;
        }

        ++this.framectr;
        this.framectr %= this.ctrmode;
        this.setvolumes();
    }

    private void setvolumes() {
        this.volume[0] = this.lengthctr[0] > 0 && !this.sweepsilence[0] ? (this.envConstVolume[0] ? this.envelopeValue[0] : this.envelopeCounter[0]) : 0;
        this.volume[1] = this.lengthctr[1] > 0 && !this.sweepsilence[1] ? (this.envConstVolume[1] ? this.envelopeValue[1] : this.envelopeCounter[1]) : 0;
        this.volume[3] = this.lengthctr[3] <= 0 ? 0 : (this.envConstVolume[3] ? this.envelopeValue[3] : this.envelopeCounter[3]);
    }

    private void clockdmc() {
        if (this.dmcBufferEmpty && this.dmcsamplesleft > 0) {
            this.dmcfillbuffer();
        }

        this.dmcpos = (this.dmcpos + 1) % this.dmcrate;
        if (this.dmcpos == 0) {
            if (this.dmcbitsleft <= 0) {
                this.dmcbitsleft = 8;
                if (this.dmcBufferEmpty) {
                    this.dmcsilence = true;
                } else {
                    this.dmcsilence = false;
                    this.dmcshiftregister = this.dmcbuffer;
                    this.dmcBufferEmpty = true;
                }
            }

            if (!this.dmcsilence) {
                this.dmcvalue += (this.dmcshiftregister & 1) != 0 ? 2 : -2;
                if (this.dmcvalue > 127) {
                    this.dmcvalue = 127;
                }

                if (this.dmcvalue < 0) {
                    this.dmcvalue = 0;
                }

                this.dmcshiftregister >>= 1;
                --this.dmcbitsleft;
            }
        }

    }

    private void dmcfillbuffer() {
        if (this.dmcsamplesleft > 0) {
            this.dmcbuffer = this.cpuram.read(this.dmcaddr++);
            this.dmcBufferEmpty = false;
            this.cpu.stealcycles(4);
            if (this.dmcaddr > 65535) {
                this.dmcaddr = 32768;
            }

            --this.dmcsamplesleft;
            if (this.dmcsamplesleft == 0) {
                if (this.dmcloop) {
                    this.restartdmc();
                } else if (this.dmcirq && !this.statusdmcint) {
                    ++this.cpu.interrupt;
                    this.statusdmcint = true;
                }
            }
        } else {
            this.dmcsilence = true;
        }

    }

    private void restartdmc() {
        this.dmcaddr = this.dmcstartaddr;
        this.dmcsamplesleft = this.dmcsamplelength;
        this.dmcsilence = false;
    }

    private void setlength() {
        for(int var1 = 0; var1 < 4; ++var1) {
            if (!this.lenctrHalt[var1] && this.lengthctr[var1] > 0) {
                int var10002 = this.lengthctr[var1]--;
                if (this.lengthctr[var1] == 0) {
                    this.setvolumes();
                }
            }
        }

    }

    private void setlinctr() {
        if (this.linctrflag) {
            this.linearctr = this.linctrreload;
        } else if (this.linearctr > 0) {
            --this.linearctr;
        }

        if (!this.lenctrHalt[2]) {
            this.linctrflag = false;
        }

    }

    private void setenvelope() {
        for(int var1 = 0; var1 < 4; ++var1) {
            int var10002;
            if (this.envelopeStartFlag[var1]) {
                this.envelopeStartFlag[var1] = false;
                this.envelopePos[var1] = this.envelopeValue[var1] + 1;
                this.envelopeCounter[var1] = 15;
            } else {
                var10002 = this.envelopePos[var1]--;
            }

            if (this.envelopePos[var1] <= 0) {
                this.envelopePos[var1] = this.envelopeValue[var1] + 1;
                if (this.envelopeCounter[var1] > 0) {
                    var10002 = this.envelopeCounter[var1]--;
                } else if (this.lenctrHalt[var1] && this.envelopeCounter[var1] <= 0) {
                    this.envelopeCounter[var1] = 15;
                }
            }
        }

    }

    private void setsweep() {
        for(int var1 = 0; var1 < 2; ++var1) {
            this.sweepsilence[var1] = false;
            if (this.sweepreload[var1]) {
                this.sweepreload[var1] = false;
                this.sweeppos[var1] = this.sweepperiod[var1];
            }

            int var10002 = this.sweeppos[var1]++;
            int var2 = this.timers[var1].getperiod() >> 1;
            int var3 = var2 >> this.sweepshift[var1];
            if (this.sweepnegate[var1]) {
                var3 = -var3 + var1;
            }

            var3 += var2;
            if (var2 >= 8 && var3 <= 2047) {
                if (this.sweepenable[var1] && this.sweepshift[var1] != 0 && this.lengthctr[var1] > 0 && this.sweeppos[var1] > this.sweepperiod[var1]) {
                    this.sweeppos[var1] = 0;
                    this.timers[var1].setperiod(var3 << 1);
                }
            } else {
                this.sweepsilence[var1] = true;
            }
        }

    }

    public void setAi(AudioOutInterface var1) {
        this.ai = var1;
    }
}
