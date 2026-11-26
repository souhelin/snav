"""Publish Android results even when the artifact storage quota is exhausted."""
import glob
import os
import xml.etree.ElementTree as ET


def annotation(message):
    escaped = message.replace('%', '%25').replace('\r', '%0D').replace('\n', '%0A')
    print('::error::' + escaped[:12000])


reports = glob.glob('app/build/outputs/androidTest-results/connected/**/*.xml', recursive=True)
reported = False
tests = failures = skipped = 0
for path in reports:
    try:
        root = ET.parse(path).getroot()
    except ET.ParseError:
        continue
    for case in root.iter('testcase'):
        tests += 1
        if case.find('skipped') is not None:
            skipped += 1
        failure = case.find('failure')
        if failure is None:
            failure = case.find('error')
        if failure is not None:
            failures += 1
            annotation(f"{case.get('classname')}.{case.get('name')}: "
                       + (failure.get('message') or failure.text or 'Falha sem detalhes')[:3000])
            reported = True
# A green XML report does not override a failed Gradle/emulator process.
outcome = os.environ.get('ANDROID_TEST_OUTCOME', 'failure')
if not reported and (outcome != 'success' or tests == 0):
    path = os.path.join(os.environ.get('RUNNER_TEMP', '.'), 'android-tests.log')
    if os.path.exists(path):
        with open(path, encoding='utf-8', errors='replace') as log:
            annotation('Teste Android ou build falhou:\n' + ''.join(log.readlines()[-60:]))
    else:
        annotation('Não há resultado de testes válido ou o emulador falhou; consulte o log do job.')

summary = os.environ.get('GITHUB_STEP_SUMMARY')
if summary:
    with open(summary, 'a', encoding='utf-8') as output:
        output.write('## Testes Android\n\n')
        output.write(f'- Execução Gradle/emulador: **{outcome}**\n')
        output.write(f'- Testes: **{tests}**; falhas/erros: **{failures}**; ignorados: **{skipped}**.\n\n')
        output.write('As falhas detalhadas ficam nas anotações e no log do job, '
                     'independentemente da disponibilidade de armazenamento de artefatos.\n')

# Only artifact publishing is optional; missing reports and real test failures are not.
if outcome != 'success' or failures or tests == 0:
    raise SystemExit(1)
