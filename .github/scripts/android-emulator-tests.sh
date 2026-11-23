#!/usr/bin/env bash
set +e
gradle --no-daemon -PsnavEmulatorTest=true :app:connectedDebugAndroidTest > "$RUNNER_TEMP/android-tests.log" 2>&1
result=$?
tail -100 "$RUNNER_TEMP/android-tests.log"
if [ "$result" -ne 0 ]; then
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
