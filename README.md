Thai-Line-Breaker
=================
This is an Android project that adds Thai word breaking functionality to Android's TextView. Some of its features includes:
- processing speed with cached dictionary
- aesthetical modification to dictionary and algorithm customized only for line breaking
- auto disabling when ellipsis is applied

[Here](https://github.com/heartnetkung/Thai-Line-Breaker/blob/master/ThaiLineBreaker/download/ThaiLineBreaker.apk?raw=true) is the .apk demo 

Installation
============
//TODO let Boom write this

Usage
=====
The only class that you'd use is ThaiLineBreakingTextView which extends from TextView. It adds 2 additional methods:
- public void setText2(int/CharSequence) which should be used in replace of setText to process line break and then setText
- public CharSequence getText2() which would return the text you've set using setText2

Note: that the use of setText and getText by user should be avoided.
Tips: You can also layout this class is xml as follow <hnk.lib.tlb.ThaiLineBreakingTextView
            android:id="@+id/result"/>

Preprocessing
=============
The preprocessing of the dictionary and the dictionary caching is done in another project [here](https://github.com/heartnetkung/LexCleaner-for-Thai-Line-Breaker). In this project, we only use the cached dict in file res/raw/trie_cache.txt 
and res/raw/whole_word_cache.txt
