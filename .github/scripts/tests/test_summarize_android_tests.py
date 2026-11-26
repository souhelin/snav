"""Exercise the result publisher without Gradle, an emulator or artifact storage."""
import os
from pathlib import Path
import subprocess
import sys
import tempfile
import unittest


SCRIPT = Path(__file__).resolve().parents[1] / 'summarize-android-tests.py'


class AndroidSummaryTest(unittest.TestCase):
    def run_summary(self, xml=None, outcome='success', log=None):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            if xml is not None:
                reports = root / 'app/build/outputs/androidTest-results/connected/device'
                reports.mkdir(parents=True)
                (reports / 'results.xml').write_text(xml, encoding='utf-8')
            if log is not None:
                (root / 'android-tests.log').write_text(log, encoding='utf-8')
            summary = root / 'summary.md'
            result = subprocess.run(
                [sys.executable, str(SCRIPT)], cwd=root, capture_output=True, text=True,
                env={**os.environ, 'RUNNER_TEMP': directory,
                     'GITHUB_STEP_SUMMARY': str(summary), 'ANDROID_TEST_OUTCOME': outcome},
            )
            return result, summary.read_text(encoding='utf-8')

    def test_success_does_not_emit_false_failure(self):
        result, summary = self.run_summary(
            '<testsuite><testcase name="passed"/><testcase name="skipped"><skipped/></testcase></testsuite>')
        self.assertEqual(0, result.returncode, result.stderr)
        self.assertNotIn('::error::', result.stdout)
        self.assertIn('Testes: **2**; falhas/erros: **0**; ignorados: **1**', summary)

    def test_failures_and_errors_remain_blocking_without_artifacts(self):
        result, summary = self.run_summary('''<testsuites><testsuite>
            <testcase classname="Fixture" name="failure"><failure message="100% broken&#10;line 2"/></testcase>
            <testcase classname="Fixture" name="error"><error>exception</error></testcase>
            </testsuite></testsuites>''', outcome='failure')
        self.assertEqual(1, result.returncode)
        self.assertIn('Fixture.failure: 100%25 broken%0Aline 2', result.stdout)
        self.assertIn('Fixture.error: exception', result.stdout)
        self.assertIn('falhas/erros: **2**', summary)

    def test_failing_xml_is_not_overridden_by_successful_process(self):
        result, _ = self.run_summary('<testsuite><testcase><failure>broken</failure></testcase></testsuite>')
        self.assertEqual(1, result.returncode)
        self.assertIn('::error::', result.stdout)

    def test_failed_process_is_not_overridden_by_passing_xml(self):
        result, _ = self.run_summary('<testsuite><testcase name="passed"/></testsuite>',
                                     outcome='failure', log='Gradle failed after instrumentation')
        self.assertEqual(1, result.returncode)
        self.assertIn('Gradle failed after instrumentation', result.stdout)

    def test_missing_reports_do_not_pass(self):
        result, summary = self.run_summary()
        self.assertEqual(1, result.returncode)
        self.assertIn('::error::', result.stdout)
        self.assertIn('Testes: **0**', summary)

    def test_malformed_reports_preserve_build_log(self):
        result, _ = self.run_summary('<truncated', outcome='failure', log='emulator disconnected')
        self.assertEqual(1, result.returncode)
        self.assertIn('emulator disconnected', result.stdout)


if __name__ == '__main__':
    unittest.main()
