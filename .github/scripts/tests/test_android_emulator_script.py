"""Verify device preparation/diagnostics do not mask the Android test exit code."""
import os
from pathlib import Path
import subprocess
import tempfile
import unittest


SCRIPT = Path(__file__).resolve().parents[1] / 'android-emulator-tests.sh'


class AndroidEmulatorScriptTest(unittest.TestCase):
    def run_script(self, gradle_exit=0, adb_exit=0):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            binaries = root / 'bin'
            binaries.mkdir()
            adb = binaries / 'adb'
            adb.write_text('''#!/bin/sh
echo "adb $*" >> "$TEST_COMMANDS"
echo "mCurrentFocus=fixture mWakefulness=Awake"
exit "$TEST_ADB_EXIT"
''', encoding='utf-8')
            gradle = binaries / 'gradle'
            gradle.write_text('''#!/bin/sh
echo "gradle $*" >> "$TEST_COMMANDS"
echo "test build output"
exit "$TEST_GRADLE_EXIT"
''', encoding='utf-8')
            adb.chmod(0o755)
            gradle.chmod(0o755)
            commands = root / 'commands.txt'
            result = subprocess.run(
                ['bash', str(SCRIPT)], cwd=root, capture_output=True, text=True,
                env={**os.environ, 'PATH': str(binaries) + os.pathsep + os.environ['PATH'],
                     'RUNNER_TEMP': directory, 'TEST_COMMANDS': str(commands),
                     'TEST_ADB_EXIT': str(adb_exit), 'TEST_GRADLE_EXIT': str(gradle_exit)},
            )
            return result, commands.read_text(encoding='utf-8').splitlines()

    def test_wakes_and_unlocks_device_before_running_gradle(self):
        result, commands = self.run_script()
        self.assertEqual(0, result.returncode, result.stderr)
        self.assertEqual([
            'adb shell svc power stayon true',
            'adb shell input keyevent KEYCODE_WAKEUP',
            'adb shell wm dismiss-keyguard',
        ], commands[:3])
        self.assertTrue(commands[3].startswith('gradle '))
        self.assertNotIn('::error::', result.stdout)

    def test_diagnostics_preserve_real_test_failure(self):
        result, commands = self.run_script(gradle_exit=7)
        self.assertEqual(7, result.returncode)
        self.assertIn('adb shell dumpsys window', commands)
        self.assertIn('adb shell dumpsys power', commands)
        self.assertIn('mCurrentFocus=fixture', result.stdout)
        self.assertIn('::error::', result.stdout)

    def test_failed_device_preparation_stops_before_tests(self):
        result, commands = self.run_script(adb_exit=9)
        self.assertEqual(9, result.returncode)
        self.assertFalse(any(command.startswith('gradle ') for command in commands))


if __name__ == '__main__':
    unittest.main()
