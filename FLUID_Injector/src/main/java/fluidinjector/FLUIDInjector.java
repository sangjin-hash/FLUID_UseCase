package fluidinjector;

import soot.*;
import soot.jimple.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class FLUIDInjector {

    private static String androidJar = System.getProperty("user.home") + "/Android/Sdk/platforms";
    final static String outputPath = System.getProperty("user.dir") + File.separator + "/output";

    public static void main(String[] args){
		if (args.length != 1) {
			System.out.println("./gradlew run --args=\"APK_FILE_PATH\"");
			System.exit(-1);
		}
		String apkPath = args[0];

        // Clean the outputPath
        final File[] files = (new File(outputPath)).listFiles();
        if (files != null && files.length > 0)
            Arrays.asList(files).forEach(File::delete);

        // Initialize Soot
        InstrumentUtil.setupSoot(androidJar, apkPath, outputPath);

        PackManager.v().getPack("jtp").add(
                new Transform("jtp.RPCIntfInjector", new RPCIntfInjector()));

        PackManager.v().runPacks();
        PackManager.v().writeOutput();
    }

	static void check(String name) {
		SootClass cls = Scene.v().getSootClass(name);
		System.out.println(cls);
		System.out.println("==Field==");
		for (SootField f : cls.getFields())
			System.out.println(f);
		System.out.println("==Method==");
		for (SootMethod m : cls.getMethods()) {
			System.out.println(m.getSubSignature());
			if (m.hasActiveBody())
				System.out.println(m.getActiveBody()); 
		}
		System.out.println("==End==");
		System.out.println();
	}
}
