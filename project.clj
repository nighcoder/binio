(defproject nighcoder/binio "0.1.1-SNAPSHOT"
  :description "Read and write bytes from and to file"
  :url "https://github.com/nighcoder/binio"
  :license {:name "MIT"
            :url "http://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.10.0"]]
  :profiles {:dev {:dependencies [[org.clojure/test.check "1.1.0"]]}}
  :repl-options {:init-ns binio.core})
