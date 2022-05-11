package fluidinjector;

import soot.*;
import soot.jimple.*;
import soot.util.Chain;
import soot.util.EmptyChain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class RPCIntfInjector extends BodyTransformer{
	boolean isInsert = true;
	boolean isAnalize = false;
	final static int MAINACTIVITY_INDEX2 = 1918;
	final static int MAINACTIVITY_INDEX = 1391;
	final static String TMP_DIR_PATH = "/data/local/tmp/";
	final static String FLUID_LIB_PATH = "/data/local/tmp/fluidlib.apk";
	final static String FLUID_MAIN_CLASS = "com.hmsl.fluidlib.FLUIDMain";
	final static String FLUID_PACKAGE_NAME = "com.hmsl.fluidmanager";
	final static String FLUID_SERVICE_NAME = "com.hmsl.fluidmanager.FLUIDManagerService";

	public RPCIntfInjector() {
		super();
	}

	@Override
	protected void internalTransform(Body b, String s, Map<String, String> map) {
		JimpleBody body = (JimpleBody) b;

		if(AndroidUtil.isAndroidMethod(b.getMethod()))
			return;
		if(isAnalize)
		{
			isAnalize = false;
			printClasses(body);
			
		}
		
		if(isInsert)
		{
			isInsert = false;
			Object[] arr = Scene.v().getApplicationClasses().toArray();
			SootClass a = (SootClass) arr[MAINACTIVITY_INDEX];//MainActivity
		
//			SootClass aa = Scene.v().getSootClass("dalvik.system.DexClassLoader");
//			SootField testfld = new SootField("dex", aa.getType());
//			a.addField(testfld);
			
			SootField testField = InstrumentUtil.addField(a, "dex", RefType.v("dalvik.system.DexClassLoader"), Modifier.PUBLIC|Modifier.STATIC);
			
			
			
		}
		if (b.getMethod().getName().equals("onCreate")) {
			System.out.println("==== before ====");
			System.out.println(b);
			injectCode((JimpleBody) b);
			//injectfield((JimpleBody) b);
			//injectClassLoader((JimpleBody) b);
			System.out.println("==== after ====");
			System.out.println(b);
		}
		if(b.getMethod().getName().equals("onLongClick"))
		{
			System.out.println("==== before ====");
			System.out.println(b);
			injectTriggerCode((JimpleBody) b);
			//injectClassLoader((JimpleBody) b);
			System.out.println("==== after ====");
			System.out.println(b);
		}
		else if(b.getMethod().getName().equals("onClick")) 
		{
			System.out.println("==== before ====");
			System.out.println(b);
			System.out.println("onCLick");
			//printLocals((JimpleBody)b);
			injectUpdateCode((JimpleBody) b);
			
			System.out.println("==== after ====");
			System.out.println(b);
		}
	}
	void printClasses(JimpleBody body)
	{
		Object[] arr = Scene.v().getApplicationClasses().toArray();
		for(int i = 0;i<arr.length;i++)
		{
			System.out.println("Class ["+ i+ "] : " + arr[i].toString());
		}
	}
	void printLocals(JimpleBody body)
	{
		Object[] arr = body.getLocals().toArray();
		for(int i = 0;i<arr.length;i++)
		{
			System.out.println("Local ["+ i+ "] : " + arr[i].toString());
		}
	}
	void injectfield(JimpleBody body)
	{
		UnitPatchingChain units = body.getUnits();
		Object[] test = units.toArray();
		for(int i = 0;i<test.length;i++)
		{
			System.out.println("unit : ["+i+"] : " + test[i].toString());
		}
	}
	void injectUpdateCode(JimpleBody body)
	{
		UnitPatchingChain units = body.getUnits();
		List<Unit> generated = new ArrayList<>();
		Local thisVar = body.getThisLocal();
		
		Local exceptionVar = InstrumentUtil.generateNewLocal(body, RefType.v("java.lang.Exception"));
		Local viewVar = InstrumentUtil.generateNewLocal(body, RefType.v("android.view.View"));
		Local methodVar = InstrumentUtil.generateNewLocal(body, RefType.v("java.lang.reflect.Method"));
		Local clazzVar = InstrumentUtil.generateNewLocal(body, RefType.v("java.lang.Class"));
		Local dexLoaderVar = InstrumentUtil.generateNewLocal(body, RefType.v("dalvik.system.DexClassLoader"));
		Local classVar = InstrumentUtil.generateNewLocal(body, RefType.v("java.lang.Class"));
		Local classLoaderVar = InstrumentUtil.generateNewLocal(body, RefType.v("java.lang.ClassLoader"));
		Local classStringVar = InstrumentUtil.generateNewLocal(body, RefType.v("java.lang.Class"));
		Local classViewVar = InstrumentUtil.generateNewLocal(body, RefType.v("java.lang.Class"));
		Local widgetnameVar = InstrumentUtil.generateNewLocal(body, RefType.v("java.lang.String"));
		Local eviewVar = InstrumentUtil.generateNewLocal(body, RefType.v("android.view.View"));
		Local signatureVar = InstrumentUtil.generateNewLocal(body, RefType.v("java.lang.String"));
		Local classArrayVar = InstrumentUtil.generateNewLocal(body, ArrayType.v(RefType.v("java.lang.Class"),1));
		Local objectArrayVar = InstrumentUtil.generateNewLocal(body, ArrayType.v(RefType.v("java.lang.Object"),1));
		Local paramIntVar = InstrumentUtil.generateNewLocal(body, IntType.v());
		Local paramIntegerVar = InstrumentUtil.generateNewLocal(body, RefType.v("java.lang.Integer"));

		//get parameter
		generated.add(Jimple.v().newAssignStmt(viewVar,body.getParameterLocal(0)));
		System.out.println("onClick");
		printLocals(body);
		
		//get dexclassloader from field
		Object[] arr = Scene.v().getApplicationClasses().toArray();
		SootClass a = (SootClass) arr[MAINACTIVITY_INDEX];
		SootField ar = a.getFieldByName("dex");
		Jimple.v().newStaticFieldRef(ar.makeRef());
		generated.add(Jimple.v().newAssignStmt(dexLoaderVar, Jimple.v().newStaticFieldRef(ar.makeRef())));
		
		generated.addAll(InstrumentUtil.generateVirtualInvokeStmt(body, "java.lang.ClassLoader", 
			"java.lang.Class loadClass(java.lang.String)", 
			dexLoaderVar, classVar, StringConstant.v(FLUID_MAIN_CLASS)));
		Unit tryBegin = generated.get(generated.size() -1);
		
		
		//create class array for getDeclaredMethod
		SootClass cls = Scene.v().getSootClass("java.lang.Class");
		generated.add(Jimple.v().newAssignStmt(classArrayVar, Jimple.v().newNewArrayExpr(cls.getType(), IntConstant.v(2))));
		
		//insert class to class array
		generated.add(Jimple.v().newAssignStmt(Jimple.v().newArrayRef(classArrayVar, IntConstant.v(0)), ClassConstant.v("Ljava/lang/String;")));
		generated.add(Jimple.v().newAssignStmt(Jimple.v().newArrayRef(classArrayVar, IntConstant.v(1)), ClassConstant.v("Landroid/view/View;")));
		
		//get runupdate
		generated.addAll(InstrumentUtil.generateVirtualInvokeStmt(body, "java.lang.Class", 
				"java.lang.reflect.Method getDeclaredMethod(java.lang.String,java.lang.Class[])", 
				classVar, methodVar, StringConstant.v("runUpdate"),classArrayVar));
		
		//make object
		SootClass cls2 = Scene.v().getSootClass("java.lang.Object");
		
		//get view
		Object[] locals = body.getLocals().toArray();
		generated.add(Jimple.v().newAssignStmt(viewVar, (Local)locals[5]));
		
		//get ui_update method unit
		Object[] unitArray = body.getUnits().toArray();
		String sig = unitArray[unitArray.length -2].toString();
		System.out.println("sig : " + sig);
		generated.add(Jimple.v().newAssignStmt(signatureVar,StringConstant.v(sig)));
		
		
		generated.add(Jimple.v().newAssignStmt(objectArrayVar, Jimple.v().newNewArrayExpr(cls2.getType(), IntConstant.v(2))));
		generated.add(Jimple.v().newAssignStmt(Jimple.v().newArrayRef(objectArrayVar, IntConstant.v(0)), signatureVar));
		generated.add(Jimple.v().newAssignStmt(Jimple.v().newArrayRef(objectArrayVar, IntConstant.v(1)), viewVar));
		
		//invoke runupdate
		generated.addAll(InstrumentUtil.generateVirtualInvokeStmt(body, "java.lang.reflect.Method", 
				"java.lang.Object invoke(java.lang.Object,java.lang.Object[])", 
				methodVar, null, NullConstant.v(),objectArrayVar));
		
		units.insertBefore(generated, units.getLast());
		Unit tryEnd = units.getLast(); //return

		// insert try-catch statement
		CaughtExceptionRef exceptionRef = soot.jimple.Jimple.v().newCaughtExceptionRef();
		Unit catchBegin = Jimple.v().newIdentityStmt(exceptionVar, exceptionRef);
		units.add(catchBegin);
		units.addAll(InstrumentUtil.generateVirtualInvokeStmt(body, "java.lang.Throwable", 
			"void printStackTrace()", exceptionVar, null));
		
		units.add(Jimple.v().newReturnVoidStmt());
		SootClass exceptionClass = Scene.v().getSootClass("java.lang.Exception");
		Trap trap = soot.jimple.Jimple.v().newTrap(exceptionClass, tryBegin, tryEnd, catchBegin);
		body.getTraps().add(trap);
		body.validate();
	}
	
	void injectUpdateCode_U(JimpleBody body)
	{
		UnitPatchingChain units = body.getUnits();
		List<Unit> generated = new ArrayList<>();
		Local thisVar = body.getThisLocal();
		
		Local exceptionVar = InstrumentUtil.generateNewLocal(body, RefType.v("java.lang.Exception"));
		Local viewVar = InstrumentUtil.generateNewLocal(body, RefType.v("android.view.View"));
		Local methodVar = InstrumentUtil.generateNewLocal(body, RefType.v("java.lang.reflect.Method"));
		Local clazzVar = InstrumentUtil.generateNewLocal(body, RefType.v("java.lang.Class"));
		Local dexLoaderVar = InstrumentUtil.generateNewLocal(body, RefType.v("dalvik.system.DexClassLoader"));
		Local classVar = InstrumentUtil.generateNewLocal(body, RefType.v("java.lang.Class"));
		Local classLoaderVar = InstrumentUtil.generateNewLocal(body, RefType.v("java.lang.ClassLoader"));
		Local classStringVar = InstrumentUtil.generateNewLocal(body, RefType.v("java.lang.Class"));
		Local classViewVar = InstrumentUtil.generateNewLocal(body, RefType.v("java.lang.Class"));
		Local widgetnameVar = InstrumentUtil.generateNewLocal(body, RefType.v("java.lang.String"));
		Local eviewVar = InstrumentUtil.generateNewLocal(body, RefType.v("android.view.View"));
		Local signatureVar = InstrumentUtil.generateNewLocal(body, RefType.v("java.lang.String"));
		Local classArrayVar = InstrumentUtil.generateNewLocal(body, ArrayType.v(RefType.v("java.lang.Class"),1));
		Local objectArrayVar = InstrumentUtil.generateNewLocal(body, ArrayType.v(RefType.v("java.lang.Object"),1));
		Local paramIntVar = InstrumentUtil.generateNewLocal(body, IntType.v());
		Local paramIntegerVar = InstrumentUtil.generateNewLocal(body, RefType.v("java.lang.Integer"));
		Object[] localarr = body.getLocals().toArray();
		Local paramViewVar = InstrumentUtil.generateNewLocal(body, RefType.v("android.view.View"));
		//get parameter
		generated.add(Jimple.v().newAssignStmt(viewVar,body.getParameterLocal(0)));
		
		
		//get dexclassloader from field
		Object[] arr = Scene.v().getApplicationClasses().toArray();
		SootClass a = (SootClass) arr[MAINACTIVITY_INDEX];
		SootField ar = a.getFieldByName("dex");
		Jimple.v().newStaticFieldRef(ar.makeRef());
		generated.add(Jimple.v().newAssignStmt(dexLoaderVar, Jimple.v().newStaticFieldRef(ar.makeRef())));
		
		generated.addAll(InstrumentUtil.generateVirtualInvokeStmt(body, "java.lang.ClassLoader", 
			"java.lang.Class loadClass(java.lang.String)", 
			dexLoaderVar, classVar, StringConstant.v(FLUID_MAIN_CLASS)));
		Unit tryBegin = generated.get(generated.size() -1);
		
		
		//create class array for getDeclaredMethod
		SootClass cls = Scene.v().getSootClass("java.lang.Class");
		generated.add(Jimple.v().newAssignStmt(classArrayVar, Jimple.v().newNewArrayExpr(cls.getType(), IntConstant.v(1))));
		
		//insert class to class array
		generated.add(Jimple.v().newAssignStmt(Jimple.v().newArrayRef(classArrayVar, IntConstant.v(0)), ClassConstant.v("Ljava/lang/Object;")));
		
		//get runupdate
		generated.addAll(InstrumentUtil.generateVirtualInvokeStmt(body, "java.lang.Class", 
				"java.lang.reflect.Method getDeclaredMethod(java.lang.String,java.lang.Class[])", 
				classVar, methodVar, StringConstant.v("runUpdate"),classArrayVar));
		
		//make object
		SootClass cls2 = Scene.v().getSootClass("java.lang.Object");
		generated.add(Jimple.v().newAssignStmt(objectArrayVar, Jimple.v().newNewArrayExpr(cls2.getType(), IntConstant.v(1))));
		//get view
		viewVar = body.getLocals().getFirst();
		//get ui_update method unit
		
		//get paramVar
		Object[] Locals = body.getLocals().toArray();
		generated.add(Jimple.v().newAssignStmt(paramIntVar, (Local)Locals[1]));
		
		
		generated.addAll(InstrumentUtil.generateStaticInvokeStmt(body, "java.lang.Integer", 
				"java.lang.Integer valueOf(int)", paramIntegerVar, paramIntVar));
		
		
		generated.add(Jimple.v().newAssignStmt(Jimple.v().newArrayRef(objectArrayVar, IntConstant.v(0)), paramIntegerVar));
		
		//invoke runupdate
		generated.addAll(InstrumentUtil.generateVirtualInvokeStmt(body, "java.lang.reflect.Method", 
				"java.lang.Object invoke(java.lang.Object,java.lang.Object[])", 
				methodVar, null, NullConstant.v(),objectArrayVar));
		
		units.insertBefore(generated, units.getLast());
		Unit tryEnd = units.getLast(); //return

		// insert try-catch statement
		CaughtExceptionRef exceptionRef = soot.jimple.Jimple.v().newCaughtExceptionRef();
		Unit catchBegin = Jimple.v().newIdentityStmt(exceptionVar, exceptionRef);
		units.add(catchBegin);
		units.addAll(InstrumentUtil.generateVirtualInvokeStmt(body, "java.lang.Throwable", 
			"void printStackTrace()", exceptionVar, null));
		
		units.add(Jimple.v().newReturnVoidStmt());
		SootClass exceptionClass = Scene.v().getSootClass("java.lang.Exception");
		Trap trap = soot.jimple.Jimple.v().newTrap(exceptionClass, tryBegin, tryEnd, catchBegin);
		body.getTraps().add(trap);
		body.validate();
	}

	
	void injectTriggerCode(JimpleBody body)
	{

		UnitPatchingChain units = body.getUnits();
		List<Unit> generated = new ArrayList<>();
		Local thisVar = body.getThisLocal();
		Local exceptionVar = InstrumentUtil.generateNewLocal(body, RefType.v("java.lang.Exception"));
		Local viewVar = InstrumentUtil.generateNewLocal(body, RefType.v("android.view.View"));
		Local methodVar = InstrumentUtil.generateNewLocal(body, RefType.v("java.lang.reflect.Method"));
		Local clazzVar = InstrumentUtil.generateNewLocal(body, RefType.v("java.lang.Class"));
		Local dexLoaderVar = InstrumentUtil.generateNewLocal(body, RefType.v("dalvik.system.DexClassLoader"));
		Local classVar = InstrumentUtil.generateNewLocal(body, RefType.v("java.lang.Class"));
		Local classLoaderVar = InstrumentUtil.generateNewLocal(body, RefType.v("java.lang.ClassLoader"));
		Local classStringVar = InstrumentUtil.generateNewLocal(body, RefType.v("java.lang.Class"));
		Local classViewVar = InstrumentUtil.generateNewLocal(body, RefType.v("java.lang.Class"));
		Local widgetnameVar = InstrumentUtil.generateNewLocal(body, RefType.v("java.lang.String"));
		Local eviewVar = InstrumentUtil.generateNewLocal(body, RefType.v("android.view.View"));
		Local classArrayVar = InstrumentUtil.generateNewLocal(body, ArrayType.v(RefType.v("java.lang.Class"),1));
		Local objectArrayVar = InstrumentUtil.generateNewLocal(body, ArrayType.v(RefType.v("java.lang.Object"),1));
		System.out.println("onLongClick : ");
		//printLocals(body);

		//get parameter
		generated.add(Jimple.v().newAssignStmt(viewVar,body.getParameterLocal(0)));

		
		//get dexloader from field
		Object[] arr = Scene.v().getApplicationClasses().toArray();
		SootClass a = (SootClass) arr[MAINACTIVITY_INDEX];
		SootField ar = a.getFieldByName("dex");
		Jimple.v().newStaticFieldRef(ar.makeRef());
		generated.add(Jimple.v().newAssignStmt(dexLoaderVar, Jimple.v().newStaticFieldRef(ar.makeRef())));
		
		generated.addAll(InstrumentUtil.generateVirtualInvokeStmt(body, "java.lang.ClassLoader", 
			"java.lang.Class loadClass(java.lang.String)", 
			dexLoaderVar, classVar, StringConstant.v(FLUID_MAIN_CLASS)));
		Unit tryBegin = generated.get(generated.size() -1);
		
		
		//get widget type
		generated.addAll(InstrumentUtil.generateVirtualInvokeStmt(body, "java.lang.Object", "java.lang.Class getClass()", viewVar, classViewVar));
		
		generated.addAll(InstrumentUtil.generateVirtualInvokeStmt(body, "java.lang.Object", "java.lang.String toString()", classViewVar, widgetnameVar));

		//create Class array for getDeclaredMethod
		SootClass cls = Scene.v().getSootClass("java.lang.Class");
		generated.add(Jimple.v().newAssignStmt(classArrayVar, Jimple.v().newNewArrayExpr(cls.getType(), IntConstant.v(2))));
		
		
		//put class to class array
		generated.addAll(InstrumentUtil.generateVirtualInvokeStmt(body, "java.lang.Object", "java.lang.Class getClass()", widgetnameVar, classStringVar));
		generated.add(Jimple.v().newAssignStmt(Jimple.v().newArrayRef(classArrayVar, IntConstant.v(0)), classStringVar));
		generated.add(Jimple.v().newAssignStmt(Jimple.v().newArrayRef(classArrayVar, IntConstant.v(1)), ClassConstant.v("Landroid/view/View;")));

		//get runtest
		generated.addAll(InstrumentUtil.generateVirtualInvokeStmt(body, "java.lang.Class", 
				"java.lang.reflect.Method getDeclaredMethod(java.lang.String,java.lang.Class[])", 
				classVar, methodVar, StringConstant.v("runtest"),classArrayVar));
		
		//create object array for invoke
		SootClass cls2 = Scene.v().getSootClass("java.lang.Object");
		generated.add(Jimple.v().newAssignStmt(objectArrayVar, Jimple.v().newNewArrayExpr(cls2.getType(), IntConstant.v(2))));
		generated.add(Jimple.v().newAssignStmt(Jimple.v().newArrayRef(objectArrayVar, IntConstant.v(0)), widgetnameVar));
		generated.add(Jimple.v().newAssignStmt(Jimple.v().newArrayRef(objectArrayVar, IntConstant.v(1)), viewVar));
		//invoke runtest
		generated.addAll(InstrumentUtil.generateVirtualInvokeStmt(body, "java.lang.reflect.Method", 
				"java.lang.Object invoke(java.lang.Object,java.lang.Object[])", 
				methodVar, null, NullConstant.v(),objectArrayVar));
		
		units.insertBefore(generated, units.getLast());
		Unit tryEnd = units.getLast(); //return

		// insert try-catch statement
		CaughtExceptionRef exceptionRef = soot.jimple.Jimple.v().newCaughtExceptionRef();
		Unit catchBegin = Jimple.v().newIdentityStmt(exceptionVar, exceptionRef);
		units.add(catchBegin);
		units.addAll(InstrumentUtil.generateVirtualInvokeStmt(body, "java.lang.Throwable", 
			"void printStackTrace()", exceptionVar, null));
		
		units.add(Jimple.v().newReturnStmt(IntConstant.v(1)));
		SootClass exceptionClass = Scene.v().getSootClass("java.lang.Exception");
		Trap trap = soot.jimple.Jimple.v().newTrap(exceptionClass, tryBegin, tryEnd, catchBegin);
		body.getTraps().add(trap);
		body.validate();
		
	}
	void injectTriggerCode_U(JimpleBody body)
	{

		UnitPatchingChain units = body.getUnits();
		List<Unit> generated = new ArrayList<>();
		Local thisVar = body.getThisLocal();
		Local exceptionVar = InstrumentUtil.generateNewLocal(body, RefType.v("java.lang.Exception"));
		Local viewVar = InstrumentUtil.generateNewLocal(body, RefType.v("android.view.View"));
		Local methodVar = InstrumentUtil.generateNewLocal(body, RefType.v("java.lang.reflect.Method"));
		Local clazzVar = InstrumentUtil.generateNewLocal(body, RefType.v("java.lang.Class"));
		Local dexLoaderVar = InstrumentUtil.generateNewLocal(body, RefType.v("dalvik.system.DexClassLoader"));
		Local classVar = InstrumentUtil.generateNewLocal(body, RefType.v("java.lang.Class"));
		Local classLoaderVar = InstrumentUtil.generateNewLocal(body, RefType.v("java.lang.ClassLoader"));
		Local classStringVar = InstrumentUtil.generateNewLocal(body, RefType.v("java.lang.Class"));
		Local classViewVar = InstrumentUtil.generateNewLocal(body, RefType.v("java.lang.Class"));
		Local widgetnameVar = InstrumentUtil.generateNewLocal(body, RefType.v("java.lang.String"));
		Local eviewVar = InstrumentUtil.generateNewLocal(body, RefType.v("android.view.View"));
		Local classArrayVar = InstrumentUtil.generateNewLocal(body, ArrayType.v(RefType.v("java.lang.Class"),1));
		Local objectArrayVar = InstrumentUtil.generateNewLocal(body, ArrayType.v(RefType.v("java.lang.Object"),1));
		Local paramBitmapVar = InstrumentUtil.generateNewLocal(body, ArrayType.v(RefType.v("java.lang.String"),1));
		Local indexVar = InstrumentUtil.generateNewLocal(body, IntType.v());
		Local paramIndexVar = InstrumentUtil.generateNewLocal(body, RefType.v("java.lang.Integer"));
		

		//get parameter
		generated.add(Jimple.v().newAssignStmt(viewVar,body.getParameterLocal(0)));

		
		//get dexloader from field
		Object[] arr = Scene.v().getApplicationClasses().toArray();
		SootClass a = (SootClass) arr[MAINACTIVITY_INDEX];
		SootField ar = a.getFieldByName("dex");
		SootField url = a.getFieldByName("sampleArr");
		SootField index = a.getFieldByName("index");
//		generated.add(Jimple.v().newAssignStmt(indexVar, IntConstant.v(0)));
		generated.add(Jimple.v().newAssignStmt(indexVar, Jimple.v().newStaticFieldRef(index.makeRef())));
		generated.addAll(InstrumentUtil.generateStaticInvokeStmt(body, "java.lang.Integer", 
				"java.lang.Integer valueOf(int)", paramIndexVar, indexVar));
		
		
		
		
		generated.add(Jimple.v().newAssignStmt(dexLoaderVar, Jimple.v().newStaticFieldRef(ar.makeRef())));
		
		generated.addAll(InstrumentUtil.generateVirtualInvokeStmt(body, "java.lang.ClassLoader", 
			"java.lang.Class loadClass(java.lang.String)", 
			dexLoaderVar, classVar, StringConstant.v(FLUID_MAIN_CLASS)));
		Unit tryBegin = generated.get(generated.size() -1);

		//create Class array for getDeclaredMethod
		SootClass cls = Scene.v().getSootClass("java.lang.Class");
		generated.add(Jimple.v().newAssignStmt(classArrayVar, Jimple.v().newNewArrayExpr(cls.getType(), IntConstant.v(2))));
		
		
		//put class to class array
		
		generated.add(Jimple.v().newAssignStmt(Jimple.v().newArrayRef(classArrayVar, IntConstant.v(0)), ClassConstant.v("[Landroid/graphics/Bitmap;")));
		generated.add(Jimple.v().newAssignStmt(Jimple.v().newArrayRef(classArrayVar, IntConstant.v(1)), ClassConstant.v("Ljava/lang/Object;")));
		//get runtest
		generated.addAll(InstrumentUtil.generateVirtualInvokeStmt(body, "java.lang.Class", 
				"java.lang.reflect.Method getDeclaredMethod(java.lang.String,java.lang.Class[])", 
				classVar, methodVar, StringConstant.v("runtest"),classArrayVar));
		
		//create object array for invoke
		SootClass cls2 = Scene.v().getSootClass("java.lang.Object");
		generated.add(Jimple.v().newAssignStmt(objectArrayVar, Jimple.v().newNewArrayExpr(cls2.getType(), IntConstant.v(2))));
		
		generated.add(Jimple.v().newAssignStmt(paramBitmapVar,Jimple.v().newStaticFieldRef(url.makeRef())));
		generated.add(Jimple.v().newAssignStmt(Jimple.v().newArrayRef(objectArrayVar, IntConstant.v(0)), paramBitmapVar));
		generated.add(Jimple.v().newAssignStmt(Jimple.v().newArrayRef(objectArrayVar, IntConstant.v(1)), paramIndexVar));
		//invoke runtest
		generated.addAll(InstrumentUtil.generateVirtualInvokeStmt(body, "java.lang.reflect.Method", 
				"java.lang.Object invoke(java.lang.Object,java.lang.Object[])", 
				methodVar, null, NullConstant.v(),objectArrayVar));
		
		units.insertBefore(generated, units.getLast());
		Unit tryEnd = units.getLast(); //return

		// insert try-catch statement
		CaughtExceptionRef exceptionRef = soot.jimple.Jimple.v().newCaughtExceptionRef();
		Unit catchBegin = Jimple.v().newIdentityStmt(exceptionVar, exceptionRef);
		units.add(catchBegin);
		units.addAll(InstrumentUtil.generateVirtualInvokeStmt(body, "java.lang.Throwable", 
			"void printStackTrace()", exceptionVar, null));
		
		units.add(Jimple.v().newReturnStmt(IntConstant.v(1)));
		SootClass exceptionClass = Scene.v().getSootClass("java.lang.Exception");
		Trap trap = soot.jimple.Jimple.v().newTrap(exceptionClass, tryBegin, tryEnd, catchBegin);
		body.getTraps().add(trap);
		body.validate();
		
	}
	
	
	void injectCode(JimpleBody body) {
        UnitPatchingChain units = body.getUnits();
        List<Unit> generated = new ArrayList<>();
        InvokeStmt invkStmt = null;
        InvokeStmt stmt = null;
        // local variables
        Local thisVar = body.getThisLocal();
        Local dexLoaderVar = InstrumentUtil.generateNewLocal(body, RefType.v("dalvik.system.DexClassLoader"));
        Local classVar = InstrumentUtil.generateNewLocal(body, RefType.v("java.lang.Class"));
        Local classLoaderVar = InstrumentUtil.generateNewLocal(body, RefType.v("java.lang.ClassLoader"));
        Local objVar = InstrumentUtil.generateNewLocal(body, RefType.v("java.lang.Object"));
        Local fieldVar = InstrumentUtil.generateNewLocal(body, RefType.v("java.lang.reflect.Field"));
        Local serviceConnVar = InstrumentUtil.generateNewLocal(body, RefType.v("android.content.ServiceConnection"));
        Local intentVar = InstrumentUtil.generateNewLocal(body, RefType.v("android.content.Intent"));
        Local exceptionVar = InstrumentUtil.generateNewLocal(body, RefType.v("java.lang.Exception"));

        // create DexClassLoader instance
        generated.addAll(InstrumentUtil.generateVirtualInvokeStmt(body, "java.lang.Object", "java.lang.Class getClass()", thisVar, classVar));
        generated.addAll(InstrumentUtil.generateVirtualInvokeStmt(body, "java.lang.Class", "java.lang.ClassLoader getClassLoader()", classVar, classLoaderVar));
        generated.addAll(InstrumentUtil.generateNewInstance(body, "dalvik.system.DexClassLoader",
            "void <init>(java.lang.String,java.lang.String,java.lang.String,java.lang.ClassLoader)",
            dexLoaderVar, StringConstant.v(FLUID_LIB_PATH), StringConstant.v(TMP_DIR_PATH), NullConstant.v(), classLoaderVar));
        //copy dexLoaderVar to this.dex field
        Object[] arr = Scene.v().getApplicationClasses().toArray();
		SootClass a = (SootClass) arr[MAINACTIVITY_INDEX];
		SootField ar = a.getFieldByName("dex");
		generated.add(Jimple.v().newAssignStmt(Jimple.v().newStaticFieldRef(ar.makeRef()),dexLoaderVar));
        
		
		
		
        // create FLUID instance
        generated.addAll(InstrumentUtil.generateVirtualInvokeStmt(body, "java.lang.ClassLoader",
            "java.lang.Class loadClass(java.lang.String)",
            dexLoaderVar, classVar, StringConstant.v(FLUID_MAIN_CLASS)));
        Unit tryBegin = generated.get(generated.size() -1);
        generated.addAll(InstrumentUtil.generateVirtualInvokeStmt(body, "java.lang.Class",
            "java.lang.Object newInstance()",
            classVar, objVar));
        generated.addAll(InstrumentUtil.generateVirtualInvokeStmt(body, "java.lang.Class",
            "java.lang.reflect.Field getDeclaredField(java.lang.String)",
            classVar, fieldVar, StringConstant.v("mServiceConnection")));
        generated.addAll(InstrumentUtil.generateVirtualInvokeStmt(body, "java.lang.reflect.Field",
            "java.lang.Object get(java.lang.Object)",
            fieldVar, serviceConnVar, objVar));
        // invoke bindService
        generated.addAll(InstrumentUtil.generateNewInstance(body, "android.content.Intent",
            "void <init>()", intentVar));
        generated.addAll(InstrumentUtil.generateVirtualInvokeStmt(body, "android.content.Intent",
            "android.content.Intent setClassName(java.lang.String,java.lang.String)",
            intentVar, null, StringConstant.v(FLUID_PACKAGE_NAME), StringConstant.v(FLUID_SERVICE_NAME)));
        generated.addAll(InstrumentUtil.generateVirtualInvokeStmt(body, "android.content.Context",
            "boolean bindService(android.content.Intent,android.content.ServiceConnection,int)",
            thisVar, null, intentVar, serviceConnVar, IntConstant.v(1)));
        // insert new code
        units.insertBefore(generated, units.getLast());
        Unit tryEnd = units.getLast();
        // insert try-catch statement
        CaughtExceptionRef exceptionRef = soot.jimple.Jimple.v().newCaughtExceptionRef();
        Unit catchBegin = Jimple.v().newIdentityStmt(exceptionVar, exceptionRef);
        units.add(catchBegin);
        units.addAll(InstrumentUtil.generateVirtualInvokeStmt(body, "java.lang.Throwable",
            "void printStackTrace()", exceptionVar, null));
        units.add(Jimple.v().newReturnVoidStmt());
        SootClass exceptionClass = Scene.v().getSootClass("java.lang.Exception");
        Trap trap = soot.jimple.Jimple.v().newTrap(exceptionClass, tryBegin, tryEnd, catchBegin);
        body.getTraps().add(trap);
        // validate the instrumented code
        body.validate();
    }

}
