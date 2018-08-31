# Missing-Android-Translations
Simple, yet powerful utility to extract untranslated strings for android projects.

### Usage
Go to your android project folder and execute
```bash
$ missing-android-translations
```
This command will generate your untranslated strings to separate files in ./missing-android-translations/untranslated-<classifier>.xml

### Example
#### Local repo
```bash
$ missing-android-translations -s  /var/folders/07/h6zt5r8n78vbmlfp8n09vrlr0000gn/T/Vape-Tool-Android6736550815054365350 -xml
Project name: Vape-Tool-Android
[values-ro, values-ru, values-it, values-cs, values-in, values-ja, values-el, values-lv, values-da, values-no, values-en-rCA, values-pt-rBR, values-pl, values-vi, values-sv, values-sk, values-tr, values-en-rZA, values-th, values-en-rGB, values-fi, values-en-rAU, values-fr, values-es, values-et, values-hu, values-nl, values-bg, values-bn, values-de, values-ko, values-ar, values-pt, values-zh, values-uk]
$ tree missing-android-translations
├── translated-ar.xml
├── translated-en-rAU.xml
├── translated-en-rZA.xml
├── translated-es.xml
├── translated-et.xml
...
├── untranslated-ar.xml
├── untranslated-en-rAU.xml
├── untranslated-en-rZA.xml
├── untranslated-es.xml
├── untranslated-et.xml
...
0 directories, 70 files
```

#### Github repo
```bash
$ missing-android-translations -c https://github.com/stasbar/Vape-Tool-Android.git -l stasbar
Project name: Vape-Tool-Android
Temp directory: /var/folders/07/h6zt5r8n78vbmlfp8n09vrlr0000gn/T/Vape-Tool-Android7231868466548645567
Enter password for github account stasbar:
Start
remote: Counting objects 0
remote: Compressing objects 709
Receiving objects 12125
Resolving deltas 7723
Done !
[values-ro, values-ru, values-it, values-cs, values-in, values-ja, values-el, values-lv, values-da, values-no, values-en-rCA, values-pt-rBR, values-pl, values-vi, values-sv, values-sk, values-tr, values-en-rZA, values-th, values-en-rGB, values-fi, values-en-rAU, values-fr, values-es, values-et, values-hu, values-nl, values-bg, values-bn, values-de, values-ko, values-ar, values-pt, values-zh, values-uk]
$ tree missing-android-translations
├── translated-ar.txt
├── translated-en-rAU.txt
├── translated-en-rZA.txt
├── translated-es.txt
├── translated-et.txt
...
├── untranslated-ar.txt
├── untranslated-en-rAU.txt
├── untranslated-en-rZA.txt
├── untranslated-es.txt
├── untranslated-et.txt
...
0 directories, 70 files
```

### Usage
```bash
usage: android-missing-translations [-c <arg>] [-d <arg>] [-l <arg>] [-p
       <arg>] [-s <arg>] [-xml]
 -s,--src <arg>        location of application root project directory
                       [DEFAULT "." ]
 or
 -c,--download <arg>   repository url location to fetch the project from

 -d,--dst <arg>        destination directory where translated and
                       untranslated files will be located
                       [DEFAULT "./missing-android-translations" ]

 -l,--login <arg>      login to the github account
 -p,--password <arg>   password to the github account

 -xml,--xml            use .xml extension instead of .txt to output files

```


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




