# Missing-Android-Translations
Simple, yet powerful utility to extract untranslated strings for android projects.

## Run
```bash
$ ./gradlew shadowJar
$ java -jar build/lib/missing-android-translations.jar
```
## Install (UNIX only)
Copy executable file to `/usr/local/bin`


```bash
$ ./gradlew install
$ missing-android-translations
```
Make sure that `/usr/local/bin` is on your `$PATH`
### Usage
Go to your android project folder and execute
```bash
$ missing-android-translations
```
This command will generate your untranslated strings to separate files in ./missing-android-translations/untranslated-<classifier>.xml


You can also change the source `--src` and destination `--dst` folders

```bash
usage: android-missing-translations [-d <arg>] [-s <arg>]
 -d,--dst <arg>   destination directory where translated and untranslated files will be saved
 -s,--src <arg>   location of application root project directory
```




