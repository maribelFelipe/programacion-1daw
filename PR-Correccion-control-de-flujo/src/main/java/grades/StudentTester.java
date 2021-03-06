package grades;
import grades.ExecutorTester.ExecutorTesterResult;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;





public abstract class StudentTester  implements Callable<Map<String,StudentTester.Result>>{

	public static class Result{
		public int bad() {
			return _bad;
		}

		public int good() {
			return _good;
		}

		public int _bad;
		public int _good;
		private String _name;
		private ExecutorTesterResult[] _results;
		private String _student;


		public Result( String student, String name, int allBad ){
			_name = name;
			_student = student;
			_good = 0;
			_bad = allBad;
		}
		
		public Result( String student, String name, ExecutorTesterResult[] results ){
			_name = name;
			_student = student;
			_results = results;
			
			for( ExecutorTesterResult etr : _results ){
				if( etr.successfull() ){
					_good += 1;
				}
				else{
					_bad += 1;
				}
			}
		}
		
		@Override
		public String toString() {
			return name() + "  good:" + good() + "  bad:" + bad();
		}
		
		public String name(){
			return _name;
		}
		
		public void dumpResults(Writer osw) throws IOException{
			for( ExecutorTesterResult etr: _results ){
				etr.dumpResult(osw);
				notifyConsole(etr);
			}
		}

		private void notifyConsole(ExecutorTesterResult etr) {
			System.err.println( _student + ":" + _name + ": " + etr.successfull() );
		}

	}

	private String[] _students;

	public StudentTester(String ... students) {
		_students = students;
	}

	protected abstract String classNameToExecute();

	protected String[] executeCommand(String student) {
		return new String[]{
				"/usr/bin/java",
				"-cp", ".",
				classNameToExecute()
		};
	}

	protected static File studentDir(String student) {
		return new File("./students/" + student);
	}


	@Override
	public Map<String, Result> call() throws Exception {
		
		Map<String,Result> ret = new HashMap<String,Result>();
		
		for( String s: _students ){
			Result result = testStudent(s);
			ret.put( s, result );
		}
		
		return ret;
	}

	protected Compiler compile(String student) throws Exception {
		return new Compiler( studentDir(student), classNameToExecute() + ".java" );
	}

	public Result testStudent(String student) throws Exception {
	
		ExecutorTester.TestDefinition[] testData = generateTestData();

		FileOutputStream fos = new FileOutputStream( new File( studentDir(student), classNameToExecute() + "-results.txt") );
		OutputStreamWriter osw = new OutputStreamWriter(fos);

		
		Compiler c = compile(student);
		if( !c.compile() ){
			c.getCompileResult().dumpResult(osw);
			osw.close();

			return new Result( student, classNameToExecute(), testData.length);
		}
	
		List<ExecutorTesterResult> etr = new ArrayList<ExecutorTesterResult>();
		for( ExecutorTester.TestDefinition data: testData ){
			ExecutorTester et = new ExecutorTester(studentDir(student), executeCommand(student), data );
			ExecutorTesterResult result = et.call();
			etr.add( result );
		}

		Result result = new Result(student, classNameToExecute(), etr.toArray( new ExecutorTesterResult[0]) );

		c.getCompileResult().dumpResult(osw);
		result.dumpResults(osw);

		osw.close();
		
		return result;
	}

	abstract protected ExecutorTester.TestDefinition[] generateTestData();

}
