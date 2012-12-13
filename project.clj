(defproject calabash-script "0.0.5"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [org.clojure/clojurescript "0.0-1535"]]

  :hooks [leiningen.cljsbuild]

  :plugins [[lein-cljsbuild "0.2.9"]]
  :source-paths ["src/calabash_script/macros"
                 "src/clj"]
  :cljsbuild {:builds
              [{:source-path "src"
                :jar true
                :compiler {:output-to "build/calabash_script.js"
                           :optimizations :whitespace
                           :pretty-print true}}
               {:source-path "base"
                :jar true
                :compiler {:output-to "build/base.js"
                           :optimizations :simple
                           :pretty-print true}}]})
