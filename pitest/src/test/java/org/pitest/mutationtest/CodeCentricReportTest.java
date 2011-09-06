/*
 * Copyright 2010 Henry Coles
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package org.pitest.mutationtest;

import static org.pitest.mutationtest.results.DetectionStatus.KILLED;
import static org.pitest.mutationtest.results.DetectionStatus.SURVIVED;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;
import org.pitest.help.PitHelpError;
import org.pitest.mutationtest.engine.gregor.MethodMutatorFactory;
import org.pitest.mutationtest.instrument.JarCreatingJarFinder;
import org.pitest.util.JavaAgent;

import com.example.FailsTestWhenEnvVariableSetTestee;
import com.example.FullyCoveredTestee;
import com.example.FullyCoveredTesteeTest;
import com.example.MultipleMutations;

public class CodeCentricReportTest extends ReportTestBase {

  @Test
  public void shouldPickRelevantTestsAndKillMutationsBasedOnCoverageData() {
    this.data.setTargetClasses(predicateFor("com.example.FullyCovered*"));
    this.data.setVerbose(true);
    createAndRun();
    verifyResults(KILLED);
  }

  @Test
  public void shouldPickRelevantTestsAndKillMutationsBasedOnCoverageDataWhenLimitedByClassReach() {
    this.data.setDependencyAnalysisMaxDistance(2);
    this.data.setTargetTests(predicateFor("com.example.*FullyCovered*"));
    this.data.setTargetClasses(predicateFor("com.example.FullyCovered*"));
    createAndRun();
    verifyResults(KILLED);
  }

  @Test
  public void shouldReportSurvivingMutations() {
    this.data.setTargetClasses(predicateFor("com.example.PartiallyCovered*"));
    createAndRun();
    verifyResults(KILLED, SURVIVED);
  }

  @Test
  public void shouldKillMutationsInStaticInitializersWhenThereIsCoverageAndMutateStaticFlagIsSet() {
    this.data.setMutateStaticInitializers(true);
    this.data
        .setTargetClasses(predicateFor("com.example.HasMutableStaticInitializer*"));
    createAndRun();
    verifyResults(KILLED);
  }

  @Test
  public void shouldNotCreateMutationsInStaticInitializersWhenFlagNotSet() {
    this.data.setMutateStaticInitializers(false);
    this.data
        .setTargetClasses(predicateFor("com.example.HasMutableStaticInitializer*"));
    createAndRun();
    verifyResults();
  }

  @Test(expected = PitHelpError.class)
  public void shouldFailRunWithHelpfulMessageIfTestsNotGreen() {
    this.data.setMutators(Collections
        .<MethodMutatorFactory> singletonList(Mutator.MATH));
    this.data
        .setTargetClasses(predicateFor("com.example.FailsTestWhenEnvVariableSet*"));
    this.data.addChildJVMArgs(Arrays.asList("-D"
        + FailsTestWhenEnvVariableSetTestee.class.getName() + "=true"));
    createAndRun();
    // should not get here
  }

  @Test
  public void shouldOnlyRunTestsMathchingSuppliedFilter() {
    this.data
        .setTargetClasses(predicateFor(com.example.HasMutableStaticInitializer.class));
    this.data
        .setTargetTests(predicateFor(com.example.HasMutableStaticInitializerTest.class));
    createAndRun();
    verifyResults(KILLED);
  }

  @Test
  public void shouldLoadResoucesOffClassPathFromFolderWithSpaces() {
    this.data.setMutators(Arrays.asList(Mutator.RETURN_VALS));

    this.data
        .setTargetClasses(predicateFor("com.example.LoadsResourcesFromClassPath*"));
    createAndRun();
    verifyResults(KILLED);
  }

  @Test
  public void shouldPickRelevantTestsFromSuppliedTestSuites() {
    this.data.setTargetClasses(predicateFor("com.example.FullyCovered*"));
    this.data
        .setTargetTests(predicateFor(com.example.SuiteForFullyCovered.class));
    createAndRun();
    verifyResults(KILLED);
  }

  @Test
  public void shouldNotMutateMethodsMatchingExclusionPredicate() {
    this.data.setTargetClasses(predicateFor("com.example.HasExcludedMethods*"));
    this.data.setExcludedMethods(predicateFor("excludeMe"));
    createAndRun();
    verifyResults();
  }

  @Test
  public void shouldLimitNumberOfMutationsPerClass() {
    this.data.setTargetClasses(predicateFor(MultipleMutations.class));
    this.data
        .setTargetTests(predicateFor(com.example.FullyCoveredTesteeTest.class));
    this.data.setMaxMutationsPerClass(1);
    createAndRun();
    verifyResults(SURVIVED);
  }

  @Test
  public void shouldWorkWithPowerMock() {
    this.data.setTargetClasses(predicateFor("com.example.PowerMockCallFoo"));
    this.data.setClassesInScope(predicateFor("com.example.Power*"));
    this.data.setTargetTests(predicateFor(com.example.PowerMockTest.class));
    this.data.setVerbose(true);
    createAndRun();
    verifyResults(KILLED);
  }

  @Test
  public void shouldWorkWhenPowerMockReplacesCallsWithinMutee() {
    this.data
        .setTargetClasses(predicateFor("com.example.PowerMockCallsOwnMethod"));
    this.data.setClassesInScope(predicateFor("com.example.Power*"));
    this.data.setTargetTests(predicateFor(com.example.PowerMockTest.class));
    this.data.setVerbose(true);
    createAndRun();
    verifyResults(KILLED);
  }

  @Test
  public void shouldWorkWithMockitoJUnitRunner() {
    this.data.setTargetClasses(predicateFor("com.example.MockitoCallFoo"));
    this.data.setClassesInScope(predicateFor("com.example.Mockito*"));
    this.data.setTargetTests(predicateFor(com.example.MockitoRunnerTest.class));
    this.data.setVerbose(true);
    createAndRun();
    verifyResults(KILLED);
  }

  @Test
  public void shouldWorkWithPowerMockJavaAgent() {
    this.data
        .setTargetClasses(predicateFor("com.example.PowerMockAgentCallFoo"));
    this.data.setClassesInScope(predicateFor("com.example.Power*"));
    this.data
        .setTargetTests(predicateFor(com.example.PowerMockAgentTest.class));
    this.data.setVerbose(true);
    createAndRun();
    verifyResults(KILLED);
  }

  @Test(expected = PitHelpError.class)
  public void shouldReportHelpfulErrorIfNoMutationsFounds() {
    this.data.setTargetClasses(predicateFor("foo"));
    this.data.setClassesInScope(predicateFor("foo"));
    createAndRun();
  }

  @Test
  public void shouldExcludeFilteredTests() {
    this.data.setTargetTests(predicateFor("com.example.*FullyCoveredTestee*"));
    this.data.setTargetClasses(predicateFor("com.example.FullyCovered*"));
    this.data.setExcludedClasses(predicateFor(FullyCoveredTesteeTest.class));
    createAndRun();
    verifyResults(SURVIVED);
  }

  @Test
  public void willAllowExcludedClassesToBeReIncludedViaSuite() {
    this.data
        .setTargetTests(predicateFor("com.example.*SuiteForFullyCovered*"));
    this.data.setTargetClasses(predicateFor("com.example.FullyCovered*"));
    this.data.setExcludedClasses(predicateFor(FullyCoveredTesteeTest.class));
    createAndRun();
    verifyResults(KILLED);
  }

  @Test(expected = PitHelpError.class)
  public void shouldExcludeFilteredClasses() {
    this.data.setTargetClasses(predicateFor(FullyCoveredTestee.class));
    this.data.setExcludedClasses(predicateFor(FullyCoveredTestee.class));
    createAndRun();
  }

  private void createAndRun() {
    final JavaAgent agent = new JarCreatingJarFinder();
    try {
      final MutationCoverageReport testee = new MutationCoverageReport(
          this.data, agent, listenerFactory(), false);

      testee.run();
    } finally {
      agent.close();
    }
  }

}
