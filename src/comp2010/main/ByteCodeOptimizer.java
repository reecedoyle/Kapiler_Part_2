package comp2010.main;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

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
import org.apache.bcel.generic.MethodGen;

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
	
	private void optimizeGoTo(ClassGen cgen, ConstantPoolGen cpgen, Method m)
	{
		Code code = m.getCode();

		InstructionList instructions = new InstructionList(code.getCode());

		MethodGen methodGen = new MethodGen(m.getAccessFlags(), m.getReturnType(), m.getArgumentTypes(), null, m.getName(), cgen.getClassName(), instructions, cpgen);

		for(InstructionHandle handle: instructions.getInstructionHandles())
		{
			Instruction instruction = handle.getInstruction();
			if(instruction instanceof GOTO)
			{
				int go2s = checkGoTo(handle, handle);
				System.out.println("number of go2s = " +go2s);  // debugging purposes and for line removal
				
			}

		}

		instructions.setPositions(true);

		methodGen.setMaxStack();
		methodGen.setMaxLocals();

		Method newMethod = methodGen.getMethod();

		cgen.replaceMethod(m, newMethod);



	}
	
	
	private int checkGoTo(InstructionHandle currentInstructionHandle, InstructionHandle originalHandle)
	{
		
			Instruction currentInstruction = currentInstructionHandle.getInstruction();
			if(currentInstruction instanceof GOTO)
			{
				
				return 1 + checkGoTo(((GOTO) currentInstruction).getTarget(), originalHandle);
			}
			else
			{
				Instruction newTarget = new GOTO(currentInstructionHandle);
				originalHandle.setInstruction(newTarget);
				return 0;
			}
		
		
		
	}
	
	/*
	private int checkTrace(InstructionHandle instruction, Instruction originalInstruction)
	{
		if(instruction instanceof GOTO)
		{
			return 1 + checktrace(instruction.getTarget(), originalInstruction);
		}
		else{
			originalInstruction.set(new InstructionHandle(instruction));
			return 0;
		}
	}
	*/

	private void optimize()
	{
		ClassGen gen = new ClassGen(original);
		ConstantPoolGen cpgen = gen.getConstantPool();

		// Do your optimization here
		Method[] methods = gen.getMethods();
		
		for (Method method: methods)
		{
			optimizeGoTo(gen, cpgen, method);
		}	



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
		ByteCodeOptimizer optimizer = new ByteCodeOptimizer(args[0]);
		optimizer.write(args[1]);

	}
}
