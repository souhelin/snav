#!/usr/bin/env bash
set +e
# Compilation can outlast the emulator's display timeout. A sleeping/locked
# display leaves every pointer test without window focus, even with a resumed
# Activity. Keep this isolated CI device awake; do not weaken focus assertions.
adb shell svc power stayon true || exit $?
adb shell input keyevent KEYCODE_WAKEUP || exit $?
adb shell wm dismiss-keyguard || exit $?
gradle --no-daemon -PsnavEmulatorTest=true :app:connectedDebugAndroidTest > "$RUNNER_TEMP/android-tests.log" 2>&1
result=$?
tail -100 "$RUNNER_TEMP/android-tests.log"
if [ "$result" -ne 0 ]; then
  # Keep focus/power evidence in the job log even if artifact uploads fail.
  echo "Emulator focus and power state:"
  adb shell dumpsys window | grep -E 'mCurrentFocus|mFocusedApp|mShowingLockscreen|mDreamingLockscreen' || true
  adb shell dumpsys power | grep -E 'mWakefulness=|mInteractive=|mStayOn=' || true
  for report in app/build/outputs/androidTest-results/connected/*/*.xml app/build/outputs/androidTest-results/connected/*.xml; do
    if [ -f "$report" ]; then
      echo "Test report: $report"
      head -c 14000 "$report"
    fi
  done
  message=$(tail -70 "$RUNNER_TEMP/android-tests.log" | sed 's/%/%25/g' | tr '\n' '|' | cut -c1-12000)
  echo "::error::$message"
fi
exit "$result"
