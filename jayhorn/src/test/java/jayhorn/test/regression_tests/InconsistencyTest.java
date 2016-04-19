/**
 * 
 */
package jayhorn.test.regression_tests;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import jayhorn.old_inconsistency_check.InconsistencyChecker;
import jayhorn.solver.ProverFactory;
import jayhorn.solver.princess.PrincessProverFactory;
import jayhorn.solver.spacer.SpacerProverFactory;
import jayhorn.test.Util;
import soottocfg.cfg.method.CfgBlock;
import soottocfg.soot.SootToCfg;
import soottocfg.soot.SootToCfg.MemModel;

/**
 * @author schaef
 *
 */
@RunWith(Parameterized.class)
public class InconsistencyTest {

	private static final String userDir = System.getProperty("user.dir") + "/";
	private static final String testRoot = userDir + "src/test/resources/";

	private File sourceFile;

	@Parameterized.Parameters(name = "{index}: check ({1})")
	public static Collection<Object[]> data() {
		List<Object[]> filenames = new LinkedList<Object[]>();
		final File source_dir = new File(testRoot + "inconsistencies/");
		File[] directoryListing = source_dir.listFiles();
		if (directoryListing != null) {
			for (File child : directoryListing) {
				if (child.isFile() && child.getName().endsWith(".java")) {
					filenames.add(new Object[] { child, child.getName() });
				} else {
					// Ignore
				}
			}
		} else {
			// Handle the case where dir is not really a directory.
			// Checking dir.isDirectory() above would not be sufficient
			// to avoid race conditions with another process that deletes
			// directories.
			System.err.println("Test data in " + userDir + " not found");
			throw new RuntimeException("Test data not found!");
		}
		return filenames;
	}

	public InconsistencyTest(File source, String name) {
		this.sourceFile = source;
	}

//	private final ProverFactory factory = new PrincessProverFactory();
//	private final ProverFactory factory = new Z3ProverFactory();

	@Test
	public void testOldAlgorithmWithPrincess() {
		oldAlgorithm(new PrincessProverFactory());
	}

//	@Test
//	public void testOldAlgorithmWithZ3() {
//		oldAlgorithm(new SpacerProverFactory());
//	}

	
	protected void oldAlgorithm(ProverFactory factory) {
		System.out.println("\nRunning test " + this.sourceFile.getName() + " with "+factory.getClass()+"\n");
		File classDir = null;
		try {
			classDir = Util.compileJavaFile(this.sourceFile);			
			SootToCfg soot2cfg = new SootToCfg(false, true, MemModel.BurstallBornat);
			soot2cfg.run(classDir.getAbsolutePath(), null);
			InconsistencyChecker checker = new InconsistencyChecker(factory);
			checker.setDuplicatedSourceLocations(soot2cfg.getDuplicatedSourceLocations());
			checker.checkProgram(soot2cfg.getProgram());
			Map<String, Set<CfgBlock>> result = checker.getInconsistentBlocksPerMethod();
			
			int check;
			String methodName;
			int goal;
			
			check = 0; goal = 2;
			methodName = "<inconsistencies.TruePositives01: int infeasible1(java.lang.Object)>";
			for (CfgBlock b : result.get(methodName)) {
				if ("Block4".equals(b.getLabel())) check++;
				if ("Block2".equals(b.getLabel())) check++;
			}
			Assert.assertTrue("For "+methodName+": should be "+goal+" but is " + check, check==goal);
			
			check = 0; goal = 2;
			methodName = "<inconsistencies.TruePositives01: int infeasible0(int[])>";
			for (CfgBlock b :  result.get(methodName)) {
				if ("Block0".equals(b.getLabel())) check++;
				if ("Block1".equals(b.getLabel())) check++;				
			}
			Assert.assertTrue("For "+methodName+": should be "+goal+" but is " + check, check==goal);

			check = 0; goal = 2;
			methodName = "<inconsistencies.TruePositives01: boolean stringCompare()>";
			for (CfgBlock b :  result.get(methodName)) {
				if ("Block5".equals(b.getLabel())) check++;
				if ("Block3".equals(b.getLabel())) check++;				
			}			
			Assert.assertTrue("For "+methodName+": should be "+goal+" but is " + check, check==goal);

			check = 0; goal = 3;
			methodName = "<inconsistencies.TruePositives01: void infeasible2(int[])>";
			for (CfgBlock b :  result.get(methodName)) {
				if ("Block4".equals(b.getLabel())) check++;
				if ("Block6".equals(b.getLabel())) check++;
				if ("Block7".equals(b.getLabel())) check++;
			}			
			Assert.assertTrue("For "+methodName+": should be "+goal+" but is " + check, check==goal);

			check = 0; goal = 2;
			methodName = "<inconsistencies.TruePositives01: boolean infeasible4(java.lang.Object)>";
			for (CfgBlock b :  result.get(methodName)) {
				if ("Block3".equals(b.getLabel())) check++;
				if ("Block5".equals(b.getLabel())) check++;
			}			
			Assert.assertTrue("For "+methodName+": should be "+goal+" but is " + check, check==goal);

			check = 0; goal = 1;
			methodName = "<inconsistencies.TruePositives01: int infeasible6(int[])>";
			for (CfgBlock b :  result.get(methodName)) {
				if ("Block0".equals(b.getLabel())) check++;
			}			
			Assert.assertTrue("For "+methodName+": should be "+goal+" but is " + check, check==goal);

			
		} catch (IOException e) {
			e.printStackTrace();
			Assert.fail();
		} finally {
			if (classDir!=null) {
				classDir.deleteOnExit();
			}
		}	
	}
		
}
