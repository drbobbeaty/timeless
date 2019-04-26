(defproject timeless "0.4.0"
  :description "Simple sequencing of log timestamps"
  :url "http://github.com/drbob/timeless"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :source-paths ["src"]
  :min-lein-version "2.7.1"
  :plugins [[environ/environ.lein "0.3.1"]]
  :hooks [environ.leiningen.hooks]
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [clj-time "0.12.0"]
                 [org.clojure/math.numeric-tower "0.0.2"]
                 [org.clojure/core.memoize "0.5.6"]
                 [camel-snake-kebab "0.4.0"]
                 ;; command line option processing
                 [org.clojure/tools.cli "0.2.2"]
                 ;; logging with log4j
                 [org.slf4j/slf4j-log4j12 "1.7.5"]
                 [log4j/log4j "1.2.17"]
                 [org.clojure/tools.logging "0.2.6"]
                 [robert/hooke "1.3.0"]
                 ;; JSON parsing library
                 [cheshire "5.5.0"]
                 [compojure "1.5.1"]
                 [ring/ring-core "1.5.0"]
                 [ring/ring-jetty-adapter "1.5.0"]
                 [ring/ring-devel "1.5.0"]
                 [ring.middleware.jsonp "0.1.6"]
                 [ring/ring-ssl "0.2.1"]
                 [ring/ring-defaults "0.2.1"]
                 [ring/ring-codec "1.0.1"]
                 [bk/ring-gzip "0.1.1"]
                 [rwilson/ring-cors "0.1.9"]
                 ;; URL parsing components
                 [clojurewerkz/urly "1.0.0"]
                 ;; Heroku environment tools
                 [environ "1.0.0"]]
  :aot [timeless.main]
  :uberjar-merge-with {#"\.sql\.Driver$" [#(str (clojure.string/trim (slurp %)) "\n") str spit]}
  :uberjar-name "timeless-standalone.jar"
  :main timeless.main
  :profiles {:repl {:main timeless.main}
             :dev {:main timeless.main/-dev-main}
             :uberjar {:main timeless.main, :aot :all}})
