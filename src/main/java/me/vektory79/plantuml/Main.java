package me.vektory79.plantuml;

/*-
 * #%L
 * plantuml-graal
 * %%
 * Copyright (C) 2019 - 2020 vektory79.me
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 2 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-2.0.html>.
 * #L%
 */

import com.oracle.svm.core.jdk.NativeLibrarySupport;
import net.sourceforge.plantuml.Run;
import sun.awt.FontConfiguration;
import sun.awt.SunHints;
import sun.java2d.loops.CompositeType;
import sun.java2d.loops.MaskBlit;
import sun.java2d.loops.SurfaceType;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException, URISyntaxException {
//        System.out.println("DISPLAY=" + System.getenv("DISPLAY"));
//        System.setProperty("java.awt.headless", "true");

        autoLoadLibs();

        if (System.getProperty("java.awt.graphicsenv") == null) {
            System.setProperty("java.awt.graphicsenv", "sun.awt.X11GraphicsEnvironment");
        }

        if (System.getProperty("awt.toolkit") == null) {
            System.setProperty("awt.toolkit", "sun.awt.X11.XToolkit");
        }

        // Initialize class sun.java2d.Disposer (aarch64-shenandoah-jdk8u222-b10):
        //   jdk/src/share/native/sun/java2d/SurfaceData.c:237
        //   jdk/src/share/native/sun/java2d/SurfaceData.c:154
        //   jdk/src/share/native/sun/java2d/Disposer.c:51
        sun.java2d.Disposer.getQueue();

        // Initialize class CompositeType (aarch64-shenandoah-jdk8u222-b10):
        //   jdk/src/share/native/sun/java2d/loops/GraphicsPrimitiveMgr.c:331
        CompositeType t2 = CompositeType.SrcNoEa;

        // Other static initialization leak fixes:
        SurfaceType opaqueColor = SurfaceType.OpaqueColor;
        int primTypeID = MaskBlit.primTypeID;
        int aatextLcdContrast = SunHints.INTKEY_AATEXT_LCD_CONTRAST;
        RenderingHints.Key alphaInterpolation = RenderingHints.KEY_ALPHA_INTERPOLATION;

        // To debug SEGFAULT: https://losst.ru/oshibka-segmentirovaniya-ubuntu
//        System.out.print("Ready. To continue press enter...");
//        System.in.read();

        Run.main(args);
    }

    private static void autoLoadLibs() throws URISyntaxException {
        Path execDirPath = Paths.get(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParent();
        String libDirPath = execDirPath.resolve("lib").toString();
        // sun/awt/FontConfiguration.java:182
        System.setProperty("java.home", execDirPath.toString());
        System.setProperty("java.library.path", libDirPath);
        NativeLibrarySupport.singleton().loadLibrary(libDirPath + "/libjvm.so", true);
        NativeLibrarySupport.singleton().loadLibrary(libDirPath + "/libverify.so", true);
        NativeLibrarySupport.singleton().loadLibrary(libDirPath + "/libjava.so", true);
        NativeLibrarySupport.singleton().loadLibrary(libDirPath + "/libawt.so", true);
        NativeLibrarySupport.singleton().loadLibrary(libDirPath + "/libmlib_image.so", true);
        if (Boolean.getBoolean("java.awt.headless")) {
            NativeLibrarySupport.singleton().loadLibrary(libDirPath + "/libawt_headless.so", true);
        } else {
            NativeLibrarySupport.singleton().loadLibrary(libDirPath + "/libawt_xawt.so", true);
        }
    }

    private static void loadLibs(String javaHome) {
        // sun/awt/FontConfiguration.java:182
        System.setProperty("java.home", javaHome);
        System.setProperty("java.library.path",
                javaHome + "/jre/lib/amd64:" +
                        javaHome + "/jre/lib/amd64/server");
//        System.out.println("Load: libjvm.so");
        NativeLibrarySupport.singleton().loadLibrary(javaHome + "/jre/lib/amd64/server/libjvm.so", true);
//        System.out.println("Load: libverify.so");
        NativeLibrarySupport.singleton().loadLibrary(javaHome + "/jre/lib/amd64/libverify.so", true);
//        System.out.println("Load: libjava.so");
        NativeLibrarySupport.singleton().loadLibrary(javaHome + "/jre/lib/amd64/libjava.so", true);
//        System.out.println("Load: libawt.so");
        NativeLibrarySupport.singleton().loadLibrary(javaHome + "/jre/lib/amd64/libawt.so", true);
//        System.out.println("Load: libawt_xawt.so");
        NativeLibrarySupport.singleton().loadLibrary(javaHome + "/jre/lib/amd64/libawt_xawt.so", true);
//        System.out.println("Libraries loaded!!!");
    }
}
