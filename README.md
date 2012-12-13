# CalabashScript

CalabashScript is a [ClojureScript](https://github.com/clojure/clojurescript) library for writing automated functional tests for native iOS Apps. CalabashScript provides friendly ClojureScript APIs based on the (perhaps not so friendly) UIAutomation APIs. 

CalabashScript also provides a query language to find visible UIViews inside the current screen -- contrast

```javascript
UIATarget.localTarget().frontMostApp().mainWindow().navigationBar().buttons()['Edit'].tap();
```

with

```clojure
(tap [:button {:marked 'Edit'}))
```

Finally CalabashScript comes with a REPL! UIAutomation provides a JavaScript execution environment, and the project uia-repl provides a Clojure implementation of a ClojureScript "UIA-connected" REPL for this environment.
    
## Usage

For a very small example see: [CalabashScript Example](https://github.com/krukow/calabash-script-example). Otherwise add

```clojure
[calabash/uia-repl "0.0.8"]
[calabash-script/calabash-script "0.0.8"]
```

to your dependencies in your [lein](https://github.com/technomancy/leiningen) project file for a ClojureScript project.

Write a test, e.g., for the [CalabashScript Example](https://github.com/krukow/calabash-script-example):

```clojure
(ns calabash-script-example.core
  (:require [calabash-script.log :as log]
            [calabash-script.utils :as utils])
  (:use [calabash-script.core :only
         [query tap tap-mark sleep screenshot
          scroll-to
          keyboard-enter-text enter]]

        [calabash-script.tests :only  [define-uia-test fail run-all!]]))

(define-uia-test
  "Water polo details should be accessible via Events"
  (fn []
    (tap-mark "Events")
    (utils/screenshot "Events")
    (sleep 3)
    (scroll-to [:view {:marked "water polo"}])
    (utils/screenshot "Events")
    (tap-mark "water polo")
    (sleep 2)
    (when-not (seq (query [:view
                           {:marked "Olympic Water Polo, past and present"}]))
      (fail "Olympic Water Polo, past and present"))
    (utils/screenshot "Water polo details")))

(run-all!)
```

Compile it (using `lein cljsbuild auto` is recommended). You can run the test using instruments or by running (replace app-path and "build/test_script.js" appropriately):

	krukow:~/github/calabash-script-example$ lein cljsbuild auto
	Compiling ClojureScript.
	Compiling "build/test_script.js" from "src"...
	Successfully compiled "build/test_script.js" in 7.683529 seconds. 
	
In another terminal:

	lein repl
	

```clojure
user=> (use 'calabash-script.clj.run)
nil
user=> (def app-path "/Users/krukow/github/2012-Olympics-iOS--iPad-and-iPhone--source-code/2012 Olympics/build/Applications/2012 Olympics.app")
 #'user/app-path

user=> (run-test :app app-path :test "build/test_script.js")

[instruments -t /Applications/Xcode.app/Contents/Applications/Instruments.app/Contents/PlugIns/AutomationInstrument.bundle/Contents/Resources/Automation.tracetemplate /Users/krukow/github/2012-Olympics-iOS--iPad-and-iPhone--source-code/2012 Olympics/build/Applications/2012 Olympics.app -D run/trace -e UIARESULTSPATH run -e UIASCRIPT build/test_script.js]

{:input #<BufferedReader java.io.BufferedReader@7ecdc97b>, :process #<UNIXProcess java.lang.UNIXProcess@7866eb46>}
user=> ;;this may take some time
```

    
## REPL Usage

CalabashScript comes with a REPL. This is provided by a separate project [uia-repl](https://github.com/krukow/uia-repl), which is the reason for the extra dependency `[calabash/uia-repl "0.0.8"]`. This is based (but not quite as elegant) on [Bodil Stokke](https://github.com/bodil)'s [cljs-noderepl](https://github.com/bodil/cljs-noderepl) to many thanks to her!

To try the REPL (warning this is quite slow for various reasons): in your project you can run:
```clojure
	lein repl
	user=> (require '[cljs.repl.uia :as uia])
	nil
	user=> (def app-path "/Users/krukow/github/2012-Olympics-iOS--iPad-and-iPhone--source-code/2012 Olympics/build/Applications/2012 Olympics.app")
	#'user/app-path
	user=> (uia/run-uia-repl :app app-path)
	..
	"Type: " :cljs/quit " to quit"
	{:status :success :value nil}
	MESSAGE: 2012-12-13 20:47:24 +0000 Default: 
	{:status :success :value {:cljs$lang$maxFixedArity 0 :cljs$lang$applyTo {} :cljs$lang$arity$variadic {}}}
	ClojureScript:cljs.user>   #_=> (+ 1 2 3)
	MESSAGE: 2012-12-13 20:47:26 +0000 Default: 
	{:status :success :value "6"}
	6
	#_=> (ns example (:require [calabash-script.core :as c]))
	… (takes some time!)
	("calabash-script.core")
	Loading files for  (calabash-script.core)
	…
	ClojureScript:example>   #_=> (c/tap [:button {:marked "Events"}])
	ClojureScript:example>   #_=> (c/names :button)
	…
	("archery" "athletics" "badminton" "basketball" "beach volleyball" "boxing" "canoe slalom" "canoe sprint" "cycling bmx" "cycling mountain bike" "cycling road" "cycling track" "diving" "equestrian dressage" "equestrian eventing" "equestrian jumping" "fencing" "football" "gymnastics artistic" "gymnastics rhythmic" "gymnastics trampoline" "handball" "hockey" "judo" "modern pentathlon" "rowing" "sailing" "shooting" "swimming" "synchronised swimming" "table tennis" "taekwondo" "tennis" "triathlon" "volleyball" "water polo" "weightlifting" "wrestling" "Home" "Events" "Count Down" "Schedule" "Anthem")

	ClojureScript:example>   #_=> (c/tap-mark "water polo")
```
	
## Caveats
CalabashScript is very young, and the REPL is quite experimental still… If you see

	ClojureScript:cljs.user> MESSAGE: 2012-12-13 20:51:37 +0000 Default: 
	MESSAGE: 2012-12-13 20:51:38 +0000 Default: 
	MESSAGE: 2012-12-13 20:51:39 +0000 Default: 
	MESSAGE: 2012-12-13 20:51:40 +0000 Default: 
	MESSAGE: 2012-12-13 20:51:41 +0000 Default: 
	MESSAGE: 2012-12-13 20:51:42 +0000 Default: 
	MESSAGE: 2012-12-13 20:51:43 +0000 Default: 
	MESSAGE: 2012-12-13 20:51:44 +0000 Default: 
	MESSAGE: 2012-12-13 20:51:45 +0000 Default: 
	
then it has gone bad and you need to restart the REPL!

## Rationale

Apple provides a technology called [UIAutomation](http://developer.apple.com/library/ios/#documentation/DeveloperTools/Reference/UIAutomationRef/_index.html) which uses JavaScript to write automated tests for iOS apps. Unfortunately UIAutomation has several problems: tests are written in JavaScript using an verbose, and rather strange API. There is no built-in support for defining test suites or grouping test cases. Test cases are starter by calling a "logger" with a special method. Error messages are horrible the the development experience is even worse since you have to restart the app and run the entire test suite every time you want to make a change to the test. We saw an example of the verbosity before, and here is another from the [official docs](https://developer.apple.com/library/mac/#documentation/developertools/Conceptual/InstrumentsUserGuide/UsingtheAutomationInstrument/UsingtheAutomationInstrument.html#//apple_ref/doc/uid/TP40004652-CH20-SW1)

```javascript
UIATarget.localTarget().frontMostApp().mainWindow().tableViews()[0].cells()[0].elements()["Chocolate Cake"];
```

The view hierarchy must be explicitly navigated which leads to brittle tests.

Some of these problems are inherent to the UIAutomation technology while others can be solved to some degree.

CalabashScript replaces JavaScript with ClojureScript, provides a REPL for interactive exploratory development, provides a query language to declaratively find views (avoiding explicit hierarchy traversal), and provides high-level functions for interacting with views based on this query language (tap, pan, swipe etc).



## License

Copyright © 2012 Karl Krukow

Distributed under the Eclipse Public License, the same as Clojure.
