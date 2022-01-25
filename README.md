# IntelliJ support for VoiceCode (IDE plugin)
<!-- Plugin description -->
This plugin adds a small web server which supports a Talon module, but can be used generically as an
HTTP based RPC driven by any system on the same machine.t
<!-- Plugin description end -->

This adds [VoiceCode](https://voicecode.io) support for IntelliJ, in tandem with [this VoiceCode package](https://github.com/anonfunc/voicecode-intellij).

This should support:

- Android Studio
- AppCode
- CLion
- DataGrip
- GoLand
- MPS
- PhpStorm
- PyCharm (Professional and Community editions)
- RubyMine
- WebStorm

However, my primary use case is currently Java and IntelliJ CE.  I will not be able to debug or reproduce issues occuring outside of the freely available IDEs.  (Pull requests are welcome. :smile:)  Support for these IDEs is contingent on their menu items remaining very similar, see *Limitations*.

## Manual Installation

### Download release zip, install plugin from zip.

## Testing uncommitted changes

    ./gradlew runIde