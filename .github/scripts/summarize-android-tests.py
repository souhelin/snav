"""Publish concise failures outside the emulator action's captured output."""
import glob
import os
import xml.etree.ElementTree as ET


def annotation(message):
    escaped = message.replace('%', '%25').replace('\r', '%0D').replace('\n', '%0A')
    print('::error::' + escaped[:12000])


reports = glob.glob('app/build/outputs/androidTest-results/connected/**/*.xml', recursive=True)
reported = False
for path in reports:
    try:
        root = ET.parse(path).getroot()
    except ET.ParseError:
        continue
    for case in root.iter('testcase'):
        failure = case.find('failure')
        if failure is None:
            failure = case.find('error')
        if failure is not None:
            annotation(f"{case.get('classname')}.{case.get('name')}: "
                       + (failure.get('message') or failure.text or 'Falha sem detalhes')[:3000])
            reported = True
if not reported:
    path = os.path.join(os.environ['RUNNER_TEMP'], 'android-tests.log')
    if os.path.exists(path):
        with open(path, encoding='utf-8', errors='replace') as log:
            annotation('Teste Android ou build falhou:\n' + ''.join(log.readlines()[-60:]))
    else:
        annotation('O emulador falhou antes de produzir o log de testes; consulte o log do job.')
