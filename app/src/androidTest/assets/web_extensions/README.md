# Pacotes usados nos testes

Os arquivos `borderify.xpi` e `borderify-unsigned.xpi` são cópias dos pacotes
de teste do GeckoView na árvore do Firefox, commit `2887d9de1f69`
(`FIREFOX_160_0_RELEASE`):

https://github.com/mozilla-firefox/firefox/tree/2887d9de1f69/mobile/android/geckoview/src/androidTest/assets/web_extensions

O código-fonte do Firefox usa a Mozilla Public License 2.0:
https://github.com/mozilla-firefox/firefox/blob/2887d9de1f69/LICENSE

SHA-256:

- `borderify.xpi`: `99e19528b08319803ec19679673c8ae8f19646535254133c3826a3556ba2e6df`
- `borderify-unsigned.xpi`: `ecfa45ea3a2bf2aeaabeb91980c86cc033d538f6c2c77974725e399a0ce9204c`

Esses arquivos são incluídos somente no APK de testes. O pacote assinado tem
um certificado de teste Mozilla válido até novembro de 2029; ele pode deixar
de ser aceito antes disso se a política de validação do Gecko mudar.
