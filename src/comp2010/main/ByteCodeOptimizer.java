package comp2010.main;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.Code;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.GOTO;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.InstructionTargeter;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.TargetLostException;

public class ByteCodeOptimizer
{
	ClassParser parser = null;
	ClassGen gen = null;

	JavaClass original = null;
	JavaClass optimized = null;

	public ByteCodeOptimizer(String classFilePath)
	{
		try{
			this.parser = new ClassParser(classFilePath);
			this.original = this.parser.parse();
			this.gen = new ClassGen(this.original);
		} catch(IOException e){
			e.printStackTrace();
		}
	}
	
	private void resolveGoTo(InstructionHandle currentInstructionHandle, InstructionHandle originalInstructionHandle, ArrayList<InstructionHandle> flagged)
	{
		Instruction currentInstruction = currentInstructionHandle.getInstruction();
		if(currentInstruction instanceof GOTO)
		{
			if(!flagged.contains(currentInstructionHandle))
				flagged.add(currentInstructionHandle);
			resolveGoTo(((GOTO) currentInstruction).getTarget(), originalInstructionHandle, flagged);
		}
		else
		{
			Instruction newTarget = new GOTO(currentInstructionHandle);
			originalInstructionHandle.setInstruction(newTarget);
			return ;
		}
	}
	
	private void optimizeMethod(ClassGen gen, ConstantPoolGen cpgen, Method method)
	{
		Code methodCode = method.getCode();
		InstructionList instList = new InstructionList(methodCode.getCode());
		
		MethodGen methodGen = new MethodGen(method.getAccessFlags(), method.getReturnType(), method.getArgumentTypes(), null, method.getName(), gen.getClassName(), instList, cpgen);
		/*
		ArrayList<InstructionHandle> flagged = new ArrayList<InstructionHandle>();
		
		for(InstructionHandle handle: instList.getInstructionHandles())
		{
			Instruction ins = handle.getInstruction();
			if(ins instanceof GOTO)
			
				resolveGoTo(((GOTO) ins).getTarget(), handle, flagged);
		}
		
		

		for(int k=0; k<flagged.size();++k)
		{
			try {
				instList.delete(flagged.get(k));
			} catch (TargetLostException e) {
				InstructionHandle[] targets = e.getTargets();
			    for(int i=0; i < targets.length; i++) {
			        InstructionTargeter[] targeters = targets[i].getTargeters();
			        for(int j=0; j < targeters.length; j++)
			           targeters[j].updateTarget(targets[i], flagged.get(k).getNext());
			           
			        
		 }
		}
		
		*/
		
		instList.setPositions(true);

		
		methodGen.setMaxStack();
		methodGen.setMaxLocals();

		
		Method newMethod = methodGen.getMethod();
		gen.replaceMethod(method, newMethod);
	
		
		
	}
	
	private void optimize()
	{
		ClassGen gen = new ClassGen(original);
		ConstantPoolGen cpgen = gen.getConstantPool();
		Method[] methods = gen.getMethods();
		
		for (Method m: methods)
		{
			//optimizeMethod(gen, cpgen, m);
		}
		
		
		
		
		// Do your optimization here
		this.optimized = gen.getJavaClass();
	}
	
	public void write(String optimisedFilePath)
	{
		this.optimize();

		try {
			FileOutputStream out = new FileOutputStream(new File(optimisedFilePath));
			this.optimized.dump(out);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main(String args[])
	{
		//String input = "/home/kapil/Documents/Java/workspace/Kapiler_Part_2/jars/Example.class";
		//String output = "/home/kapil/Documents/Java/workspace/Kapiler_Part_2/jars/output/Example.class";
		ByteCodeOptimizer optimizer = new ByteCodeOptimizer(args[0]);
		optimizer.write(args[1]);

	}
}
