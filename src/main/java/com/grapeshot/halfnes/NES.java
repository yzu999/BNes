//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.grapeshot.halfnes;

import com.grapeshot.halfnes.cheats.ActionReplay;
import com.grapeshot.halfnes.mappers.BadMapperException;
import com.grapeshot.halfnes.mappers.Mapper;
import com.grapeshot.halfnes.ui.ControllerInterface;
import com.grapeshot.halfnes.ui.FrameLimiterImpl;
import com.grapeshot.halfnes.ui.FrameLimiterInterface;
import com.grapeshot.halfnes.ui.GUIInterface;

public class NES {
    public static final String VERSION = "062";
    private final FrameLimiterInterface limiter = new FrameLimiterImpl(this, 16639267L);
    public boolean runEmulation = false;
    public long frameStartTime;
    public long framecount;
    public long frameDoneTime;
    private Mapper mapper;
    private APU apu;
    private CPU cpu;
    private CPURAM cpuram;
    private PPU ppu;
    private GUIInterface gui;
    private ControllerInterface controller1;
    private ControllerInterface controller2;
    private boolean dontSleep = false;
    private boolean shutdown = false;
    private boolean frameLimiterOn = true;
    private String curRomPath;
    private String curRomName;
    private ActionReplay actionReplay;

    public NES(GUIInterface var1) {
        if (var1 != null) {
            this.gui = var1;
            var1.setNES(this);
            var1.run();
        }

    }

    public CPURAM getCPURAM() {
        return this.cpuram;
    }

    public CPU getCPU() {
        return this.cpu;
    }

    public APU getApu() {
        return this.apu;
    }

    public PPU getPpu() {
        return this.ppu;
    }

    public Mapper getMapper() {
        return this.mapper;
    }

    public void run(String var1) {
        Thread.currentThread().setPriority(6);
        this.curRomPath = var1;
        this.gui.loadROMs(var1);
        this.run();
    }

    public void run() {
        while(!this.shutdown) {
            if (this.runEmulation) {
                this.frameStartTime = System.nanoTime();
                this.actionReplay.applyPatches();
                this.runframe();
                if (this.frameLimiterOn && !this.dontSleep) {
                    this.limiter.sleep();
                }

                this.frameDoneTime = System.nanoTime() - this.frameStartTime;
            } else {
                this.limiter.sleepFixed();
                if (this.ppu != null && this.framecount > 1L) {
                    this.gui.render();
                }
            }
        }

    }

    public synchronized void runframe() {
        this.ppu.runFrame();
        this.dontSleep = this.apu.bufferHasLessThan(1000);
        this.apu.finishframe();
        this.cpu.modcycles();
        this.ppu.renderFrame(this.gui);
        if ((this.framecount & 2047L) == 0L) {
            this.saveSRAM(true);
        }

        ++this.framecount;
    }

    public void setControllers(ControllerInterface var1, ControllerInterface var2) {
        this.controller1 = var1;
        this.controller2 = var2;
    }

    public void toggleFrameLimiter() {
        this.frameLimiterOn = !this.frameLimiterOn;
    }

    public synchronized void loadROM(String var1) {
        this.loadROM(var1, (Integer)null);
    }

    public synchronized void loadROM(String var1, Integer var2) {
        this.runEmulation = false;
        if (!FileUtils.exists(var1) || !FileUtils.getExtension(var1).equalsIgnoreCase(".nes") && !FileUtils.getExtension(var1).equalsIgnoreCase(".nsf")) {
            this.gui.messageBox("Could not load file:\nFile " + var1 + "\ndoes not exist or is not a valid NES game.");
        } else {
            Mapper var3;
            try {
                ROMLoader var4 = new ROMLoader(var1);
                var4.parseHeader();
                var3 = Mapper.getCorrectMapper(var4);
                var3.setLoader(var4);
                var3.loadrom();
            } catch (BadMapperException var5) {
                this.gui.messageBox("Error Loading File: ROM is corrupted or uses an unsupported mapper.\n" + var5.getMessage());
                return;
            } catch (Exception var6) {
                this.gui.messageBox("Error Loading File: ROM is corrupted or uses an unsupported mapper.\n" + var6.toString() + var6.getMessage());
                var6.printStackTrace();
                return;
            }

            if (this.apu != null) {
                this.apu.destroy();
                this.saveSRAM(false);
                this.mapper.destroy();
                this.cpu = null;
                this.cpuram = null;
                this.ppu = null;
            }

            this.mapper = var3;
            this.cpuram = this.mapper.getCPURAM();
            this.actionReplay = new ActionReplay(this.cpuram);
            this.cpu = this.mapper.cpu;
            this.ppu = this.mapper.ppu;
            this.apu = new APU(this, this.cpu, this.cpuram);
            this.cpuram.setAPU(this.apu);
            this.cpuram.setPPU(this.ppu);
            this.curRomPath = var1;
            this.curRomName = FileUtils.getFilenamefromPath(var1);
            this.framecount = 0L;
            if (this.mapper.hasSRAM()) {
                this.loadSRAM();
            }

            this.cpu.init(var2);
            this.mapper.init();
            this.setParameters();
            this.runEmulation = true;
        }

    }

    public void saveSRAM(boolean var1) {
        if (this.mapper != null && this.mapper.hasSRAM() && this.mapper.supportsSaves()) {
            if (var1) {
                FileUtils.asyncwritetofile(this.mapper.getPRGRam(), FileUtils.stripExtension(this.curRomPath) + ".sav");
            } else {
                FileUtils.writetofile(this.mapper.getPRGRam(), FileUtils.stripExtension(this.curRomPath) + ".sav");
            }
        }

    }

    public void loadSRAM() {
        String var1 = FileUtils.stripExtension(this.curRomPath) + ".sav";
        if (FileUtils.exists(var1) && this.mapper.supportsSaves()) {
            this.mapper.setPRGRAM(FileUtils.readfromfile(var1));
        }

    }

    public void quit() {
        if (!this.shutdown) {
            if (this.cpu != null && this.curRomPath != null) {
                this.runEmulation = false;
                this.saveSRAM(false);
            }

            if (this.apu != null) {
                this.apu.destroy();
            }

            if (this.mapper != null) {
                this.mapper.destroy();
            }

            this.shutdown = true;
        }
    }

    public synchronized void reset() {
        if (this.cpu != null) {
            this.mapper.reset();
            this.cpu.reset();
            this.runEmulation = true;
            this.apu.pause();
            this.apu.resume();
        }

        this.framecount = 0L;
    }

    public synchronized void reloadROM() {
        this.loadROM(this.curRomPath);
    }

    public synchronized void pause() {
        if (this.apu != null) {
            this.apu.pause();
        }

        this.runEmulation = false;
    }

    public long getFrameTime() {
        return this.frameDoneTime;
    }

    public String getrominfo() {
        return this.mapper != null ? this.mapper.getrominfo() : null;
    }

    public synchronized void frameAdvance() {
        this.runEmulation = false;
        if (this.cpu != null) {
            this.runframe();
        }

    }

    public synchronized void resume() {
        if (this.apu != null) {
            this.apu.resume();
        }

        if (this.cpu != null) {
            this.runEmulation = true;
        }

    }

    public String getCurrentRomName() {
        return this.curRomName;
    }

    public boolean isFrameLimiterOn() {
        return this.frameLimiterOn;
    }

    public void messageBox(String var1) {
        if (this.gui != null) {
            this.gui.messageBox(var1);
        }

    }

    public ControllerInterface getcontroller1() {
        return this.controller1;
    }

    public ControllerInterface getcontroller2() {
        return this.controller2;
    }

    public synchronized void setParameters() {
        if (this.apu != null) {
            this.apu.setParameters();
        }

        if (this.ppu != null) {
            this.ppu.setParameters();
        }

        if (this.limiter != null && this.mapper != null) {
            switch (this.mapper.getTVType()) {
                case NTSC:
                default:
                    this.limiter.setInterval(16639267L);
                    break;
                case PAL:
                case DENDY:
                    this.limiter.setInterval(19997200L);
            }
        }

    }

    public boolean isShutdown() {
        return this.shutdown;
    }

    public synchronized ActionReplay getActionReplay() {
        return this.actionReplay;
    }
}
