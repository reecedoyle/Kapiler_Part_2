package comp2010.main;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.generic.ClassGen;

public class ByteCodeOptimizer{
	
	ClassParser parser = null;
	ClassGen gen = null;

	JavaClass original = null;
	JavaClass optimized = null;

	public ByteCodeOptimizer(String classFilePath){

		try{
			this.parser = new ClassParser(classFilePath);
			this.original = this.parser.parse();
			this.gen = new ClassGen(this.original);
		} catch(IOException e){
			e.printStackTrace();
		}
	}

	private void optimiseMethod(ClassGen cgen, ConstantPoolGen cpgen, Method m){

		Code code = m.getCode();

		InstructionList instructions = new InstructionList(code.getCode());

		MethodGen methodGen = new MethodGen(m.getAccessFlags(), m.getReturnType(), m.getAargumentTypes(), null, cgen.getClassName(), instructions, cpgen);

		for(InstructionHandle handle: instructions.getInstructionHandles()){

			Instruction instruction = handle.getInstruction();
			if(instruction instanceof GOTO){
				int gotos = checkGotos(instruction.getTarget(), instruction);
			}

		}

		instructions.setPositions(true);

		methodGen.setMaxStack();
		methodGen.setMaxLocals();

		Method newMethod = methodGen.getMethod();

		cgen.replaceMethod(m, newMethod);



	}
	

	private int checkGotos(Instruction instruction, Instruction originalInstruction){

		if(instruction.target instanceof GOTO){
			return 1 + checkGotos(instruction.getTarget(), originalInstruction);
		}
		else{
			originalInstruction.set(new InstructionHandle(instruction));
			return 0;
		}
	}


	private void optimize(){

		ClassGen gen = new ClassGen(original);
		ConstantPoolGen cpgen = new cpgen.getConstantPool();

		// Do your optimization here
		Method[] methods = gen.getMethods();
		
		for (Method method: methods)
		{
			optimiseMethod(gen, cpgen, method);
		}	

		this.optimized = gen.getJavaClass();
	}
	
	public void write(String optimisedFilePath){

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
	
	public static void main(String args[]){

		ByteCodeOptimizer optimizer = new ByteCodeOptimizer(args[0]);
		optimizer.write(args[1]);

	}
}
