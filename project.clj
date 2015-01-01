(defproject calabash-script "0.0.9"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/clojurescript "0.0-2280"]]

  :hooks [leiningen.cljsbuild]
  :profiles {:dev {:plugins [[com.cemerick/austin "0.1.5"]]}}
  :plugins [[lein-cljsbuild "1.0.3"]]
  :source-paths ["src/clj"]
  :cljsbuild
  {:builds {:dev
            {:source-paths ["src/cljs"]
             :jar true
             :compiler
             {:optimizations :whitespace
              :output-to "build/calabash_script.js"
              :pretty-print true}}
            :base
            {:source-paths ["base"],
             :jar true,
             :compiler
             {:optimizations :simple,
              :output-to "build/base.js",
              :pretty-print true}}}})
